package org.oagi.srt.config;

import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.mysql.MysqlRepositoryFactory;
import org.oagi.srt.repository.oracle.OracleRepositoryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = {"org.oagi.srt.repository"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.oagi.srt.repository")
@EntityScan("org.oagi.srt.repository.entity")
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

    @Bean
    public HibernateJpaSessionFactoryBean sessionFactory() {
        return new HibernateJpaSessionFactoryBean();
    }
}
