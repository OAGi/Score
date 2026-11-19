package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** The write freeze of a test that has none of the application's own: it only records that it was asked. */
public class RecordingWriteFreeze implements WriteFreeze {

    public final List<String> calls = new CopyOnWriteArrayList<>();

    @Override
    public void freeze() {
        calls.add("freeze");
    }

    @Override
    public void release() {
        calls.add("release");
    }

    @TestConfiguration
    public static class Configuration {
        @Bean
        RecordingWriteFreeze writeFreeze() {
            return new RecordingWriteFreeze();
        }
    }
}
