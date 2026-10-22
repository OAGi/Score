package org.oagi.score.gateway.http.api.cc_management.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.oagi.score.gateway.http.api.cc_management.repository.CcChangeLogRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.CcChangeLogRepository.RecordRef;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Makes the cache forget what changed. The triggers on the core component tables write a row to {@code cc_change_log} for
 * every changed row, in the transaction of the change; this reads the rows and has the {@link CcCache} forget the entries of
 * the records they name. Every instance does it for its own cache, or for the one they share.
 * <p>
 * <b>When it reads.</b> At once after a commit of this instance (so that whoever saved sees it); when a document is made and the
 * last read is older than a quarter of a second (so that a change another instance made is seen by the next request); and twice
 * a second, so that an idle instance keeps the log read, looks at the gaps again and reports that it is keeping up. Each read is
 * one indexed query (not logged by jOOQ). (MariaDB has no way to tell a client that a row was
 * written, so something has to ask; a binlog reader would be told, which is the CDC the owner left out.)
 * <p>
 * <b>Order.</b> The rows get their numbers when they are written, but become visible when their transaction commits, so a
 * transaction that is long (a release being published) shows rows below the ones read after it. A hole in the numbers is
 * remembered as a gap and looked at again, less and less often, for an hour, which is longer than any transaction.
 * <p>
 * <b>When it cannot.</b> Without the log (the table is not there, the database does not answer) it reports so and the cache is
 * bypassed: an entry that nobody forgets must not be served. The rows are deleted after two hours.
 */
@Component
public class CcCacheWatchdog implements CcCacheWriteObserver {

    private static final Logger logger = LoggerFactory.getLogger(CcCacheWatchdog.class);

    private static final int BATCH = 5000;
    private static final long POLL_MILLIS = 500;
    /** How far back the rows are read once more when the instance starts: the transactions that were open then. */
    private static final long STARTUP_LOOK_BACK = 10_000;
    static final long GAP_LIFETIME_NANOS = Duration.ofHours(1).toNanos();
    private static final long GAP_FIRST_DELAY_NANOS = Duration.ofMillis(500).toNanos();
    private static final long GAP_MAX_DELAY_NANOS = Duration.ofSeconds(30).toNanos();
    static final Duration RETENTION = Duration.ofHours(2);
    /**
     * The numbers start again from 1 once the log has been empty for this long, so that they stay small (a number above a
     * hundred million is taken for something else by other code). By then every instance has seen the empty log, and has put its
     * cursor back to 0, many times over.
     */
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

    private final CcChangeLogRepository repository;
    private final CcCache cache;
    private final CcCacheStatus status;

    private final Object requestLock = new Object();
    /** The read that has been asked for and has not begun; callers that ask meanwhile share it. */
    private CompletableFuture<Void> queued;
    private long cursor = -1;
    private final List<Gap> gaps = new ArrayList<>();
    private boolean failureLogged;
    /** The {@link System#nanoTime()} since which the log has been seen empty, or 0. */
    private volatile long emptySince;

    private ScheduledExecutorService executor;

    @Autowired
    public CcCacheWatchdog(RepositoryFactory repositoryFactory, CcCache cache, CcCacheStatus status) {
        this.repository = repositoryFactory.ccChangeLogRepository();
        this.cache = cache;
        this.status = status;
        status.bind(this);
    }

    @PostConstruct
    void start() {
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "cc-change-watchdog");
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

    // ---- CcCacheWriteObserver --------------------------------------------------------------------------------------

    @Override
    public void written() {
        WriteTracker.written(this::catchUp);
    }

    // ---- reading -------------------------------------------------------------------------------------------------

    /** Reads the change log and forgets what it names; returns when it has, however long that takes (a minute at most). */
    public void catchUp() {
        catchUp(60_000);
    }

    /**
     * Has the log read by the thread of the watchdog, which has a connection of its own, so that the caller (who may be inside a
     * transaction and holds a connection already) needs no second one.
     *
     * @param maxWaitMillis how long to wait for it. A reader who does not wait long enough may go on with a cache that has not
     *                      heard of the very latest change.
     */
    void catchUp(long maxWaitMillis) {
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
            List<CcChange> batch = repository.readAfter(cursor, BATCH);
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
                        logger.warn("The numbers of the CC change log went back (from {}); reading it again from its end. Entries of "
                                + "the cache that changed meanwhile expire by themselves.", cursor);
                        start0();
                        batch = repository.readAfter(cursor, BATCH);
                    }
                }
            } else {
                emptySince = 0;
            }
            while (!batch.isEmpty()) {
                forget(batch);
                noteGaps(batch);
                cursor = batch.get(batch.size() - 1).seq();
                batch = (batch.size() < BATCH) ? List.of() : repository.readAfter(cursor, BATCH);
            }
            recheckGaps();
            status.succeeded();
            failureLogged = false;
        } catch (RuntimeException e) {
            status.failed();
            if (!failureLogged) {
                failureLogged = true;
                logger.warn("The CC cache cannot read the change log, so it is not used and the database answers until it can "
                        + "(is the migration that creates cc_change_log applied?).", e);
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

    private void noteGaps(List<CcChange> batch) {
        long expected = cursor + 1;
        for (CcChange change : batch) {
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
            List<CcChange> appeared = new ArrayList<>();
            for (CcChange change : repository.readRange(gap.from, gap.to)) {
                if (!gap.applied.contains(change.seq())) {
                    appeared.add(change);
                }
            }
            forget(appeared);
            appeared.forEach(change -> gap.applied.add(change.seq()));
            gap.delay = Math.min(gap.delay * 2, GAP_MAX_DELAY_NANOS);
            gap.nextCheck = now + gap.delay;
        }
    }

    // ---- forgetting ----------------------------------------------------------------------------------------------

    private void forget(List<CcChange> changes) {
        if (changes.isEmpty()) {
            return;
        }
        Set<String> keys = new HashSet<>();
        Set<Long> releases = new HashSet<>();
        Set<Long> libraries = new HashSet<>();
        Set<Long> users = new HashSet<>();
        Set<Long> tags = new HashSet<>();
        Map<CcChangeKind, Set<Long>> shownBy = new EnumMap<>(CcChangeKind.class);
        boolean dependenciesChanged = false;
        for (CcChange change : changes) {
            if (CcCacheKeys.isRecord(change.kind())) {
                CcCacheKeys.add(keys, change.kind(), change.id(), change.parentId(), change.releaseId());
                if (change.kind() == CcChangeKind.XBT || change.kind() == CcChangeKind.CODE_LIST
                        || change.kind() == CcChangeKind.AGENCY_ID_LIST) {
                    shownBy.computeIfAbsent(change.kind(), k -> new HashSet<>()).add(change.id());
                }
                continue;
            }
            switch (change.kind()) {
                case RELEASE -> releases.add(change.id());
                case RELEASE_DEP -> dependenciesChanged = true;
                case LIBRARY -> libraries.add(change.id());
                case USER -> users.add(change.id());
                case TAG -> tags.add(change.id());
                default -> {
                }
            }
        }
        releases.forEach(id -> addAll(keys, repository.recordsOfRelease(id)));
        libraries.forEach(id -> addAll(keys, repository.recordsOfLibrary(id)));
        users.forEach(id -> addAll(keys, repository.recordsOfUser(id)));
        tags.forEach(id -> addAll(keys, repository.recordsTaggedWith(id)));
        // the allowed primitives of a data type show the names of an XBT, a code list and an agency identification list
        shownBy.forEach((kind, ids) -> addAll(keys, repository.recordsShowing(kind, ids)));
        if (dependenciesChanged) {
            repository.allReleaseIds().forEach(id -> keys.add(CcCacheKeys.scope(id)));
        }
        cache.invalidate(keys);
    }

    private static void addAll(Set<String> keys, List<RecordRef> refs) {
        refs.forEach(ref -> CcCacheKeys.add(keys, ref.kind(), ref.id(), ref.parentId(), ref.releaseId()));
    }

    // ---- housekeeping --------------------------------------------------------------------------------------------

    /** Deletes the rows that are older than {@link #RETENTION}; run every ten minutes. */
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
            logger.warn("The old rows of the CC change log could not be deleted.", e);
        }
    }
}
