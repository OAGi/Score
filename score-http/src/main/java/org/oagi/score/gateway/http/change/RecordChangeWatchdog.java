package org.oagi.score.gateway.http.change;

import org.oagi.score.gateway.http.change.repository.RecordChangeLogRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Reads {@code record_change_log} and publishes what it reads as a {@link RecordsChangedEvent}, then a {@link ChangeLogReadEvent}
 * that says whether the read worked. It does not know who listens or what they do.
 * <p>
 * Rows become visible when their transaction commits, not in the order of their numbers, so a skipped number is kept as a gap and
 * looked at again for a while. Old rows are deleted.
 */
@Component
public class RecordChangeWatchdog implements ChangeLogReader {

    private static final Logger logger = LoggerFactory.getLogger(RecordChangeWatchdog.class);

    private static final int BATCH = 5000;
    private static final long POLL_MILLIS = 500;
    /** How many of the latest rows are read again at start, in case their transactions were still open. */
    private static final long STARTUP_LOOK_BACK = 10_000;
    static final long GAP_LIFETIME_NANOS = Duration.ofHours(1).toNanos();
    private static final long GAP_FIRST_DELAY_NANOS = Duration.ofMillis(500).toNanos();
    private static final long GAP_MAX_DELAY_NANOS = Duration.ofSeconds(30).toNanos();
    static final Duration RETENTION = Duration.ofHours(2);
    /** How long the log must stay empty before its numbers start again from 1, so that they stay small. */
    long restartAfterEmptyNanos = Duration.ofMinutes(10).toNanos();

    /** Numbers that were skipped and may still appear. */
    private static final class Gap {
        final long from;
        final long to;
        final long since = System.nanoTime();
        final Set<Long> applied = new HashSet<>();
        long delay = GAP_FIRST_DELAY_NANOS;
        long nextCheck = since + GAP_FIRST_DELAY_NANOS;

        Gap(long from, long to) {
            this.from = from;
            this.to = to;
        }

        /** A gap to look at in the next read, not after the first delay. */
        Gap now() {
            nextCheck = since;
            return this;
        }
    }

    private final RecordChangeLogRepository repository;
    private final ApplicationEventPublisher publisher;

    private final Object requestLock = new Object();
    /** The read that was asked for and has not begun; callers that ask meanwhile share it. */
    private CompletableFuture<Void> queued;
    private long cursor = -1;
    private final List<Gap> gaps = new ArrayList<>();
    private boolean failureLogged;
    /** The {@link System#nanoTime()} since which the log has been seen empty, or 0. */
    private volatile long emptySince;

    private ScheduledExecutorService executor;

    @Autowired
    public RecordChangeWatchdog(RepositoryFactory repositoryFactory, ApplicationEventPublisher publisher) {
        this.repository = repositoryFactory.recordChangeLogRepository();
        this.publisher = publisher;
    }

    @PostConstruct
    void start() {
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "record-change-watchdog");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(this::poll, 0, POLL_MILLIS, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(this::purge, 1, 10, TimeUnit.MINUTES);
    }

    @PreDestroy
    void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    // ---- reading -------------------------------------------------------------------------------------------------

    @Override
    public void catchUp(long maxWaitMillis) {
        if (executor == null) {
            poll();
            return;
        }
        CompletableFuture<Void> read;
        synchronized (requestLock) {
            if (queued == null) {
                CompletableFuture<Void> mine = new CompletableFuture<>();
                queued = mine;
                executor.execute(() -> {
                    synchronized (requestLock) {
                        queued = null;
                    }
                    try {
                        poll();
                    } finally {
                        mine.complete(null);
                    }
                });
            }
            read = queued;
        }
        try {
            read.get(maxWaitMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            // The read goes on without the caller.
        }
    }

    /** One read of the log. Only one thread at a time. */
    synchronized void poll() {
        try {
            if (cursor < 0) {
                start0();
            }
            List<ChangeLogEntry> batch = repository.readAfter(cursor, BATCH);
            if (batch.isEmpty()) {
                long max = repository.maxSeq();
                if (max == 0) {
                    // Nothing is left (the old rows were deleted): what was read is gone, so the cursor goes back to the start,
                    // where the numbers will start again.
                    cursor = 0;
                    gaps.clear();
                    if (emptySince == 0) {
                        emptySince = System.nanoTime();
                    }
                } else {
                    emptySince = 0;
                    if (max < cursor) {
                        // The numbers went back: the table was emptied with TRUNCATE, recreated, or restored from a backup. Rows
                        // below the cursor would never be read, so start again from the end of what is there.
                        logger.warn("The numbers of the change log went back (from {}); reading it again from its end. Whatever changed meanwhile "
                                + "is not announced.", cursor);
                        start0();
                        batch = repository.readAfter(cursor, BATCH);
                    }
                }
            } else {
                emptySince = 0;
            }
            while (!batch.isEmpty()) {
                announce(batch);
                noteGaps(batch);
                cursor = batch.get(batch.size() - 1).seq();
                batch = (batch.size() < BATCH) ? List.of() : repository.readAfter(cursor, BATCH);
            }
            recheckGaps();
            publisher.publishEvent(new ChangeLogReadEvent(true));
            failureLogged = false;
        } catch (RuntimeException e) {
            publisher.publishEvent(new ChangeLogReadEvent(false));
            if (!failureLogged) {
                failureLogged = true;
                logger.warn("The change log cannot be read or a listener of its changes failed; it is read again with the same rows "
                        + "(is the migration that creates record_change_log applied?).", e);
            }
        }
    }

    /** Starts after the last row there is, but looks once more at the latest ones, for transactions that were open. */
    private void start0() {
        long max = repository.maxSeq();
        cursor = max;
        gaps.clear();
        if (max > 0) {
            gaps.add(new Gap(Math.max(1, max - STARTUP_LOOK_BACK), max).now());
        }
    }

    private void noteGaps(List<ChangeLogEntry> batch) {
        long expected = cursor + 1;
        for (ChangeLogEntry change : batch) {
            if (change.seq() > expected) {
                gaps.add(new Gap(expected, change.seq() - 1));
            }
            expected = change.seq() + 1;
        }
    }

    private void recheckGaps() {
        long now = System.nanoTime();
        for (Iterator<Gap> it = gaps.iterator(); it.hasNext(); ) {
            Gap gap = it.next();
            if (now - gap.since > GAP_LIFETIME_NANOS) {
                it.remove();
                continue;
            }
            if (now - gap.nextCheck < 0) {
                continue;
            }
            List<ChangeLogEntry> appeared = new ArrayList<>();
            for (ChangeLogEntry change : repository.readRange(gap.from, gap.to)) {
                if (!gap.applied.contains(change.seq())) {
                    appeared.add(change);
                }
            }
            announce(appeared);
            appeared.forEach(change -> gap.applied.add(change.seq()));
            gap.delay = Math.min(gap.delay * 2, GAP_MAX_DELAY_NANOS);
            gap.nextCheck = now + gap.delay;
        }
    }

    // ---- announcing ----------------------------------------------------------------------------------------------

    private void announce(List<ChangeLogEntry> changes) {
        if (!changes.isEmpty()) {
            publisher.publishEvent(new RecordsChangedEvent(changes));
        }
    }

    // ---- housekeeping --------------------------------------------------------------------------------------------

    /** Deletes the rows that are older than {@link #RETENTION}, and restarts the numbering when the log has stayed empty. */
    void purge() {
        try {
            while (repository.purgeOlderThan(RETENTION, 5000) == 5000) {
                // go on until the rest is younger
            }
            long since = emptySince;
            if (since != 0 && System.nanoTime() - since >= restartAfterEmptyNanos) {
                repository.restartNumbering();
            }
        } catch (RuntimeException e) {
            logger.warn("The old rows of the change log could not be deleted.", e);
        }
    }
}
