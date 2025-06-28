package com.example.question.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class QuestionBankException extends RuntimeException {

    public ResponseStatusException AuthorizationException(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
