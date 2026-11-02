package org.oagi.score.gateway.http.common.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Removes the {@link TempFiles} directories that a failed export left behind. A response removes its own file, but a
 * generation that throws, or a restart in the middle of one, never reaches that point.
 */
@Component
public class TempFileSweeper {

    private static final Logger logger = LoggerFactory.getLogger(TempFileSweeper.class);

    /** Far longer than an export takes, so that a directory still in use is never swept. */
    static final Duration MAX_AGE = Duration.ofHours(2);
    private static final Duration INTERVAL = Duration.ofMinutes(30);

    private ScheduledExecutorService executor;

    @PostConstruct
    void start() {
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "temp-file-sweeper");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(() -> {
            try {
                sweep(FileUtils.getTempDirectory(), MAX_AGE);
            } catch (RuntimeException e) {
                logger.warn("Sweeping the temporary export files failed.", e);
            }
        }, 0, INTERVAL.toMinutes(), TimeUnit.MINUTES);
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }

    /**
     * Deletes the {@link TempFiles} directories of {@code directory} that were last changed more than {@code maxAge} ago.
     *
     * @return how many it deleted
     */
    static int sweep(File directory, Duration maxAge) {
        File[] candidates = directory.listFiles(file ->
                file.isDirectory() && file.getName().startsWith(TempFiles.DIRECTORY_PREFIX));
        if (candidates == null) {
            return 0;
        }
        long limit = System.currentTimeMillis() - maxAge.toMillis();
        int deleted = 0;
        for (File candidate : candidates) {
            if (candidate.lastModified() < limit && FileUtils.deleteQuietly(candidate)) {
                deleted++;
            }
        }
        if (deleted > 0) {
            logger.info("Removed {} temporary export directories left behind.", deleted);
        }
        return deleted;
    }
}
