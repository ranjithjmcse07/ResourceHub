package com.toolshare.dao;

import com.toolshare.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ReviewDao {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        Review r = new Review();
        r.setId(rs.getLong("id"));
        r.setToolId(rs.getLong("tool_id"));
        r.setBorrowerId(rs.getLong("borrower_id"));
        r.setBorrowRequestId(rs.getLong("borrow_request_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) r.setCreatedAt(ca.toLocalDateTime());

        try { r.setBorrowerName(rs.getString("borrower_name")); } catch (Exception ignored) {}
        try { r.setToolName(rs.getString("tool_name")); } catch (Exception ignored) {}

        return r;
    };

    public Review createReview(Review review) {
        String sql = "INSERT INTO reviews (tool_id, borrower_id, borrow_request_id, rating, comment) " +
                     "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getToolId());
            ps.setLong(2, review.getBorrowerId());
            ps.setLong(3, review.getBorrowRequestId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            review.setId(keyHolder.getKey().longValue());
        }
        return review;
    }

    public List<Review> findByToolId(Long toolId) {
        String sql = "SELECT r.*, u.full_name AS borrower_name, t.tool_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.borrower_id = u.id " +
                     "JOIN tools t ON r.tool_id = t.id " +
                     "WHERE r.tool_id = ? " +
                     "ORDER BY r.id DESC";
        return jdbcTemplate.query(sql, reviewRowMapper, toolId);
    }

    public boolean existsByBorrowRequestId(Long borrowRequestId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE borrow_request_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, borrowRequestId);
        return count != null && count > 0;
    }

    public List<Review> findAllReviews() {
        String sql = "SELECT r.*, u.full_name AS borrower_name, t.tool_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.borrower_id = u.id " +
                     "JOIN tools t ON r.tool_id = t.id " +
                     "ORDER BY r.id DESC";
        return jdbcTemplate.query(sql, reviewRowMapper);
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM reviews";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
