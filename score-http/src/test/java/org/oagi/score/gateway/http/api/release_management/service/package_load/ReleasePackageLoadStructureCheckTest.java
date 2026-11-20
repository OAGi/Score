package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.repository.ReleasePackageLoadQueryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReleasePackageLoadStructureCheckTest {

    private final ReleasePackageLoadQueryRepository query = mock(ReleasePackageLoadQueryRepository.class);

    @Test
    void anEmptyColumnOnlyTheServedSchemaHasDoesNotStopTheSwap() {
        when(query.hasValues("oagi", "xbt_manifest", "cdt_pri_id")).thenReturn(false);
        var result = ReleasePackageLoadStructureCheck.of(List.of(
                "only in oagi: column xbt_manifest|cdt_pri_id|bigint(20) unsigned|YES|NULL||",
                "only in oagi: index xbt_manifest|xbt_manifest_cdt_pri_id_fk|1|cdt_pri_id|1||BTREE",
                "only in oagi: foreign key xbt_manifest|xbt_manifest_cdt_pri_id_fk|1|cdt_pri_id||cdt_pri|cdt_pri_id|RESTRICT|RESTRICT",
                "only in oagi_load_1: check log|log_chk_1|json_valid(`snapshot`)"), "oagi", query);
        assertEquals(List.of(), result.blocking());
        assertEquals(4, result.tolerated().size());
    }

    @Test
    void aColumnWithValuesOnlyTheServedSchemaHasStopsTheSwap() {
        when(query.hasValues("oagi", "t", "c")).thenReturn(true);
        var result = ReleasePackageLoadStructureCheck.of(List.of("only in oagi: column t|c|int|YES|NULL||"), "oagi", query);
        assertEquals(1, result.blocking().size());
    }

    @Test
    void aTableWithRecordsOnlyTheServedSchemaHasStopsTheSwapAndAnEmptyOneDoesNot() {
        when(query.hasRows("oagi", "full")).thenReturn(true);
        when(query.hasRows("oagi", "empty")).thenReturn(false);
        var result = ReleasePackageLoadStructureCheck.of(List.of("only in oagi: table full", "only in oagi: table empty"), "oagi", query);
        assertEquals(List.of("only in oagi: table full"), result.blocking());
    }

    @Test
    void aTriggerOrRoutineThatDiffersStopsTheSwapWhicheverSchemaHasIt() {
        var result = ReleasePackageLoadStructureCheck.of(List.of(
                "only in oagi: trigger a|t|AFTER|INSERT|x", "only in oagi_load_1: routine r|FUNCTION|y"), "oagi", query);
        assertEquals(2, result.blocking().size());
    }

    @Test
    void whatOnlyTheLoadedSchemaHasDoesNotStopTheSwap() {
        when(query.hasValues(anyString(), anyString(), anyString())).thenReturn(true);
        var result = ReleasePackageLoadStructureCheck.of(List.of(
                "only in oagi_load_1: column t|new|int|YES|NULL||", "only in oagi_load_1: table fresh"), "oagi", query);
        assertEquals(List.of(), result.blocking());
    }
}
