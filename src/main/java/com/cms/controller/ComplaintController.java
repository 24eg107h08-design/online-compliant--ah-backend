package com.cms.controller;

import com.cms.dto.request.ComplaintCreateRequest;
import com.cms.dto.request.ComplaintUpdateRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.ComplaintResponse;
import com.cms.dto.response.DashboardStatsResponse;
import com.cms.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    @Autowired
    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * Submit complaint via multipart form data (with optional file)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ComplaintResponse>> submitComplaintMultipart(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        ComplaintCreateRequest request = new ComplaintCreateRequest(title, category, description, location);
        ComplaintResponse created = complaintService.createComplaint(request, file);

        return new ResponseEntity<>(
                ApiResponse.success("Complaint submitted successfully! ID: " + created.getComplaintCode(), created),
                HttpStatus.CREATED
        );
    }

    /**
     * Submit complaint via JSON (without file)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ComplaintResponse>> submitComplaintJson(
            @Valid @RequestBody ComplaintCreateRequest request) {

        ComplaintResponse created = complaintService.createComplaint(request, null);
        return new ResponseEntity<>(
                ApiResponse.success("Complaint submitted successfully! ID: " + created.getComplaintCode(), created),
                HttpStatus.CREATED
        );
    }

    /**
     * Get user's own complaints with pagination and filtering
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getMyComplaints(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        Page<ComplaintResponse> complaints = complaintService.getUserComplaints(
                status, category, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched successfully", complaints));
    }

    /**
     * Get user's complaint by database ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable Long id) {
        ComplaintResponse complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved", complaint));
    }

    /**
     * Open tracking endpoint by Complaint Code (e.g. CMP-20261004-9842)
     */
    @GetMapping("/track/{complaintCode}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> trackComplaint(@PathVariable String complaintCode) {
        ComplaintResponse complaint = complaintService.getComplaintByCode(complaintCode);
        return ResponseEntity.ok(ApiResponse.success("Complaint tracked successfully", complaint));
    }

    /**
     * Edit complaint (allowed only if status is SUBMITTED)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintUpdateRequest request) {

        ComplaintResponse updated = complaintService.updateComplaint(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint updated successfully", updated));
    }

    /**
     * Get dashboard statistics for current user
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getUserDashboardStats() {
        DashboardStatsResponse stats = complaintService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success("User statistics retrieved", stats));
    }
}
