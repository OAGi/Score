package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.oagi.score.gateway.http.api.release_management.dataload.info.Info;
import org.oagi.score.gateway.http.api.release_management.dataload.info.InfoLoader;
import org.oagi.score.gateway.http.api.release_management.dataload.script.ZipScriptSource;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadJobRepository;
import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.ReleasePackageFormat;
import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.ReleasePackageFormats;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadRepositories;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

/**
 * What an administrator asks of the loads of release packages: to load a package, to undo the swap of a load, and how a load
 * goes. The load is done in the background, on the server that was given the package; what is returned is the record of it.
 *
 * <p>It is not transactional: a load changes schemas, which MariaDB commits as it goes.</p>
 */
@Service
public class MigrationLoadCommandService {

    private final MigrationLoadRepositories repositories;
    private final MigrationLoadSettings settings;
    private final MigrationLoadWorker worker;
    private final MigrationLoadExecutor executor;
    private final ReleasePackageFormats formats;

    public MigrationLoadCommandService(MigrationLoadRepositories repositories, MigrationLoadSettings settings,
                                       MigrationLoadWorker worker, MigrationLoadExecutor executor, ReleasePackageFormats formats) {
        this.repositories = repositories;
        this.settings = settings;
        this.worker = worker;
        this.executor = executor;
        this.formats = formats;
    }

    private MigrationLoadJobRepository jobs() {
        return repositories.jobs();
    }

    /**
     * Starts loading a package, which is a zip of the information file and the scripts it names. The file is the service's from now
     * on: it is deleted when the load is over, and at once when the load is refused.
     *
     * @return the record of the load, which is where it goes from PENDING
     * @throws AccessControlException       for one who is not an administrator
     * @throws MigrationLoadRefusedException when loads are turned off, the package is not a package, or another load goes on
     */
    public MigrationLoadJob start(ScoreUser requester, Path packageFile, String fileName) {
        try {
            requireAllowed(requester);
            Info info = read(packageFile);
            // The version of the package says how it is loaded; one that is not loaded is refused before anything is recorded.
            ReleasePackageFormat format = formats.resolve(info);
            MigrationLoadJobId id = jobs().createUnlessActive(requester.userId(), fileName,
                    info.getMigration().getRelease().getReleaseNum(), nodeId())
                    .orElseThrow(() -> new MigrationLoadRefusedException("Another load goes on."));
            MigrationLoadJob job = jobs().find(id).orElseThrow();
            try {
                executor.execute(() -> worker.load(job, requester, packageFile, info, format));
            } catch (RejectedExecutionException e) {
                jobs().moveState(id, MigrationLoadState.ACTIVE, MigrationLoadState.FAILED, "The application is shutting down.");
                throw new MigrationLoadRefusedException("The application is shutting down.", e);
            }
            return job;
        } catch (RuntimeException e) {
            deleteQuietly(packageFile);
            throw e;
        }
    }

    /**
     * Undoes the swap of the latest load that succeeded, in the background.
     *
     * @throws MigrationLoadRefusedException when the load is not the latest that succeeded, or another goes on
     */
    public MigrationLoadJob rollback(ScoreUser requester, MigrationLoadJobId id) {
        requireAllowed(requester);
        MigrationLoadJob job = jobs().find(id).orElseThrow(() -> new NotFoundException("The load " + id + " does not exist."));
        if (job.state() != MigrationLoadState.SUCCEEDED || job.backupSchema() == null) {
            throw new MigrationLoadRefusedException("Only a load that succeeded is undone; the load " + id + " is " + job.state() + ".");
        }
        if (!jobs().findLatest(MigrationLoadState.SUCCEEDED).map(latest -> latest.id().equals(id)).orElse(false)) {
            throw new MigrationLoadRefusedException("Only the latest load that succeeded is undone: a later one swapped what this one "
                    + "swapped in, and would be lost.");
        }
        if (!jobs().findActive().isEmpty()) {
            throw new MigrationLoadRefusedException("Another load goes on.");
        }
        try {
            executor.execute(() -> worker.rollback(job, requester));
        } catch (RejectedExecutionException e) {
            throw new MigrationLoadRefusedException("The application is shutting down.", e);
        }
        return job;
    }

    /** How a load goes. */
    public MigrationLoadJob get(ScoreUser requester, MigrationLoadJobId id) {
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }
        return jobs().find(id).orElseThrow(() -> new NotFoundException("The load " + id + " does not exist."));
    }

    private void requireAllowed(ScoreUser requester) {
        if (!settings.enabled()) {
            throw new MigrationLoadRefusedException("Loading a release package is turned off (score.migration-load.enabled).");
        }
        if (!requester.hasRole(ScoreRole.ADMINISTRATOR)) {
            throw new AccessControlException(requester);
        }
    }

    private Info read(Path packageFile) {
        try (ZipScriptSource source = new ZipScriptSource(packageFile.toString())) {
            try (var stream = source.open(source.findInfoEntry())) {
                return InfoLoader.loadFromJson(stream);
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw new MigrationLoadRefusedException("The file is not a release package: " + e.getMessage(), e);
        }
    }

    private static String nodeId() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    private static void deleteQuietly(Path file) {
        try {
            if (file != null) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ignored) {
            // A file that cannot be deleted is left to whoever made it.
        }
    }
}
