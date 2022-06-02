package com.trading.journal.authentication.email;

import java.util.List;

public record EmailRequest(
        String subject,
        String template,
        List<EmailField> fields,
        List<String> receipts
) {
}
