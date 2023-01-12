package org.oagi.score.e2e.impl.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.MetricsTrackerFactory;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class HikariDataSourceFactory {

    private final HikariConfig hikariConfig = new HikariConfig();

    public String getCatalog() {
        return hikariConfig.getCatalog();
    }

    public void setCatalog(String catalog) {
        hikariConfig.setCatalog(catalog);
    }

    public long getConnectionTimeout() {
        return hikariConfig.getConnectionTimeout();
    }

    public void setConnectionTimeout(long connectionTimeoutMs) {
        hikariConfig.setConnectionTimeout(connectionTimeoutMs);
    }

    public long getIdleTimeout() {
        return hikariConfig.getIdleTimeout();
    }

    public void setIdleTimeout(long idleTimeoutMs) {
        hikariConfig.setIdleTimeout(idleTimeoutMs);
    }

    public long getLeakDetectionThreshold() {
        return hikariConfig.getLeakDetectionThreshold();
    }

    public void setLeakDetectionThreshold(long leakDetectionThresholdMs) {
        hikariConfig.setLeakDetectionThreshold(leakDetectionThresholdMs);
    }

    public long getMaxLifetime() {
        return hikariConfig.getMaxLifetime();
    }

    public void setMaxLifetime(long maxLifetimeMs) {
        hikariConfig.setMaxLifetime(maxLifetimeMs);
    }

    public int getMaximumPoolSize() {
        return hikariConfig.getMaximumPoolSize();
    }

    public void setMaximumPoolSize(int maxPoolSize) {
        hikariConfig.setMaximumPoolSize(maxPoolSize);
    }

    public int getMinimumIdle() {
        return hikariConfig.getMinimumIdle();
    }

    public void setMinimumIdle(int minIdle) {
        hikariConfig.setMinimumIdle(minIdle);
    }

    public String getPassword() {
        return hikariConfig.getPassword();
    }

    public void setPassword(String password) {
        hikariConfig.setPassword(password);
    }

    public String getUsername() {
        return hikariConfig.getUsername();
    }

    public void setUsername(String username) {
        hikariConfig.setUsername(username);
    }

    public long getValidationTimeout() {
        return hikariConfig.getValidationTimeout();
    }

    public void setValidationTimeout(long validationTimeoutMs) {
        hikariConfig.setValidationTimeout(validationTimeoutMs);
    }

    public String getConnectionTestQuery() {
        return hikariConfig.getConnectionTestQuery();
    }

    public void setConnectionTestQuery(String connectionTestQuery) {
        hikariConfig.setConnectionTestQuery(connectionTestQuery);
    }

    public String getConnectionInitSql() {
        return hikariConfig.getConnectionInitSql();
    }

    public void setConnectionInitSql(String connectionInitSql) {
        hikariConfig.setConnectionInitSql(connectionInitSql);
    }

    public DataSource getDataSource() {
        return hikariConfig.getDataSource();
    }

    public void setDataSource(DataSource dataSource) {
        hikariConfig.setDataSource(dataSource);
    }

    public String getDataSourceClassName() {
        return hikariConfig.getDataSourceClassName();
    }

    public void setDataSourceClassName(String className) {
        hikariConfig.setDataSourceClassName(className);
    }

    public void addDataSourceProperty(String propertyName, Object value) {
        hikariConfig.addDataSourceProperty(propertyName, value);
    }

    public String getDataSourceJNDI() {
        return hikariConfig.getDataSourceJNDI();
    }

    public void setDataSourceJNDI(String jndiDataSource) {
        hikariConfig.setDataSourceJNDI(jndiDataSource);
    }

    public Properties getDataSourceProperties() {
        return hikariConfig.getDataSourceProperties();
    }

    public void setDataSourceProperties(Properties dsProperties) {
        hikariConfig.setDataSourceProperties(dsProperties);
    }

    public String getDriverClassName() {
        return hikariConfig.getDriverClassName();
    }

    public void setDriverClassName(String driverClassName) {
        hikariConfig.setDriverClassName(driverClassName);
    }

    public String getJdbcUrl() {
        return hikariConfig.getJdbcUrl();
    }

    public void setJdbcUrl(String jdbcUrl) {
        hikariConfig.setJdbcUrl(jdbcUrl);
    }

    public boolean isAutoCommit() {
        return hikariConfig.isAutoCommit();
    }

    public void setAutoCommit(boolean isAutoCommit) {
        hikariConfig.setAutoCommit(isAutoCommit);
    }

    public boolean isAllowPoolSuspension() {
        return hikariConfig.isAllowPoolSuspension();
    }

    public void setAllowPoolSuspension(boolean isAllowPoolSuspension) {
        hikariConfig.setAllowPoolSuspension(isAllowPoolSuspension);
    }

    public long getInitializationFailTimeout() {
        return hikariConfig.getInitializationFailTimeout();
    }

    public void setInitializationFailTimeout(long initializationFailTimeout) {
        hikariConfig.setInitializationFailTimeout(initializationFailTimeout);
    }

    public boolean isIsolateInternalQueries() {
        return hikariConfig.isIsolateInternalQueries();
    }

    public void setIsolateInternalQueries(boolean isolate) {
        hikariConfig.setIsolateInternalQueries(isolate);
    }

    public MetricsTrackerFactory getMetricsTrackerFactory() {
        return hikariConfig.getMetricsTrackerFactory();
    }

    public void setMetricsTrackerFactory(MetricsTrackerFactory metricsTrackerFactory) {
        hikariConfig.setMetricsTrackerFactory(metricsTrackerFactory);
    }

    public Object getMetricRegistry() {
        return hikariConfig.getMetricRegistry();
    }

    public void setMetricRegistry(Object metricRegistry) {
        hikariConfig.setMetricRegistry(metricRegistry);
    }

    public Object getHealthCheckRegistry() {
        return hikariConfig.getHealthCheckRegistry();
    }

    public void setHealthCheckRegistry(Object healthCheckRegistry) {
        hikariConfig.setHealthCheckRegistry(healthCheckRegistry);
    }

    public Properties getHealthCheckProperties() {
        return hikariConfig.getHealthCheckProperties();
    }

    public void setHealthCheckProperties(Properties healthCheckProperties) {
        hikariConfig.setHealthCheckProperties(healthCheckProperties);
    }

    public void addHealthCheckProperty(String key, String value) {
        hikariConfig.addHealthCheckProperty(key, value);
    }

    public long getKeepaliveTime() {
        return hikariConfig.getKeepaliveTime();
    }

    public void setKeepaliveTime(long keepaliveTimeMs) {
        hikariConfig.setKeepaliveTime(keepaliveTimeMs);
    }

    public boolean isReadOnly() {
        return hikariConfig.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
        hikariConfig.setReadOnly(readOnly);
    }

    public boolean isRegisterMbeans() {
        return hikariConfig.isRegisterMbeans();
    }

    public void setRegisterMbeans(boolean register) {
        hikariConfig.setRegisterMbeans(register);
    }

    public String getPoolName() {
        return hikariConfig.getPoolName();
    }

    public void setPoolName(String poolName) {
        hikariConfig.setPoolName(poolName);
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return hikariConfig.getScheduledExecutor();
    }

    public void setScheduledExecutor(ScheduledExecutorService executor) {
        hikariConfig.setScheduledExecutor(executor);
    }

    public String getTransactionIsolation() {
        return hikariConfig.getTransactionIsolation();
    }

    public void setTransactionIsolation(String isolationLevel) {
        hikariConfig.setTransactionIsolation(isolationLevel);
    }

    public String getSchema() {
        return hikariConfig.getSchema();
    }

    public void setSchema(String schema) {
        hikariConfig.setSchema(schema);
    }

    public String getExceptionOverrideClassName() {
        return hikariConfig.getExceptionOverrideClassName();
    }

    public void setExceptionOverrideClassName(String exceptionOverrideClassName) {
        hikariConfig.setExceptionOverrideClassName(exceptionOverrideClassName);
    }

    public ThreadFactory getThreadFactory() {
        return hikariConfig.getThreadFactory();
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        hikariConfig.setThreadFactory(threadFactory);
    }

    public void copyStateTo(HikariConfig other) {
        hikariConfig.copyStateTo(other);
    }

    public void validate() {
        hikariConfig.validate();
    }

    public HikariDataSource build() {
        return new HikariDataSource(hikariConfig);
    }

}
