package com.trading.journal.authentication.email.impl;

import com.trading.journal.authentication.email.EmailField;
import com.trading.journal.authentication.email.EmailProperties;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.TemplateFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EmailSenderImplTest {

    @Mock
    JavaMailSender mailSender;

    @Mock
    EmailProperties emailProperties;

    @Mock
    TemplateFormat templateFormat;

    @InjectMocks
    EmailSenderImpl emailSender;

    @DisplayName("Send email")
    @Test
    void sendEmail() {
        EmailRequest emailRequest = new EmailRequest("Subject", "template", singletonList(new EmailField("$NAME", "Application User Complete Name")), singletonList("mail@mail.com"));
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        when(emailProperties.getUsername()).thenReturn("user@mail.com");
        when(templateFormat.format(emailRequest.template(), emailRequest.fields())).thenReturn("Any email text");
        when(templateFormat.addBodyToEmail("Any email text")).thenReturn("Complete message");

        Mono<Void> voidMono = emailSender.send(emailRequest);
        StepVerifier.create(voidMono)
                .verifyComplete();
    }
}