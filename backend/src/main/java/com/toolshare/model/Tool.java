package com.toolshare.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Tool {
    private Long id;
    private Long lenderId;
    private String lenderName;
    private String lenderCompany;
    private String toolName;
    private String category;
    private String description;
    private String toolCondition; // Brand New, Like New, Good, Fair
    private String location;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private String imageUrl;
    private String availabilityStatus; // AVAILABLE, REQUESTED, BORROWED, MAINTENANCE, INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed / Joined fields
    private Double averageRating;
    private Integer reviewCount;
    private String lenderMobile;
    private String lenderAddress;
    private String lenderEmail;

    public Tool() {
    }

    public Tool(Long id, Long lenderId, String toolName, String category, String description,
                String toolCondition, String location, BigDecimal hourlyRate, BigDecimal dailyRate,
                String imageUrl, String availabilityStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.lenderId = lenderId;
        this.toolName = toolName;
        this.category = category;
        this.description = description;
        this.toolCondition = toolCondition;
        this.location = location;
        this.hourlyRate = hourlyRate;
        this.dailyRate = dailyRate;
        this.imageUrl = imageUrl;
        this.availabilityStatus = availabilityStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLenderId() {
        return lenderId;
    }

    public void setLenderId(Long lenderId) {
        this.lenderId = lenderId;
    }

    public String getLenderName() {
        return lenderName;
    }

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
    }

    public String getLenderCompany() {
        return lenderCompany;
    }

    public void setLenderCompany(String lenderCompany) {
        this.lenderCompany = lenderCompany;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToolCondition() {
        return toolCondition;
    }

    public void setToolCondition(String toolCondition) {
        this.toolCondition = toolCondition;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getLenderMobile() {
        return lenderMobile;
    }

    public void setLenderMobile(String lenderMobile) {
        this.lenderMobile = lenderMobile;
    }

    public String getLenderAddress() {
        return lenderAddress;
    }

    public void setLenderAddress(String lenderAddress) {
        this.lenderAddress = lenderAddress;
    }

    public String getLenderEmail() {
        return lenderEmail;
    }

    public void setLenderEmail(String lenderEmail) {
        this.lenderEmail = lenderEmail;
    }
}
