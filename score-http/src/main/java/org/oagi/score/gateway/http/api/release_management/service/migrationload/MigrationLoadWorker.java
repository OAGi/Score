package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.CopyStep;
import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.ReleasePackageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Info;
import org.oagi.score.gateway.http.api.release_management.dataload.script.ZipScriptSource;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadQueryRepository;
import org.oagi.score.gateway.http.cache.RecordCache;
import org.oagi.score.gateway.http.common.async.AsyncJob;
import org.oagi.score.gateway.http.common.async.AsyncJobRunner;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadRepositories;
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
public class MigrationLoadWorker {

    /** The name of the lock of the loads: the same in every server, since the lock is of the application. */
    private static final String LOCK_NAME = "score:migration-load";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MigrationLoadRepositories repositories;
    private final MigrationLoadSettings settings;
    private final MigrationLoadReporter reporter;
    private final MigrationLoadPreflight preflight;
    private final MigrationLoadCopier copier;
    private final RedissonClient redisson;
    private final ObjectProvider<WriteFreeze> writeFreeze;
    private final ObjectProvider<RecordCache> recordCache;
    private final AsyncJobRunner asyncJobRunner;

    public MigrationLoadWorker(MigrationLoadRepositories repositories, MigrationLoadSettings settings, MigrationLoadReporter reporter,
                               MigrationLoadPreflight preflight, MigrationLoadCopier copier, RedissonClient redisson,
                               ObjectProvider<WriteFreeze> writeFreeze, ObjectProvider<RecordCache> recordCache,
                               AsyncJobRunner asyncJobRunner) {
        this.repositories = repositories;
        this.settings = settings;
        this.reporter = reporter;
        this.preflight = preflight;
        this.copier = copier;
        this.redisson = redisson;
        this.writeFreeze = writeFreeze;
        this.recordCache = recordCache;
        this.asyncJobRunner = asyncJobRunner;
    }

    /** What a run of the worker holds, for what is to be put back when it ends, whichever way. */
    private static final class Run {
        final MigrationLoadJobId id;
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

        Run(MigrationLoadJobId id, ScoreUser requester, Path packageFile, Info info, ReleasePackageFormat format) {
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
    public void load(MigrationLoadJob job, ScoreUser requester, Path packageFile, Info info, ReleasePackageFormat format) {
        Run run = new Run(job.id(), requester, packageFile, info, format);
        asyncJobRunner.run(AsyncJob.named("migration-load#" + job.id()).withoutTransaction()
                .work(() -> doLoad(run))
                .onFailure(cause -> failedLoad(run, cause))
                .always(() -> finish(run, true))
                .build());
    }

    /** Undoes the swap of a load that succeeded, on the thread that calls. */
    public void rollback(MigrationLoadJob job, ScoreUser requester) {
        Run run = new Run(job.id(), requester, null, null, null);
        run.backup = job.backupSchema();
        asyncJobRunner.run(AsyncJob.named("migration-load.rollback#" + job.id()).withoutTransaction()
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
        MigrationLoadCommandRepository command = repositories.command();
        MigrationLoadQueryRepository query = repositories.query();
        run.served = query.currentSchema();
        run.staging = MigrationLoadNames.staging(run.served, run.id);
        run.backup = MigrationLoadNames.backup(run.served, run.id);
        try (ZipScriptSource source = new ZipScriptSource(run.packageFile.toString())) {
            long delta = settings.delta();

            reporter.moved(run.id, run.requester, MigrationLoadState.PREFLIGHT, "Checking that the package can be loaded");
            preflight.check(run.format, run.info, source, delta, run.id);
            repositories.jobs().schemas(run.id, run.staging, run.backup, null);

            reporter.moved(run.id, run.requester, MigrationLoadState.LOADING, "Loading the package into " + run.staging);
            command.createSchema(run.staging);
            run.stagingMade = true;
            // The shape the package says it was written for when it says (so that its statements fit), otherwise the latest; then the rest of the migrations.
            command.migrateSchema(run.staging, run.format.schemaVersionOf(run.info).orElse(null));
            command.load(run.staging, run.info, source, delta,
                    message -> reporter.said(run.id, run.requester, MigrationLoadState.LOADING, message), run.format.statements());

            reporter.moved(run.id, run.requester, MigrationLoadState.MIGRATING, "Bringing " + run.staging + " to this version");
            command.migrateSchema(run.staging, null);

            reporter.moved(run.id, run.requester, MigrationLoadState.COPYING, "Stopping writes, and copying what the users made");
            WriteFreeze freeze = writeFreeze.getIfAvailable(UnavailableWriteFreeze::new);
            freeze.freeze();
            run.frozen = true;
            List<CopyStep> steps = copier.stepsToCopy(run.format, run.served, run.staging);
            Map<String, String> before = copier.fingerprints(run.served, steps);
            copier.copy(run.served, run.staging, steps, delta,
                    message -> reporter.said(run.id, run.requester, MigrationLoadState.COPYING, message));

            reporter.moved(run.id, run.requester, MigrationLoadState.VERIFYING, "Checking " + run.staging + " against the schema that is served");
            requireVerified(run, steps, before, delta);

            reporter.moved(run.id, run.requester, MigrationLoadState.SWAPPING, "Swapping " + run.staging + " for the schema that is served");
            command.createSchema(run.backup);
            run.backupMade = true;
            command.swap(run.staging, run.backup, settings.lockWait(), MigrationLoadTables.STAYS_IN_PLACE);
            run.swapped = true;
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        succeeded(run);
    }

    private void requireVerified(Run run, List<CopyStep> steps, Map<String, String> before, long delta) {
        MigrationLoadQueryRepository query = repositories.query();
        List<String> problems = new ArrayList<>();
        // The table of the history is the migration tool's own (its text columns take the collation of the schema it was made in), not the application's.
        problems.addAll(query.differences(run.served, run.staging).stream().filter(line -> !ofTheMigrationHistory(line)).limit(10).toList());
        problems.addAll(copier.missing(run.served, run.staging, steps, delta).stream().map(m -> "not all copied: " + m).toList());
        problems.addAll(query.foreignKeyViolations(run.staging).stream().limit(5).toList());
        Map<String, String> after = copier.fingerprints(run.served, steps);
        before.forEach((table, fingerprint) -> {
            if (!fingerprint.equals(after.get(table))) {
                problems.add("something was written to " + table + " while it was copied");
            }
        });
        if (!problems.isEmpty()) {
            throw new MigrationLoadRefusedException("The schema the package was loaded into is not swapped in: " + String.join("; ", problems) + ".");
        }
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
            warnings.add("the cache of the records could not be emptied: " + e.getMessage());
        }
        try {
            repositories.command().dropSchema(run.staging);
        } catch (RuntimeException e) {
            warnings.add("the schema " + run.staging + " could not be dropped: " + e.getMessage());
        }
        String message = "The schema is swapped; the one that was served is kept in " + run.backup
                + (warnings.isEmpty() ? "." : ", but " + String.join("; ", warnings) + ".");
        reporter.moved(run.id, run.requester, MigrationLoadState.SUCCEEDED, message);
    }

    private void failedLoad(Run run, Throwable cause) {
        if (run.swapped) {
            // The swap is done, and cannot be what failed: it is what came after.
            reporter.moved(run.id, run.requester, MigrationLoadState.SUCCEEDED,
                    "The schema is swapped; the one that was served is kept in " + run.backup + ", but " + describe(cause));
            return;
        }
        StringBuilder message = new StringBuilder(describe(cause));
        MigrationLoadCommandRepository command = repositories.command();
        MigrationLoadQueryRepository query = repositories.query();
        if (run.stagingMade) {
            dropQuietly(command, run.staging, message);
        }
        if (run.backupMade) {
            // The tables of the served schema may be in it, when a swap could not be undone: it is dropped only when it holds none.
            List<String> tables = query.tables(run.backup);
            if (tables.isEmpty() || tables.equals(List.of("migration_load_swap_record"))) {
                dropQuietly(command, run.backup, message);
            } else {
                message.append(" The schema ").append(run.backup).append(" holds tables and is kept: ").append(tables.size());
            }
        }
        try {
            reporter.moved(run.id, run.requester, MigrationLoadState.FAILED, message.toString());
        } catch (IllegalStateException taken) {
            logger.warn("The load " + run.id + " was taken back before it failed: " + taken.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The undo
    // ---------------------------------------------------------------------------------------------------------------

    private void doRollback(Run run) {
        takeLock(run);
        MigrationLoadCommandRepository command = repositories.command();
        String served = repositories.query().currentSchema();
        String discarded = MigrationLoadNames.discarded(served, run.id);

        reporter.moved(run.id, run.requester, Set.of(MigrationLoadState.SUCCEEDED), MigrationLoadState.ROLLING_BACK,
                "Stopping writes, and putting back " + run.backup);
        writeFreeze.getIfAvailable(UnavailableWriteFreeze::new).freeze();
        run.frozen = true;
        command.createSchema(discarded);
        repositories.jobs().schemas(run.id, null, null, discarded);
        command.rollbackSwap(run.backup, discarded, settings.lockWait());

        List<String> warnings = new ArrayList<>();
        try {
            RecordCache cache = recordCache.getIfAvailable();
            if (cache != null) {
                cache.invalidateAll();
            }
        } catch (RuntimeException e) {
            warnings.add("the cache of the records could not be emptied: " + e.getMessage());
        }
        StringBuilder message = new StringBuilder("The swap is undone; what was swapped in is kept in " + discarded);
        dropQuietly(command, run.backup, message);
        reporter.moved(run.id, run.requester, MigrationLoadState.ROLLED_BACK,
                message + (warnings.isEmpty() ? "." : ", but " + String.join("; ", warnings) + "."));
    }

    private void failedRollback(Run run, Throwable cause) {
        try {
            reporter.moved(run.id, run.requester, MigrationLoadState.FAILED,
                    "The swap was not undone: " + describe(cause) + " The schemas are to be looked at; " + run.backup
                            + " keeps what was served before the swap.");
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
                writeFreeze.getIfAvailable(UnavailableWriteFreeze::new).release();
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
            throw new MigrationLoadRefusedException("Another load goes on.");
        }
        run.lock = lock;
    }

    private void dropQuietly(MigrationLoadCommandRepository command, String schema, StringBuilder message) {
        try {
            command.dropSchema(schema);
        } catch (RuntimeException e) {
            message.append(" The schema ").append(schema).append(" could not be dropped: ").append(e.getMessage());
        }
    }

    /**
     * What failed, for whoever reads the record of the load: the message of the failure and of what caused it, since the first is
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
