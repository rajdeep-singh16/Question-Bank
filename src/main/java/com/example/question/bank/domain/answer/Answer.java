package com.example.question.bank.domain.answer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Answer {
    private String questionId;
    private String answerId;
    private String userId;
    private String answer;
    private String lastModifiedDate;
    @Builder.Default
    private int upvotes = 0;
    @Builder.Default
    private int downvotes = 0;
    private String userName;
}
