package com.toolshare.model;

import java.time.LocalDateTime;

public class Review {
    private Long id;
    private Long toolId;
    private String toolName;
    private Long borrowerId;
    private String borrowerName;
    private Long borrowRequestId;
    private Integer rating; // 1 to 5
    private String comment;
    private LocalDateTime createdAt;

    public Review() {
    }

    public Review(Long id, Long toolId, Long borrowerId, Long borrowRequestId, Integer rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.toolId = toolId;
        this.borrowerId = borrowerId;
        this.borrowRequestId = borrowRequestId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getToolId() {
        return toolId;
    }

    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public Long getBorrowRequestId() {
        return borrowRequestId;
    }

    public void setBorrowRequestId(Long borrowRequestId) {
        this.borrowRequestId = borrowRequestId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
