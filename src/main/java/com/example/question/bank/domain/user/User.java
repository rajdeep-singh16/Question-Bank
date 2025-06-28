package com.example.question.bank.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    @Field("_id")
    private String userId;
    private String firstName;
    private String lastName;
    @Indexed(unique = true)
    private String email;
    private String password;
    private Role role;
    @Builder.Default
    private String city = "Bangalore";
    private String state = "Karnataka";
    private double medals;

    @JsonIgnore
    private boolean isPasswordMatching;

    public String getUsername() {
        return email;
    }

    @Builder.Default
    private List<String> favouriteQuestions = new ArrayList<>();

}
