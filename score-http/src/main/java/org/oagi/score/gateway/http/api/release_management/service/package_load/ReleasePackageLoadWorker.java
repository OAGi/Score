package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.service.package_load.format.CopyStep;
import org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ZipScriptSource;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadQueryRepository;
import org.oagi.score.gateway.http.cache.RecordCache;
import org.oagi.score.gateway.http.common.async.AsyncJob;
import org.oagi.score.gateway.http.common.async.AsyncJobRunner;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.concurrent.locks.Lock;

/**
 * Does a load, step by step, and undoes one: the package is loaded into a schema of its own and brought to the shape of this
 * version, the records of the users are copied into it while writing is stopped, it is checked against the schema that is served,
 * and it is swapped for it. Whatever fails before the swap leaves the served schema as it was.
 *
 * <p>It is run as a job that has no transaction (the work is the changing of schemas, which commits as it goes) and is given
 * back when it fails: the schemas that were made are dropped and the load ends as FAILED.</p>
 */
@Component
public class ReleasePackageLoadWorker {

    /** The name of the lock of the loads: the same in every server, since the lock is of the application. */
    private static final String LOCK_NAME = "score:release-package-load";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReleasePackageLoadRepositories repositories;
    private final ReleasePackageLoadSettings settings;
    private final ReleasePackageLoadReporter reporter;
    private final ReleasePackageLoadPreflight preflight;
    private final ReleasePackageLoadCopier copier;
    private final RedissonClient redisson;
    private final ObjectProvider<ReleasePackageLoadWriteFreeze> writeFreeze;
    private final ObjectProvider<RecordCache> recordCache;
    private final AsyncJobRunner asyncJobRunner;
    private final ReleasePackageLoadConnectionPoolRefresher connections;

    public ReleasePackageLoadWorker(ReleasePackageLoadRepositories repositories, ReleasePackageLoadSettings settings, ReleasePackageLoadReporter reporter,
                               ReleasePackageLoadPreflight preflight, ReleasePackageLoadCopier copier, RedissonClient redisson,
                               ObjectProvider<ReleasePackageLoadWriteFreeze> writeFreeze, ObjectProvider<RecordCache> recordCache,
                               AsyncJobRunner asyncJobRunner, ReleasePackageLoadConnectionPoolRefresher connections) {
        this.repositories = repositories;
        this.settings = settings;
        this.reporter = reporter;
        this.preflight = preflight;
        this.copier = copier;
        this.redisson = redisson;
        this.writeFreeze = writeFreeze;
        this.recordCache = recordCache;
        this.asyncJobRunner = asyncJobRunner;
        this.connections = connections;
    }

    /** What a run of the worker holds, for what is to be put back when it ends, whichever way. */
    private static final class Run {
        final ReleasePackageLoadJobId id;
        final ScoreUser requester;
        final Path packageFile;
        final Info info;
        final ReleasePackageFormat format;
        String served;
        String staging;
        String backup;
        Lock lock;
        boolean frozen;
        boolean stagingMade;
        boolean backupMade;
        boolean swapped;

        Run(ReleasePackageLoadJobId id, ScoreUser requester, Path packageFile, Info info, ReleasePackageFormat format) {
            this.id = id;
            this.requester = requester;
            this.packageFile = packageFile;
            this.info = info;
            this.format = format;
        }
    }

    /**
     * Does the load on the thread that calls, and ends it as SUCCEEDED or FAILED, which is what the record of the load says. The
     * file of the package is the load's from now on, and is deleted when the load is over.
     */
    public void load(ReleasePackageLoadJob job, ScoreUser requester, Path packageFile, Info info, ReleasePackageFormat format) {
        Run run = new Run(job.id(), requester, packageFile, info, format);
        asyncJobRunner.run(AsyncJob.named("release-package-load#" + job.id()).withoutTransaction()
                .work(() -> doLoad(run))
                .onFailure(cause -> failedLoad(run, cause))
                .always(() -> finish(run, true))
                .build());
    }

    /** Undoes the swap of a load that succeeded, on the thread that calls. */
    public void rollback(ReleasePackageLoadJob job, ScoreUser requester) {
        Run run = new Run(job.id(), requester, null, null, null);
        run.backup = job.backupSchema();
        asyncJobRunner.run(AsyncJob.named("release-package-load.rollback#" + job.id()).withoutTransaction()
                .work(() -> doRollback(run))
                .onFailure(cause -> failedRollback(run, cause))
                .always(() -> finish(run, false))
                .build());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The load
    // ---------------------------------------------------------------------------------------------------------------

    private void doLoad(Run run) {
        takeLock(run);
        ReleasePackageLoadCommandRepository command = repositories.command();
        ReleasePackageLoadQueryRepository query = repositories.query();
        run.served = query.currentSchema();
        run.staging = ReleasePackageLoadNames.staging(run.served, run.id);
        run.backup = ReleasePackageLoadNames.backup(run.served, run.id);
        try (ZipScriptSource source = new ZipScriptSource(run.packageFile.toString())) {
            long delta = settings.delta();

            reporter.moved(run.id, run.requester, ReleasePackageLoadState.PREFLIGHT, "Checking that the package can be loaded");
            preflight.check(run.format, run.info, source, delta, run.id);
            repositories.jobs().schemas(run.id, run.staging, run.backup, null);

            reporter.moved(run.id, run.requester, ReleasePackageLoadState.LOADING, ReleasePackageLoadMessages.LOADING);
            command.createSchema(run.staging);
            run.stagingMade = true;
            // The shape the package says it was written for when it says (so that its statements fit), otherwise the latest; then the rest of the migrations.
            command.migrateSchema(run.staging, run.format.schemaVersionOf(run.info).orElse(null));
            command.load(run.staging, run.info, source, delta,
                    message -> logger.debug("Load " + run.id + ": " + message), run.format.statements());

            reporter.moved(run.id, run.requester, ReleasePackageLoadState.MIGRATING, ReleasePackageLoadMessages.MIGRATING);
            command.migrateSchema(run.staging, null);

            reporter.moved(run.id, run.requester, ReleasePackageLoadState.COPYING, ReleasePackageLoadMessages.COPYING);
            ReleasePackageLoadWriteFreeze freeze = writeFreeze.getIfAvailable(ReleasePackageLoadUnavailableWriteFreeze::new);
            freeze.freeze();
            run.frozen = true;
            List<CopyStep> steps = copier.stepsToCopy(run.format, run.served, run.staging);
            Map<String, String> before = copier.fingerprints(run.served, steps);
            AtomicInteger copied = new AtomicInteger();
            copier.copy(run.served, run.staging, steps, delta, run.info, message -> {
                logger.debug("Load " + run.id + ": " + message);
                reporter.said(run.id, run.requester, ReleasePackageLoadState.COPYING,
                        ReleasePackageLoadMessages.copying(copied.incrementAndGet(), steps.size()));
            });

            reporter.moved(run.id, run.requester, ReleasePackageLoadState.VERIFYING, ReleasePackageLoadMessages.VERIFYING);
            requireVerified(run, steps, before, delta);

            reporter.moved(run.id, run.requester, ReleasePackageLoadState.SWAPPING, ReleasePackageLoadMessages.SWAPPING);
            command.createSchema(run.backup);
            run.backupMade = true;
            command.swap(run.staging, run.backup, settings.lockWait(), ReleasePackageLoadTables.STAYS_IN_PLACE);
            run.swapped = true;
            connections.refresh();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        succeeded(run);
    }

    private void requireVerified(Run run, List<CopyStep> steps, Map<String, String> before, long delta) {
        ReleasePackageLoadQueryRepository query = repositories.query();
        List<String> problems = new ArrayList<>();
        // The table of the history is the migration tool's own (its text columns take the collation of the schema it was made in), not the application's.
        problems.addAll(query.differences(run.served, run.staging).stream().filter(line -> !ofTheMigrationHistory(line)).limit(10).toList());
        problems.addAll(copier.missing(run.served, run.staging, steps, delta, run.info).stream().map(m -> "not all copied: " + m).toList());
        problems.addAll(query.foreignKeyViolations(run.staging).stream().limit(5).toList());
        Map<String, String> after = copier.fingerprints(run.served, steps);
        before.forEach((table, fingerprint) -> {
            if (!fingerprint.equals(after.get(table))) {
                problems.add("something was written to " + table + " while it was copied");
            }
        });
        if (!problems.isEmpty()) {
            logger.warn("The load " + run.id + " is not swapped in: " + String.join("; ", problems));
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.stopped(String.join(" ", reasonsOf(problems))));
        }
    }

    /** What the problems found come to, in the words of whoever loads: one sentence for each kind of them. */
    private static List<String> reasonsOf(List<String> problems) {
        Predicate<String> notCopied = p -> p.startsWith("not all copied");
        Predicate<String> written = p -> p.startsWith("something was written");
        Predicate<String> dangling = p -> p.contains("record(s) refer");
        List<String> reasons = new ArrayList<>();
        // What is none of the others is a difference of the structure of the schemas.
        if (problems.stream().anyMatch(notCopied.or(written).or(dangling).negate())) {
            reasons.add(ReleasePackageLoadMessages.STRUCTURE_DIFFERS);
        }
        if (problems.stream().anyMatch(p -> p.contains("of app_user that is not there"))) {
            reasons.add(ReleasePackageLoadMessages.MISSING_USERS);
        } else if (problems.stream().anyMatch(dangling)) {
            reasons.add(ReleasePackageLoadMessages.MISSING_RECORDS);
        }
        if (problems.stream().anyMatch(notCopied)) {
            reasons.add(ReleasePackageLoadMessages.NOT_ALL_CARRIED_OVER);
        }
        if (problems.stream().anyMatch(written)) {
            reasons.add(ReleasePackageLoadMessages.CHANGED_WHILE_COPIED);
        }
        return reasons;
    }

    private static boolean ofTheMigrationHistory(String line) {
        return line.contains(" flyway_schema_history|") || line.endsWith(" flyway_schema_history");
    }

    /** What is done once the schemas are swapped: nothing here undoes it, so what fails is told, not thrown. */
    private void succeeded(Run run) {
        List<String> warnings = new ArrayList<>();
        try {
            RecordCache cache = recordCache.getIfAvailable();
            if (cache != null) {
                cache.invalidateAll();
            }
        } catch (RuntimeException e) {
            logger.warn("The cache of the records could not be emptied after the load " + run.id + ": " + e.getMessage(), e);
            warnings.add(ReleasePackageLoadMessages.STALE_SCREENS);
        }
        try {
            repositories.command().dropSchema(run.staging);
        } catch (RuntimeException e) {
            logger.warn("The schema " + run.staging + " could not be dropped after the load " + run.id + ": " + e.getMessage(), e);
            warnings.add(ReleasePackageLoadMessages.LEFTOVERS);
        }
        String message = ReleasePackageLoadMessages.SUCCEEDED + String.join("", warnings);
        reporter.moved(run.id, run.requester, ReleasePackageLoadState.SUCCEEDED, message);
    }

    private void failedLoad(Run run, Throwable cause) {
        if (run.swapped) {
            // The swap is done, and cannot be what failed: it is what came after.
            logger.error("The load " + run.id + " failed after the swap: " + describe(cause), cause);
            reporter.moved(run.id, run.requester, ReleasePackageLoadState.SUCCEEDED, ReleasePackageLoadMessages.AFTER_LOAD_PROBLEM);
            return;
        }
        logger.error("The load " + run.id + " failed: " + describe(cause), cause);
        StringBuilder message = new StringBuilder(whatToTell(cause));
        ReleasePackageLoadCommandRepository command = repositories.command();
        ReleasePackageLoadQueryRepository query = repositories.query();
        if (run.stagingMade) {
            dropQuietly(command, run.staging, message);
        }
        if (run.backupMade) {
            // The tables of the served schema may be in it, when a swap could not be undone: it is dropped only when it holds none.
            List<String> tables = query.tables(run.backup);
            if (tables.isEmpty() || tables.equals(List.of("release_package_load_swap_record"))) {
                dropQuietly(command, run.backup, message);
            } else {
                logger.error("The schema " + run.backup + " of the load " + run.id + " holds " + tables.size() + " table(s) and is kept.");
                message.append(ReleasePackageLoadMessages.KEPT_ASIDE);
            }
        }
        try {
            reporter.moved(run.id, run.requester, ReleasePackageLoadState.FAILED, message.toString());
        } catch (IllegalStateException taken) {
            logger.warn("The load " + run.id + " was taken back before it failed: " + taken.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The undo
    // ---------------------------------------------------------------------------------------------------------------

    private void doRollback(Run run) {
        takeLock(run);
        ReleasePackageLoadCommandRepository command = repositories.command();
        String served = repositories.query().currentSchema();
        String discarded = ReleasePackageLoadNames.discarded(served, run.id);

        reporter.moved(run.id, run.requester, Set.of(ReleasePackageLoadState.SUCCEEDED), ReleasePackageLoadState.ROLLING_BACK,
                ReleasePackageLoadMessages.ROLLING_BACK);
        writeFreeze.getIfAvailable(ReleasePackageLoadUnavailableWriteFreeze::new).freeze();
        run.frozen = true;
        command.createSchema(discarded);
        repositories.jobs().schemas(run.id, null, null, discarded);
        command.rollbackSwap(run.backup, discarded, settings.lockWait());
        connections.refresh();

        List<String> warnings = new ArrayList<>();
        try {
            RecordCache cache = recordCache.getIfAvailable();
            if (cache != null) {
                cache.invalidateAll();
            }
        } catch (RuntimeException e) {
            logger.warn("The cache of the records could not be emptied after the undo of the load " + run.id + ": " + e.getMessage(), e);
            warnings.add(ReleasePackageLoadMessages.STALE_SCREENS);
        }
        StringBuilder message = new StringBuilder(ReleasePackageLoadMessages.ROLLED_BACK);
        dropQuietly(command, run.backup, message);
        reporter.moved(run.id, run.requester, ReleasePackageLoadState.ROLLED_BACK, message + String.join("", warnings));
    }

    private void failedRollback(Run run, Throwable cause) {
        try {
            logger.error("The undo of the load " + run.id + " failed: " + describe(cause), cause);
            reporter.moved(run.id, run.requester, ReleasePackageLoadState.FAILED, ReleasePackageLoadMessages.NOT_UNDONE);
        } catch (IllegalStateException taken) {
            logger.warn("The load " + run.id + " was taken back before its undo failed: " + taken.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The end of a run, whichever way it ends
    // ---------------------------------------------------------------------------------------------------------------

    private void finish(Run run, boolean deletePackage) {
        try {
            if (run.frozen) {
                writeFreeze.getIfAvailable(ReleasePackageLoadUnavailableWriteFreeze::new).release();
            }
        } finally {
            try {
                if (run.lock != null) {
                    run.lock.unlock();
                }
            } finally {
                if (deletePackage && run.packageFile != null) {
                    try {
                        Files.deleteIfExists(run.packageFile);
                    } catch (IOException e) {
                        logger.warn("The package " + run.packageFile + " could not be deleted: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Takes the lock that lets one load go on at a time in the whole of the application, whichever server it is on. It is a lock of
     * Redisson, which is a {@link Lock} that is held by the thread that took it (this one, which also ends the run) and, with no lease,
     * is kept for as long as the server is up: a server that stops does not keep it.
     */
    private void takeLock(Run run) {
        Lock lock = redisson.getLock(LOCK_NAME);
        if (!lock.tryLock()) {
            throw new ReleasePackageLoadRefusedException("Another load goes on.");
        }
        run.lock = lock;
    }

    private void dropQuietly(ReleasePackageLoadCommandRepository command, String schema, StringBuilder message) {
        try {
            command.dropSchema(schema);
        } catch (RuntimeException e) {
            logger.warn("The schema " + schema + " could not be dropped: " + e.getMessage(), e);
            message.append(ReleasePackageLoadMessages.LEFTOVERS);
        }
    }

    /** What whoever loads is told of a failure: what was refused is in words that are meant for them, anything else is only told to have failed. */
    private static String whatToTell(Throwable cause) {
        for (Throwable t = cause; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof ReleasePackageLoadRefusedException) {
                return t.getMessage();
            }
        }
        return ReleasePackageLoadMessages.UNEXPECTED;
    }

    /**
     * What failed, for the log of the load: the message of the failure and of what caused it, since the first is
     * often only what a library says of any failure ("Error while running ...") and the cause is what is wanted.
     */
    private static String describe(Throwable cause) {
        List<String> messages = new ArrayList<>();
        for (Throwable t = cause; t != null && messages.size() < 4; t = t.getCause() == t ? null : t.getCause()) {
            String message = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
            if (!messages.contains(message)) {
                messages.add(message);
            }
        }
        String described = String.join(": ", messages);
        return described.length() > 1800 ? described.substring(0, 1800) + "..." : described;
    }
}
