package com.toolshare.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ToolRequest {
    @NotBlank(message = "Tool name is required")
    private String toolName;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    private String toolCondition; // Brand New, Like New, Good, Fair

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Hourly rate cannot be negative")
    private BigDecimal hourlyRate;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Daily rate cannot be negative")
    private BigDecimal dailyRate;

    private String imageUrl;

    public ToolRequest() {
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
}
