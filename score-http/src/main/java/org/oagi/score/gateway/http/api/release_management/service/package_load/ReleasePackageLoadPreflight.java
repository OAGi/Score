package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ReleasePackageReleases;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ScriptSource;
import org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormat;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadQueryRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * What is looked at before anything is changed: whether the package can be loaded into this database, and whether the
 * application can do what a load needs. Everything that is not as it has to be is said together, so that it is put right once.
 */
@Component
public class ReleasePackageLoadPreflight {

    private final Logger logger = LoggerFactory.getLogger(ReleasePackageLoadPreflight.class);

    private final RepositoryFactory repositoryFactory;
    private final ReleasePackageLoadRepositories repositories;
    private final PlatformTransactionManager transactionManager;

    public ReleasePackageLoadPreflight(RepositoryFactory repositoryFactory, ReleasePackageLoadRepositories repositories,
                                  PlatformTransactionManager transactionManager) {
        this.repositoryFactory = repositoryFactory;
        this.repositories = repositories;
        this.transactionManager = transactionManager;
    }

    /** @throws ReleasePackageLoadRefusedException with everything that is not as it has to be */
    public void check(ReleasePackageFormat format, Info info, ScriptSource scripts, long delta, ReleasePackageLoadJobId job) {
        List<String> problems = problems(format, info, scripts, delta, job);
        if (!problems.isEmpty()) {
            throw new ReleasePackageLoadRefusedException(String.join(" ", problems));
        }
    }

    /** What a look finds: what is in the way, and the latest release of each library, in the package and in this database. */
    public record Look(List<String> problems, List<ReleasePackageLoadPreflightReport.Library> libraries) {
    }

    /**
     * Everything that is not as it has to be, which is nothing changed to find out (but for a schema that is made and dropped to see
     * whether the database user may, when the rest is as it has to be).
     */
    public List<String> problems(ReleasePackageFormat format, Info info, ScriptSource scripts, long delta, ReleasePackageLoadJobId job) {
        return look(format, info, scripts, delta, job).problems();
    }

    public Look look(ReleasePackageFormat format, Info info, ScriptSource scripts, long delta, ReleasePackageLoadJobId job) {
        ReleasePackageLoadQueryRepository query = repositories.query();
        String served = query.currentSchema();
        List<String> problems = new ArrayList<>();

        problems.addAll(format.problemsOf(info, scripts));
        warnOfTheNumbering(format, query, served, delta);
        List<ReleasePackageLoadPreflightReport.Library> libraries = List.of();
        if (problems.isEmpty()) {
            List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> packaged = releasesOfThePackage(info, scripts);
            List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> current = latestReleasesOfThisDatabase();
            problems.addAll(problemsOfTheReleases(current, packaged));
            libraries = librariesOf(current, packaged);
        }
        if (new TransactionTemplate(transactionManager).execute(status -> repositoryFactory.releaseProgressRepository().anyLibraryIsBusy())) {
            problems.add(ReleasePackageLoadMessages.BUSY_RELEASE);
        }
        if (problems.isEmpty()) {
            // The last, since it makes a schema (and drops it): whether the user of the database is allowed to.
            problems.addAll(problemsOfThePrivileges(served, job));
        }
        return new Look(problems, libraries);
    }

    /** The latest release of each library of this database that has one, as the library and the release queries say. */
    private List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latestReleasesOfThisDatabase() {
        ReleaseQueryRepository releases = repositoryFactory.releaseQueryRepository(null);
        List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> latest = new ArrayList<>();
        for (LibrarySummaryRecord library : repositoryFactory.libraryQueryRepository(null).getLibrarySummaryList()) {
            ReleasePackageReleases.latestOf(releases.getReleaseSummaryList(library.libraryId()))
                    .ifPresent(release -> latest.add(Pair.of(library, release)));
        }
        return latest;
    }

    private List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> releasesOfThePackage(Info info, ScriptSource scripts) {
        List<String> names = Stream.concat(info.getMigration().getEntrypoints().stream(), info.getMigration().getProcesses().stream())
                .map(executable -> executable.getScript()).toList();
        try {
            return ReleasePackageReleases.latest(scripts, names);
        } catch (IOException e) {
            logger.warn("The releases of a package could not be read: " + e.getMessage(), e);
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_READ, e);
        }
    }

    /**
     * The records of the users are told by being numbered from {@code delta}; where the numbering of a table is not there yet, a record
     * that a user made before the package is loaded could be taken for the developers' and be lost, as it was with the data-loader.
     * The package sets the numbering for what is made after, so this is not in the way of a load: it is for the log.
     */
    private void warnOfTheNumbering(ReleasePackageFormat format, ReleasePackageLoadQueryRepository query, String served, long delta) {
        List<String> below = new ArrayList<>();
        for (String table : query.tables(served).stream().filter(format.numberedFromDelta()::contains).toList()) {
            BigInteger next = query.autoIncrement(served, table).orElse(null);
            if (next == null || next.compareTo(BigInteger.valueOf(delta)) < 0) {
                below.add(table);
            }
        }
        if (!below.isEmpty()) {
            logger.warn("The numbering of " + below + " is below " + delta + ": records that users made in them before this load are taken "
                    + "for those of the developers, and are not kept.");
        }
    }

    /**
     * The package is refused when, in any library, it is behind this database: its latest release is earlier than the one this database
     * has (a release is numbered as it is made), or it has none of that library but its working release. One that is as far, or further, is
     * not in the way, and a library of this database that has only its working release is not looked at.
     */
    static List<String> problemsOfTheReleases(List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> current,
                                              List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> packaged) {
        List<String> problems = new ArrayList<>();
        for (Pair<LibrarySummaryRecord, ReleaseSummaryRecord> have : current) {
            Pair<LibrarySummaryRecord, ReleaseSummaryRecord> brings = packaged.stream()
                    .filter(p -> p.getLeft().libraryId().equals(have.getLeft().libraryId())).findFirst().orElse(null);
            if (!isBehind(have, brings)) {
                continue;
            }
            String library = nameOf(have, brings);
            if (brings == null || ReleasePackageReleases.isWorking(brings.getRight())) {
                problems.add(ReleasePackageLoadMessages.libraryIsMissing(library, have.getRight().releaseNum()));
            } else {
                problems.add(ReleasePackageLoadMessages.libraryIsAhead(library, have.getRight().releaseNum(), brings.getRight().releaseNum()));
            }
        }
        return problems;
    }

    /** Whether the package is behind this database in the library: the rule that refuses it. */
    private static boolean isBehind(Pair<LibrarySummaryRecord, ReleaseSummaryRecord> have,
                                    Pair<LibrarySummaryRecord, ReleaseSummaryRecord> brings) {
        if (have == null || ReleasePackageReleases.isWorking(have.getRight())) {
            return false;   // a library that has only its working release has nothing a package could be behind
        }
        return brings == null || ReleasePackageReleases.isWorking(brings.getRight())
                || brings.getRight().releaseId().value().compareTo(have.getRight().releaseId().value()) < 0;
    }

    private static List<ReleasePackageLoadPreflightReport.Library> librariesOf(List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> current,
                                                                               List<Pair<LibrarySummaryRecord, ReleaseSummaryRecord>> packaged) {
        Map<BigInteger, ReleasePackageLoadPreflightReport.Library> libraries = new TreeMap<>();
        for (Pair<LibrarySummaryRecord, ReleaseSummaryRecord> brings : packaged) {
            Pair<LibrarySummaryRecord, ReleaseSummaryRecord> have = current.stream()
                    .filter(c -> c.getLeft().libraryId().equals(brings.getLeft().libraryId())).findFirst().orElse(null);
            libraries.put(brings.getLeft().libraryId().value(), new ReleasePackageLoadPreflightReport.Library(nameOf(have, brings),
                    brings.getRight().releaseNum(), have == null ? null : have.getRight().releaseNum(), isBehind(have, brings)));
        }
        for (Pair<LibrarySummaryRecord, ReleaseSummaryRecord> have : current) {
            libraries.computeIfAbsent(have.getLeft().libraryId().value(),
                    id -> new ReleasePackageLoadPreflightReport.Library(nameOf(have, null), null, have.getRight().releaseNum(), isBehind(have, null)));
        }
        return List.copyOf(libraries.values());
    }

    private static String nameOf(Pair<LibrarySummaryRecord, ReleaseSummaryRecord> have,
                                 Pair<LibrarySummaryRecord, ReleaseSummaryRecord> brings) {
        if (have != null && have.getLeft().name() != null) {
            return have.getLeft().name();
        }
        if (brings != null && brings.getLeft().name() != null) {
            return brings.getLeft().name();
        }
        Pair<LibrarySummaryRecord, ReleaseSummaryRecord> any = have != null ? have : brings;
        return "library " + any.getLeft().libraryId();
    }

    /**
     * Whether the database user of the application may make and drop schemas named after the one it serves, which a load needs;
     * what is in the way is told, and nothing is in the way when it is empty. A schema is made and dropped to find out.
     */
    public List<String> problemsOfThePrivileges(ReleasePackageLoadJobId job) {
        return problemsOfThePrivileges(repositories.query().currentSchema(), job);
    }

    private List<String> problemsOfThePrivileges(String served, ReleasePackageLoadJobId job) {
        ReleasePackageLoadCommandRepository command = repositories.command();
        String probe = ReleasePackageLoadNames.probe(served, job);
        try {
            command.createSchema(probe);
        } catch (RuntimeException e) {
            logger.warn("The database user of the application cannot make the schema '" + probe + "' (" + e.getMessage()
                    + "); it needs all privileges on the schemas whose name starts with '" + served + "_' (deploy/grant-load-privileges.sh gives them)");
            return List.of(ReleasePackageLoadMessages.NOT_ALLOWED_TO_CREATE_DATABASES);
        }
        try {
            command.dropSchema(probe);
            return List.of();
        } catch (RuntimeException e) {
            logger.warn("The database user of the application made the schema '" + probe + "' but cannot drop it: " + e.getMessage());
            return List.of(ReleasePackageLoadMessages.NOT_ALLOWED_TO_CREATE_DATABASES);
        }
    }
}
