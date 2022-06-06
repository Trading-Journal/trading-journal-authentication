package com.trading.journal.authentication.configuration.mysql;

import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.util.ParsingUtils;

@Configuration
@RequiredArgsConstructor
public class MySqlConfiguration {

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
