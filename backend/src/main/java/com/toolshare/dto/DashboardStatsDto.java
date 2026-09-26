package com.toolshare.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private long totalTools;
    private long availableTools;
    private long borrowedTools;
    private long pendingRequests;
    private long activeLoans;
    private long completedLoans;
    private BigDecimal totalFines;
    private BigDecimal totalEarnings;

    public DashboardStatsDto() {
        this.totalFines = BigDecimal.ZERO;
        this.totalEarnings = BigDecimal.ZERO;
    }

    public long getTotalTools() {
        return totalTools;
    }

    public void setTotalTools(long totalTools) {
        this.totalTools = totalTools;
    }

    public long getAvailableTools() {
        return availableTools;
    }

    public void setAvailableTools(long availableTools) {
        this.availableTools = availableTools;
    }

    public long getBorrowedTools() {
        return borrowedTools;
    }

    public void setBorrowedTools(long borrowedTools) {
        this.borrowedTools = borrowedTools;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(long activeLoans) {
        this.activeLoans = activeLoans;
    }

    public long getCompletedLoans() {
        return completedLoans;
    }

    public void setCompletedLoans(long completedLoans) {
        this.completedLoans = completedLoans;
    }

    public BigDecimal getTotalFines() {
        return totalFines;
    }

    public void setTotalFines(BigDecimal totalFines) {
        this.totalFines = totalFines;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }
}
