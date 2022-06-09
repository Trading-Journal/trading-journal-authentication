package com.trading.journal.authentication.verification.service.impl.service;

import com.trading.journal.authentication.authority.AuthoritiesHelper;
import com.trading.journal.authentication.authority.Authority;
import com.trading.journal.authentication.authority.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnProperty(prefix = "journal.authentication.authority", name = "type", havingValue = "DATABASE")
public class AuthorityFeedStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final AuthorityRepository authorityRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (AuthoritiesHelper authoritiesHelper : AuthoritiesHelper.values()) {
            Authority authority = new Authority(authoritiesHelper.getCategory(), authoritiesHelper.getLabel());
            Authority byName = authorityRepository.getByName(authority.getName());
            if (byName == null) {
                authorityRepository.save(authority);
                log.info("New authority available {}", authority.getName());
            } else {
                log.info("Authority already available {}", authority.getName());
            }
        }
    }
}
