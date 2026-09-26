package com.toolshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SuggestionStatusUpdateDto {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "OPEN|REVIEWED|RESOLVED", message = "Status must be OPEN, REVIEWED, or RESOLVED")
    private String status;

    private String adminResponse;

    public SuggestionStatusUpdateDto() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }
}
