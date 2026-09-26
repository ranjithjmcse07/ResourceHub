package com.toolshare.dao;

import com.toolshare.model.Tool;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ToolDao {

    private final JdbcTemplate jdbcTemplate;

    public ToolDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Tool> toolRowMapper = (rs, rowNum) -> {
        Tool tool = new Tool();
        tool.setId(rs.getLong("id"));
        tool.setLenderId(rs.getLong("lender_id"));
        tool.setToolName(rs.getString("tool_name"));
        tool.setCategory(rs.getString("category"));
        tool.setDescription(rs.getString("description"));
        tool.setToolCondition(rs.getString("tool_condition"));
        tool.setLocation(rs.getString("location"));
        tool.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        tool.setDailyRate(rs.getBigDecimal("daily_rate"));
        tool.setImageUrl(rs.getString("image_url"));
        tool.setAvailabilityStatus(rs.getString("availability_status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            tool.setCreatedAt(createdAt.toLocalDateTime());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            tool.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        // Joined columns if available
        try {
            tool.setLenderName(rs.getString("lender_name"));
        } catch (Exception ignored) {}

        try {
            tool.setLenderCompany(rs.getString("company_name"));
        } catch (Exception ignored) {}

        try {
            tool.setLenderMobile(rs.getString("lender_mobile"));
        } catch (Exception ignored) {}

        try {
            tool.setLenderAddress(rs.getString("lender_address"));
        } catch (Exception ignored) {}

        try {
            tool.setLenderEmail(rs.getString("lender_email"));
        } catch (Exception ignored) {}

        try {
            double avg = rs.getDouble("avg_rating");
            tool.setAverageRating(rs.wasNull() ? 0.0 : Math.round(avg * 10.0) / 10.0);
        } catch (Exception ignored) {}

        try {
            tool.setReviewCount(rs.getInt("review_count"));
        } catch (Exception ignored) {}

        return tool;
    };

    private static final String BASE_TOOL_QUERY = 
        "SELECT t.*, u.full_name AS lender_name, u.company_name, u.mobile AS lender_mobile, u.address AS lender_address, u.email AS lender_email, " +
        "COALESCE(AVG(r.rating), 0) AS avg_rating, " +
        "COUNT(r.id) AS review_count " +
        "FROM tools t " +
        "JOIN users u ON t.lender_id = u.id " +
        "LEFT JOIN reviews r ON t.id = r.tool_id ";

    public Tool createTool(Tool tool) {
        String sql = "INSERT INTO tools (lender_id, tool_name, category, description, tool_condition, location, " +
                     "hourly_rate, daily_rate, image_url, availability_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tool.getLenderId());
            ps.setString(2, tool.getToolName());
            ps.setString(3, tool.getCategory());
            ps.setString(4, tool.getDescription());
            ps.setString(5, tool.getToolCondition());
            ps.setString(6, tool.getLocation());
            ps.setBigDecimal(7, tool.getHourlyRate() != null ? tool.getHourlyRate() : BigDecimal.ZERO);
            ps.setBigDecimal(8, tool.getDailyRate() != null ? tool.getDailyRate() : BigDecimal.ZERO);
            ps.setString(9, tool.getImageUrl());
            ps.setString(10, tool.getAvailabilityStatus() != null ? tool.getAvailabilityStatus() : "AVAILABLE");
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            tool.setId(keyHolder.getKey().longValue());
        }
        return tool;
    }

    public void updateTool(Tool tool) {
        String sql = "UPDATE tools SET tool_name = ?, category = ?, description = ?, tool_condition = ?, " +
                     "location = ?, hourly_rate = ?, daily_rate = ?, image_url = ?, availability_status = ? " +
                     "WHERE id = ? AND lender_id = ?";
        jdbcTemplate.update(sql,
                tool.getToolName(),
                tool.getCategory(),
                tool.getDescription(),
                tool.getToolCondition(),
                tool.getLocation(),
                tool.getHourlyRate(),
                tool.getDailyRate(),
                tool.getImageUrl(),
                tool.getAvailabilityStatus(),
                tool.getId(),
                tool.getLenderId()
        );
    }

    public void deleteTool(Long id) {
        String sql = "DELETE FROM tools WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<Tool> findById(Long id) {
        String sql = BASE_TOOL_QUERY + "WHERE t.id = ? GROUP BY t.id, u.id";
        try {
            Tool tool = jdbcTemplate.queryForObject(sql, toolRowMapper, id);
            return Optional.ofNullable(tool);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Tool> findByLenderId(Long lenderId) {
        String sql = BASE_TOOL_QUERY + "WHERE t.lender_id = ? GROUP BY t.id, u.id ORDER BY t.id DESC";
        return jdbcTemplate.query(sql, toolRowMapper, lenderId);
    }

    public List<Tool> searchTools(String query, String category, String location, String condition,
                                  Double maxDailyRate, String status, String sortBy) {
        StringBuilder sql = new StringBuilder(BASE_TOOL_QUERY);
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(t.tool_name) LIKE ? OR LOWER(t.description) LIKE ? OR LOWER(t.category) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
            sql.append("AND LOWER(t.category) = ? ");
            params.add(category.trim().toLowerCase());
        }

        if (location != null && !location.trim().isEmpty() 
                && !location.equalsIgnoreCase("All") 
                && !location.equalsIgnoreCase("All Cities")
                && !location.equalsIgnoreCase("All Cities (Pan-India)")) {
            sql.append("AND LOWER(t.location) LIKE ? ");
            params.add("%" + location.trim().toLowerCase() + "%");
        }

        if (condition != null && !condition.trim().isEmpty() && !condition.equalsIgnoreCase("All")) {
            sql.append("AND t.tool_condition = ? ");
            params.add(condition.trim());
        }

        if (maxDailyRate != null && maxDailyRate > 0) {
            sql.append("AND t.daily_rate <= ? ");
            params.add(maxDailyRate);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All")) {
            sql.append("AND t.availability_status = ? ");
            params.add(status.trim().toUpperCase());
        } else {
            // By default, don't show deleted or inactive tools in general browse
            sql.append("AND t.availability_status != 'INACTIVE' ");
        }

        sql.append("GROUP BY t.id, u.id ");

        // Sorting
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY t.daily_rate ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY t.daily_rate DESC ");
        } else if ("rating_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY avg_rating DESC, review_count DESC ");
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY t.tool_name ASC ");
        } else {
            sql.append("ORDER BY t.id DESC "); // newest first
        }

        return jdbcTemplate.query(sql.toString(), toolRowMapper, params.toArray());
    }

    public void updateAvailability(Long id, String status) {
        String sql = "UPDATE tools SET availability_status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public long countAll() {
        String sql = "SELECT COUNT(*) FROM tools WHERE availability_status != 'INACTIVE'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tools WHERE availability_status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, status);
        return count != null ? count : 0L;
    }

    public long countByLender(Long lenderId) {
        String sql = "SELECT COUNT(*) FROM tools WHERE lender_id = ? AND availability_status != 'INACTIVE'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, lenderId);
        return count != null ? count : 0L;
    }

    public long countByLenderAndStatus(Long lenderId, String status) {
        String sql = "SELECT COUNT(*) FROM tools WHERE lender_id = ? AND availability_status = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, lenderId, status);
        return count != null ? count : 0L;
    }

    public List<String> getDistinctCities() {
        String sql = "SELECT DISTINCT " +
                     "TRIM(SUBSTRING_INDEX(location, '-', 1)) AS city " +
                     "FROM tools " +
                     "WHERE availability_status != 'INACTIVE' " +
                     "GROUP BY city " +
                     "ORDER BY city ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
