package com.trading.journal.authentication.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public record ApplicationUser(
        @Id String userName,

        String password,

        String firstName,

        String lastName,

        String email,

        Boolean enabled,

        Boolean verified,

        List<Authority> authorities,

        LocalDateTime createdAt) {

    public ApplicationUser {
        enabled = true;
        verified = true;
    }
}