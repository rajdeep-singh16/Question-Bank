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
public class ChatGptRequest {

    @Builder.Default
    private String model = "gpt-3.5-turbo";
    private List<Message> messages;
}
