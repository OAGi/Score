package org.oagi.score.gateway.http.api.release_management.repository;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.repository.jooq.JooqMigrationLoadCommandRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** That what brings a schema to a version is whatever the repository is given, and nothing of it is known to the repository. */
class MigrationLoadMigratorTest {

    private final List<String> migrated = new ArrayList<>();
    private final SchemaMigrator recording = (schema, target) -> migrated.add(schema + "@" + target);
    private final MigrationLoadCommandRepository repository = new JooqMigrationLoadCommandRepository(null, null, null, recording);

    @Test
    void theSchemaIsMigratedByTheMigratorThatIsGiven() {
        repository.migrateSchema("stg_1", "3.4.0");
        repository.migrateSchema("stg_2", null);

        assertEquals(List.of("stg_1@3.4.0", "stg_2@null"), migrated);
    }

    @Test
    void aNameThatIsNotOfLettersAndDigitsIsNotGivenToTheMigrator() {
        assertThrows(IllegalArgumentException.class, () -> repository.migrateSchema("x`; DROP DATABASE y", null));

        assertEquals(List.of(), migrated);
    }
}
