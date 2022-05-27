package com.trading.journal.authentication.authority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnProperty(prefix = "journal.authentication.authority", name = "type", havingValue = "DATABASE")
public class AuthorityFeedStartup implements ApplicationListener<ApplicationReadyEvent> {

    private final AuthorityRepository authorityRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Flux.fromArray(AuthoritiesHelper.values())
                .map(authoritiesHelper -> new Authority(authoritiesHelper.getCategory(), authoritiesHelper.getLabel()))
                .map(authority -> authorityRepository.getByName(authority.getName())
                        .switchIfEmpty(authorityRepository.save(authority))
                ).subscribe(authorityMono -> authorityMono.subscribe(authority -> log.info("Authority available {}", authority.getName())));
    }
}
