package com.trading.journal.authentication;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;

import java.io.Serial;

public class ApplicationException extends HttpClientErrorException {
    @Serial
    private static final long serialVersionUID = -6161720324171559483L;

    public ApplicationException(String message) {
        super(BAD_REQUEST, message);
    }

    public ApplicationException(HttpStatusCode status, String message) {
        super(status, message);
    }
}