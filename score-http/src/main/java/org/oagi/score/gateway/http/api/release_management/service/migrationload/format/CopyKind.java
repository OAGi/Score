package org.oagi.score.gateway.http.api.release_management.service.migrationload.format;

/** What is done, when a package is loaded, with a table of the schema that is served. */
public enum CopyKind {
    /** The package carries the data of the developers, numbered below the number the users' records start from: the records from that number are copied. */
    FROM_DELTA,
    /** Only the package writes the table: nothing is copied. */
    OF_PACKAGE,
    /** The package's records when it has some for the table, otherwise those of the schema. */
    OF_PACKAGE_IF_GIVEN,
    /** What follows the load itself, which the swap does not move. */
    STAYS_IN_PLACE,
    /** What the schema makes again on its own (the history of its migrations, the log of changes). */
    NOT_COPIED,
    /** The users' own: copied as it is. */
    ALL
}
