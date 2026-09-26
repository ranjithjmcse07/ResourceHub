package com.toolshare.controller;

import com.toolshare.dto.ApiResponse;
import com.toolshare.dto.SuggestionRequest;
import com.toolshare.model.Suggestion;
import com.toolshare.service.SuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Suggestion>> createSuggestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SuggestionRequest req
    ) {
        Suggestion s = suggestionService.createSuggestion(userDetails.getUsername(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Suggestion submitted successfully", s));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Suggestion>>> getMySuggestions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<Suggestion> list = suggestionService.getMySuggestions(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Suggestions retrieved successfully", list));
    }
}
