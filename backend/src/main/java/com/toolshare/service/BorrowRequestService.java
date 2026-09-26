package com.toolshare.service;

import com.toolshare.dao.BorrowRequestDao;
import com.toolshare.dao.ToolDao;
import com.toolshare.dao.UserDao;
import com.toolshare.dto.BorrowRequestCreateDto;
import com.toolshare.exception.BadRequestException;
import com.toolshare.exception.ResourceNotFoundException;
import com.toolshare.model.BorrowRequest;
import com.toolshare.model.Tool;
import com.toolshare.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowRequestService {

    private final BorrowRequestDao borrowRequestDao;
    private final ToolDao toolDao;
    private final UserDao userDao;

    @Value("${app.fine.per-day:50.00}")
    private BigDecimal finePerDay;

    @Value("${app.fine.per-hour:10.00}")
    private BigDecimal finePerHour;

    @Value("${app.fine.grace-minutes:30}")
    private long graceMinutes;

    public BorrowRequestService(BorrowRequestDao borrowRequestDao, ToolDao toolDao, UserDao userDao) {
        this.borrowRequestDao = borrowRequestDao;
        this.toolDao = toolDao;
        this.userDao = userDao;
    }

    public BigDecimal calculateLatePenalty(BorrowRequest req, LocalDateTime checkTime) {
        if (req == null || req.getExpectedReturnTime() == null || checkTime == null) {
            return BigDecimal.ZERO;
        }

        if (!checkTime.isAfter(req.getExpectedReturnTime())) {
            return BigDecimal.ZERO;
        }

        Duration lateDuration = Duration.between(req.getExpectedReturnTime(), checkTime);
        long totalLateMinutes = lateDuration.toMinutes();

        // 30-minute grace period rule: penalty starts only after a half hour (30 minutes)
        if (totalLateMinutes <= graceMinutes) {
            return BigDecimal.ZERO;
        }

        // Chargeable time applies for delay beyond the 30-minute grace period
        long chargeableMinutes = totalLateMinutes - graceMinutes;

        if ("HOURLY".equalsIgnoreCase(req.getRentalType())) {
            long chargeableHours = (long) Math.ceil((double) chargeableMinutes / 60.0);
            if (chargeableHours < 1) chargeableHours = 1;
            return finePerHour.multiply(BigDecimal.valueOf(chargeableHours));
        } else {
            long chargeableDays = (long) Math.ceil((double) chargeableMinutes / 1440.0);
            if (chargeableDays < 1) chargeableDays = 1;
            return finePerDay.multiply(BigDecimal.valueOf(chargeableDays));
        }
    }

    public void enrichRentalTrackingInfo(BorrowRequest req) {
        if (req == null) return;

        LocalDateTime now = LocalDateTime.now();

        if ("COMPLETED".equalsIgnoreCase(req.getStatus()) || "RETURNED".equalsIgnoreCase(req.getStatus())) {
            LocalDateTime returnRefTime = req.getActualReturnTime() != null ? req.getActualReturnTime()
                    : (req.getReturnedAt() != null ? req.getReturnedAt() : now);

            if (req.getExpectedReturnTime() != null && returnRefTime.isAfter(req.getExpectedReturnTime())) {
                long lateMins = Duration.between(req.getExpectedReturnTime(), returnRefTime).toMinutes();
                req.setLateMinutes(lateMins);
                req.setIsOverdue(lateMins > 0);
            } else {
                req.setLateMinutes(0L);
                req.setIsOverdue(false);
            }

            if (req.getFineAmount() != null) {
                req.setCurrentPenalty(req.getFineAmount());
            } else {
                req.setCurrentPenalty(calculateLatePenalty(req, returnRefTime));
            }
        } else if ("APPROVED".equalsIgnoreCase(req.getStatus()) || "ACTIVE".equalsIgnoreCase(req.getStatus())) {
            if (req.getExpectedReturnTime() != null && now.isAfter(req.getExpectedReturnTime())) {
                long lateMins = Duration.between(req.getExpectedReturnTime(), now).toMinutes();
                req.setLateMinutes(lateMins);
                req.setIsOverdue(true);
                req.setCurrentPenalty(calculateLatePenalty(req, now));
            } else {
                req.setLateMinutes(0L);
                req.setIsOverdue(false);
                req.setCurrentPenalty(BigDecimal.ZERO);
            }
        } else {
            req.setLateMinutes(0L);
            req.setIsOverdue(false);
            req.setCurrentPenalty(BigDecimal.ZERO);
        }
    }

    @Transactional
    public BorrowRequest createRequest(String borrowerUsername, BorrowRequestCreateDto reqDto) {
        User borrower = userDao.findByUsername(borrowerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + borrowerUsername));

        Tool tool = toolDao.findById(reqDto.getToolId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with id: " + reqDto.getToolId()));

        if (!"AVAILABLE".equalsIgnoreCase(tool.getAvailabilityStatus())) {
            throw new BadRequestException("This tool is currently not available for borrowing (" + tool.getAvailabilityStatus() + ")");
        }

        if (tool.getLenderId().equals(borrower.getId())) {
            throw new BadRequestException("You cannot borrow your own tool");
        }

        if (reqDto.getDuration() == null || reqDto.getDuration() < 1) {
            throw new BadRequestException("Borrow duration must be at least 1");
        }

        if (reqDto.getStartTime() == null) {
            reqDto.setStartTime(LocalDateTime.now());
        }

        if (borrowRequestDao.hasActiveOrPendingRequestForTool(tool.getId(), borrower.getId())) {
            throw new BadRequestException("You already have a pending or active request for this tool");
        }

        LocalDateTime expectedReturn;
        BigDecimal totalAmount;

        if ("HOURLY".equalsIgnoreCase(reqDto.getRentalType())) {
            expectedReturn = reqDto.getStartTime().plusHours(reqDto.getDuration());
            BigDecimal rate = tool.getHourlyRate() != null ? tool.getHourlyRate() : BigDecimal.ZERO;
            totalAmount = rate.multiply(BigDecimal.valueOf(reqDto.getDuration()));
        } else {
            // DAILY
            expectedReturn = reqDto.getStartTime().plusDays(reqDto.getDuration());
            BigDecimal rate = tool.getDailyRate() != null ? tool.getDailyRate() : BigDecimal.ZERO;
            totalAmount = rate.multiply(BigDecimal.valueOf(reqDto.getDuration()));
        }

        BorrowRequest br = new BorrowRequest();
        br.setToolId(tool.getId());
        br.setBorrowerId(borrower.getId());
        br.setRentalType(reqDto.getRentalType().toUpperCase());
        br.setDuration(reqDto.getDuration());
        br.setStartTime(reqDto.getStartTime());
        br.setExpectedReturnTime(expectedReturn);
        br.setTotalAmount(totalAmount);
        br.setFineAmount(BigDecimal.ZERO);
        br.setStatus("PENDING");
        br.setNotes(reqDto.getNotes());

        BorrowRequest created = borrowRequestDao.createRequest(br);
        enrichRentalTrackingInfo(created);
        return created;
    }

    @Transactional
    public void approveRequest(Long requestId, String lenderUsername) {
        User lender = userDao.findByUsername(lenderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + lenderUsername));

        BorrowRequest req = borrowRequestDao.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found: " + requestId));

        Tool tool = toolDao.findById(req.getToolId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found: " + req.getToolId()));

        if (!tool.getLenderId().equals(lender.getId()) && !"ADMIN".equals(lender.getRole())) {
            throw new BadRequestException("You can only approve requests for your own tools");
        }

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new BadRequestException("Only PENDING requests can be approved. Current status: " + req.getStatus());
        }

        borrowRequestDao.markApproved(requestId, LocalDateTime.now());
        toolDao.updateAvailability(tool.getId(), "BORROWED");
    }

    @Transactional
    public void rejectRequest(Long requestId, String lenderUsername) {
        User lender = userDao.findByUsername(lenderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + lenderUsername));

        BorrowRequest req = borrowRequestDao.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found: " + requestId));

        Tool tool = toolDao.findById(req.getToolId())
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found: " + req.getToolId()));

        if (!tool.getLenderId().equals(lender.getId()) && !"ADMIN".equals(lender.getRole())) {
            throw new BadRequestException("You can only reject requests for your own tools");
        }

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new BadRequestException("Only PENDING requests can be rejected. Current status: " + req.getStatus());
        }

        borrowRequestDao.updateStatus(requestId, "REJECTED");
    }

    @Transactional
    public void cancelRequest(Long requestId, String borrowerUsername) {
        User borrower = userDao.findByUsername(borrowerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + borrowerUsername));

        BorrowRequest req = borrowRequestDao.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found: " + requestId));

        if (!req.getBorrowerId().equals(borrower.getId()) && !"ADMIN".equals(borrower.getRole())) {
            throw new BadRequestException("You can only cancel your own requests");
        }

        if (!"PENDING".equalsIgnoreCase(req.getStatus())) {
            throw new BadRequestException("Only PENDING requests can be cancelled");
        }

        borrowRequestDao.updateStatus(requestId, "CANCELLED");
    }

    @Transactional
    public BorrowRequest returnTool(Long requestId, String username) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        BorrowRequest req = borrowRequestDao.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow request not found: " + requestId));

        // Can be returned by the borrower, or confirmed returned by the lender
        boolean isBorrower = req.getBorrowerId().equals(user.getId());
        boolean isLender = req.getLenderId() != null && req.getLenderId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());

        if (!isBorrower && !isLender && !isAdmin) {
            throw new BadRequestException("You are not authorized to return or close this request");
        }

        if (!"APPROVED".equalsIgnoreCase(req.getStatus()) && !"ACTIVE".equalsIgnoreCase(req.getStatus())) {
            throw new BadRequestException("Only APPROVED or ACTIVE borrowings can be returned");
        }

        LocalDateTime returnTime = LocalDateTime.now();
        BigDecimal fine = calculateLatePenalty(req, returnTime);

        borrowRequestDao.markReturned(requestId, returnTime, fine);
        toolDao.updateAvailability(req.getToolId(), "AVAILABLE");

        req.setStatus("COMPLETED");
        req.setActualReturnTime(returnTime);
        req.setFineAmount(fine);
        enrichRentalTrackingInfo(req);
        return req;
    }

    public List<BorrowRequest> getMyRequests(String borrowerUsername) {
        User borrower = userDao.findByUsername(borrowerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + borrowerUsername));
        List<BorrowRequest> list = borrowRequestDao.findByBorrowerId(borrower.getId());
        list.forEach(this::enrichRentalTrackingInfo);
        return list;
    }

    public List<BorrowRequest> getLenderRequests(String lenderUsername) {
        User lender = userDao.findByUsername(lenderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + lenderUsername));
        List<BorrowRequest> list = borrowRequestDao.findByLenderId(lender.getId());
        list.forEach(this::enrichRentalTrackingInfo);
        return list;
    }

    public List<BorrowRequest> getAllTransactions() {
        List<BorrowRequest> list = borrowRequestDao.findAllTransactions();
        list.forEach(this::enrichRentalTrackingInfo);
        return list;
    }
}
