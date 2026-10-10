package com.cms.controller;

import com.cms.dto.request.StatusUpdateRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.ComplaintResponse;
import com.cms.dto.response.DashboardStatsResponse;
import com.cms.dto.response.UserResponse;
import com.cms.service.ComplaintService;
import com.cms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ComplaintService complaintService;
    private final UserService userService;

    @Autowired
    public AdminController(ComplaintService complaintService, UserService userService) {
        this.complaintService = complaintService;
        this.userService = userService;
    }

    /**
     * Get system-wide dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAdminDashboard() {
        DashboardStatsResponse stats = complaintService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved", stats));
    }

    /**
     * Get all complaints with multi-criteria filtering and pagination
     */
    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getAllComplaints(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        Page<ComplaintResponse> complaints = complaintService.getAllComplaints(
                status, category, search, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Complaints fetched successfully", complaints));
    }

    /**
     * Get single complaint details by ID
     */
    @GetMapping("/complaints/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintDetails(@PathVariable Long id) {
        ComplaintResponse complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved", complaint));
    }

    /**
     * Update complaint status, assign department/staff, add remarks
     */
    @PatchMapping("/complaints/{id}/status")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {

        ComplaintResponse updated = complaintService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", updated));
    }

    /**
     * List all registered users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("User list retrieved successfully", users));
    }

    /**
     * Generate randomized complaints on demand
     */
    @PostMapping("/complaints/randomize")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> randomizeComplaints(
            @RequestParam(value = "count", defaultValue = "5") int count) {
        List<ComplaintResponse> created = complaintService.generateRandomComplaints(count);
        return ResponseEntity.ok(ApiResponse.success("Generated " + created.size() + " randomized complaints successfully!", created));
    }
}
