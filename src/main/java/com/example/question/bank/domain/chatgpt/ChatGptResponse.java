package com.example.question.bank.domain.chatgpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatGptResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;

}
