package org.oagi.srt.config;

import org.oagi.srt.persistence.dao.OracleDAOFactory;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.mysql.MysqlCodeListRepository;
import org.oagi.srt.repository.mysql.MysqlRepositoryFactory;
import org.oagi.srt.repository.oracle.OracleRepositoryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class RepositoryConfiguration {

    @Value("${spring.datasource.platform}")
    private String platform;

    @Bean
    public RepositoryFactory repositoryFactory() {
        switch (platform.toLowerCase()) {
            case "oracle":
                return oracleRepositoryFactory();
            case "mysql":
                return mysqlRepositoryFactory();
            default:
                throw new IllegalStateException();
        }
    }

    @Bean
    @Lazy
    public OracleRepositoryFactory oracleRepositoryFactory() {
        return new OracleRepositoryFactory();
    }

    @Bean
    @Lazy
    public MysqlRepositoryFactory mysqlRepositoryFactory() {
        return new MysqlRepositoryFactory();
    }

}
