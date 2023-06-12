package com.trading.journal.authentication.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class ApiExceptionHandlerTest {
    final ApiExceptionHandler apiExceptionHandler = new ApiExceptionHandler();

    @Test
    @DisplayName("When handle HttpClientErrorException with no message, return status as message")
    void handleClientExceptionNoMessage() {
        ResponseEntity<Map<String, String>> response = apiExceptionHandler.handleClientException(
                new HttpClientErrorException(HttpStatus.NOT_FOUND));
        assertEquals(404, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("NOT_FOUND", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle HttpClientErrorException message, return message")
    void handleClientExceptionWithMessage() {
        ResponseEntity<Map<String, String>> response = apiExceptionHandler.handleClientException(
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any message"));
        assertEquals(400, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("any message", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle Exception, return message and status 500")
    void handleException() {
        ResponseEntity<Map<String, String>> response = apiExceptionHandler
                .handleException(new RuntimeException("any message"));
        assertEquals(500, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("any message", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    @DisplayName("When handle Exception with cause, return cause as message and status 500")
    void handleExceptionWithCause() {
        RuntimeException causeMessage = new RuntimeException("cause message");
        ResponseEntity<Map<String, String>> response = apiExceptionHandler
                .handleException(new RuntimeException("any message", causeMessage));
        assertEquals(500, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("error"));
        assertEquals("cause message", Objects.requireNonNull(response.getBody()).get("error"));
    }
}
