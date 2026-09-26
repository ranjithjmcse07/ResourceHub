package com.toolshare.controller;

import com.toolshare.dto.ApiResponse;
import com.toolshare.dto.BorrowRequestCreateDto;
import com.toolshare.model.BorrowRequest;
import com.toolshare.service.BorrowRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow-requests")
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    public BorrowRequestController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BorrowRequest>> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BorrowRequestCreateDto reqDto
    ) {
        BorrowRequest br = borrowRequestService.createRequest(userDetails.getUsername(), reqDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Borrow request submitted successfully", br));
    }

    @GetMapping({"/my", "/borrower/my"})
    public ResponseEntity<ApiResponse<List<BorrowRequest>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<BorrowRequest> list = borrowRequestService.getMyRequests(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("My borrow requests retrieved", list));
    }

    @GetMapping({"/lender", "/lender/my"})
    public ResponseEntity<ApiResponse<List<BorrowRequest>>> getLenderRequests(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<BorrowRequest> list = borrowRequestService.getLenderRequests(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Lender borrow requests retrieved", list));
    }

    @RequestMapping(value = "/{id}/approve", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Void>> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        borrowRequestService.approveRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Request approved successfully. Tool marked as BORROWED."));
    }

    @RequestMapping(value = "/{id}/reject", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        borrowRequestService.rejectRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Request rejected successfully."));
    }

    @RequestMapping(value = "/{id}/cancel", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<Void>> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        borrowRequestService.cancelRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Request cancelled successfully."));
    }

    @RequestMapping(value = "/{id}/return", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ApiResponse<BorrowRequest>> returnTool(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        BorrowRequest br = borrowRequestService.returnTool(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Tool returned successfully. Late fine: ₹" + br.getFineAmount(), br));
    }
}
