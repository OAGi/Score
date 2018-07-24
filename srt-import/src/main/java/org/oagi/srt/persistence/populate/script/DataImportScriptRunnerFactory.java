package org.oagi.srt.persistence.populate.script;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.persistence.EntityManager;

public abstract class DataImportScriptRunnerFactory {

    public static class NullDataImportScriptRunner implements DataImportScriptRunner {
        @Override
        public void printHeader() {
        }

        @Override
        public void printSettings() {
        }

        @Override
        public void printResetSequences(EntityManager entityManager) {
        }
    }

    public static DataImportScriptRunner loadDataImportScriptRunner(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getBean(Environment.class);
        String platform = environment.getProperty("spring.datasource.platform");
        try {
            return applicationContext.getBean(platform + "DataImportScriptRunner", DataImportScriptRunner.class);
        } catch (NoSuchBeanDefinitionException e) {
            return new NullDataImportScriptRunner();
        }
    }
}
