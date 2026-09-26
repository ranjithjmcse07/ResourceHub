package com.toolshare.controller;

import com.toolshare.dto.ApiResponse;
import com.toolshare.dto.SuggestionStatusUpdateDto;
import com.toolshare.model.BorrowRequest;
import com.toolshare.model.Review;
import com.toolshare.model.Suggestion;
import com.toolshare.model.Tool;
import com.toolshare.model.User;
import com.toolshare.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ToolService toolService;
    private final BorrowRequestService borrowRequestService;
    private final ReviewService reviewService;
    private final SuggestionService suggestionService;

    public AdminController(UserService userService, ToolService toolService,
                           BorrowRequestService borrowRequestService, ReviewService reviewService,
                           SuggestionService suggestionService) {
        this.userService = userService;
        this.toolService = toolService;
        this.borrowRequestService = borrowRequestService;
        this.reviewService = reviewService;
        this.suggestionService = suggestionService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(ApiResponse.ok("All users retrieved", users));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        Boolean active = body.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status field 'active' is required"));
        }
        userService.updateUserStatus(id, active);
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully"));
    }

    @GetMapping("/tools")
    public ResponseEntity<ApiResponse<List<Tool>>> getAllTools() {
        List<Tool> tools = toolService.searchTools(null, null, null, null, null, "All", "newest");
        return ResponseEntity.ok(ApiResponse.ok("All tools retrieved", tools));
    }

    @DeleteMapping("/tools/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTool(@PathVariable Long id) {
        toolService.deleteTool(id, "admin", true);
        return ResponseEntity.ok(ApiResponse.ok("Tool removed by admin successfully"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<BorrowRequest>>> getAllTransactions() {
        List<BorrowRequest> transactions = borrowRequestService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.ok("All transactions retrieved", transactions));
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(ApiResponse.ok("All reviews retrieved", reviews));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<Suggestion>>> getAllSuggestions() {
        List<Suggestion> suggestions = suggestionService.getAllSuggestions();
        return ResponseEntity.ok(ApiResponse.ok("All suggestions retrieved", suggestions));
    }

    @PatchMapping("/suggestions/{id}")
    public ResponseEntity<ApiResponse<Void>> updateSuggestion(
            @PathVariable Long id,
            @Valid @RequestBody SuggestionStatusUpdateDto dto
    ) {
        suggestionService.updateSuggestionStatus(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Suggestion status and response updated successfully"));
    }
}
