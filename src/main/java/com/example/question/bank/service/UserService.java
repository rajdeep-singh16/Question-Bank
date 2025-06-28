package com.example.question.bank.service;

import com.example.question.bank.domain.user.User;
import com.example.question.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    public Mono<User> findLoggedInUser(String userId) {
        return userRepository.findById(userId);
    }
}
