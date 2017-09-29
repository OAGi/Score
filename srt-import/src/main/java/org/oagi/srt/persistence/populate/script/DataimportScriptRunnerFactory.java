package org.oagi.srt.persistence.populate.script;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public abstract class DataimportScriptRunnerFactory {

    public static DataImportScriptRunner loadDataImportScriptRunner(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getBean(Environment.class);
        String platform = environment.getProperty("spring.datasource.platform");
        return applicationContext.getBean(platform + "DataImportScriptRunner", DataImportScriptRunner.class);
    }
}
