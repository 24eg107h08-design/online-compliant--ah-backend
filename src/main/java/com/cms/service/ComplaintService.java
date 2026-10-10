package com.cms.service;

import com.cms.dto.request.ComplaintCreateRequest;
import com.cms.dto.request.ComplaintUpdateRequest;
import com.cms.dto.request.StatusUpdateRequest;
import com.cms.dto.response.ComplaintResponse;
import com.cms.dto.response.DashboardStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface ComplaintService {

    ComplaintResponse createComplaint(ComplaintCreateRequest request, MultipartFile file);

    Page<ComplaintResponse> getUserComplaints(String status, String category, String search, int page, int size, String sortBy, String sortDir);

    ComplaintResponse getComplaintById(Long id);

    ComplaintResponse getComplaintByCode(String complaintCode);

    ComplaintResponse updateComplaint(Long id, ComplaintUpdateRequest request);

    Page<ComplaintResponse> getAllComplaints(String status, String category, String search, int page, int size, String sortBy, String sortDir);

    ComplaintResponse updateStatus(Long id, StatusUpdateRequest request);

    DashboardStatsResponse getUserStats();

    DashboardStatsResponse getAdminStats();

    java.util.List<ComplaintResponse> generateRandomComplaints(int count);
}
