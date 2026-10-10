package com.cms.service.impl;

import com.cms.entity.Complaint;
import com.cms.entity.Notification;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.exception.UnauthorizedException;
import com.cms.repository.NotificationRepository;
import com.cms.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Notification createNotification(User user, Complaint complaint, String message) {
        Notification notification = new Notification(user, complaint, message);
        Notification saved = notificationRepository.save(notification);

        // Trigger future email if recipient email is available
        if (user != null && user.getEmail() != null) {
            sendEmailNotification(user.getEmail(), "CMS Complaint Update", message);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to modify this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void sendEmailNotification(String toEmail, String subject, String body) {
        // Structured extensible placeholder for Spring Boot JavaMailSender
        logger.info("[EMAIL NOTIFICATION DISPATCH] To: {} | Subject: {} | Message: {}", toEmail, subject, body);
        // When JavaMailSender is configured:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(toEmail);
        // message.setSubject(subject);
        // message.setText(body);
        // mailSender.send(message);
    }
}
