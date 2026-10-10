package com.cms.dto.request;

import jakarta.validation.constraints.NotBlank;

public class StatusUpdateRequest {

    @NotBlank(message = "Status is required (SUBMITTED, IN_PROGRESS, RESOLVED, REJECTED)")
    private String status;

    private String remarks;

    private String assignedDepartment;

    private String assignedStaff;

    public StatusUpdateRequest() {
    }

    public StatusUpdateRequest(String status, String remarks, String assignedDepartment, String assignedStaff) {
        this.status = status;
        this.remarks = remarks;
        this.assignedDepartment = assignedDepartment;
        this.assignedStaff = assignedStaff;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAssignedDepartment() {
        return assignedDepartment;
    }

    public void setAssignedDepartment(String assignedDepartment) {
        this.assignedDepartment = assignedDepartment;
    }

    public String getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }
}
