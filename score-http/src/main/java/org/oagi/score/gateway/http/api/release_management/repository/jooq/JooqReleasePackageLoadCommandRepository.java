package org.oagi.score.gateway.http.api.release_management.repository.jooq;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadProgress;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoader;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.SqlDefiner;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ScriptSource;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadCommandRepository;
import org.oagi.score.gateway.http.api.release_management.repository.SchemaMigrator;
import org.oagi.score.gateway.http.api.release_management.repository.SchemaSwapException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JooqReleasePackageLoadCommandRepository extends JooqBaseRepository implements ReleasePackageLoadCommandRepository {

    /** The names of the schemas and the tables are put into statements, so they are of what needs no quoting. */
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]{1,64}");

    private record Trigger(String name, String sqlMode, String ddl) {
    }

    private record Routine(String name, String type, String sqlMode, String ddl) {
    }

    private final SchemaMigrator schemaMigrator;

    /** @param schemaMigrator what brings a schema to the shape of a version */
    public JooqReleasePackageLoadCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory,
                                              SchemaMigrator schemaMigrator) {
        super(dslContext, requester, repositoryFactory);
        this.schemaMigrator = schemaMigrator;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Making, migrating, loading
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public void createSchema(String schema) {
        identifier(schema);
        inConnection(con -> {
            if (exists(con, schema)) {
                throw new IllegalStateException("The schema '" + schema + "' exists.");
            }
            String characterSet;
            String collation;
            try (Statement statement = con.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT default_character_set_name, default_collation_name "
                         + "FROM information_schema.schemata WHERE schema_name = DATABASE()")) {
                rs.next();
                characterSet = rs.getString(1);
                collation = rs.getString(2);
            }
            execute(con, "CREATE DATABASE `" + schema + "` CHARACTER SET " + characterSet + " COLLATE " + collation);
            return null;
        });
    }

    @Override
    public void dropSchema(String schema) {
        identifier(schema);
        inConnection(con -> {
            if (schema.equals(con.getCatalog())) {
                throw new IllegalArgumentException("The schema the application serves is not dropped: " + schema);
            }
            execute(con, "DROP DATABASE IF EXISTS `" + schema + "`");
            return null;
        });
    }

    @Override
    public void migrateSchema(String schema, String targetVersion) {
        schemaMigrator.migrate(identifier(schema), targetVersion);
    }

    @Override
    public void load(String schema, Info info, ScriptSource scripts, long delta, ReleasePackageLoadProgress progress, UnaryOperator<String> statements) {
        identifier(schema);
        inConnection(con -> {
            String original = con.getCatalog();
            con.setCatalog(schema);
            try {
                new ReleasePackageLoader(delta, scripts, progress, statements).run(con, info);
            } catch (SQLException | RuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                con.setCatalog(original);
            }
            return null;
        });
    }

    @Override
    public int copyRecords(String fromSchema, String toSchema, String table, String idColumn, BigInteger atLeast) {
        if (idColumn != null) {
            identifier(idColumn);
        }
        return copy(fromSchema, toSchema, table, idColumn != null && atLeast != null ? "`" + idColumn + "` >= ?" : null, atLeast);
    }

    @Override
    public int copyRecordsBelow(String fromSchema, String toSchema, String table, String idColumn, BigInteger below) {
        return copy(fromSchema, toSchema, table, "`" + identifier(idColumn) + "` < ?", below);
    }

    /** Copies the records that the condition (which has one parameter, or none) tells, the columns the two tables have in common. */
    private int copy(String fromSchema, String toSchema, String table, String condition, BigInteger parameter) {
        identifier(fromSchema);
        identifier(toSchema);
        identifier(table);
        return inConnection(con -> {
            List<String> columns = commonColumns(con, fromSchema, toSchema, table);
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("The table '" + table + "' is not in both '" + fromSchema + "' and '" + toSchema + "'.");
            }
            String list = columns.stream().map(column -> "`" + column + "`").collect(Collectors.joining(", "));
            String sql = "INSERT INTO `" + toSchema + "`.`" + table + "` (" + list + ") SELECT " + list
                    + " FROM `" + fromSchema + "`.`" + table + "`" + (condition != null ? " WHERE " + condition : "");
            String foreignKeyChecks = single(con, "SELECT @@foreign_key_checks");
            String sqlMode = single(con, "SELECT @@sql_mode");
            execute(con, "SET foreign_key_checks = 0");
            // A record numbered 0 (the system's account is) is given its number, not a new one, as a script of a package says too.
            execute(con, "SET sql_mode = CONCAT_WS(',', NULLIF(@@sql_mode, ''), 'NO_AUTO_VALUE_ON_ZERO')");
            // What is copied is not a change: the triggers that write what changed (for the cache) have nothing to say.
            execute(con, "SET @disable_record_change_log = 1");
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                if (condition != null) {
                    statement.setObject(1, parameter);
                }
                return statement.executeUpdate();
            } finally {
                execute(con, "SET @disable_record_change_log = NULL");
                try (PreparedStatement mode = con.prepareStatement("SET sql_mode = ?")) {
                    mode.setString(1, sqlMode);
                    mode.execute();
                }
                execute(con, "SET foreign_key_checks = " + foreignKeyChecks);
            }
        });
    }

    private static final String ID_MAP = "release_package_load_id_map";

    @Override
    public void createIdMap(String schema) {
        identifier(schema);
        inConnection(con -> {
            execute(con, "DROP TABLE IF EXISTS `" + schema + "`.`" + ID_MAP + "`");
            execute(con, "CREATE TABLE `" + schema + "`.`" + ID_MAP + "` (map_table VARCHAR(64) NOT NULL, map_old BIGINT UNSIGNED NOT NULL, "
                    + "map_new BIGINT UNSIGNED NOT NULL, PRIMARY KEY (map_table, map_old))");
            return null;
        });
    }

    @Override
    public void putIdMap(String schema, String table, Map<BigInteger, BigInteger> ids) {
        identifier(schema);
        identifier(table);
        inConnection(con -> {
            try (PreparedStatement statement = con.prepareStatement("INSERT INTO `" + schema + "`.`" + ID_MAP + "` (map_table, map_old, map_new) VALUES (?, ?, ?)")) {
                for (Map.Entry<BigInteger, BigInteger> entry : ids.entrySet()) {
                    statement.setString(1, table);
                    statement.setObject(2, entry.getKey());
                    statement.setObject(3, entry.getValue());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            return null;
        });
    }

    @Override
    public void dropIdMap(String schema) {
        identifier(schema);
        inConnection(con -> {
            execute(con, "DROP TABLE IF EXISTS `" + schema + "`.`" + ID_MAP + "`");
            return null;
        });
    }

    @Override
    public int copyRecordsRenumbered(String fromSchema, String toSchema, String table, String condition, Map<String, String> mappedColumns) {
        identifier(fromSchema);
        identifier(toSchema);
        identifier(table);
        return inConnection(con -> {
            List<String> columns = commonColumns(con, fromSchema, toSchema, table);
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("The table '" + table + "' is not in both '" + fromSchema + "' and '" + toSchema + "'.");
            }
            StringBuilder insert = new StringBuilder();
            StringBuilder select = new StringBuilder();
            StringBuilder joins = new StringBuilder();
            int n = 0;
            for (String column : columns) {
                identifier(column);
                insert.append(insert.length() == 0 ? "" : ", ").append('`').append(column).append('`');
                String mapped = mappedColumns.get(column);
                String value = "s.`" + column + "`";
                if (mapped != null) {
                    String alias = "m" + (n++);
                    joins.append(" LEFT JOIN `").append(toSchema).append("`.`").append(ID_MAP).append("` ").append(alias)
                            .append(" ON ").append(alias).append(".map_table = '").append(identifier(mapped)).append("' AND ")
                            .append(alias).append(".map_old = ").append(value);
                    value = "COALESCE(" + alias + ".map_new, " + value + ")";
                }
                select.append(select.length() == 0 ? "" : ", ").append(value);
            }
            String sql = "INSERT INTO `" + toSchema + "`.`" + table + "` (" + insert + ") SELECT " + select
                    + " FROM `" + fromSchema + "`.`" + table + "` s" + joins + (condition != null ? " WHERE " + condition : "");
            String foreignKeyChecks = single(con, "SELECT @@foreign_key_checks");
            String sqlMode = single(con, "SELECT @@sql_mode");
            execute(con, "SET foreign_key_checks = 0");
            execute(con, "SET sql_mode = CONCAT_WS(',', NULLIF(@@sql_mode, ''), 'NO_AUTO_VALUE_ON_ZERO')");
            execute(con, "SET @disable_record_change_log = 1");
            try (Statement statement = con.createStatement()) {
                return statement.executeUpdate(sql);
            } finally {
                execute(con, "SET @disable_record_change_log = NULL");
                try (PreparedStatement mode = con.prepareStatement("SET sql_mode = ?")) {
                    mode.setString(1, sqlMode);
                    mode.execute();
                }
                execute(con, "SET foreign_key_checks = " + foreignKeyChecks);
            }
        });
    }

    @Override
    public void renumberInJson(String schema, String table, String column, String condition, Collection<String> keys, Map<BigInteger, BigInteger> ids) {
        identifier(schema);
        identifier(table);
        identifier(column);
        inConnection(con -> {
            execute(con, "SET @disable_record_change_log = 1");
            try {
                for (String key : keys) {
                    identifier(key);
                    for (Map.Entry<BigInteger, BigInteger> entry : ids.entrySet()) {
                        // The number is taken whole: 5 is not the start of 55. \1 and \2 are the groups the pattern keeps.
                        String pattern = "(\"" + key + "\": )" + entry.getKey() + "([^0-9])";
                        try (PreparedStatement statement = con.prepareStatement("UPDATE `" + schema + "`.`" + table + "` SET `" + column
                                + "` = REGEXP_REPLACE(`" + column + "`, ?, ?) WHERE `" + column + "` LIKE ?"
                                + (condition != null ? " AND " + condition : ""))) {
                            statement.setString(1, pattern);
                            statement.setString(2, "\\1" + entry.getValue() + "\\2");
                            statement.setString(3, "%\"" + key + "\": " + entry.getKey() + "%");
                            statement.executeUpdate();
                        }
                    }
                }
            } finally {
                execute(con, "SET @disable_record_change_log = NULL");
            }
            return null;
        });
    }

    @Override
    public void raiseAutoIncrement(String schema, String table, String idColumn) {
        identifier(schema);
        identifier(table);
        identifier(idColumn);
        inConnection(con -> {
            String max = single(con, "SELECT MAX(`" + idColumn + "`) FROM `" + schema + "`.`" + table + "`");
            String next = single(con, "SELECT auto_increment FROM information_schema.tables WHERE table_schema = '" + schema + "' AND table_name = '" + table + "'");
            if (max != null && next != null && new BigInteger(max).compareTo(new BigInteger(next).subtract(BigInteger.ONE)) >= 0) {
                execute(con, "ALTER TABLE `" + schema + "`.`" + table + "` AUTO_INCREMENT = " + new BigInteger(max).add(BigInteger.ONE));
            }
            return null;
        });
    }

    @Override
    public int removeRecordsBelow(String schema, String table, String idColumn, BigInteger below) {
        identifier(schema);
        identifier(table);
        identifier(idColumn);
        return inConnection(con -> {
            String foreignKeyChecks = single(con, "SELECT @@foreign_key_checks");
            execute(con, "SET foreign_key_checks = 0");
            execute(con, "SET @disable_record_change_log = 1");
            try (PreparedStatement statement = con.prepareStatement("DELETE FROM `" + schema + "`.`" + table + "` WHERE `" + idColumn + "` < ?")) {
                statement.setObject(1, below);
                return statement.executeUpdate();
            } finally {
                execute(con, "SET @disable_record_change_log = NULL");
                execute(con, "SET foreign_key_checks = " + foreignKeyChecks);
            }
        });
    }

    @Override
    public int emptyTable(String schema, String table) {
        identifier(schema);
        identifier(table);
        return inConnection(con -> {
            String foreignKeyChecks = single(con, "SELECT @@foreign_key_checks");
            execute(con, "SET foreign_key_checks = 0");
            execute(con, "SET @disable_record_change_log = 1");
            try (Statement statement = con.createStatement()) {
                return statement.executeUpdate("DELETE FROM `" + schema + "`.`" + table + "`");
            } finally {
                execute(con, "SET @disable_record_change_log = NULL");
                execute(con, "SET foreign_key_checks = " + foreignKeyChecks);
            }
        });
    }

    @Override
    public void copyAutoIncrement(String fromSchema, String toSchema, String table) {
        identifier(fromSchema);
        identifier(toSchema);
        identifier(table);
        inConnection(con -> {
            String from = single(con, "SELECT auto_increment FROM information_schema.tables WHERE table_schema = '" + fromSchema
                    + "' AND table_name = '" + table + "'");
            String to = single(con, "SELECT auto_increment FROM information_schema.tables WHERE table_schema = '" + toSchema
                    + "' AND table_name = '" + table + "'");
            if (from != null && (to == null || new BigInteger(from).compareTo(new BigInteger(to)) > 0)) {
                execute(con, "ALTER TABLE `" + toSchema + "`.`" + table + "` AUTO_INCREMENT = " + new BigInteger(from));
            }
            return null;
        });
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Swapping
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public SwapOutcome swap(String stagingSchema, String backupSchema, Duration lockWait, Collection<String> staysInPlace) {
        identifier(stagingSchema);
        identifier(backupSchema);
        staysInPlace.forEach(JooqReleasePackageLoadCommandRepository::identifier);
        return inConnection(con -> {
            String live = con.getCatalog();
            requireDistinct(live, stagingSchema, backupSchema);
            requireExists(con, stagingSchema);
            requireExists(con, backupSchema);
            return replaceServed(con, stagingSchema, backupSchema, lockWait, staysInPlace);
        });
    }

    @Override
    public SwapOutcome rollbackSwap(String backupSchema, String discardedSchema, Duration lockWait, Collection<String> staysInPlace) {
        identifier(backupSchema);
        identifier(discardedSchema);
        staysInPlace.forEach(JooqReleasePackageLoadCommandRepository::identifier);
        return inConnection(con -> {
            requireExists(con, backupSchema);
            if (tables(con, backupSchema).isEmpty()) {
                throw new SchemaSwapException("The schema '" + backupSchema + "' does not hold a swap.");
            }
            // The undo is the swap again: what the backup schema holds comes in, and what is served goes to the discarded schema.
            return replaceServed(con, backupSchema, discardedSchema, lockWait, staysInPlace);
        });
    }

    /**
     * The one thing both the swap and its undo are: the tables, the triggers and the routines of the served schema go to
     * {@code outgoing}, an empty schema, and those of {@code incoming} take their place. Each side is kept as it is, so that
     * the schema the served one went to can be swapped back in the same way.
     */
    private SwapOutcome replaceServed(Connection con, String incoming, String outgoing, Duration lockWait,
                                      Collection<String> staysInPlace) throws SQLException {
        String live = con.getCatalog();
        requireDistinct(live, incoming, outgoing);
        requireExists(con, incoming);
        requireExists(con, outgoing);
        if (!tables(con, outgoing).isEmpty()) {
            throw new SchemaSwapException("The schema '" + outgoing + "' that the served tables go to is not empty.");
        }
        List<String> liveTables = tables(con, live);
        List<String> incomingTables = tables(con, incoming);
        liveTables.removeAll(staysInPlace);
        incomingTables.removeAll(staysInPlace);
        requireNoTriggersOn(con, staysInPlace, live, incoming);
        // What only the served schema has is not what the swap is about: it is not moved, and stays where it is.
        List<String> kept = liveTables.stream().filter(table -> !incomingTables.contains(table)).toList();
        liveTables.removeAll(kept);
        requireNoKeysFromMovedTo(con, live, liveTables, kept);
        List<ForeignKey> keptKeys = foreignKeys(con, live, kept, liveTables);

        Side served = new Side(live, liveTables, triggers(con, live, liveTables), routines(con, live));
        Side coming = new Side(incoming, incomingTables, triggers(con, incoming, incomingTables), routines(con, incoming));
        requireNoSchemaNamed(served);
        requireNoSchemaNamed(coming);

        String savedLockWait = single(con, "SELECT @@lock_wait_timeout");
        execute(con, "SET lock_wait_timeout = " + Math.max(1, lockWait.toSeconds()));
        String savedKeyChecks = single(con, "SELECT @@foreign_key_checks");
        try {
            // A table that stays refers to a table that moves by a key that would go with it, so the key is taken off and put on the table that comes.
            execute(con, "SET foreign_key_checks = 0");
            dropForeignKeys(con, live, keptKeys);
            try {
                place(con, served, coming, outgoing, false);
            } catch (SQLException | RuntimeException e) {
                try {
                    addForeignKeys(con, live, keptKeys);
                } catch (SQLException | RuntimeException restoreFailure) {
                    e.addSuppressed(restoreFailure);
                }
                throw e;
            }
            addForeignKeys(con, live, keptKeys);
            return new SwapOutcome(liveTables.size(), coming.triggers().size(), coming.routines().size());
        } finally {
            execute(con, "SET foreign_key_checks = " + savedKeyChecks);
            execute(con, "SET lock_wait_timeout = " + savedLockWait);
        }
    }

    /** A key of a table: what makes {@code columns} of {@code table} refer to {@code parentColumns} of {@code parentTable}. */
    private record ForeignKey(String table, String name, List<String> columns, String parentTable, List<String> parentColumns,
                              String onUpdate, String onDelete) {
    }

    /** The keys of the tables {@code children} that refer to the tables {@code parents} of the same schema. */
    private List<ForeignKey> foreignKeys(Connection con, String schema, Collection<String> children, Collection<String> parents) throws SQLException {
        Map<String, ForeignKey> keys = new java.util.LinkedHashMap<>();
        try (PreparedStatement statement = con.prepareStatement(
                "SELECT k.table_name, k.constraint_name, k.column_name, k.referenced_table_name, k.referenced_column_name, r.update_rule, r.delete_rule "
                        + "FROM information_schema.key_column_usage k JOIN information_schema.referential_constraints r "
                        + "ON r.constraint_schema = k.constraint_schema AND r.table_name = k.table_name AND r.constraint_name = k.constraint_name "
                        + "WHERE k.table_schema = ? AND k.referenced_table_schema = ? ORDER BY k.table_name, k.constraint_name, k.ordinal_position")) {
            statement.setString(1, schema);
            statement.setString(2, schema);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    if (!children.contains(rs.getString(1)) || !parents.contains(rs.getString(4))) {
                        continue;
                    }
                    String id = rs.getString(1) + "." + rs.getString(2);
                    ForeignKey key = keys.get(id);
                    if (key == null) {
                        key = new ForeignKey(rs.getString(1), rs.getString(2), new ArrayList<>(), rs.getString(4), new ArrayList<>(),
                                rs.getString(6), rs.getString(7));
                        keys.put(id, key);
                    }
                    key.columns().add(rs.getString(3));
                    key.parentColumns().add(rs.getString(5));
                }
            }
        }
        return new ArrayList<>(keys.values());
    }

    /** What moves does not refer to what stays: nothing of the package knows the tables that stay, and a key from the moved ones to them would be left behind. */
    private void requireNoKeysFromMovedTo(Connection con, String schema, Collection<String> moved, Collection<String> kept) throws SQLException {
        List<ForeignKey> keys = foreignKeys(con, schema, moved, kept);
        if (!keys.isEmpty()) {
            throw new SchemaSwapException("Tables that move refer to tables that stay in place: "
                    + keys.stream().map(key -> key.table() + "." + key.name()).toList());
        }
    }

    private void dropForeignKeys(Connection con, String schema, List<ForeignKey> keys) throws SQLException {
        for (ForeignKey key : keys) {
            execute(con, "ALTER TABLE `" + schema + "`.`" + key.table() + "` DROP FOREIGN KEY `" + key.name() + "`");
        }
    }

    private void addForeignKeys(Connection con, String schema, List<ForeignKey> keys) throws SQLException {
        for (ForeignKey key : keys) {
            if (!strings(con, "SELECT constraint_name FROM information_schema.referential_constraints WHERE constraint_schema = ? AND table_name = ? AND constraint_name = ?",
                    schema, key.table(), key.name()).isEmpty()) {
                continue;
            }
            execute(con, "ALTER TABLE `" + schema + "`.`" + key.table() + "` ADD CONSTRAINT `" + key.name() + "` FOREIGN KEY ("
                    + quotedList(key.columns()) + ") REFERENCES `" + schema + "`.`" + key.parentTable() + "` (" + quotedList(key.parentColumns())
                    + ")" + rule(" ON UPDATE ", key.onUpdate()) + rule(" ON DELETE ", key.onDelete()));
        }
    }

    /** RESTRICT is what a key does when it says nothing, and said out loud it is kept as NO ACTION: so it is not said. */
    private static String rule(String clause, String rule) {
        return "RESTRICT".equals(rule) ? "" : clause + rule;
    }

    private static String quotedList(List<String> names) {
        return names.stream().map(name -> "`" + name + "`").collect(Collectors.joining(", "));
    }

    /** What a schema holds that moves: its tables, and its triggers and routines, which cannot go with a table but are made again. */
    private record Side(String schema, List<String> tables, List<Trigger> triggers, List<Routine> routines) {
    }

    /**
     * {@code served} goes to {@code outgoing} and {@code coming} goes to the schema of {@code served}. A table that has a
     * trigger cannot be moved to another schema, so the triggers of both are dropped first and made again where their tables
     * are afterwards. Nothing is lost whichever way it stops: a failure before the tables move puts the triggers back (a
     * RENAME is all or nothing), and one after it moves everything back the same way, with the roles turned round.
     */
    private void place(Connection con, Side served, Side coming, String outgoing, boolean undoing) throws SQLException {
        String live = served.schema();
        try {
            dropTriggers(con, live, served.triggers());
            dropTriggers(con, coming.schema(), coming.triggers());
            execute(con, renameStatement(live, outgoing, served.tables(), coming.schema(), live, coming.tables()));
        } catch (SQLException e) {
            try {
                createTriggers(con, live, served.triggers());
                createTriggers(con, coming.schema(), coming.triggers());
            } catch (SQLException | RuntimeException restoreFailure) {
                e.addSuppressed(restoreFailure);
                throw new SchemaSwapException("The swap did not go through, and the triggers could not be put back: " + e.getMessage(), e);
            }
            throw new SchemaSwapException("The swap did not go through, and the schemas are as they were: " + e.getMessage(), e);
        }
        try {
            createTriggers(con, live, coming.triggers());
            createTriggers(con, outgoing, served.triggers());
            setRoutines(con, live, coming.routines());
            setRoutines(con, outgoing, served.routines());
        } catch (SQLException | RuntimeException e) {
            if (undoing) {
                throw new SchemaSwapException("The swap did not go through, and it could not be undone: the tables of the served "
                        + "schema are in '" + outgoing + "': " + e.getMessage(), e);
            }
            try {
                // What is in the served schema now is what came; what went is in 'outgoing'.
                place(con, new Side(live, coming.tables(), coming.triggers(), coming.routines()),
                        new Side(outgoing, served.tables(), served.triggers(), served.routines()), coming.schema(), true);
            } catch (SQLException | RuntimeException undoFailure) {
                e.addSuppressed(undoFailure);
                throw new SchemaSwapException("The swap did not go through, and it could not be undone: the tables of the "
                        + "served schema are in '" + outgoing + "': " + e.getMessage(), e);
            }
            throw new SchemaSwapException("The swap did not go through, and the served schema is as it was: " + e.getMessage(), e);
        }
    }

    /** A trigger or a routine that names its own schema would name the wrong one after it moves, so it is not to be moved. */
    private void requireNoSchemaNamed(Side side) {
        String named = "`" + side.schema() + "`.";
        for (Trigger trigger : side.triggers()) {
            if (trigger.ddl().contains(named)) {
                throw new SchemaSwapException("The trigger '" + trigger.name() + "' names the schema '" + side.schema() + "', so it cannot move.");
            }
        }
        for (Routine routine : side.routines()) {
            if (routine.ddl().contains(named)) {
                throw new SchemaSwapException("The routine '" + routine.name() + "' names the schema '" + side.schema() + "', so it cannot move.");
            }
        }
    }

    /** One statement: the tables of {@code fromSchema} go to {@code toSchema}, then the tables of {@code otherSchema} go to {@code intoSchema}. */
    private String renameStatement(String fromSchema, String toSchema, List<String> fromTables,
                                   String otherSchema, String intoSchema, List<String> otherTables) {
        List<String> pairs = new ArrayList<>();
        for (String table : fromTables) {
            pairs.add("`" + fromSchema + "`.`" + table + "` TO `" + toSchema + "`.`" + table + "`");
        }
        for (String table : otherTables) {
            pairs.add("`" + otherSchema + "`.`" + table + "` TO `" + intoSchema + "`.`" + table + "`");
        }
        return "RENAME TABLE " + String.join(", ", pairs);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // What the swap keeps, and what it moves besides tables
    // ---------------------------------------------------------------------------------------------------------------

    private void requireNoTriggersOn(Connection con, Collection<String> tables, String... schemas) throws SQLException {
        for (String schema : schemas) {
            for (String table : tables) {
                List<String> triggers = strings(con, "SELECT trigger_name FROM information_schema.triggers "
                        + "WHERE trigger_schema = ? AND event_object_table = ?", schema, table);
                if (!triggers.isEmpty()) {
                    throw new SchemaSwapException("The table '" + table + "' stays in place, but has triggers (" + triggers + "), which "
                            + "the swap drops and makes again: it is not to have any.");
                }
            }
        }
    }

    /** The triggers of the tables {@code onTables} of the schema. */
    private List<Trigger> triggers(Connection con, String schema, Collection<String> onTables) throws SQLException {
        List<Trigger> triggers = new ArrayList<>();
        for (String name : strings(con, "SELECT trigger_name FROM information_schema.triggers WHERE trigger_schema = ? AND event_object_table IN ("
                + (onTables.isEmpty() ? "NULL" : onTables.stream().map(t -> "'" + t.replace("'", "''") + "'").collect(Collectors.joining(", ")))
                + ") ORDER BY trigger_name", schema)) {
            // The columns of SHOW CREATE TRIGGER: the name, the sql_mode it was made with, and the statement that made it.
            try (Statement statement = con.createStatement();
                 ResultSet rs = statement.executeQuery("SHOW CREATE TRIGGER `" + schema + "`.`" + name + "`")) {
                if (rs.next()) {
                    triggers.add(new Trigger(name, rs.getString(2), rs.getString(3)));
                }
            }
        }
        return triggers;
    }

    private List<Routine> routines(Connection con, String schema) throws SQLException {
        List<Routine> routines = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(
                "SELECT routine_name, routine_type FROM information_schema.routines WHERE routine_schema = ? ORDER BY routine_name")) {
            statement.setString(1, schema);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    String type = rs.getString(2);
                    try (Statement show = con.createStatement();
                         ResultSet created = show.executeQuery("SHOW CREATE " + type + " `" + schema + "`.`" + name + "`")) {
                        if (created.next()) {
                            routines.add(new Routine(name, type, created.getString(2), created.getString(3)));
                        }
                    }
                }
            }
        }
        return routines;
    }

    private void dropTriggers(Connection con, String schema, List<Trigger> triggers) throws SQLException {
        for (Trigger trigger : triggers) {
            execute(con, "DROP TRIGGER IF EXISTS `" + schema + "`.`" + trigger.name() + "`");
        }
    }

    /**
     * Makes the triggers that the schema does not have, each with the sql_mode it was made with, as the connection's own is put
     * back. One that is there is left alone, so that this is also what puts back the ones that a failed swap dropped: the table
     * of a trigger that was not dropped may be held by another connection, and touching it would wait for that.
     */
    private void createTriggers(Connection con, String schema, List<Trigger> triggers) throws SQLException {
        String original = con.getCatalog();
        String sqlMode = single(con, "SELECT @@sql_mode");
        List<String> there = strings(con, "SELECT trigger_name FROM information_schema.triggers WHERE trigger_schema = ?", schema);
        con.setCatalog(schema);
        try (PreparedStatement mode = con.prepareStatement("SET sql_mode = ?")) {
            for (Trigger trigger : triggers) {
                if (there.contains(trigger.name())) {
                    continue;
                }
                mode.setString(1, trigger.sqlMode() == null ? "" : trigger.sqlMode());
                mode.execute();
                execute(con, SqlDefiner.strip(trigger.ddl()));
            }
        } finally {
            try (PreparedStatement mode = con.prepareStatement("SET sql_mode = ?")) {
                mode.setString(1, sqlMode);
                mode.execute();
            }
            con.setCatalog(original);
        }
    }

    /** Makes the schema have these routines and no others. */
    private void setRoutines(Connection con, String schema, List<Routine> routines) throws SQLException {
        for (Routine present : routines(con, schema)) {
            execute(con, "DROP " + present.type() + " IF EXISTS `" + schema + "`.`" + present.name() + "`");
        }
        String original = con.getCatalog();
        String sqlMode = single(con, "SELECT @@sql_mode");
        con.setCatalog(schema);
        try (PreparedStatement mode = con.prepareStatement("SET sql_mode = ?")) {
            for (Routine routine : routines) {
                mode.setString(1, routine.sqlMode() == null ? "" : routine.sqlMode());
                mode.execute();
                execute(con, SqlDefiner.strip(routine.ddl()));
            }
        } finally {
            try (PreparedStatement mode = con.prepareStatement("SET sql_mode = ?")) {
                mode.setString(1, sqlMode);
                mode.execute();
            }
            con.setCatalog(original);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Small things
    // ---------------------------------------------------------------------------------------------------------------

    private interface ConnectionWork<T> {
        T run(Connection con) throws Exception;
    }

    /** Runs on a connection of the application, and lets the exceptions of the work through as they are. */
    private <T> T inConnection(ConnectionWork<T> work) {
        try {
            return dslContext().connectionResult(con -> {
                try {
                    return work.run(con);
                } catch (SQLException | RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (org.jooq.exception.DataAccessException e) {
            // jOOQ wraps what the database says; the cause is what the caller wants to see.
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }

    private static String identifier(String name) {
        if (name == null || !IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("Not a name of a schema or a table: " + name);
        }
        return name;
    }

    private static void requireDistinct(String live, String a, String b) {
        if (live == null || live.equals(a) || live.equals(b) || a.equals(b)) {
            throw new IllegalArgumentException("The served schema '" + live + "' and the schemas '" + a + "' and '" + b + "' are to be three.");
        }
    }

    private void requireExists(Connection con, String schema) throws SQLException {
        if (!exists(con, schema)) {
            throw new SchemaSwapException("The schema '" + schema + "' does not exist.");
        }
    }

    private boolean exists(Connection con, String schema) throws SQLException {
        return !strings(con, "SELECT schema_name FROM information_schema.schemata WHERE schema_name = ?", schema).isEmpty();
    }

    private List<String> tables(Connection con, String schema) throws SQLException {
        return strings(con, "SELECT table_name FROM information_schema.tables WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name", schema);
    }

    private List<String> commonColumns(Connection con, String fromSchema, String toSchema, String table) throws SQLException {
        return strings(con, "SELECT t.column_name FROM information_schema.columns t "
                + "JOIN information_schema.columns f ON f.table_schema = ? AND f.table_name = t.table_name AND f.column_name = t.column_name "
                + "WHERE t.table_schema = ? AND t.table_name = ? ORDER BY t.ordinal_position", fromSchema, toSchema, table);
    }

    private List<String> strings(Connection con, String sql, String... parameters) throws SQLException {
        List<String> values = new ArrayList<>();
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setString(i + 1, parameters[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getString(1));
                }
            }
        }
        return values;
    }

    private String single(Connection con, String sql) throws SQLException {
        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }

    private void execute(Connection con, String sql) throws SQLException {
        try (Statement statement = con.createStatement()) {
            statement.execute(sql);
        }
    }
}
