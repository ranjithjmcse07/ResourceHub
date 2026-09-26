package com.toolshare.controller;

import com.toolshare.dto.ApiResponse;
import com.toolshare.dto.ReviewRequest;
import com.toolshare.model.Review;
import com.toolshare.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewRequest req
    ) {
        Review review = reviewService.addReview(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review submitted successfully", review));
    }

    @GetMapping("/tool/{toolId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByTool(@PathVariable Long toolId) {
        List<Review> reviews = reviewService.getReviewsByToolId(toolId);
        return ResponseEntity.ok(ApiResponse.ok("Tool reviews retrieved", reviews));
    }
}
