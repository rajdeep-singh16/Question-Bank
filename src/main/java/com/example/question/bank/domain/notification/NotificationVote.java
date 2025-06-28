package com.example.question.bank.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVote {
    private String questionId;
    private String questionDescription;
    @Builder.Default
    private List<String> upvotedUsers = new ArrayList<>();
    @Builder.Default
    private List<String> downvotedUsers = new ArrayList<>();
}
