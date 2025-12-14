package com.ensam.notificationservice.repositories;

import com.ensam.notificationservice.entities.Notification;
import com.ensam.notificationservice.entities.Notification.NotificationStatus;
import com.ensam.notificationservice.entities.Notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientId(String recipientId);

    List<Notification> findByRecipientIdAndIsRead(String recipientId, Boolean isRead);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByType(NotificationType type);

    List<Notification> findByRelatedAbsenceId(Long absenceId);

    Long countByRecipientIdAndIsRead(String recipientId, Boolean isRead);
}
