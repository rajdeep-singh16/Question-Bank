package com.example.question.bank.service;

import com.example.question.bank.connector.ChatGptConnector;
import com.example.question.bank.constants.ApplicationConstants;
import com.example.question.bank.domain.answer.Answer;
import com.example.question.bank.domain.answer.AnswerRequest;
import com.example.question.bank.domain.chatgpt.ChatGptRequest;
import com.example.question.bank.domain.chatgpt.Message;
import com.example.question.bank.domain.question.Question;
import com.example.question.bank.domain.question.QuestionRequest;
import com.example.question.bank.domain.user.User;
import com.example.question.bank.helper.QuestionBankHelper;
import com.example.question.bank.repository.QuestionRepository;
import com.example.question.bank.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionsService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionBankHelper questionBankHelper;

    @Autowired
    private ChatGptConnector chatGptConnector;

    Mono<User> getUser() {
        return Mono.subscriberContext()
                .map(context -> context.get(ApplicationConstants.LOGGED_USER))
                .map(e -> (User) e);
    }

    public Mono<Question> addQuestion(QuestionRequest questionRequest, String chatGpt) {

        return getUser()
                .flatMap(user -> saveQuestion(user, questionRequest))
                .flatMap(question -> {
                    saveChatGptAnswer(question.getQuestionId(), question.getQuestionDescription(), chatGpt);
                    return Mono.just(question);
                });

    }

    private void saveChatGptAnswer(String questionId, String questionDescription, String chatGpt) {
        ChatGptRequest chatGptRequest = ChatGptRequest.builder().messages(Collections.singletonList(Message.builder().content(questionDescription).build())).build();
        Mono.just(chatGptRequest)
                .publishOn(Schedulers.elastic())
                .subscribe(request -> chatGptConnector.fetchChatGptResponse(chatGptRequest, chatGpt)
                        .flatMap(chatGptResponse -> {

                            AnswerRequest answerRequest = AnswerRequest.builder()
                                    .questionId(questionId)
                                    .answer(Optional.ofNullable(chatGptResponse.getChoices()).map(choices -> choices.get(0).getMessage().getContent()).orElse(null))
                                    .isChatGpt(true)
                                    .build();
                            return saveAnswer(null, answerRequest);
                        }).subscribe()
                );
    }

    private Mono<Question> saveQuestion(User user, QuestionRequest questionRequest) {
        String userName = !StringUtils.isEmpty(user.getFirstName()) ? user.getFirstName() : "";
        userName += !StringUtils.isEmpty(user.getLastName()) ? " " + user.getLastName() : "";

        String id = System.currentTimeMillis() + user.getUserId();
        Question question =
                Question.builder()
                        .userId(user.getUserId())
                        .questionId(id)
                        .lastModifiedDate(LocalDate.now().toString())
                        .userName(userName)
                        .questionTitle(questionRequest.getQuestionTitle())
                        .questionDescription(questionRequest.getQuestionDescription())
                        .tags(questionRequest.getTagList())
                        .build();

        return questionRepository.save(question);
    }

    public Mono<Question> updateQuestion(QuestionRequest questionRequest) {
        return questionRepository.findById(questionRequest.getQuestionId())
                .flatMap(existingQuestion -> saveQuestionContent(questionRequest, existingQuestion))
                .flatMap(existingQuestion -> voteQuestion(questionRequest, existingQuestion))
                .flatMap(existingQuestion -> addRemoveFavourite(questionRequest, existingQuestion))
                .flatMap(existingQuestion -> questionRepository.save(existingQuestion));
    }

    private Mono<Question> addRemoveFavourite(QuestionRequest questionRequest, Question existingQuestion) {
        if (questionRequest.isFavourite()) {
            String userId = questionRequest.getUserId();
            String questionId = questionRequest.getQuestionId();

            if (existingQuestion.getFavouriteAddedUsers().contains(userId)) {
                existingQuestion.getFavouriteAddedUsers().remove(userId);
            } else {
                existingQuestion.getFavouriteAddedUsers().add(userId);
            }

            return questionBankHelper.toggleQuestionIdInUserFavourite(userId, questionId)
                    .then(Mono.just(existingQuestion));
        }
        return Mono.just(existingQuestion);
    }

    private Mono<Question> saveQuestionContent(QuestionRequest questionRequest, Question question) {
        if (!StringUtils.isEmpty(questionRequest.getQuestionTitle()) && !StringUtils.isEmpty(questionRequest.getQuestionDescription())
                && !CollectionUtils.isEmpty(questionRequest.getTagList())) {
            question.setQuestionTitle(questionRequest.getQuestionTitle());
            question.setQuestionDescription(questionRequest.getQuestionDescription());
            question.setTags(questionRequest.getTagList());
        }
        return Mono.just(question);
    }

    private Mono<Question> voteQuestion(QuestionRequest questionRequest, Question question) {

        if (!StringUtils.isEmpty(questionRequest.getVoteType())) {

            String vote = questionRequest.getVoteType();
            String userId = questionRequest.getUserId();

            if (StringUtils.equalsIgnoreCase(vote, ApplicationConstants.UPVOTE) && !question.getUpvotedUsers().contains(userId)) {
                question.setUpvotes(question.getUpvotes() + 1);
                question.getUpvotedUsers().add(userId);
                boolean removed = question.getDownvotedUsers().remove(userId);
                if (removed) {
                    question.setDownvotes(question.getDownvotes() - 1);
                }
                if (!StringUtils.equalsIgnoreCase(questionRequest.getUserId(), question.getUserId())) {
                    questionBankHelper.updateVoteNotification(questionRequest, question, ApplicationConstants.UPVOTE);
                }

            } else if (StringUtils.equalsIgnoreCase(vote, ApplicationConstants.DOWNVOTE) && !question.getDownvotedUsers().contains(userId)) {
                question.setDownvotes(question.getDownvotes() + 1);
                question.getDownvotedUsers().add(userId);

                boolean removed = question.getUpvotedUsers().remove(userId);
                if (removed) {
                    question.setUpvotes(question.getUpvotes() - 1);
                }
                if (!StringUtils.equalsIgnoreCase(questionRequest.getUserId(), question.getUserId())) {
                    questionBankHelper.updateVoteNotification(questionRequest, question, ApplicationConstants.DOWNVOTE);
                }
            }
        }
        return Mono.just(question);
    }

    public Mono<Void> deleteQuestion(String questionId) {
        return questionRepository.deleteById(questionId);
    }

    public Mono<List<Question>> getAllQuestions(QuestionRequest questionRequest) {
        if (!CollectionUtils.isEmpty(questionRequest.getFilters())) {
            return getAllFilteredQuestions(questionRequest);
        }
        return questionRepository.findQuestions().collectList()
                .flatMap(questionList -> questionBankHelper.resolveSearch(questionList, questionRequest));
    }

    public Mono<Question> getQuestion(String questionId) {
        return questionRepository.findById(questionId)
                .switchIfEmpty(Mono.error(new Exception("No question found with given questionId")));
    }

    public Mono<Answer> addAnswer(AnswerRequest answerRequest) {
        return Mono.subscriberContext()
                .map(context -> context.get(ApplicationConstants.LOGGED_USER))
                .map(e -> (User) e)
                .flatMap(user -> saveAnswer(user, answerRequest));
    }

    private Mono<Answer> saveAnswer(User user, AnswerRequest answerRequest) {
        if (StringUtils.isEmpty(answerRequest.getAnswer())) {
            return Mono.empty();
        }
        String answerId = user != null ? System.currentTimeMillis() + user.getUserId() : null;

        String userName = "";
        if (answerRequest.isChatGpt()) {
            userName = "Chat GPT";
        } else {
            userName += !StringUtils.isEmpty(user.getFirstName()) ? user.getFirstName() : "";
            userName += !StringUtils.isEmpty(user.getLastName()) ? " " + user.getLastName() : "";
        }

        questionBankHelper.updateCommentNotification(userName, answerRequest, user.getUserId());
        String finalUserName = userName;
        return questionRepository.findById(answerRequest.getQuestionId())
                .flatMap(existingQuestion -> {
                    Answer answer = Answer.builder()
                            .answer(answerRequest.getAnswer())
                            .userId(Optional.ofNullable(user).map(User::getUserId).orElse(null))
                            .lastModifiedDate(LocalDate.now().toString())
                            .answerId(answerId)
                            .userName(finalUserName)
                            .build();
                    existingQuestion.getAnswers().add(answer);
                    existingQuestion.setAnswersCount(existingQuestion.getAnswersCount() + 1);
                    return questionRepository.save(existingQuestion)
                            .then(Mono.just(answer));
                });
    }

    public Mono<Answer> updateAnswer(Answer answer, String questionId) {
        return questionRepository.findById(questionId)
                .flatMap(existingQuestion -> {
                    existingQuestion.getAnswers().forEach(ans -> {
                        if (ans.getAnswerId().equalsIgnoreCase(answer.getAnswerId())) {
                            ans.setAnswer(answer.getAnswer());
                        }
                    });
                    answer.setLastModifiedDate(LocalDate.now().toString());
                    return questionRepository.save(existingQuestion)
                            .then(Mono.just(answer));
                });
    }

    public Mono<Void> deleteAnswer(String answerId, String questionId) {
        return questionRepository.findById(questionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "We went into some errors.")))
                .flatMap(existingQuestion -> {
                    List<Answer> updatedAnswerList = existingQuestion.getAnswers().stream()
                            .filter(ans -> !ans.getAnswerId().equalsIgnoreCase(answerId))
                            .collect(Collectors.toList());
                    existingQuestion.setAnswers(updatedAnswerList);
                    existingQuestion.setAnswersCount(existingQuestion.getAnswersCount() - 1);
                    return questionRepository.save(existingQuestion)
                            .then(Mono.empty());
                });
    }

    public Mono<List<Question>> getAllFilteredQuestions(QuestionRequest questionRequest) {
        if (questionRequest.getFilters().contains("Favourite questions")) {
            return filterFavouriteQuestion(questionRequest);
        } else if (questionRequest.getFilters().contains("My questions")) {
            return filterMyQuestions(questionRequest);
        } else {
            return filterUpvotedQuestions(questionRequest);
        }
    }

    private Mono<List<Question>> filterMyQuestions(QuestionRequest questionRequest) {
        return questionRepository.findQuestions()
                .filter(question -> question.getUserId().equalsIgnoreCase(questionRequest.getUserId()))
                .collectList()
                .flatMap(questionList -> questionBankHelper.resolveSearch(questionList, questionRequest));
    }

    private Mono<List<Question>> filterFavouriteQuestion(QuestionRequest questionRequest) {
        return userRepository.findById(questionRequest.getUserId())
                .flatMap(user -> Flux.fromIterable(user.getFavouriteQuestions())
                        .flatMap(questionId -> questionRepository.findByQuestionId(questionId))
                        .collectList())
                .flatMap(questionList -> questionBankHelper.resolveSearch(questionList, questionRequest));
    }

    private Mono<List<Question>> filterUpvotedQuestions(QuestionRequest questionRequest) {
        return questionRepository.findQuestions()
                .filter(question -> question.getUpvotedUsers().contains(questionRequest.getUserId()))
                .collectList()
                .flatMap(questionList -> questionBankHelper.resolveSearch(questionList, questionRequest));
    }
}
