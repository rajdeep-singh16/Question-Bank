package com.example.question.bank.domain.chatgpt;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Builder.Default
    private String role = "user";
    private String content;
}
