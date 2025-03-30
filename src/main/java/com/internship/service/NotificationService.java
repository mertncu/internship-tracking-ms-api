package com.internship.service;

import com.internship.entity.InternshipStatus;
import com.internship.entity.NotificationType;
import com.internship.entity.Notification;
import com.internship.entity.User;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User user, String title, String message, NotificationType type);
    List<Notification> getUserNotifications(User user);
    List<Notification> getUnreadNotifications(User user);
    void markAsRead(Long notificationId);
    void markAllAsRead(User user);
    Long getUnreadCount(User user);
    void deleteNotification(Long notificationId);
    void createSystemNotification(String title, String message);
    void createApprovalRequiredNotification(User user, String title, String message);
    void createRevisionRequestedNotification(User user, String title, String message);
    void notifyDepartmentCoordinator(Long internshipId, Long coordinatorId);
    void notifyUniversityCoordinator(Long internshipId, Long coordinatorId);
    void notifyRevisionRequest(Long internshipId, Long studentId, String reason);
    void notifyStudentStatusChange(Long internshipId, Long studentId, InternshipStatus newStatus);
    void sendNotification(Long userId, String message, NotificationType type);
    void sendBulkNotification(List<Long> userIds, String message, NotificationType type);
    void notifyAdvisorNewApplication(Long internshipId, Long advisorId);
    void notifyStudentSGKDeclaration(Long internshipId, Long studentId, String message);
} 