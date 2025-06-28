package com.example.question.bank.repository;

import com.example.question.bank.domain.question.Question;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface QuestionRepository extends ReactiveMongoRepository<Question, String> {

    // TODO: 27/07/23 keep editing this based on homepage requirement
    @Query(value = "{}", fields = "{ 'answers': 0 }")
    Flux<Question> findQuestions();

    Mono<Question> findByQuestionId(String questionId);
}