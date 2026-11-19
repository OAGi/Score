package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ZipScriptSource;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadState;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadJobRepository;
import org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormat;
import org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormats;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;

/**
 * What an administrator asks of the loads of release packages: to load a package, to undo the swap of a load, and how a load
 * goes. The load is done in the background, on the server that was given the package; what is returned is the record of it.
 *
 * <p>It is not transactional: a load changes schemas, which MariaDB commits as it goes.</p>
 */
@Service
public class ReleasePackageLoadCommandService {

    private final Logger logger = LoggerFactory.getLogger(ReleasePackageLoadCommandService.class);

    private final ReleasePackageLoadRepositories repositories;
    private final ReleasePackageLoadProperties settings;
    private final ReleasePackageLoadWorker worker;
    private final ReleasePackageLoadExecutor executor;
    private final ReleasePackageFormats formats;
    private final ReleasePackageLoadPreflight preflight;
    private final ReleasePackageLoadDiscarder discarder;

    public ReleasePackageLoadCommandService(ReleasePackageLoadRepositories repositories, ReleasePackageLoadProperties settings,
                                       ReleasePackageLoadWorker worker, ReleasePackageLoadExecutor executor, ReleasePackageFormats formats,
                                       ReleasePackageLoadPreflight preflight, ReleasePackageLoadDiscarder discarder) {
        this.repositories = repositories;
        this.settings = settings;
        this.worker = worker;
        this.executor = executor;
        this.formats = formats;
        this.preflight = preflight;
        this.discarder = discarder;
    }

    private ReleasePackageLoadJobRepository jobs() {
        return repositories.jobs();
    }

    /**
     * Starts loading a package, which is a zip of the information file and the scripts it names. The file is the service's from now
     * on: it is deleted when the load is over, and at once when the load is refused. The one who is not an administrator is
     * refused before the method is called, so the file is still the caller's then (a file comes from {@link ReleasePackageLoadUploads#take},
     * which is for administrators too).
     *
     * @return the record of the load, which is where it goes from PENDING
     * @throws AccessControlException       for one who is not an administrator
     * @throws ReleasePackageLoadRefusedException when the package is not a package, or another load goes on
     */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public ReleasePackageLoadJob start(ScoreUser requester, Path packageFile, String fileName) {
        try {
            requireAvailable();
            Info info = read(packageFile);
            // The version of the package says how it is loaded; one that is not loaded is refused before anything is recorded.
            ReleasePackageFormat format = formats.resolve(info);
            ReleasePackageLoadJobId id = jobs().createUnlessActive(requester.userId(), fileName,
                    info.getMigration().getRelease().getReleaseNum(), nodeId())
                    .orElseThrow(() -> new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.ANOTHER_LOAD));
            ReleasePackageLoadJob job = jobs().find(id).orElseThrow();
            try {
                executor.execute(() -> worker.load(job, requester, packageFile, info, format));
            } catch (RejectedExecutionException e) {
                jobs().moveState(id, ReleasePackageLoadState.ACTIVE, ReleasePackageLoadState.FAILED, ReleasePackageLoadMessages.SHUTTING_DOWN);
                throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.SHUTTING_DOWN, e);
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
     * @throws ReleasePackageLoadRefusedException when the load is not the latest that succeeded, or another goes on
     */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public ReleasePackageLoadJob rollback(ScoreUser requester, ReleasePackageLoadJobId id) {
        requireAvailable();
        ReleasePackageLoadJob job = jobs().find(id).orElseThrow(() -> new NotFoundException(ReleasePackageLoadMessages.LOAD_NOT_FOUND));
        if (job.state() != ReleasePackageLoadState.SUCCEEDED || job.backupSchema() == null) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.ONLY_SUCCEEDED);
        }
        if (!jobs().findLatest(ReleasePackageLoadState.SUCCEEDED).map(latest -> latest.id().equals(id)).orElse(false)) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.ONLY_LATEST);
        }
        if (!jobs().findActive().isEmpty()) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.ANOTHER_LOAD);
        }
        try {
            executor.execute(() -> worker.rollback(job, requester));
        } catch (RejectedExecutionException e) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.SHUTTING_DOWN, e);
        }
        return job;
    }

    /**
     * Discards the record of a load that is over, with the schema that holds what it took out (so it can no longer be
     * undone). What a later load has replaced goes with it, as the schemas of those loads can no longer be put back either.
     *
     * @throws ReleasePackageLoadRefusedException when the load is not over, or another goes on
     */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public void discard(ScoreUser requester, ReleasePackageLoadJobId id) {
        requireAvailable();
        ReleasePackageLoadJob job = jobs().find(id).orElseThrow(() -> new NotFoundException(ReleasePackageLoadMessages.LOAD_NOT_FOUND));
        if (!ReleasePackageLoadDiscarder.DISCARDABLE.contains(job.state())) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.ONLY_ENDED);
        }
        if (!jobs().findActive().isEmpty()) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.ANOTHER_LOAD);
        }
        if (!discarder.canDiscard(job)) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.KEEPS_DATA);
        }
        discarder.discard(job);
    }

    /**
     * What a load of the package would meet, which nothing is changed to find out: everything that is not as it has to be is told, and a
     * package that has nothing in the way is told to have none.
     */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public ReleasePackageLoadPreflightReport preflight(ScoreUser requester, Path packageFile) {
        Info info = read(packageFile);
        String releaseNum = info.getMigration().getRelease().getReleaseNum();
        List<String> problems = new ArrayList<>();
        ReleasePackageFormat format = null;
        try {
            format = formats.resolve(info);
        } catch (ReleasePackageLoadRefusedException e) {
            problems.add(e.getMessage());
        }
        if (!jobs().findActive().isEmpty()) {
            problems.add(ReleasePackageLoadMessages.ANOTHER_LOAD);
        }
        List<ReleasePackageLoadPreflightReport.Library> libraries = List.of();
        if (format != null) {
            try (ZipScriptSource source = new ZipScriptSource(packageFile.toString())) {
                ReleasePackageLoadPreflight.Look look = preflight.look(format, info, source, settings.getDelta(), new ReleasePackageLoadJobId(BigInteger.ZERO));
                problems.addAll(look.problems());
                libraries = look.libraries();
            } catch (IOException e) {
                logger.warn("A package could not be read: " + e.getMessage(), e);
                throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_READ, e);
            }
        }
        return new ReleasePackageLoadPreflightReport(releaseNum, info.getVersion(), format == null ? null : format.name(), List.copyOf(problems), libraries);
    }

    /** The loads that were started last, the latest first. */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public List<ReleasePackageLoadJob> recent(ScoreUser requester, int limit) {
        return jobs().findRecent(Math.max(1, Math.min(limit, 50)));
    }

    /** How a load goes. */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public ReleasePackageLoadJob get(ScoreUser requester, ReleasePackageLoadJobId id) {
        return jobs().find(id).orElseThrow(() -> new NotFoundException(ReleasePackageLoadMessages.LOAD_NOT_FOUND));
    }

    /**
     * What keeps loads from working in this application, which is nothing when it is empty: the database user of the application
     * must be allowed to make and drop the schemas a load goes through.
     */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public List<String> unavailable(ScoreUser requester) {
        return problemsOfAvailability();
    }

    /** @throws ReleasePackageLoadRefusedException when {@link #unavailable} tells anything */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public void requireAvailable(ScoreUser requester) {
        requireAvailable();
    }

    private void requireAvailable() {
        List<String> problems = problemsOfAvailability();
        if (!problems.isEmpty()) {
            throw new ReleasePackageLoadRefusedException(String.join(" ", problems));
        }
    }

    private List<String> problemsOfAvailability() {
        // A name of its own, so that two who ask at once do not meet on one schema.
        List<String> problems = preflight.problemsOfThePrivileges(new ReleasePackageLoadJobId(BigInteger.valueOf(System.nanoTime() & Long.MAX_VALUE)));
        if (problems.isEmpty()) {
            return problems;
        }
        // What is wrong in the database is for the log, not for whoever asks.
        logger.warn("Release packages cannot be loaded: " + String.join("; ", problems));
        return List.of(ReleasePackageLoadMessages.NOT_ALLOWED_TO_CREATE_DATABASES);
    }

    Info read(Path packageFile) {
        try (ZipScriptSource source = new ZipScriptSource(packageFile.toString())) {
            try (var stream = source.openInfo(source.findInfoEntry())) {
                return ReleasePackageInfoLoader.loadFromJson(stream);
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            logger.warn("A file is not a release package: " + e.getMessage(), e);
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_A_PACKAGE, e);
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
