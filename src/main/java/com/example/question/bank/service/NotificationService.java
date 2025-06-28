package com.example.question.bank.service;

import com.example.question.bank.domain.notification.Notification;
import com.example.question.bank.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Mono<Notification> getNotification(String userId) {
        return notificationRepository.findById(userId);
    }

    public Mono<Void> deleteNotification(String userId) {
        return notificationRepository.deleteById(userId);
    }
}
