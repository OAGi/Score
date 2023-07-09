package org.oagi.score.e2e.impl.api;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import org.oagi.score.e2e.Configuration;
import org.oagi.score.e2e.api.*;
import org.oagi.score.e2e.impl.api.jooq.entity.DSLContextAssignedBusinessTermAPIImpl;

public class DSLContextAPIFactory implements APIFactory {

    private final HikariDataSource dataSource;
    private final DSLContext dslContext;

    public DSLContextAPIFactory(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        this.dslContext = new DefaultDSLContext(dataSource, SQLDialect.MYSQL);
    }

    public static APIFactory build(Configuration config) {
        HikariDataSourceFactory dataSourceFactory = new HikariDataSourceFactory();
        dataSourceFactory.setDriverClassName(config.getProperty("org.oagi.score.e2e.datasource.driver-class-name"));
        dataSourceFactory.setUsername(config.getProperty("org.oagi.score.e2e.datasource.username"));
        dataSourceFactory.setPassword(config.getProperty("org.oagi.score.e2e.datasource.password"));
        dataSourceFactory.setJdbcUrl(config.getProperty("org.oagi.score.e2e.datasource.url"));
        dataSourceFactory.setMaximumPoolSize(config.getIntProperty("org.oagi.score.e2e.datasource.maximum-pool-size"));

        HikariDataSource dataSource = dataSourceFactory.build();
        return new DSLContextAPIFactory(dataSource);
    }

    @Override
    public ApplicationSettingsAPI getApplicationSettingsAPI() {
        return new DSLContextApplicationSettingsAPIImpl(dslContext);
    }

    @Override
    public AppUserAPI getAppUserAPI() {
        return new DSLContextAppUserAPIImpl(dslContext);
    }

    @Override
    public NamespaceAPI getNamespaceAPI() {
        return new DSLContextNamespaceAPIImpl(dslContext);
    }

    @Override
    public ContextCategoryAPI getContextCategoryAPI() {
        return new DSLContextContextCategoryAPIImpl(dslContext);
    }

    @Override
    public ContextSchemeAPI getContextSchemeAPI() {
        return new DSLContextContextSchemeAPIImpl(dslContext);
    }

    @Override
    public ContextSchemeValueAPI getContextSchemeValueAPI() {
        return new DSLContextContextSchemeValueAPIImpl(dslContext);
    }

    @Override
    public BusinessContextAPI getBusinessContextAPI() {
        return new DSLContextBusinessContextAPIImpl(dslContext, this);
    }

    @Override
    public BusinessContextValueAPI getBusinessContextValueAPI() {
        return new DSLContextBusinessContextValueAPIImpl(dslContext);
    }

    @Override
    public BusinessTermAPI getBusinessTermAPI() {
        return new DSLContextBusinessTermAPIImpl(dslContext, this);
    }

    @Override
    public AssignedBusinessTermAPI getAssignedBusinessTermAPI() {
        return new DSLContextAssignedBusinessTermAPIImpl(dslContext, this);
    }

    @Override
    public AgencyIDListAPI getAgencyIDListAPI() {
        return new DSLContextAgencyIDListAPIImpl(dslContext, this);
    }

    @Override
    public AgencyIDListValueAPI getAgencyIDListValueAPI() {
        return new DSLContextAgencyIDListValueAPIImpl(dslContext, this);
    }

    @Override
    public CodeListAPI getCodeListAPI() {
        return new DSLContextCodeListAPIImpl(dslContext, this);
    }

    @Override
    public CodeListValueAPI getCodeListValueAPI() {
        return new DSLContextCodeListValueAPIImpl(dslContext);
    }

    @Override
    public CoreComponentAPI getCoreComponentAPI() {
        return new DSLContextCoreComponentAPIImpl(dslContext, this);
    }

    @Override
    public ReleaseAPI getReleaseAPI() {
        return new DSLContextReleaseAPIImpl(dslContext);
    }

    @Override
    public BusinessInformationEntityAPI getBusinessInformationEntityAPI() {
        return new DSLContextBusinessInformationEntityAPIImpl(dslContext, this);
    }

    @Override
    public ModuleSetAPI getModuleSetAPI() {
        return new DSLContextModuleSetAPIImpl(dslContext, this);
    }

    @Override
    public void close() throws Exception {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
