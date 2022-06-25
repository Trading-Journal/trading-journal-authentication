package com.trading.journal.authentication.authority;

import com.trading.journal.authentication.authority.service.impl.AuthorityServiceDatabaseImpl;
import com.trading.journal.authentication.authority.service.impl.AuthorityServiceStaticImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class AuthorityServiceContextLoadTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConditionEvaluationReportLoggingListener())
            .withUserConfiguration(AuthorityServiceStaticImpl.class, AuthorityServiceDatabaseImpl.class);

    @Test
    @DisplayName("Properties do not have configuration for journal.authentication.authority type load AuthorityServiceStaticImpl and do not load UserAuthorityServiceImpl or AuthorityRepository")
    void noAuthorityProperty() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthorityServiceStaticImpl.class);
            assertThat(context).doesNotHaveBean(AuthorityServiceDatabaseImpl.class);
            assertThat(context).doesNotHaveBean(AuthorityRepository.class);
        });
    }

    @Test
    @DisplayName("Properties journal.authentication.authority is type STATIC load AuthorityServiceStaticImpl and do not load UserAuthorityServiceImpl or AuthorityRepository")
    void authorityPropertyStatic() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuthorityServiceStaticImpl.class);
            assertThat(context).doesNotHaveBean(AuthorityServiceDatabaseImpl.class);
            assertThat(context).doesNotHaveBean(AuthorityRepository.class);
        });
    }
}