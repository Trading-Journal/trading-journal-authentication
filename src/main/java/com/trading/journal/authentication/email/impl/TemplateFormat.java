package com.trading.journal.authentication.email.impl;

import com.trading.journal.authentication.email.EmailConstants;
import com.trading.journal.authentication.email.EmailField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Component
@Slf4j
public class TemplateFormat {
    public String format(String resourceName, List<EmailField> fields) {
        final String template = getResource(resourceName);
        return ofNullable(fields)
                .orElse(emptyList())
                .stream()
                .reduce(template, (s, e) -> s.replace(e.name(), e.value().toString()), (s1, s2) -> null);
    }

    public  String addBodyToEmail(String body) {
        List<EmailField> fields = singletonList(new EmailField(EmailConstants.TemplateFields.MESSAGE_BODY, body));
        return format(EmailConstants.Resources.EMAIL_TEMPLATE, fields);
    }

    private String getResource(String resourceName) {
        BufferedReader bufferedReader = null;
        InputStream resourceAsStream = null;
        String template;
        try {
            resourceAsStream = TemplateFormat.class.getResourceAsStream(resourceName);
            requireNonNull(resourceAsStream, "Resource folder is not accessible.");
            bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, UTF_8));
            template = bufferedReader.lines().collect(joining());
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (resourceAsStream != null) {
                    resourceAsStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the resource", e);
            }
        }
        return template;
    }
}
