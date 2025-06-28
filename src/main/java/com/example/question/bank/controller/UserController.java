package com.example.question.bank.controller;

import com.example.question.bank.domain.user.User;
import com.example.question.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public Mono<User> getLoggedInUser(@RequestParam String userId) {
        return userService.findLoggedInUser(userId);
    }
}
