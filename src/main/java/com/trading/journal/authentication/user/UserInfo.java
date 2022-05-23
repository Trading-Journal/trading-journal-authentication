package com.trading.journal.authentication.user;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trading.journal.authentication.jwt.helper.DateHelper;

import org.springframework.data.mongodb.core.mapping.Field;

public record UserInfo(

        @Field("_id") String userName,

        String firstName,

        String lastName,

        String email,

        Boolean enabled,

        Boolean verified,

        List<Authority> authorities,

        @JsonFormat(pattern = DateHelper.DATE_TIME_FORMAT) LocalDateTime createdAt) {
}