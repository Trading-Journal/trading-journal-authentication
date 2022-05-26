package com.trading.journal.authentication.configuration.mysql;

import com.trading.journal.authentication.configuration.DatasourceProperties;
import io.jsonwebtoken.lang.Assert;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.mariadb.r2dbc.MariadbConnectionConfiguration;
import org.mariadb.r2dbc.MariadbConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.util.ParsingUtils;

@Configuration
@RequiredArgsConstructor
public class MySqlConfiguration extends AbstractR2dbcConfiguration {

    private final DatasourceProperties datasourceProperties;

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactor) {
        return new R2dbcEntityTemplate(connectionFactor);
    }

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        MariadbConnectionConfiguration conf = MariadbConnectionConfiguration.builder()
                .host(datasourceProperties.host())
                .port(datasourceProperties.port())
                .database(datasourceProperties.database())
                .username(datasourceProperties.username())
                .password(datasourceProperties.password())
                .build();
        return new MariadbConnectionFactory(conf);
    }

    @Bean
    public NamingStrategy namingStrategy() {
        return new NamingStrategy() {
            @Override
            public String getColumnName(RelationalPersistentProperty property) {
                Assert.notNull(property, "Property must not be null.");
                return ParsingUtils.reconcatenateCamelCase(property.getName(), "");
            }

            @Override
            public String getTableName(Class<?> type) {
                org.springframework.util.Assert.notNull(type, "Type must not be null.");
                return ParsingUtils.reconcatenateCamelCase(type.getSimpleName(), "");
            }
        };
    }

}
