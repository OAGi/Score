package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.oagi.score.gateway.http.api.release_management.dataload.info.Info;
import org.oagi.score.gateway.http.api.release_management.dataload.script.ScriptSource;
import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.ReleasePackageFormat;
import org.oagi.score.gateway.http.api.release_management.model.MigrationLoadJobId;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadQueryRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadRepositories;
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
public class MigrationLoadPreflight {

    private final RepositoryFactory repositoryFactory;
    private final MigrationLoadRepositories repositories;
    private final PlatformTransactionManager transactionManager;

    public MigrationLoadPreflight(RepositoryFactory repositoryFactory, MigrationLoadRepositories repositories,
                                  PlatformTransactionManager transactionManager) {
        this.repositoryFactory = repositoryFactory;
        this.repositories = repositories;
        this.transactionManager = transactionManager;
    }

    /** @throws MigrationLoadRefusedException with everything that is not as it has to be */
    public void check(ReleasePackageFormat format, Info info, ScriptSource scripts, long delta, MigrationLoadJobId job) {
        MigrationLoadQueryRepository query = repositories.query();
        String served = query.currentSchema();
        List<String> problems = new ArrayList<>();

        problems.addAll(format.problemsOf(info, scripts));
        problems.addAll(problemsOfTheNumbering(format, query, served, delta));
        problems.addAll(problemsOfTheReleases(query, served, info));
        if (new TransactionTemplate(transactionManager).execute(status -> repositoryFactory.releaseProgressRepository().anyLibraryIsBusy())) {
            problems.add("a release is being drafted or published, which is to end before a package is loaded");
        }
        if (problems.isEmpty()) {
            // The last, since it makes a schema (and drops it): whether the user of the database is allowed to.
            problems.addAll(problemsOfThePrivileges(served, job));
        }
        if (!problems.isEmpty()) {
            throw new MigrationLoadRefusedException("The package is not loaded: " + String.join("; ", problems) + ".");
        }
    }

    /**
     * The records of the users are told by being numbered from {@code delta}; where the numbering of a table is not there yet, a
     * record that was made in the numbers of the developers could be taken for the developers', and be lost.
     */
    private List<String> problemsOfTheNumbering(ReleasePackageFormat format, MigrationLoadQueryRepository query, String served, long delta) {
        List<String> below = new ArrayList<>();
        for (String table : query.tables(served).stream().filter(format.numberedFromDelta()::contains).toList()) {
            BigInteger next = query.autoIncrement(served, table).orElse(null);
            if (next == null || next.compareTo(BigInteger.valueOf(delta)) < 0) {
                below.add(table);
            }
        }
        return below.isEmpty() ? List.of() : List.of("records of the users may be numbered among those of the developers in "
                + below + " (their numbering is below " + delta + "); load a package of this instance with the data-loader first");
    }

    private List<String> problemsOfTheReleases(MigrationLoadQueryRepository query, String served, Info info) {
        // The package names the other releases than its own (the one it adds, which a database that has it already has too).
        Set<String> known = Stream.concat(info.getMigration().getMetadata().getReleases().stream(), Stream.of(info.getMigration().getRelease()))
                .map(release -> String.valueOf(release.getId())).collect(Collectors.toSet());
        List<String> unknown = query.releases(served).entrySet().stream()
                .filter(release -> !known.contains(release.getKey().toString()))
                .map(release -> release.getValue()).toList();
        return unknown.isEmpty() ? List.of() : List.of("this database has releases the package does not know of: " + unknown);
    }

    private List<String> problemsOfThePrivileges(String served, MigrationLoadJobId job) {
        MigrationLoadCommandRepository command = repositories.command();
        String probe = MigrationLoadNames.probe(served, job);
        try {
            command.createSchema(probe);
        } catch (RuntimeException e) {
            return List.of("the database user of the application cannot make a schema named '" + probe + "' ("
                    + e.getMessage() + "); it needs all privileges on the schemas whose name starts with '" + served + "'");
        }
        try {
            command.dropSchema(probe);
            return List.of();
        } catch (RuntimeException e) {
            return List.of("the database user of the application made the schema '" + probe + "' but cannot drop it: " + e.getMessage());
        }
    }
}
