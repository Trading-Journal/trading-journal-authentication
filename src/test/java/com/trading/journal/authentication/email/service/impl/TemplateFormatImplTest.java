package com.trading.journal.authentication.email.service.impl;

import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.service.impl.TemplateFormatImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateFormatImplTest {

    @DisplayName("Load variable values into the resource")
    @Test
    void loadResource() {
        String resource = "mail/registration.html";

        List<EmailField> fields = Arrays.asList(
                new EmailField("$NAME", "Application User Complete Name"),
                new EmailField("$URL", "http://application.com")
        );

        String formatted = new TemplateFormatImpl().format(resource, fields);

        assertThat(formatted).contains("Application User Complete Name");
        assertThat(formatted).contains("http://application.com");

        assertThat(formatted).doesNotContain("$NAME");
        assertThat(formatted).doesNotContain("$URL");
    }

    @DisplayName("Load variable values into the resource with fields that do not exist")
    @Test
    void loadResourceWithInvalidFields() {
        String resource = "mail/registration.html";

        List<EmailField> fields = Arrays.asList(
                new EmailField("$NAME", "Application User Complete Name"),
                new EmailField("$URL", "http://application.com"),
                new EmailField("$FIELD1", "Some Field Value"),
                new EmailField("$FIELD2", "Some Field Value"),
                new EmailField("$FIELD3", "Some Field Value")
        );

        String formatted = new TemplateFormatImpl().format(resource, fields);

        assertThat(formatted).contains("Application User Complete Name");
        assertThat(formatted).contains("http://application.com");

        assertThat(formatted).doesNotContain("$NAME");
        assertThat(formatted).doesNotContain("$URL");
    }

    @DisplayName("Load variable values into the resource that does not exist return exception")
    @Test
    void loadResourceException() {
        String resource = "mail/invalid.html";

        List<EmailField> fields = Arrays.asList(
                new EmailField("$NAME", "Application User Complete Name"),
                new EmailField("$URL", "http://application.com")
        );

        assertThrows(NullPointerException.class, () -> new TemplateFormatImpl().format(resource, fields),
                "Resource folder is not accessible.");
    }

    @DisplayName("Add some message to the email template body")
    @Test
    void addBodyToEmail() {
        String body = "Some body to the email template";
        String formatted = new TemplateFormatImpl().addBodyToEmail(body);

        assertThat(formatted).contains("Some body to the email template");

        assertThat(formatted).doesNotContain("$MESSAGE_BODY");
    }
}