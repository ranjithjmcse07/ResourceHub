package com.toolshare.controller;

import com.toolshare.dto.ApiResponse;
import com.toolshare.dto.ToolRequest;
import com.toolshare.model.Tool;
import com.toolshare.service.ToolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Tool>>> searchTools(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) Double maxDailyRate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "newest") String sortBy
    ) {
        List<Tool> tools = toolService.searchTools(query, category, location, condition, maxDailyRate, status, sortBy);
        return ResponseEntity.ok(ApiResponse.ok("Tools retrieved successfully", tools));
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getCities() {
        List<String> cities = toolService.getDistinctCities();
        return ResponseEntity.ok(ApiResponse.ok("Cities retrieved successfully", cities));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tool>> getToolById(@PathVariable Long id) {
        Tool tool = toolService.getToolById(id);
        return ResponseEntity.ok(ApiResponse.ok("Tool details retrieved", tool));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Tool>>> getMyTools(@AuthenticationPrincipal UserDetails userDetails) {
        List<Tool> tools = toolService.getToolsByLender(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Lender tools retrieved", tools));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Tool>> createTool(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ToolRequest req
    ) {
        Tool tool = toolService.createTool(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tool added successfully", tool));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Tool>> updateTool(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ToolRequest req
    ) {
        Tool tool = toolService.updateTool(id, userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.ok("Tool updated successfully", tool));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTool(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        toolService.deleteTool(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Tool deactivated/removed successfully"));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required"));
        }
        toolService.updateAvailability(id, userDetails.getUsername(), status.trim().toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Availability updated successfully"));
    }
}
