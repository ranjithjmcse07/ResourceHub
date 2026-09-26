package com.toolshare.dao;

import com.toolshare.model.BorrowRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BorrowRequestDao {

    private final JdbcTemplate jdbcTemplate;

    public BorrowRequestDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<BorrowRequest> requestRowMapper = (rs, rowNum) -> {
        BorrowRequest br = new BorrowRequest();
        br.setId(rs.getLong("id"));
        br.setToolId(rs.getLong("tool_id"));
        br.setBorrowerId(rs.getLong("borrower_id"));
        br.setRentalType(rs.getString("rental_type"));
        br.setDuration(rs.getInt("duration"));

        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) br.setStartTime(st.toLocalDateTime());

        Timestamp ert = rs.getTimestamp("expected_return_time");
        if (ert != null) br.setExpectedReturnTime(ert.toLocalDateTime());

        Timestamp art = rs.getTimestamp("actual_return_time");
        if (art != null) br.setActualReturnTime(art.toLocalDateTime());

        br.setTotalAmount(rs.getBigDecimal("total_amount"));
        br.setFineAmount(rs.getBigDecimal("fine_amount"));
        br.setStatus(rs.getString("status"));

        Timestamp reqAt = rs.getTimestamp("requested_at");
        if (reqAt != null) br.setRequestedAt(reqAt.toLocalDateTime());

        Timestamp appAt = rs.getTimestamp("approved_at");
        if (appAt != null) br.setApprovedAt(appAt.toLocalDateTime());

        Timestamp retAt = rs.getTimestamp("returned_at");
        if (retAt != null) br.setReturnedAt(retAt.toLocalDateTime());

        br.setNotes(rs.getString("notes"));

        // Joined columns
        try { br.setToolName(rs.getString("tool_name")); } catch (Exception ignored) {}
        try { br.setToolCategory(rs.getString("tool_category")); } catch (Exception ignored) {}
        try { br.setToolImageUrl(rs.getString("tool_image_url")); } catch (Exception ignored) {}
        try { br.setLenderId(rs.getLong("lender_id")); } catch (Exception ignored) {}
        try { br.setLenderName(rs.getString("lender_name")); } catch (Exception ignored) {}
        try { br.setBorrowerName(rs.getString("borrower_name")); } catch (Exception ignored) {}
        try { br.setBorrowerEmail(rs.getString("borrower_email")); } catch (Exception ignored) {}
        try { br.setBorrowerMobile(rs.getString("borrower_mobile")); } catch (Exception ignored) {}
        try { br.setBorrowerAddress(rs.getString("borrower_address")); } catch (Exception ignored) {}
        try { br.setLenderMobile(rs.getString("lender_mobile")); } catch (Exception ignored) {}
        try { br.setLenderAddress(rs.getString("lender_address")); } catch (Exception ignored) {}
        try { br.setLenderEmail(rs.getString("lender_email")); } catch (Exception ignored) {}
        try { br.setLenderCompany(rs.getString("lender_company")); } catch (Exception ignored) {}

        return br;
    };

    private static final String BASE_JOIN_QUERY = 
        "SELECT br.*, " +
        "t.tool_name, t.category AS tool_category, t.image_url AS tool_image_url, t.lender_id, " +
        "l.full_name AS lender_name, l.mobile AS lender_mobile, l.address AS lender_address, l.email AS lender_email, l.company_name AS lender_company, " +
        "b.full_name AS borrower_name, b.email AS borrower_email, b.mobile AS borrower_mobile, b.address AS borrower_address " +
        "FROM borrow_requests br " +
        "JOIN tools t ON br.tool_id = t.id " +
        "JOIN users l ON t.lender_id = l.id " +
        "JOIN users b ON br.borrower_id = b.id ";

    public BorrowRequest createRequest(BorrowRequest req) {
        String sql = "INSERT INTO borrow_requests (tool_id, borrower_id, rental_type, duration, start_time, " +
                     "expected_return_time, total_amount, fine_amount, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, req.getToolId());
            ps.setLong(2, req.getBorrowerId());
            ps.setString(3, req.getRentalType());
            ps.setInt(4, req.getDuration());
            ps.setTimestamp(5, Timestamp.valueOf(req.getStartTime()));
            ps.setTimestamp(6, Timestamp.valueOf(req.getExpectedReturnTime()));
            ps.setBigDecimal(7, req.getTotalAmount() != null ? req.getTotalAmount() : BigDecimal.ZERO);
            ps.setBigDecimal(8, BigDecimal.ZERO);
            ps.setString(9, "PENDING");
            ps.setString(10, req.getNotes());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            req.setId(keyHolder.getKey().longValue());
        }
        return req;
    }

    public Optional<BorrowRequest> findById(Long id) {
        String sql = BASE_JOIN_QUERY + "WHERE br.id = ?";
        try {
            BorrowRequest req = jdbcTemplate.queryForObject(sql, requestRowMapper, id);
            return Optional.ofNullable(req);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<BorrowRequest> findByBorrowerId(Long borrowerId) {
        String sql = BASE_JOIN_QUERY + "WHERE br.borrower_id = ? ORDER BY br.id DESC";
        return jdbcTemplate.query(sql, requestRowMapper, borrowerId);
    }

    public List<BorrowRequest> findByLenderId(Long lenderId) {
        String sql = BASE_JOIN_QUERY + "WHERE t.lender_id = ? ORDER BY br.id DESC";
        return jdbcTemplate.query(sql, requestRowMapper, lenderId);
    }

    public List<BorrowRequest> findAllTransactions() {
        String sql = BASE_JOIN_QUERY + "ORDER BY br.id DESC";
        return jdbcTemplate.query(sql, requestRowMapper);
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE borrow_requests SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public void markApproved(Long id, LocalDateTime approvedAt) {
        String sql = "UPDATE borrow_requests SET status = 'APPROVED', approved_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(approvedAt), id);
    }

    public void markReturned(Long id, LocalDateTime returnedAt, BigDecimal fineAmount) {
        String sql = "UPDATE borrow_requests SET status = 'COMPLETED', actual_return_time = ?, returned_at = ?, fine_amount = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(returnedAt), Timestamp.valueOf(returnedAt), fineAmount, id);
    }

    public boolean hasActiveOrPendingRequestForTool(Long toolId, Long borrowerId) {
        String sql = "SELECT COUNT(*) FROM borrow_requests WHERE tool_id = ? AND borrower_id = ? AND status IN ('PENDING', 'APPROVED', 'ACTIVE')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, toolId, borrowerId);
        return count != null && count > 0;
    }

    public long countByLenderAndStatus(Long lenderId, String status) {
        String sql = "SELECT COUNT(*) FROM borrow_requests br JOIN tools t ON br.tool_id = t.id WHERE t.lender_id = ? AND br.status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, lenderId, status);
        return count != null ? count : 0L;
    }

    public BigDecimal sumEarningsByLender(Long lenderId) {
        String sql = "SELECT COALESCE(SUM(br.total_amount), 0) FROM borrow_requests br JOIN tools t ON br.tool_id = t.id WHERE t.lender_id = ? AND br.status = 'COMPLETED'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    public long countByBorrowerAndStatus(Long borrowerId, String status) {
        String sql = "SELECT COUNT(*) FROM borrow_requests WHERE borrower_id = ? AND status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, borrowerId, status);
        return count != null ? count : 0L;
    }

    public BigDecimal sumFinesByBorrower(Long borrowerId) {
        String sql = "SELECT COALESCE(SUM(fine_amount), 0) FROM borrow_requests WHERE borrower_id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

    public long countTotalTransactions() {
        String sql = "SELECT COUNT(*) FROM borrow_requests";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
