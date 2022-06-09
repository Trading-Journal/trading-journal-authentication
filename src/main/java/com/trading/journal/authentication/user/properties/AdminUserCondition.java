package com.trading.journal.authentication.user.properties;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

@NoArgsConstructor
public class AdminUserCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String adminEmail = context.getEnvironment().getProperty("journal.authentication.admin-user.email");
        return StringUtils.hasText(adminEmail);
    }
}
