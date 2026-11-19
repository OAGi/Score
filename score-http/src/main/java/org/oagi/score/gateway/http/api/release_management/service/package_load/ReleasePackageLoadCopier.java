package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.service.package_load.format.CopyKind;
import org.oagi.score.gateway.http.api.release_management.service.package_load.format.CopyStep;
import org.oagi.score.gateway.http.api.release_management.service.package_load.format.ReleasePackageFormat;

import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Metadata;
import org.oagi.score.gateway.http.api.release_management.repository.ColumnReference;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadQueryRepository;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadRepositories;
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
public class ReleasePackageLoadCopier {

    private final ReleasePackageLoadRepositories repositories;

    public ReleasePackageLoadCopier(ReleasePackageLoadRepositories repositories) {
        this.repositories = repositories;
    }

    /** What is to be copied: the steps of the plan of the format that copy, with a table of the package decided by whether the package gave it. */
    public List<CopyStep> stepsToCopy(ReleasePackageFormat format, String served, String staging) {
        ReleasePackageLoadQueryRepository query = repositories.query();
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

    private static final String USERS = "app_user";

    /** The keys of the JSON of the log that hold the number of an account. */
    private static final List<String> USER_KEYS = List.of("app_user_id", "created_by", "last_updated_by", "owner_user_id", "sender_id", "recipient_id");

    /**
     * Copies what the users made, as {@link #stepsToCopy} says. What an instance numbered below {@code delta} (the end users made
     * before the numbering of the instance was set, and what they made) has is numbered above everything the instance has, as
     * the data-loader does, and the numbers that refer to it are changed with it: in the copied records only, since the
     * package's own records (the developers' accounts among them) refer to numbers of their own.
     */
    public void copy(String served, String staging, List<CopyStep> steps, long delta, Info info, Consumer<String> progress) {
        ReleasePackageLoadCommandRepository command = repositories.command();
        ReleasePackageLoadQueryRepository query = repositories.query();
        BigInteger limit = BigInteger.valueOf(delta);
        command.createIdMap(staging);
        try {
            Map<String, Map<BigInteger, BigInteger>> renumbered = new LinkedHashMap<>();
            for (CopyStep step : steps) {
                String key = renumberedKey(step);
                if (key == null) {
                    continue;
                }
                List<BigInteger> ids = query.idsBetween(served, step.table(), key, lowest(step, info, delta), limit, USERS.equals(step.table()));
                if (ids.isEmpty()) {
                    continue;
                }
                BigInteger next = limit.subtract(BigInteger.ONE)
                        .max(query.maxId(served, step.table(), key).orElse(BigInteger.ZERO))
                        .max(query.maxId(staging, step.table(), key).orElse(BigInteger.ZERO));
                Map<BigInteger, BigInteger> map = new LinkedHashMap<>();
                for (BigInteger id : ids) {
                    next = next.add(BigInteger.ONE);
                    map.put(id, next);
                }
                command.putIdMap(staging, step.table(), map);
                renumbered.put(step.table(), map);
            }
            Map<String, Map<String, String>> mappedColumns = new LinkedHashMap<>();
            renumbered.forEach((table, map) -> {
                mappedColumns.computeIfAbsent(table, t -> new LinkedHashMap<>()).put(table + "_id", table);
                for (ColumnReference reference : query.columnsReferencing(served, table, table + "_id")) {
                    mappedColumns.computeIfAbsent(reference.table(), t -> new LinkedHashMap<>()).put(reference.column(), table);
                }
            });

            for (CopyStep step : steps) {
                progress.accept("Copying " + step.table());
                Map<String, String> columns = mappedColumns.getOrDefault(step.table(), Map.of());
                if (step.kind() == CopyKind.FROM_DELTA) {
                    if (step.ownBelow() != null) {
                        // What the package does not carry and the instance has of its own is not what the migrations seeded.
                        command.removeRecordsBelow(staging, step.table(), step.idColumn(), step.ownBelow());
                        command.copyRecordsBelow(served, staging, step.table(), step.idColumn(), step.ownBelow());
                    }
                    if (USERS.equals(step.table())) {
                        command.copyRecordsRenumbered(served, staging, step.table(), "s.`app_user_id` >= " + delta
                                + " OR (s.`is_developer` = 0 AND s.`app_user_id` >= " + step.ownBelow() + ")", columns);
                    } else if (columns.isEmpty()) {
                        command.copyRecords(served, staging, step.table(), step.idColumn(), lowest(step, info, delta).add(BigInteger.ONE));
                    } else {
                        command.copyRecordsRenumbered(served, staging, step.table(), "s.`" + step.idColumn() + "` > " + lowest(step, info, delta), columns);
                    }
                } else {
                    // What the schema has is what the staging schema made of its own (the seeds of its migrations); the users' is what counts.
                    command.emptyTable(staging, step.table());
                    if (columns.isEmpty()) {
                        command.copyRecords(served, staging, step.table(), null, null);
                    } else {
                        command.copyRecordsRenumbered(served, staging, step.table(), null, columns);
                    }
                }
                command.copyAutoIncrement(served, staging, step.table());
                if (renumbered.containsKey(step.table())) {
                    command.raiseAutoIncrement(staging, step.table(), step.table() + "_id");
                }
            }

            Map<BigInteger, BigInteger> users = renumbered.get(USERS);
            if (users != null && steps.stream().anyMatch(step -> "log".equals(step.table()))) {
                // The snapshots of the log say who made what: of the records that were copied, as the data-loader does.
                CopyStep log = steps.stream().filter(step -> "log".equals(step.table())).findFirst().orElseThrow();
                command.renumberInJson(staging, "log", "snapshot", "`log_id` > " + lowest(log, info, delta), USER_KEYS, users);
            }
        } finally {
            command.dropIdMap(staging);
        }
    }

    /** The column that numbers the records of a table whose records below {@code delta} are renumbered, or {@code null} when they are not. */
    private static String renumberedKey(CopyStep step) {
        if (step.kind() != CopyKind.FROM_DELTA || step.idColumn() == null || !step.idColumn().equals(step.table() + "_id")) {
            return null;
        }
        return step.idColumn();
    }

    /**
     * The number above which the records of the table are the users': the highest the package has of it, for a table the package says
     * (the data-loader moves what is above it and below {@code delta}), otherwise the one before {@code delta}. The accounts are the
     * users' from the first that is not the instance's own, whatever number the package's accounts go up to: it tells them by what they are.
     */
    private static BigInteger lowest(CopyStep step, Info info, long delta) {
        BigInteger beforeDelta = BigInteger.valueOf(delta - 1);
        if (USERS.equals(step.table())) {
            return step.ownBelow() != null ? step.ownBelow().subtract(BigInteger.ONE) : beforeDelta;
        }
        String table = step.idColumn() != null && step.idColumn().endsWith("_id")
                ? step.idColumn().substring(0, step.idColumn().length() - 3) : step.table();
        try {
            Metadata metadata = info.getMigration().getMetadata();
            return BigInteger.valueOf(metadata.getTable(table).getMaxId()).min(beforeDelta);
        } catch (IllegalArgumentException | NullPointerException notSaid) {
            return beforeDelta;
        }
    }

    /** What tells, of each table that is copied, whether it was written to: read before the copy and again before the swap. */
    public Map<String, String> fingerprints(String served, List<CopyStep> steps) {
        ReleasePackageLoadQueryRepository query = repositories.query();
        Map<String, String> fingerprints = new LinkedHashMap<>();
        for (CopyStep step : steps) {
            fingerprints.put(step.table(), query.fingerprint(served, step.table()));
        }
        return fingerprints;
    }

    /** The tables that are not as many in the staging schema as they are in the served one. */
    public List<String> missing(String served, String staging, List<CopyStep> steps, long delta, Info info) {
        ReleasePackageLoadQueryRepository query = repositories.query();
        List<String> missing = new ArrayList<>();
        BigInteger limit = BigInteger.valueOf(delta);
        for (CopyStep step : steps) {
            boolean fromDelta = step.kind() == CopyKind.FROM_DELTA;
            boolean users = USERS.equals(step.table());
            // The accounts of the instance numbered below delta are in the staging schema from delta on.
            BigInteger thereFrom = fromDelta ? (users ? limit : lowest(step, info, delta).add(BigInteger.ONE)) : null;
            BigInteger hereFrom = fromDelta ? (users ? limit : thereFrom) : null;
            String column = fromDelta ? step.idColumn() : null;
            long there = query.countRecords(served, step.table(), column, thereFrom);
            long here = query.countRecords(staging, step.table(), column, hereFrom);
            if (users) {
                there += query.idsBetween(served, step.table(), column, lowest(step, info, delta), limit, true).size();
            }
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
