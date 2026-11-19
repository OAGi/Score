package org.oagi.score.gateway.http.api.release_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.SqlText;
import org.oagi.score.gateway.http.api.release_management.repository.ColumnReference;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class JooqReleasePackageLoadQueryRepository extends JooqBaseRepository implements ReleasePackageLoadQueryRepository {

    /**
     * What of a schema the application depends on, by the kind of it: a query that gives one line for each thing, from the
     * catalog of the server, with the schema as its one parameter. What names the schema itself (a foreign key to a table of
     * the schema) is left out of the line, so that two schemas of different names can be the same.
     */
    private static final Map<String, String> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put("table", "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = ? AND table_type = 'BASE TABLE'");
        CATALOG.put("view", "SELECT table_name FROM information_schema.views WHERE table_schema = ?");
        CATALOG.put("column", "SELECT CONCAT_WS('|', table_name, column_name, column_type, is_nullable, "
                + "IFNULL(column_default, '<null>'), extra, IFNULL(collation_name, '')) "
                + "FROM information_schema.columns WHERE table_schema = ?");
        CATALOG.put("index", "SELECT CONCAT_WS('|', table_name, index_name, seq_in_index, column_name, non_unique, "
                + "IFNULL(sub_part, ''), index_type) FROM information_schema.statistics WHERE table_schema = ?");
        CATALOG.put("foreign key", "SELECT CONCAT_WS('|', k.table_name, k.constraint_name, k.ordinal_position, "
                + "k.column_name, IF(k.referenced_table_schema = k.table_schema, '', k.referenced_table_schema), "
                + "k.referenced_table_name, k.referenced_column_name, r.update_rule, r.delete_rule) "
                + "FROM information_schema.key_column_usage k "
                + "JOIN information_schema.referential_constraints r ON r.constraint_schema = k.constraint_schema "
                + "AND r.table_name = k.table_name AND r.constraint_name = k.constraint_name "
                + "WHERE k.table_schema = ? AND k.referenced_table_name IS NOT NULL");
        CATALOG.put("check", "SELECT CONCAT_WS('|', table_name, constraint_name, check_clause) "
                + "FROM information_schema.check_constraints WHERE constraint_schema = ?");
        CATALOG.put("trigger", "SELECT CONCAT_WS('|', trigger_name, event_object_table, action_timing, "
                + "event_manipulation, action_statement) FROM information_schema.triggers WHERE trigger_schema = ?");
        CATALOG.put("routine", "SELECT CONCAT_WS('|', routine_name, routine_type, routine_definition) "
                + "FROM information_schema.routines WHERE routine_schema = ?");
    }

    public JooqReleasePackageLoadQueryRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public String currentSchema() {
        return dslContext().resultQuery("SELECT DATABASE()").fetchOne(0, String.class);
    }

    @Override
    public boolean schemaExists(String schema) {
        return dslContext().resultQuery(
                "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?", schema)
                .fetchOne(0, Integer.class) > 0;
    }

    @Override
    public List<String> differences(String schema, String otherSchema) {
        List<String> differences = new ArrayList<>();
        for (Map.Entry<String, String> kind : CATALOG.entrySet()) {
            // The body of a trigger or of a routine is what its maker kept: the comments and the white space are not what it does.
            boolean program = kind.getKey().equals("trigger") || kind.getKey().equals("routine");
            Set<String> inSchema = lines(kind.getValue(), schema, program);
            Set<String> inOther = lines(kind.getValue(), otherSchema, program);
            for (String line : inSchema) {
                if (!inOther.contains(line)) {
                    differences.add("only in " + schema + ": " + kind.getKey() + " " + line);
                }
            }
            for (String line : inOther) {
                if (!inSchema.contains(line)) {
                    differences.add("only in " + otherSchema + ": " + kind.getKey() + " " + line);
                }
            }
        }
        return differences;
    }

    private Set<String> lines(String query, String schema, boolean normalized) {
        return new TreeSet<>(dslContext().resultQuery(query, schema).fetch(record -> {
            String line = record.get(0, String.class);
            return normalized ? SqlText.normalize(line) : line;
        }));
    }

    @Override
    public List<String> tables(String schema) {
        return dslContext().resultQuery("SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name", schema)
                .fetch(record -> record.get(0, String.class));
    }

    @Override
    public Optional<BigInteger> autoIncrement(String schema, String table) {
        return dslContext().resultQuery("SELECT auto_increment FROM information_schema.tables "
                        + "WHERE table_schema = ? AND table_name = ?", schema, table)
                .fetchOptional(record -> record.get(0, BigInteger.class));
    }

    @Override
    public long countRecords(String schema, String table, String idColumn, BigInteger atLeast) {
        String name = "`" + name(schema) + "`.`" + name(table) + "`";
        if (idColumn != null && atLeast != null) {
            return dslContext().resultQuery("SELECT COUNT(*) FROM " + name + " WHERE `" + name(idColumn) + "` >= ?", atLeast)
                    .fetchOne(0, Long.class);
        }
        return dslContext().resultQuery("SELECT COUNT(*) FROM " + name).fetchOne(0, Long.class);
    }

    @Override
    public long countRecordsBelow(String schema, String table, String idColumn, BigInteger below) {
        return dslContext().resultQuery("SELECT COUNT(*) FROM `" + name(schema) + "`.`" + name(table) + "` WHERE `" + name(idColumn) + "` < ?", below)
                .fetchOne(0, Long.class);
    }

    @Override
    public String fingerprint(String schema, String table) {
        String name = "`" + name(schema) + "`.`" + name(table) + "`";
        boolean hasTime = dslContext().resultQuery("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema = ? AND table_name = ? AND column_name = 'last_update_timestamp'", schema, table)
                .fetchOne(0, Integer.class) > 0;
        return dslContext().resultQuery("SELECT CONCAT(COUNT(*), '|', "
                        + (hasTime ? "IFNULL(MAX(`last_update_timestamp`), '')" : "''") + ") FROM " + name)
                .fetchOne(0, String.class);
    }

    @Override
    public Map<BigInteger, String> releases(String schema) {
        Map<BigInteger, String> releases = new LinkedHashMap<>();
        dslContext().resultQuery("SELECT release_id, release_num FROM `" + name(schema) + "`.`release` ORDER BY release_id")
                .forEach(record -> releases.put(record.get(0, BigInteger.class), record.get(1, String.class)));
        return releases;
    }

    @Override
    public List<String> foreignKeyViolations(String schema) {
        // One row for each column of each foreign key, in the order of the key: the key, its table, and where it goes.
        Map<String, List<String[]>> keys = new LinkedHashMap<>();
        dslContext().resultQuery("SELECT constraint_name, table_name, column_name, referenced_table_name, referenced_column_name "
                        + "FROM information_schema.key_column_usage WHERE table_schema = ? AND referenced_table_schema = ? "
                        + "AND referenced_table_name IS NOT NULL ORDER BY table_name, constraint_name, ordinal_position", schema, schema)
                .forEach(record -> keys.computeIfAbsent(record.get(1, String.class) + "." + record.get(0, String.class), k -> new ArrayList<>())
                        .add(new String[]{record.get(1, String.class), record.get(2, String.class),
                                record.get(3, String.class), record.get(4, String.class)}));
        List<String> violations = new ArrayList<>();
        for (Map.Entry<String, List<String[]>> key : keys.entrySet()) {
            List<String[]> columns = key.getValue();
            String child = "`" + name(schema) + "`.`" + name(columns.get(0)[0]) + "`";
            String parent = "`" + name(schema) + "`.`" + name(columns.get(0)[2]) + "`";
            StringBuilder present = new StringBuilder();
            StringBuilder match = new StringBuilder();
            for (String[] column : columns) {
                present.append(present.length() == 0 ? "" : " AND ").append("c.`").append(name(column[1])).append("` IS NOT NULL");
                match.append(match.length() == 0 ? "" : " AND ").append("p.`").append(name(column[3])).append("` = c.`").append(name(column[1])).append("`");
            }
            long broken = dslContext().resultQuery("SELECT COUNT(*) FROM " + child + " c WHERE " + present
                    + " AND NOT EXISTS (SELECT 1 FROM " + parent + " p WHERE " + match + ")").fetchOne(0, Long.class);
            if (broken > 0) {
                violations.add(key.getKey() + ": " + broken + " record(s) refer to a record of " + columns.get(0)[2] + " that is not there");
            }
        }
        return violations;
    }

    @Override
    public List<ColumnReference> columnsReferencing(String schema, String table, String column) {
        // A key of more than one column is told by a constraint that names more than one column: only the single ones are taken.
        List<ColumnReference> columns = new ArrayList<>();
        dslContext().resultQuery("SELECT k.table_name, k.column_name FROM information_schema.key_column_usage k "
                        + "WHERE k.referenced_table_schema = ? AND k.referenced_table_name = ? AND k.referenced_column_name = ? "
                        + "AND k.table_schema = ? AND (SELECT COUNT(*) FROM information_schema.key_column_usage o "
                        + "WHERE o.constraint_schema = k.constraint_schema AND o.constraint_name = k.constraint_name "
                        + "AND o.table_name = k.table_name) = 1 ORDER BY k.table_name, k.column_name", schema, table, column, schema)
                .forEach(record -> columns.add(new ColumnReference(record.get(0, String.class), record.get(1, String.class))));
        return columns;
    }

    @Override
    public Optional<BigInteger> maxId(String schema, String table, String idColumn) {
        return Optional.ofNullable(dslContext().resultQuery("SELECT MAX(`" + name(idColumn) + "`) FROM `" + name(schema) + "`.`" + name(table) + "`")
                .fetchOne(0, BigInteger.class));
    }

    @Override
    public List<BigInteger> idsBetween(String schema, String table, String idColumn, BigInteger above, BigInteger below, boolean withoutDevelopers) {
        String column = "`" + name(idColumn) + "`";
        return dslContext().resultQuery("SELECT " + column + " FROM `" + name(schema) + "`.`" + name(table) + "` WHERE " + column + " > ? AND "
                        + column + " < ?" + (withoutDevelopers ? " AND `is_developer` = 0" : "") + " ORDER BY " + column, above, below)
                .fetch(0, BigInteger.class);
    }

    /** The names of the schemas and the tables are put into statements, so they are of what needs no quoting. */
    private static String name(String name) {
        if (name == null || !name.matches("[A-Za-z0-9_]{1,64}")) {
            throw new IllegalArgumentException("Not a name of a schema, a table or a column: " + name);
        }
        return name;
    }
}
