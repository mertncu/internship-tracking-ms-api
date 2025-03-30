package com.internship.service.impl;

import com.internship.entity.InternshipStatus;
import com.internship.entity.Notification;
import com.internship.entity.NotificationType;
import com.internship.entity.User;
import com.internship.repository.NotificationRepository;
import com.internship.repository.UserRepository;
import com.internship.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void notifyDepartmentCoordinator(Long internshipId, Long coordinatorId) {
        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new RuntimeException("Coordinator not found"));
        
        createNotification(
            coordinator,
            "Yeni bir staj başvurusu onayınızı bekliyor",
            "Staj ID: " + internshipId + " için danışman onayı alındı. İncelemeniz gerekiyor.",
            NotificationType.STATUS_UPDATE
        );
    }

    @Override
    public void notifyUniversityCoordinator(Long internshipId, Long coordinatorId) {
        User coordinator = userRepository.findById(coordinatorId)
                .orElseThrow(() -> new RuntimeException("Coordinator not found"));
        
        createNotification(
            coordinator,
            "Yeni bir staj başvurusu onayınızı bekliyor",
            "Staj ID: " + internshipId + " için bölüm koordinatörü onayı alındı. İncelemeniz gerekiyor.",
            NotificationType.STATUS_UPDATE
        );
    }

    @Override
    public void notifyRevisionRequest(Long internshipId, Long studentId, String reason) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        createNotification(
            student,
            "Staj başvurunuz için düzeltme talebi",
            "Staj ID: " + internshipId + " için düzeltme talebi: " + reason,
            NotificationType.COMMENT_ADDED
        );
    }

    @Override
    public void notifyStudentStatusChange(Long internshipId, Long studentId, InternshipStatus newStatus) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        String title = "Staj başvurunuzun durumu güncellendi";
        String message = String.format("Staj ID: %d için yeni durum: %s", internshipId, newStatus.toString());
        
        createNotification(
            student,
            title,
            message,
            NotificationType.STATUS_UPDATE
        );
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public Notification createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public void sendNotification(Long userId, String message, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        createNotification(user, "System Notification", message, type);
    }

    @Override
    public void sendBulkNotification(List<Long> userIds, String message, NotificationType type) {
        userIds.forEach(userId -> sendNotification(userId, message, type));
    }

    @Override
    public void notifyAdvisorNewApplication(Long internshipId, Long advisorId) {
        sendNotification(advisorId,
                "You have been assigned as an advisor for internship #" + internshipId,
                NotificationType.ADVISOR_ASSIGNMENT);
    }

    @Override
    public void notifyStudentSGKDeclaration(Long internshipId, Long studentId, String message) {
        sendNotification(studentId,
                "SGK Declaration for internship #" + internshipId + ": " + message,
                NotificationType.SYSTEM_NOTIFICATION);
    }
    
    @Override
    public void createSystemNotification(String title, String message) {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM_NOTIFICATION)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void createApprovalRequiredNotification(User user, String title, String message) {
        createNotification(user, title, message, NotificationType.STATUS_UPDATE);
    }

    @Override
    public void createRevisionRequestedNotification(User user, String title, String message) {
        createNotification(user, title, message, NotificationType.COMMENT_ADDED);
    }
} 