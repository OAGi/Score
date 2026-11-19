package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    /**
     * Everything that is not as it has to be, which is nothing changed to find out (but for a schema that is made and dropped to see
     * whether the database user may, when the rest is as it has to be).
     */
    public List<String> problems(ReleasePackageFormat format, Info info, ScriptSource scripts, long delta, ReleasePackageLoadJobId job) {
        ReleasePackageLoadQueryRepository query = repositories.query();
        String served = query.currentSchema();
        List<String> problems = new ArrayList<>();

        problems.addAll(format.problemsOf(info, scripts));
        warnOfTheNumbering(format, query, served, delta);
        problems.addAll(problemsOfTheReleases(query, served, info));
        if (new TransactionTemplate(transactionManager).execute(status -> repositoryFactory.releaseProgressRepository().anyLibraryIsBusy())) {
            problems.add(ReleasePackageLoadMessages.BUSY_RELEASE);
        }
        if (problems.isEmpty()) {
            // The last, since it makes a schema (and drops it): whether the user of the database is allowed to.
            problems.addAll(problemsOfThePrivileges(served, job));
        }
        return problems;
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

    private List<String> problemsOfTheReleases(ReleasePackageLoadQueryRepository query, String served, Info info) {
        // The package names the other releases than its own (the one it adds, which a database that has it already has too).
        Set<String> known = Stream.concat(info.getMigration().getMetadata().getReleases().stream(), Stream.of(info.getMigration().getRelease()))
                .map(release -> String.valueOf(release.getId())).collect(Collectors.toSet());
        List<String> unknown = query.releases(served).entrySet().stream()
                .filter(release -> !known.contains(release.getKey().toString()))
                .map(release -> release.getValue()).toList();
        return unknown.isEmpty() ? List.of() : List.of(ReleasePackageLoadMessages.unknownReleases(unknown));
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
