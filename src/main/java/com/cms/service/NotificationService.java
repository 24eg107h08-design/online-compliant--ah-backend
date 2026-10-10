package com.cms.service;

import com.cms.entity.Complaint;
import com.cms.entity.Notification;
import com.cms.entity.User;

import java.util.List;

public interface NotificationService {

    Notification createNotification(User user, Complaint complaint, String message);

    List<Notification> getUserNotifications(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    long getUnreadCount(Long userId);

    // Structure for future email notification integration
    void sendEmailNotification(String toEmail, String subject, String body);
}
