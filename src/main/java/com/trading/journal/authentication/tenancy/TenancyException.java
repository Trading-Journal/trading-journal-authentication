package com.trading.journal.authentication.tenancy;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.Serial;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class TenancyException extends HttpClientErrorException {
    @Serial
    private static final long serialVersionUID = -6161720324171559483L;

    public TenancyException(String message) {
        super(BAD_REQUEST, message);
    }

    public TenancyException(HttpStatus status, String message) {
        super(status, message);
    }
}