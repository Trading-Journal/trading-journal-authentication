package com.trading.journal.authentication.jwt.data;

public record ContextUser(String email, Long tenancyId, String tenancyName) {

}
