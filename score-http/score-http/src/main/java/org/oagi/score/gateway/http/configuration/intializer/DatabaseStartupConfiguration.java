package org.oagi.score.gateway.http.configuration.intializer;

import org.jooq.DSLContext;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.DatabaseStartupValidator;

import javax.sql.DataSource;
import java.util.stream.Stream;

@Configuration
public class DatabaseStartupConfiguration {

    static {
        // Turn off the jooq logo.
        System.setProperty("org.jooq.no-logo", "true");
    }

    @Bean
    public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
        DatabaseStartupValidator validator = new DatabaseStartupValidator();
        validator.setDataSource(dataSource);
        validator.setValidationQuery(DatabaseDriver.MYSQL.getValidationQuery());
        return validator;
    }

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return beanFactory -> Stream.of(beanFactory.getBeanNamesForType(DSLContext.class))
                .map(beanFactory::getBeanDefinition)
                .forEach(it -> it.setDependsOn("databaseStartupValidator"));
    }
}
