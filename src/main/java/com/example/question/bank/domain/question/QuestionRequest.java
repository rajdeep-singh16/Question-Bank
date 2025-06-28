package com.example.question.bank.domain.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {
    private String questionId;
    private String userId;
    private String questionTitle;
    private String questionDescription;
    private String voteType;
    private String searchTerm;
    private boolean favourite;
    private List<String> filters;
    private List<String> tagList;
}
