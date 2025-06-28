package com.example.question.bank.domain.answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequest {
    private String questionId;
    private String askedUserId;
    private String answer;
    private boolean isChatGpt;
}
