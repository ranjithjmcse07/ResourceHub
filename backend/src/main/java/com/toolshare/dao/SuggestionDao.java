package com.toolshare.dao;

import com.toolshare.model.Suggestion;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class SuggestionDao {

    private final JdbcTemplate jdbcTemplate;

    public SuggestionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Suggestion> suggestionRowMapper = (rs, rowNum) -> {
        Suggestion s = new Suggestion();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getLong("user_id"));
        s.setSubject(rs.getString("subject"));
        s.setMessage(rs.getString("message"));
        s.setStatus(rs.getString("status"));
        s.setAdminResponse(rs.getString("admin_response"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) s.setCreatedAt(ca.toLocalDateTime());

        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) s.setUpdatedAt(ua.toLocalDateTime());

        try { s.setUserName(rs.getString("user_name")); } catch (Exception ignored) {}
        try { s.setUserRole(rs.getString("user_role")); } catch (Exception ignored) {}
        try { s.setUserEmail(rs.getString("user_email")); } catch (Exception ignored) {}

        return s;
    };

    private static final String BASE_JOIN_QUERY = 
        "SELECT s.*, u.full_name AS user_name, u.role AS user_role, u.email AS user_email " +
        "FROM suggestions s " +
        "JOIN users u ON s.user_id = u.id ";

    public Suggestion createSuggestion(Suggestion s) {
        String sql = "INSERT INTO suggestions (user_id, subject, message, status) VALUES (?, ?, ?, 'OPEN')";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, s.getUserId());
            ps.setString(2, s.getSubject());
            ps.setString(3, s.getMessage());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            s.setId(keyHolder.getKey().longValue());
        }
        return s;
    }

    public Optional<Suggestion> findById(Long id) {
        String sql = BASE_JOIN_QUERY + "WHERE s.id = ?";
        try {
            Suggestion s = jdbcTemplate.queryForObject(sql, suggestionRowMapper, id);
            return Optional.ofNullable(s);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Suggestion> findByUserId(Long userId) {
        String sql = BASE_JOIN_QUERY + "WHERE s.user_id = ? ORDER BY s.id DESC";
        return jdbcTemplate.query(sql, suggestionRowMapper, userId);
    }

    public List<Suggestion> findAll() {
        String sql = BASE_JOIN_QUERY + "ORDER BY s.id DESC";
        return jdbcTemplate.query(sql, suggestionRowMapper);
    }

    public void updateStatusAndResponse(Long id, String status, String adminResponse) {
        String sql = "UPDATE suggestions SET status = ?, admin_response = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, adminResponse, id);
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM suggestions";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
