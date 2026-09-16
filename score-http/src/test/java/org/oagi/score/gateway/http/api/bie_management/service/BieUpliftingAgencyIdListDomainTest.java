package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListQueryService;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListQueryService;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class BieUpliftingAgencyIdListDomainTest {

    @ParameterizedTest(name = "SC={0}, restriction={1}, target exists={2}, retained={3}")
    @CsvSource({
            "false, open, true, true", "true, open, true, true",
            "false, allowed, true, true", "true, allowed, true, true",
            "false, disallowed, true, false", "true, disallowed, true, false",
            "false, open, false, false", "true, open, false, false"
    })
    void validationAndGenerationAgreeOnAgencyIdListAvailability(
            boolean supplementary, String restriction, boolean targetExists, boolean retained) throws Exception {
        BieUpliftingService service = new BieUpliftingService(new CcMatchingService());
        CcDocument document = mock(CcDocument.class);
        AgencyIdListSummaryRecord sourceList = agencyIdList(4291, 73);
        AgencyIdListSummaryRecord targetList = agencyIdList(4633, 73);
        List<AgencyIdListSummaryRecord> targets = targetExists ? List.of(targetList) : List.of();
        AgencyIdListManifestId allowedId = switch (restriction) {
            case "allowed" -> targetList.agencyIdListManifestId();
            case "disallowed" -> new AgencyIdListManifestId(BigInteger.valueOf(999));
            default -> null;
        };
        List<AgencyIdListSummaryRecord> availableTargets = "disallowed".equals(restriction)
                ? List.of() : targets;
        AgencyIdListQueryService agencyIdListQueryService = mock(AgencyIdListQueryService.class);
        doReturn(availableTargets).when(agencyIdListQueryService)
                .availableAgencyIdListListByDtManifestId(any(), any());
        doReturn(availableTargets).when(agencyIdListQueryService)
                .availableAgencyIdListListByDtScManifestId(any(), any());
        ReflectionTestUtils.setField(service, "agencyIdListQueryService", agencyIdListQueryService);
        ReflectionTestUtils.setField(service, "codeListQueryService", mock(CodeListQueryService.class));
        XbtManifestId defaultPrimitive = new XbtManifestId(BigInteger.valueOf(5193));

        var handlerClass = Class.forName(BieUpliftingService.class.getName() + "$BieUpliftingHandler");
        var constructor = handlerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object handler = constructor.newInstance(service, null, List.of(),
                new BieUpliftingCustomMappingTable(List.of()), null, document, null,
                List.of(), List.of(), List.of(), null, List.of(sourceList), null);

        Object validation;
        AgencyIdListManifestId actualAgencyIdList;
        XbtManifestId actualPrimitive;
        if (supplementary) {
            DtScManifestId id = new DtScManifestId(BigInteger.ONE);
            DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
            when(dtSc.dtScManifestId()).thenReturn(id);
            when(document.getDtSc(id)).thenReturn(dtSc);
            DtScAwdPriSummaryRecord approved = mock(DtScAwdPriSummaryRecord.class);
            when(approved.agencyIdListManifestId()).thenReturn(allowedId);
            when(approved.xbtManifestId()).thenReturn(defaultPrimitive);
            when(approved.isDefault()).thenReturn(true);
            when(document.getDtScAwdPriList(id)).thenReturn(List.of(approved));
            BbieSc source = new BbieSc();
            source.setAgencyIdListManifestId(sourceList.agencyIdListManifestId());
            BbieSc target = new BbieSc();
            ReflectionTestUtils.invokeMethod(handler, "setValueDomain", source, target, id,
                    List.of(), List.of(), List.of(sourceList));
            actualAgencyIdList = target.getAgencyIdListManifestId();
            actualPrimitive = target.getXbtManifestId();
            validation = ReflectionTestUtils.invokeMethod(service, "checkBdtAgencyIdListMappable",
                    sourceList, availableTargets);
        } else {
            DtManifestId id = new DtManifestId(BigInteger.valueOf(36469));
            DtSummaryRecord dt = mock(DtSummaryRecord.class);
            when(dt.dtManifestId()).thenReturn(id);
            when(document.getDt(id)).thenReturn(dt);
            DtAwdPriSummaryRecord approved = mock(DtAwdPriSummaryRecord.class);
            when(approved.agencyIdListManifestId()).thenReturn(allowedId);
            when(approved.xbtManifestId()).thenReturn(defaultPrimitive);
            when(approved.isDefault()).thenReturn(true);
            when(document.getDtAwdPriList(id)).thenReturn(List.of(approved));
            Bbie source = new Bbie();
            source.setAgencyIdListManifestId(sourceList.agencyIdListManifestId());
            Bbie target = new Bbie();
            ReflectionTestUtils.invokeMethod(handler, "setValueDomain", source, target, id,
                    List.of(), List.of(), List.of(sourceList));
            actualAgencyIdList = target.getAgencyIdListManifestId();
            actualPrimitive = target.getXbtManifestId();
            validation = ReflectionTestUtils.invokeMethod(service, "checkBdtAgencyIdListMappable",
                    sourceList, availableTargets);
        }

        assertThat(actualAgencyIdList).isEqualTo(retained ? targetList.agencyIdListManifestId() : null);
        assertThat(actualPrimitive).isEqualTo(retained ? null : defaultPrimitive);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(validation, "valid")).isEqualTo(retained);
    }

    private AgencyIdListSummaryRecord agencyIdList(int manifestId, int agencyIdListId) {
        return new AgencyIdListSummaryRecord(
                new AgencyIdListManifestId(BigInteger.valueOf(manifestId)),
                new AgencyIdListId(BigInteger.valueOf(agencyIdListId)),
                new Guid("a".repeat(32)), null,
                null,
                "Test Agency List", "TEST", "1", null, null, null, "Value",
                false, CcState.Published, null, null, null, null);
    }
}
