package com.trading.journal.authentication.configuration;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String CLIENT_EXCEPTION_HAPPENED = "Client Exception happened";
    private static final String UNEXPECTED_EXCEPTION_HAPPENED = "Unexpected Exception happened";
    private static final String CONSTRAINT_MESSAGE = "Constraints violations found.";

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleClientException(final HttpClientErrorException ex) {
        logger.error(CLIENT_EXCEPTION_HAPPENED, ex, kv("error", ex.getStatusText()),
                kv("status", ex.getRawStatusCode()));
        final Map<String, String> errors = new ConcurrentHashMap<>();
        errors.put("error", ex.getStatusText());
        return status(ex.getStatusCode()).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(final Exception ex) {
        return status(INTERNAL_SERVER_ERROR).body(extractMessage(ex));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleWebExchangeBindException(final AuthenticationException ex) {
        return status(UNAUTHORIZED).body(extractMessage(ex));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleWebExchangeBindException(final WebExchangeBindException ex) {
        return status(BAD_REQUEST).body(getBindingResult(ex.getBindingResult(), ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex) {
        return status(BAD_REQUEST).body(getBindingResult(ex.getBindingResult(), ex));
    }

    private Map<String, String> getBindingResult(final BindingResult bindingResult, final Exception ex) {
        final Map<String, String> errors = new ConcurrentHashMap<>();
        for (final ObjectError error : bindingResult.getAllErrors()) {
            String fieldName = error.getObjectName();
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            }
            errors.put(fieldName, error.getDefaultMessage());
        }
        final String message = Optional.ofNullable(ex.getCause()).orElse(ex).getMessage();
        logger.error(CONSTRAINT_MESSAGE, ex, kv("error-message", message), kv("errors", errors));
        return errors;
    }

    private Map<String, String> extractMessage(Exception exception) {
        final String message = Optional.ofNullable(exception.getCause()).orElse(exception).getMessage();
        logger.error(UNEXPECTED_EXCEPTION_HAPPENED, exception, kv("error-message", message));
        final Map<String, String> errors = new ConcurrentHashMap<>();
        errors.put("error", Optional.ofNullable(message).orElse(UNEXPECTED_EXCEPTION_HAPPENED));
        return errors;
    }
}