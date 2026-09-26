package com.toolshare.service;

import com.toolshare.dao.BorrowRequestDao;
import com.toolshare.dao.ReviewDao;
import com.toolshare.dao.UserDao;
import com.toolshare.dto.ReviewRequest;
import com.toolshare.exception.BadRequestException;
import com.toolshare.exception.ResourceNotFoundException;
import com.toolshare.model.BorrowRequest;
import com.toolshare.model.Review;
import com.toolshare.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewDao reviewDao;
    private final BorrowRequestDao borrowRequestDao;
    private final UserDao userDao;

    public ReviewService(ReviewDao reviewDao, BorrowRequestDao borrowRequestDao, UserDao userDao) {
        this.reviewDao = reviewDao;
        this.borrowRequestDao = borrowRequestDao;
        this.userDao = userDao;
    }

    public Review addReview(String borrowerUsername, ReviewRequest req) {
        User borrower = userDao.findByUsername(borrowerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + borrowerUsername));

        BorrowRequest br = borrowRequestDao.findById(req.getBorrowRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found: " + req.getBorrowRequestId()));

        if (!br.getBorrowerId().equals(borrower.getId())) {
            throw new BadRequestException("You can only review tools you have borrowed yourself");
        }

        if (!"COMPLETED".equalsIgnoreCase(br.getStatus())) {
            throw new BadRequestException("You can only submit a review after completing the borrowing transaction");
        }

        if (!br.getToolId().equals(req.getToolId())) {
            throw new BadRequestException("Tool ID does not match the borrowing record");
        }

        if (reviewDao.existsByBorrowRequestId(req.getBorrowRequestId())) {
            throw new BadRequestException("A review has already been submitted for this borrowing transaction");
        }

        Review review = new Review();
        review.setToolId(req.getToolId());
        review.setBorrowerId(borrower.getId());
        review.setBorrowRequestId(req.getBorrowRequestId());
        review.setRating(req.getRating());
        review.setComment(req.getComment().trim());

        return reviewDao.createReview(review);
    }

    public List<Review> getReviewsByToolId(Long toolId) {
        return reviewDao.findByToolId(toolId);
    }

    public List<Review> getAllReviews() {
        return reviewDao.findAllReviews();
    }
}
