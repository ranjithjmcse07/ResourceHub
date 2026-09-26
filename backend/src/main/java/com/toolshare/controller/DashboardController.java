package com.toolshare.controller;

import com.toolshare.dto.ApiResponse;
import com.toolshare.dto.DashboardStatsDto;
import com.toolshare.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/lender", "/lender/stats"})
    @PreAuthorize("hasRole('LENDER')")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getLenderStats(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DashboardStatsDto stats = dashboardService.getLenderStats(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Lender dashboard stats retrieved", stats));
    }

    @GetMapping({"/borrower", "/borrower/stats"})
    @PreAuthorize("hasRole('BORROWER')")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getBorrowerStats(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        DashboardStatsDto stats = dashboardService.getBorrowerStats(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Borrower dashboard stats retrieved", stats));
    }

    @GetMapping({"/admin", "/admin/stats"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        Map<String, Object> stats = dashboardService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.ok("Admin dashboard stats retrieved", stats));
    }
}
