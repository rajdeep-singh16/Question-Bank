package com.example.question.bank.service;

import com.example.question.bank.constants.ApplicationConstants;
import com.example.question.bank.domain.ExpiredToken;
import com.example.question.bank.domain.authentication.AuthenticationRequest;
import com.example.question.bank.domain.authentication.AuthenticationResponse;
import com.example.question.bank.domain.authentication.RegisterRequest;
import com.example.question.bank.domain.user.Role;
import com.example.question.bank.domain.user.User;
import com.example.question.bank.exception.QuestionBankException;
import com.example.question.bank.repository.TokenRepository;
import com.example.question.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final QuestionBankException questionBankException;

    private final TokenRepository tokenRepository;

    public Mono<AuthenticationResponse> register(RegisterRequest request) {
        String id = String.valueOf(UUID.randomUUID());
        User user = User.builder()
                .userId(id)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .role(Role.USER)
                .build();

        return Mono.just(user)
                .flatMap(user1 -> userRepository.save(user))
                .map(jwtService::generateToken)
                .map(jwtToken -> AuthenticationResponse.builder().token(jwtToken).userId(user.getUserId()).build())
                .onErrorResume(error -> {
                    if (error.toString().contains("11000")) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists"));
                    }
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "an unknown error has occurred"));
                });

    }


    public Mono<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.error(questionBankException.AuthorizationException(HttpStatus.UNAUTHORIZED, ApplicationConstants.AUTHENTICATION_ERROR_MESSAGE)))
                .flatMap(user -> {
                    boolean passwordMatches = BCrypt.checkpw(request.getPassword(), user.getPassword());
                    if (!passwordMatches) {
                        return Mono.error(questionBankException.AuthorizationException(HttpStatus.UNAUTHORIZED, ApplicationConstants.AUTHENTICATION_ERROR_MESSAGE));
                    }
                    return Mono.just(user);
                })
                .map(user -> AuthenticationResponse.builder().token(jwtService.generateToken(user)).userId(user.getUserId()).build());
    }

    public Mono<List<User>> getAllUsers() {
        return userRepository.findAll().collectList();
    }

    public Mono<Void> logout(ExpiredToken expiredToken) {
        return tokenRepository.save(expiredToken).then(Mono.empty());
    }
}
