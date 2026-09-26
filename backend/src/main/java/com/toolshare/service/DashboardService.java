package com.toolshare.service;

import com.toolshare.dao.BorrowRequestDao;
import com.toolshare.dao.SuggestionDao;
import com.toolshare.dao.ToolDao;
import com.toolshare.dao.UserDao;
import com.toolshare.dto.DashboardStatsDto;
import com.toolshare.exception.ResourceNotFoundException;
import com.toolshare.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ToolDao toolDao;
    private final BorrowRequestDao borrowRequestDao;
    private final UserDao userDao;
    private final SuggestionDao suggestionDao;

    public DashboardService(ToolDao toolDao, BorrowRequestDao borrowRequestDao, UserDao userDao, SuggestionDao suggestionDao) {
        this.toolDao = toolDao;
        this.borrowRequestDao = borrowRequestDao;
        this.userDao = userDao;
        this.suggestionDao = suggestionDao;
    }

    public DashboardStatsDto getLenderStats(String username) {
        User lender = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalTools(toolDao.countByLender(lender.getId()));
        stats.setAvailableTools(toolDao.countByLenderAndStatus(lender.getId(), "AVAILABLE"));
        stats.setBorrowedTools(toolDao.countByLenderAndStatus(lender.getId(), "BORROWED"));
        stats.setPendingRequests(borrowRequestDao.countByLenderAndStatus(lender.getId(), "PENDING"));
        stats.setActiveLoans(borrowRequestDao.countByLenderAndStatus(lender.getId(), "APPROVED") +
                             borrowRequestDao.countByLenderAndStatus(lender.getId(), "ACTIVE"));
        stats.setCompletedLoans(borrowRequestDao.countByLenderAndStatus(lender.getId(), "COMPLETED"));
        BigDecimal earnings = borrowRequestDao.sumEarningsByLender(lender.getId());
        stats.setTotalEarnings(earnings != null ? earnings : BigDecimal.ZERO);

        return stats;
    }

    public DashboardStatsDto getBorrowerStats(String username) {
        User borrower = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setAvailableTools(toolDao.countByStatus("AVAILABLE"));
        stats.setPendingRequests(borrowRequestDao.countByBorrowerAndStatus(borrower.getId(), "PENDING"));
        stats.setActiveLoans(borrowRequestDao.countByBorrowerAndStatus(borrower.getId(), "APPROVED") +
                             borrowRequestDao.countByBorrowerAndStatus(borrower.getId(), "ACTIVE"));
        stats.setCompletedLoans(borrowRequestDao.countByBorrowerAndStatus(borrower.getId(), "COMPLETED"));
        BigDecimal fines = borrowRequestDao.sumFinesByBorrower(borrower.getId());
        stats.setTotalFines(fines != null ? fines : BigDecimal.ZERO);

        return stats;
    }

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userDao.countAll());
        stats.put("totalTools", toolDao.countAll());
        stats.put("totalTransactions", borrowRequestDao.countTotalTransactions());
        stats.put("totalSuggestions", suggestionDao.countAll());
        return stats;
    }
}
