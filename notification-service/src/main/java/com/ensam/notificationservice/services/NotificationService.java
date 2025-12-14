package com.ensam.notificationservice.services;

import com.ensam.notificationservice.dto.NotificationDto;
import com.ensam.notificationservice.entities.Notification;
import com.ensam.notificationservice.entities.Notification.NotificationStatus;
import com.ensam.notificationservice.entities.Notification.NotificationType;
import com.ensam.notificationservice.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDto createNotification(NotificationDto dto) {
        Notification notification = mapToEntity(dto);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationDto getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
        return mapToDto(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByRecipient(String recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(String recipientId) {
        return notificationRepository.findByRecipientIdAndIsRead(recipientId, false).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(String recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsByType(NotificationType type) {
        return notificationRepository.findByType(type).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        return mapToDto(updated);
    }

    @Transactional
    public void markAllAsRead(String recipientId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsRead(recipientId, false);

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public NotificationDto sendNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));

        // Simuler l'envoi (en production: intégrer email/SMS)
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    // Mapping methods
    private NotificationDto mapToDto(Notification entity) {
        NotificationDto dto = new NotificationDto();
        dto.setId(entity.getId());
        dto.setRecipientId(entity.getRecipientId());
        dto.setRecipientType(entity.getRecipientType());
        dto.setSubject(entity.getSubject());
        dto.setMessage(entity.getMessage());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        dto.setIsRead(entity.getIsRead());
        dto.setRelatedAbsenceId(entity.getRelatedAbsenceId());
        dto.setSentAt(entity.getSentAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private Notification mapToEntity(NotificationDto dto) {
        Notification entity = new Notification();
        entity.setRecipientId(dto.getRecipientId());
        entity.setRecipientType(dto.getRecipientType());
        entity.setSubject(dto.getSubject());
        entity.setMessage(dto.getMessage());
        entity.setType(dto.getType());
        entity.setRelatedAbsenceId(dto.getRelatedAbsenceId());
        return entity;
    }
}
