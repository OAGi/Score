package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * The thread a load is done on: one, since one load goes on at a time. It is not a bean of the type {@code Executor}, which
 * would turn off the one Spring Boot makes for the application (it makes it only when there is none).
 */
@Component
public class MigrationLoadExecutor {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "migration-load");
        thread.setDaemon(true);
        return thread;
    });

    /** @throws RejectedExecutionException when the application is shutting down */
    public void execute(Runnable work) {
        executor.execute(work);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
