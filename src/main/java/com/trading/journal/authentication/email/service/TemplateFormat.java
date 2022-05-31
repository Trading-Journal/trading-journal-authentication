package com.trading.journal.authentication.email.service;

import com.trading.journal.authentication.email.EmailField;

import java.util.List;

public interface TemplateFormat {

    String format(String resourceName, List<EmailField> fields);

    String addBodyToEmail(String body);
}
