package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.repository.ForeignKeyViolation;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleasePackageLoadBrokenReferencesTest {

    @Test
    void aKindOfRecordIsToldByItsName() {
        assertEquals("ACC", ReleasePackageLoadBrokenReferences.nameOf("acc_manifest"));
        assertEquals("agency ID list", ReleasePackageLoadBrokenReferences.nameOf("agency_id_list_manifest"));
        assertEquals("code list value", ReleasePackageLoadBrokenReferences.nameOf("code_list_value"));
        assertEquals("user account", ReleasePackageLoadBrokenReferences.nameOf("app_user"));
    }

    @Test
    void whatBreaksIsToldWithHowManyAndWhatItReferredTo() {
        String told = ReleasePackageLoadBrokenReferences.told(List.of(new ForeignKeyViolation("abie", "abie_based_acc_manifest_id_fk", "acc_manifest", 5)));

        assertEquals("5 ABIE records refer to ACC records", told);
    }

    @Test
    void aKindThatBreaksByMoreThanOneKeyIsToldOnceByTheOneThatBreaksMost() {
        String told = ReleasePackageLoadBrokenReferences.told(List.of(
                new ForeignKeyViolation("abie", "abie_created_by_fk", "app_user", 4),
                new ForeignKeyViolation("abie", "abie_last_updated_by_fk", "app_user", 6)));

        assertEquals("6 ABIE records", told);
    }

    @Test
    void oneRecordIsNotPlural() {
        assertEquals("1 BCC record refers to BCCP records", ReleasePackageLoadBrokenReferences.told(
                List.of(new ForeignKeyViolation("bcc_manifest", "fk", "bccp_manifest", 1))));
    }

    @Test
    void theFirstKindsAreToldAndHowManyMore() {
        List<ForeignKeyViolation> many = IntStream.range(0, 7).mapToObj(i -> new ForeignKeyViolation("t" + i, "fk", "p" + i, 1)).toList();

        String told = ReleasePackageLoadBrokenReferences.told(many);

        assertTrue(told.endsWith("; and 2 more kinds"), told);
    }
}
