package org.oagi.score.gateway.http.api.release_management.service.migrationload;

import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.CopyKind;
import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.CopyStep;
import org.oagi.score.gateway.http.api.release_management.service.migrationload.format.ReleasePackageFormat;

import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadQueryRepository;
import org.oagi.score.gateway.http.api.release_management.repository.MigrationLoadRepositories;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Copies what the users made from the schema the application serves into the schema the package was loaded into, as
 * {@link ReleasePackageTables} says, and finds out whether it was all copied.
 */
@Component
public class MigrationLoadCopier {

    private final MigrationLoadRepositories repositories;

    public MigrationLoadCopier(MigrationLoadRepositories repositories) {
        this.repositories = repositories;
    }

    /** What is to be copied: the steps of the plan of the format that copy, with a table of the package decided by whether the package gave it. */
    public List<CopyStep> stepsToCopy(ReleasePackageFormat format, String served, String staging) {
        MigrationLoadQueryRepository query = repositories.query();
        List<CopyStep> steps = new ArrayList<>();
        for (CopyStep step : format.copyPlan(query.tables(served), query.tables(staging))) {
            switch (step.kind()) {
                case FROM_DELTA, ALL -> steps.add(step);
                case OF_PACKAGE_IF_GIVEN -> {
                    if (query.countRecords(staging, step.table(), null, null) == 0) {
                        steps.add(new CopyStep(step.table(), CopyKind.ALL, null, null));
                    }
                }
                default -> {
                }
            }
        }
        return steps;
    }

    public void copy(String served, String staging, List<CopyStep> steps, long delta, Consumer<String> progress) {
        MigrationLoadCommandRepository command = repositories.command();
        for (CopyStep step : steps) {
            progress.accept("Copying " + step.table());
            if (step.kind() == CopyKind.FROM_DELTA) {
                if (step.ownBelow() != null) {
                    // What the package does not carry and the instance has of its own is not what the migrations seeded.
                    command.removeRecordsBelow(staging, step.table(), step.idColumn(), step.ownBelow());
                    command.copyRecordsBelow(served, staging, step.table(), step.idColumn(), step.ownBelow());
                }
                command.copyRecords(served, staging, step.table(), step.idColumn(), BigInteger.valueOf(delta));
            } else {
                // What the schema has is what the staging schema made of its own (the seeds of its migrations); the users' is what counts.
                command.emptyTable(staging, step.table());
                command.copyRecords(served, staging, step.table(), null, null);
            }
            command.copyAutoIncrement(served, staging, step.table());
        }
    }

    /** What tells, of each table that is copied, whether it was written to: read before the copy and again before the swap. */
    public Map<String, String> fingerprints(String served, List<CopyStep> steps) {
        MigrationLoadQueryRepository query = repositories.query();
        Map<String, String> fingerprints = new LinkedHashMap<>();
        for (CopyStep step : steps) {
            fingerprints.put(step.table(), query.fingerprint(served, step.table()));
        }
        return fingerprints;
    }

    /** The tables that are not as many in the staging schema as they are in the served one. */
    public List<String> missing(String served, String staging, List<CopyStep> steps, long delta) {
        MigrationLoadQueryRepository query = repositories.query();
        List<String> missing = new ArrayList<>();
        for (CopyStep step : steps) {
            BigInteger atLeast = step.kind() == CopyKind.FROM_DELTA ? BigInteger.valueOf(delta) : null;
            String column = step.kind() == CopyKind.FROM_DELTA ? step.idColumn() : null;
            long there = query.countRecords(served, step.table(), column, atLeast);
            long here = query.countRecords(staging, step.table(), column, atLeast);
            if (step.ownBelow() != null) {
                there += query.countRecordsBelow(served, step.table(), step.idColumn(), step.ownBelow());
                here += query.countRecordsBelow(staging, step.table(), step.idColumn(), step.ownBelow());
            }
            if (there != here) {
                missing.add(step.table() + " (" + there + " records, " + here + " copied)");
            }
        }
        return missing;
    }
}
