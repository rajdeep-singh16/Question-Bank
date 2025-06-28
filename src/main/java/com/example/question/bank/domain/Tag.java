package com.example.question.bank.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@Builder
@Document(collection = "tags")
public class Tag {
}

