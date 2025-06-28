package com.example.question.bank.repository;

import com.example.question.bank.domain.ExpiredToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends ReactiveMongoRepository<ExpiredToken, String> {
}
