package org.oagi.score.gateway.http.api.release_management.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What is asked of the schemas of the server while a release package is loaded into a schema of its own: which one the
 * application serves, whether one exists, and whether two are the same. It is read-only, and is for no user to ask.
 */
public interface ReleasePackageLoadQueryRepository {

    /** The schema the application serves, which is the one the connection is of. */
    String currentSchema();

    boolean schemaExists(String schema);

    /**
     * How the two schemas differ in what the application depends on: the tables, their columns (type, nullability, default,
     * collation), the indexes, the foreign keys, the check constraints, the triggers and the routines. The data is not
     * compared, and neither are the comments. Empty when they are the same.
     *
     * <p>Each difference is a line that says which schema it is only in, and what it is.</p>
     */
    List<String> differences(String schema, String otherSchema);

    /** The tables of the schema (not the views), by name. */
    List<String> tables(String schema);

    /** The number the next record of the table is given, when the table numbers its records. */
    Optional<BigInteger> autoIncrement(String schema, String table);

    /** The number of records of the table, those that have {@code idColumn} at least {@code atLeast} when both are given. */
    long countRecords(String schema, String table, String idColumn, BigInteger atLeast);

    /** The number of records of the table that have {@code idColumn} below {@code below}. */
    long countRecordsBelow(String schema, String table, String idColumn, BigInteger below);

    /**
     * What tells that records of the table were written: the number of the records and the latest time one was updated (when
     * the table says it). Two readings that are the same say that nothing was written between them, as far as a count and a
     * time can say, which a record that is updated without its time changing escapes.
     */
    String fingerprint(String schema, String table);

    /** The releases of the schema: the identifier, and the number of the release. */
    Map<BigInteger, String> releases(String schema);

    /**
     * The foreign keys of the schema that records break: a record that refers to a record that is not there. A schema the
     * application made has none; one that records were copied into (without the foreign keys being checked) may.
     *
     * @return a line for each foreign key that is broken, with how many records break it; empty when none is
     */
    List<String> foreignKeyViolations(String schema);

    /** The columns of the schema that are foreign keys to the column of the table (a key of more than one column is not one of them). */
    List<ColumnReference> columnsReferencing(String schema, String table, String column);

    /** The highest value of the column in the table, when the table has a record. */
    Optional<BigInteger> maxId(String schema, String table, String idColumn);

    /**
     * The values of {@code idColumn} that are above {@code above} and below {@code below}, lowest first.
     *
     * @param withoutDevelopers when true, only the records of {@code app_user} that are not of a developer
     */
    List<BigInteger> idsBetween(String schema, String table, String idColumn, BigInteger above, BigInteger below, boolean withoutDevelopers);
}
