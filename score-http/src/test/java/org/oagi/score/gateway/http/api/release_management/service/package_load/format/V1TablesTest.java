package org.oagi.score.gateway.http.api.release_management.service.package_load.format;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class V1TablesTest {

    private static Map<String, CopyStep> plan(String... servedAndStaging) {
        List<String> tables = List.of(servedAndStaging);
        return V1Tables.plan(tables, tables).stream().collect(Collectors.toMap(CopyStep::table, Function.identity()));
    }

    @Test
    void whatThePackageNumbersBelowTheUsersIsCopiedFromTheNumberOfTheUsers() {
        Map<String, CopyStep> plan = plan("acc", "library", "app_user", "log", "dt_sc_awd_pri");

        for (String table : plan.keySet()) {
            assertEquals(CopyKind.FROM_DELTA, plan.get(table).kind(), table);
            assertEquals(table + "_id", plan.get(table).idColumn(), table);
        }
    }

    @Test
    void theTagsOfTheManifestsAreCopiedByTheManifestTheyAreOf() {
        Map<String, CopyStep> plan = plan("acc_manifest_tag", "asccp_manifest_tag", "bccp_manifest_tag", "dt_manifest_tag");

        assertEquals("acc_manifest_id", plan.get("acc_manifest_tag").idColumn());
        assertEquals("dt_manifest_id", plan.get("dt_manifest_tag").idColumn());
        plan.values().forEach(step -> assertEquals(CopyKind.FROM_DELTA, step.kind()));
    }

    @Test
    void whatOnlyThePackageWritesIsNotCopied() {
        Map<String, CopyStep> plan = plan("xbt", "xbt_manifest", "cdt_pri", "module", "module_set_release", "blob_content");

        plan.values().forEach(step -> assertEquals(CopyKind.OF_PACKAGE, step.kind(), step.table()));
    }

    @Test
    void whatTheUsersMadeAndThePackageKnowsNothingOfIsCopiedWhole() {
        Map<String, CopyStep> plan = plan("abie", "asbie", "top_level_asbiep", "biz_ctx", "configuration", "tag", "app_oauth2_user");

        plan.values().forEach(step -> {
            assertEquals(CopyKind.ALL, step.kind(), step.table());
            assertNull(step.idColumn(), step.table());
        });
    }

    @Test
    void whatTheSwapAndTheMigrationsOwnIsNeitherMovedNorCopied() {
        Map<String, CopyStep> plan = plan("release_package_load_job", "named_lock", "flyway_schema_history", "record_change_log");

        assertEquals(CopyKind.STAYS_IN_PLACE, plan.get("release_package_load_job").kind());
        assertEquals(CopyKind.STAYS_IN_PLACE, plan.get("named_lock").kind());
        assertEquals(CopyKind.NOT_COPIED, plan.get("flyway_schema_history").kind());
        assertEquals(CopyKind.NOT_COPIED, plan.get("record_change_log").kind());
        assertEquals(java.util.Set.of("release_package_load_job", "named_lock"), org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadTables.STAYS_IN_PLACE);
    }

    @Test
    void theOrderOfTheViewIsThePackagesWhenItGaveOne() {
        assertEquals(CopyKind.OF_PACKAGE_IF_GIVEN, plan("bie_view_order").get("bie_view_order").kind());
    }

    @Test
    void aTableOfTheServedSchemaThatTheStagingSchemaLacksIsNotInThePlan() {
        List<CopyStep> steps = V1Tables.plan(List.of("acc", "only_in_served"), List.of("acc"));

        assertEquals(List.of("acc"), steps.stream().map(CopyStep::table).toList());
    }

    @Test
    void thePinnedTablesAreThoseTheGeneratorPinsAndAreAllCopiedFromTheNumberOfTheUsers() {
        List<String> pinned = V1Tables.pinned();

        assertEquals(31, pinned.size());
        assertTrue(pinned.containsAll(List.of("acc", "app_user", "library", "release", "release_dep", "log", "seq_key", "namespace")));
        pinned.forEach(table -> assertEquals(CopyKind.FROM_DELTA, plan(table).get(table).kind(), table));
    }

    @Test
    void theFirstAccountIsTheInstancesOwnAndThePackageCarriesNoOtherTableLikeThat() {
        Map<String, CopyStep> plan = plan("app_user", "acc", "library", "namespace", "tag");

        assertEquals(java.math.BigInteger.TWO, plan.get("app_user").ownBelow());
        plan.values().stream().filter(step -> !step.table().equals("app_user"))
                .forEach(step -> assertNull(step.ownBelow(), step.table()));
    }
}
