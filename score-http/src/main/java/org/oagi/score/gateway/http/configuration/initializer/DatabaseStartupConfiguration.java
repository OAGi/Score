package org.oagi.score.gateway.http.configuration.initializer;

import org.jooq.DSLContext;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.DatabaseStartupValidator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.stream.Stream;

@Configuration
public class DatabaseStartupConfiguration {

    static {
        // Turn off the jooq logo and tips
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
    }

    @Bean
    public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
        DatabaseStartupValidator validator = new DatabaseStartupValidator();
        validator.setInterval(2); // 2s
        validator.setTimeout(300); // 5m
        validator.setDataSource(dataSource);
        validator.setValidationQuery(DatabaseDriver.MARIADB.getValidationQuery());
        return validator;
    }

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return beanFactory -> Stream.of(beanFactory.getBeanNamesForType(DSLContext.class))
                .map(beanFactory::getBeanDefinition)
                // Add to, not replace, the dependencies Spring Boot registered (notably Flyway), so a new
                // database is migrated before the first bean queries it.
                .forEach(it -> it.setDependsOn(StringUtils.addStringToArray(it.getDependsOn(), "databaseStartupValidator")));
    }
}
