package com.example.question.bank.controller;

import com.example.question.bank.domain.ExpiredToken;
import com.example.question.bank.domain.authentication.AuthenticationRequest;
import com.example.question.bank.domain.authentication.AuthenticationResponse;
import com.example.question.bank.domain.authentication.RegisterRequest;
import com.example.question.bank.domain.user.User;
import com.example.question.bank.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public Mono<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }

    @PostMapping("/authenticate")
    public Mono<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/logout")
    public Mono<Void> logout(@RequestBody ExpiredToken expiredToken) {
        return authenticationService.logout(expiredToken);
    }

    @GetMapping("/users/all")
    public Mono<List<User>> getAllUsers() {
        return authenticationService.getAllUsers();
    }


}
