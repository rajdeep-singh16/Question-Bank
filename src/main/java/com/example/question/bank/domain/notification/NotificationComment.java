package com.example.question.bank.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationComment {
    private String userName;
    private String comment;
    private String questionId;
}
