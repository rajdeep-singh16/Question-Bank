package com.example.question.bank.repository;

import com.example.question.bank.domain.notification.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
}
