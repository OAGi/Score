package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.repository.ForeignKeyViolation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** What of the broken foreign keys of a schema is the doing of a load: only what the schema served before did not have. */
class ForeignKeyViolationTest {

    private static ForeignKeyViolation violation(String key, long records) {
        return new ForeignKeyViolation("abie", key, "app_user", records);
    }

    private final List<ForeignKeyViolation> before = List.of(violation("fk_created_by", 14));

    @Test
    void whatWasAsBrokenBeforeIsLeftAsItIs() {
        assertEquals(List.of(), ForeignKeyViolation.addedBy(List.of(violation("fk_created_by", 14)), before));
        assertEquals(List.of(), ForeignKeyViolation.addedBy(List.of(violation("fk_created_by", 3)), before));
        assertEquals(List.of(), ForeignKeyViolation.addedBy(List.of(), before));
    }

    @Test
    void aKeyBrokenByMoreRecordsOrBrokenWhenItWasNotIsWhatTheLoadAdded() {
        ForeignKeyViolation more = violation("fk_created_by", 15);
        ForeignKeyViolation fresh = violation("fk_owner", 1);
        assertEquals(List.of(more, fresh), ForeignKeyViolation.addedBy(List.of(more, fresh), before));
    }
}
