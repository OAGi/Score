package org.oagi.score.e2e;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class ScoreE2EParallelExecutionConfiguration
        implements ParallelExecutionConfiguration, ParallelExecutionConfigurationStrategy {

    private static final String CONFIG_FIXED_PARALLELISM_PROPERTY_NAME = "custom.parallelism";

    private static final int KEEP_ALIVE_SECONDS = 30;

    private int parallelism;
    private int minimumRunnable;
    private int maxPoolSize;
    private int corePoolSize;
    private int keepAliveSeconds;

    @Override
    public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        int parallelism = configurationParameters.get(CONFIG_FIXED_PARALLELISM_PROPERTY_NAME,
                Integer::valueOf).orElseThrow(
                () -> new JUnitException(String.format("Configuration parameter '%s' must be set",
                        CONFIG_FIXED_PARALLELISM_PROPERTY_NAME)));

        this.parallelism = parallelism;
        this.minimumRunnable = 0;
        this.maxPoolSize = parallelism;
        this.corePoolSize = parallelism;
        this.keepAliveSeconds = KEEP_ALIVE_SECONDS;

        return this;
    }

    @Override
    public int getParallelism() {
        return parallelism;
    }

    @Override
    public int getMinimumRunnable() {
        return minimumRunnable;
    }

    @Override
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    @Override
    public int getCorePoolSize() {
        return corePoolSize;
    }

    @Override
    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

}
