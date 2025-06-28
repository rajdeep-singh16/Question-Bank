package com.example.question.bank.domain.chatgpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Choice {

    private int index;
    private Message message;
    private String finish_reason;
}
