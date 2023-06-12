package com.trading.journal.authentication.email.service.impl;

import com.trading.journal.authentication.ApplicationException;
import com.trading.journal.authentication.email.EmailProperties;
import com.trading.journal.authentication.email.EmailRequest;
import com.trading.journal.authentication.email.service.EmailSender;
import com.trading.journal.authentication.email.service.TemplateFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;
    private final TemplateFormat templateFormat;

    @Override
    public void send(EmailRequest request) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
        try {
            String body = templateFormat.format(request.template(), request.fields());
            String message = templateFormat.addBodyToEmail(body);
            mimeMessageHelper.setSubject(request.subject());
            mimeMessageHelper.setText(message, true);
            mimeMessageHelper.setFrom(emailProperties.getUsername());
            mimeMessageHelper.setTo(request.receipts().toArray(new String[]{""}));
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.error("Error sending email", ex);
            throw new ApplicationException(INTERNAL_SERVER_ERROR, String.format("Error sending email: %s", ofNullable(ex.getCause()).orElse(ex).getMessage()));
        }
    }
}
