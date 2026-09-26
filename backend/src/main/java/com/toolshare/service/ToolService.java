package com.toolshare.service;

import com.toolshare.dao.ToolDao;
import com.toolshare.dao.UserDao;
import com.toolshare.dto.ToolRequest;
import com.toolshare.exception.BadRequestException;
import com.toolshare.exception.ResourceNotFoundException;
import com.toolshare.model.Tool;
import com.toolshare.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService {

    private final ToolDao toolDao;
    private final UserDao userDao;

    public ToolService(ToolDao toolDao, UserDao userDao) {
        this.toolDao = toolDao;
        this.userDao = userDao;
    }

    public Tool createTool(String username, ToolRequest req) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Tool tool = new Tool();
        tool.setLenderId(user.getId());
        tool.setToolName(req.getToolName().trim());
        tool.setCategory(req.getCategory().trim());
        tool.setDescription(req.getDescription().trim());
        tool.setToolCondition(req.getToolCondition() != null ? req.getToolCondition() : "Good");
        tool.setLocation(req.getLocation().trim());
        tool.setHourlyRate(req.getHourlyRate());
        tool.setDailyRate(req.getDailyRate());
        tool.setImageUrl(req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty() 
                ? req.getImageUrl().trim() 
                : "https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=800&q=80");
        tool.setAvailabilityStatus("AVAILABLE");

        return toolDao.createTool(tool);
    }

    public Tool updateTool(Long id, String username, ToolRequest req) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Tool existing = toolDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + id));

        // Ownership check unless admin
        if (!existing.getLenderId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new BadRequestException("You can only edit your own tools");
        }

        existing.setToolName(req.getToolName().trim());
        existing.setCategory(req.getCategory().trim());
        existing.setDescription(req.getDescription().trim());
        existing.setToolCondition(req.getToolCondition() != null ? req.getToolCondition() : existing.getToolCondition());
        existing.setLocation(req.getLocation().trim());
        existing.setHourlyRate(req.getHourlyRate());
        existing.setDailyRate(req.getDailyRate());
        if (req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty()) {
            existing.setImageUrl(req.getImageUrl().trim());
        }

        toolDao.updateTool(existing);
        return existing;
    }

    public void deleteTool(Long id, String username, boolean isAdmin) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Tool existing = toolDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + id));

        if (!existing.getLenderId().equals(user.getId()) && !isAdmin) {
            throw new BadRequestException("You can only delete your own tools");
        }

        // Soft delete / mark inactive if borrowed, or hard delete
        toolDao.updateAvailability(id, "INACTIVE");
    }

    public Tool getToolById(Long id) {
        return toolDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + id));
    }

    public List<Tool> getToolsByLender(String username) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return toolDao.findByLenderId(user.getId());
    }

    public List<Tool> searchTools(String query, String category, String location, String condition,
                                  Double maxDailyRate, String status, String sortBy) {
        return toolDao.searchTools(query, category, location, condition, maxDailyRate, status, sortBy);
    }

    public void updateAvailability(Long id, String username, String status) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Tool existing = toolDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + id));

        if (!existing.getLenderId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new BadRequestException("You can only update availability of your own tools");
        }

        toolDao.updateAvailability(id, status);
    }

    public List<String> getDistinctCities() {
        return toolDao.getDistinctCities();
    }
}
