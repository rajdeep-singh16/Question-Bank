package com.example.question.bank.controller;

import com.example.question.bank.domain.notification.Notification;
import com.example.question.bank.domain.notification.NotificationRequest;
import com.example.question.bank.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin("*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notification")
    public Mono<Notification> getNotification(@RequestParam String userId) {
        return notificationService.getNotification(userId);
    }

    @DeleteMapping("/notification")
    public Mono<Void> deleteNotification(@RequestBody NotificationRequest request) {
        return notificationService.deleteNotification(request.getUserId());
    }

}
