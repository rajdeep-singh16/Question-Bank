package com.example.question.bank.helper;

import com.example.question.bank.constants.ApplicationConstants;
import com.example.question.bank.domain.answer.AnswerRequest;
import com.example.question.bank.domain.notification.Notification;
import com.example.question.bank.domain.notification.NotificationComment;
import com.example.question.bank.domain.notification.NotificationVote;
import com.example.question.bank.domain.question.Question;
import com.example.question.bank.domain.question.QuestionRequest;
import com.example.question.bank.domain.user.User;
import com.example.question.bank.repository.NotificationRepository;
import com.example.question.bank.repository.QuestionRepository;
import com.example.question.bank.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class QuestionBankHelper {
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Mono<List<Question>> resolveSearch(List<Question> questionList, QuestionRequest questionRequest) {
        if(!StringUtils.isEmpty(questionRequest.getSearchTerm())) {
            return Mono.just(questionList.stream()
                    .filter(question -> question.getQuestionTitle().contains(questionRequest.getSearchTerm())
                            || question.getQuestionDescription().contains(questionRequest.getSearchTerm())).collect(Collectors.toList()));
        }
        return Mono.just(questionList);
    }

    public Mono<User> toggleQuestionIdInUserFavourite(String userId, String questionId) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    if(user.getFavouriteQuestions().contains(questionId)) {
                        user.getFavouriteQuestions().remove(questionId);
                    } else {
                        user.getFavouriteQuestions().add(questionId);
                    }

                    return Mono.just(user);
                })
                .flatMap(user -> userRepository.save(user));
    }

    public void updateVoteNotification(QuestionRequest questionRequest, Question question, String vote) {
        notificationRepository.findById(question.getUserId())
                .switchIfEmpty(Mono.just(Notification.builder().userId(question.getUserId()).build()))
                .flatMap(notification -> {
                    String questionId = question.getQuestionId();
                    String userId = questionRequest.getUserId();
                    if (StringUtils.equalsIgnoreCase(vote, ApplicationConstants.UPVOTE)) {
                        Optional<NotificationVote> optionalNotificationVote = notification.getNotificationVotes()
                                .stream()
                                .filter(e -> StringUtils.equalsIgnoreCase(e.getQuestionId(), questionId))
                                .findAny();

                        if(optionalNotificationVote.isPresent()) {
                            NotificationVote notificationVote = optionalNotificationVote.get();
                            notificationVote.getDownvotedUsers().remove(userId);
                            notificationVote.getUpvotedUsers().add(userId);
                        } else {
                            NotificationVote notificationVote = NotificationVote.builder()
                                    .questionId(questionId)
                                    .questionDescription(question.getQuestionDescription())
                                    .upvotedUsers(Arrays.asList(userId))
                                    .build();
                            notification.getNotificationVotes().add(notificationVote);
                        }
                        notificationRepository.save(notification).subscribe();
                    } else {
                        Optional<NotificationVote> optionalNotificationVote = notification.getNotificationVotes()
                                .stream()
                                .filter(e -> StringUtils.equalsIgnoreCase(e.getQuestionId(), questionId))
                                .findAny();

                        if(optionalNotificationVote.isPresent()) {
                            NotificationVote notificationVote = optionalNotificationVote.get();
                            notificationVote.getDownvotedUsers().add(userId);
                            notificationVote.getUpvotedUsers().remove(userId);
                        } else {
                            NotificationVote notificationVote = NotificationVote.builder()
                                    .questionId(questionId)
                                    .questionDescription(question.getQuestionDescription())
                                    .downvotedUsers(Arrays.asList(userId))
                                    .build();
                            notification.getNotificationVotes().add(notificationVote);
                        }
                        notificationRepository.save(notification).subscribe();
                    }
                    return null;
                })
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    public void updateCommentNotification(String userName, AnswerRequest answerRequest, String loggedInUserId) {
        if(StringUtils.equalsIgnoreCase(loggedInUserId, answerRequest.getAskedUserId())) {
            return;
        }
        notificationRepository.findById(answerRequest.getAskedUserId())
                .switchIfEmpty(Mono.just(Notification.builder().userId(answerRequest.getAskedUserId()).build()))
                .flatMap(notification -> {

                    NotificationComment notificationComment = NotificationComment.builder()
                            .userName(userName)
                            .comment(answerRequest.getAnswer())
                            .questionId(answerRequest.getQuestionId())
                            .build();
                    notification.getNotificationComments().add(notificationComment);
                    notificationRepository.save(notification).subscribe();
                    return null;
                })
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }
}
