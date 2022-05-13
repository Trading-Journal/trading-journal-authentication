package com.trading.journal.authentication;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class ApplicationException extends HttpClientErrorException {
    private static final long serialVersionUID = -6161720324171559483L;

    public ApplicationException(String message) {
        super(BAD_REQUEST, message);
    }

    public ApplicationException(HttpStatus status, String message) {
        super(status, message);
    }
}