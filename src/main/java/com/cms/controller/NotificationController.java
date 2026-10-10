package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.entity.Notification;
import com.cms.entity.User;
import com.cms.service.AuthService;
import com.cms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @Autowired
    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<com.cms.dto.response.NotificationResponse>>> getUserNotifications() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
        List<com.cms.dto.response.NotificationResponse> responseList = notifications.stream()
                .map(n -> new com.cms.dto.response.NotificationResponse(
                        n.getId(),
                        n.getComplaint() != null ? n.getComplaint().getId() : null,
                        n.getComplaint() != null ? n.getComplaint().getComplaintCode() : null,
                        n.getMessage(),
                        n.getIsRead(),
                        n.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", responseList));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        long count = notificationService.getUnreadCount(currentUser.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", response));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
