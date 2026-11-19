package org.oagi.score.gateway.http.api.release_management.repository;

import org.oagi.score.gateway.http.api.release_management.dataload.LoadProgress;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Info;
import org.oagi.score.gateway.http.api.release_management.dataload.script.ScriptSource;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * What it takes to load a release package into a schema of its own, and then to put that schema in the place of the one the
 * application serves: the schema is made, brought to the shape of a version, loaded, and swapped in for the served one, which
 * is kept aside so that the swap can be undone. It is for no user to ask.
 *
 * <p>Every schema is given by its name, which is of letters, digits and underscores only. None of these changes the
 * data of the schema the application serves, except {@link #swap} and {@link #rollbackSwap}, which are for when nothing
 * is writing to it.</p>
 */
public interface MigrationLoadCommandRepository {

    /** What a swap moved. */
    record SwapOutcome(int tables, int triggers, int routines) {
    }

    /**
     * Makes an empty schema, of the character set and collation of the schema the application serves.
     *
     * @throws IllegalStateException when a schema of the name exists
     */
    void createSchema(String schema);

    /**
     * Drops the schema and all in it.
     *
     * @throws IllegalArgumentException for the schema the application serves
     */
    void dropSchema(String schema);

    /**
     * Brings the schema to the version, by the migrations of the application: an empty schema is made from the baseline and
     * is migrated from there.
     *
     * @param targetVersion the last version to apply, or {@code null} for the latest
     */
    void migrateSchema(String schema, String targetVersion);

    /**
     * Loads the package into the schema, which is not the one the application serves.
     *
     * @param statements what each statement of the scripts is changed by before it is run
     *
     * @throws IllegalStateException when the schema has a release the package does not know of
     */
    void load(String schema, Info info, ScriptSource scripts, long delta, LoadProgress progress, UnaryOperator<String> statements);

    /** Loads the package as its scripts are, each statement being run as it is written. */
    default void load(String schema, Info info, ScriptSource scripts, long delta, LoadProgress progress) {
        load(schema, info, scripts, delta, progress, UnaryOperator.identity());
    }

    /**
     * Copies the records of a table of one schema into the same table of another, the columns the two have in common.
     * Foreign keys are not checked while it does, so the order of the tables does not matter.
     *
     * @param idColumn the column to tell the records by, or {@code null} to copy all
     * @param atLeast  the lowest value of {@code idColumn} to copy, or {@code null} to copy all
     * @return the number of records copied
     */
    int copyRecords(String fromSchema, String toSchema, String table, String idColumn, BigInteger atLeast);

    /** Copies the records of the table that have {@code idColumn} below {@code below}, as {@link #copyRecords} does the others. */
    int copyRecordsBelow(String fromSchema, String toSchema, String table, String idColumn, BigInteger below);

    /** Removes the records of the table of the schema that have {@code idColumn} below {@code below}, foreign keys not being checked. */
    int removeRecordsBelow(String schema, String table, String idColumn, BigInteger below);

    /**
     * Removes all the records of the table of the schema, foreign keys not being checked.
     *
     * @return the number of records removed
     */
    int emptyTable(String schema, String table);

    /**
     * Makes the numbering of the table of {@code toSchema} go on from where it is in {@code fromSchema}, when that is further:
     * what is copied keeps its numbers, and the numbers that were given and are not in a record (a record that was removed) are
     * not given again.
     */
    void copyAutoIncrement(String fromSchema, String toSchema, String table);

    /**
     * Puts the staging schema in the place of the served one: the tables of the served schema are moved to the backup schema
     * and those of the staging schema are moved to the served one, in one statement. A table that has a trigger cannot be
     * moved to another schema, so the triggers are dropped and made again in the served schema, from the ones of the staging
     * schema; the triggers and the routines that were there are kept in the backup schema, for {@link #rollbackSwap}.
     *
     * <p>Nothing is to write to the served schema while it does. When it does not go through, the served schema is as it was
     * (this is said in the exception if it could not be put back).</p>
     *
     * @param backupSchema an empty schema
     * @param lockWait     how long to wait for a lock a statement of another connection holds, before giving up
     * @param staysInPlace the tables that are not moved, neither those of the served schema nor those of the staging schema (what
     *                     follows the swap itself, such as the record of the load); they have no foreign key, since one that
     *                     goes to a table that is moved goes with that table. The undo leaves them where they are too.
     * @throws SchemaSwapException when the swap did not go through
     */
    SwapOutcome swap(String stagingSchema, String backupSchema, Duration lockWait, Collection<String> staysInPlace);

    /** A swap in which every table is moved. */
    default SwapOutcome swap(String stagingSchema, String backupSchema, Duration lockWait) {
        return swap(stagingSchema, backupSchema, lockWait, Set.of());
    }

    /**
     * Undoes a swap: the tables of the backup schema are moved back, the triggers and the routines that were kept are made
     * again, and the tables that were swapped in are moved to the discarded schema.
     *
     * @param discardedSchema an empty schema
     * @throws SchemaSwapException when the backup schema does not hold a swap, or when it did not go through
     */
    SwapOutcome rollbackSwap(String backupSchema, String discardedSchema, Duration lockWait);
}
