package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListQueryService;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListQueryService;
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

class BieUpliftingCodeListDomainTest {

    @ParameterizedTest(name = "SC={0}, restriction={1}, target exists={2}, retained={3}")
    @CsvSource({
            "false, open, true, true", "true, open, true, true",
            "false, allowed, true, true", "true, allowed, true, true",
            "false, disallowed, true, false", "true, disallowed, true, false",
            "false, open, false, false", "true, open, false, false"
    })
    void validationAndGenerationAgreeOnCodeListAvailability(
            boolean supplementary, String restriction, boolean targetExists, boolean retained) throws Exception {
        BieUpliftingService service = new BieUpliftingService(new CcMatchingService());
        CcDocument document = mock(CcDocument.class);
        CodeListSummaryRecord sourceList = codeList(4291);
        CodeListSummaryRecord targetList = codeList(4633);
        List<CodeListSummaryRecord> targets = targetExists ? List.of(targetList) : List.of();
        CodeListManifestId allowedId = switch (restriction) {
            case "allowed" -> targetList.codeListManifestId();
            case "disallowed" -> new CodeListManifestId(BigInteger.valueOf(999));
            default -> null;
        };
        List<CodeListSummaryRecord> availableTargets = "disallowed".equals(restriction)
                ? List.of() : targets;
        CodeListQueryService codeListQueryService = mock(CodeListQueryService.class);
        doReturn(availableTargets).when(codeListQueryService)
                .availableCodeListListByDtManifestId(any(), any());
        doReturn(availableTargets).when(codeListQueryService)
                .availableCodeListListByDtScManifestId(any(), any());
        ReflectionTestUtils.setField(service, "codeListQueryService", codeListQueryService);
        ReflectionTestUtils.setField(service, "agencyIdListQueryService", mock(AgencyIdListQueryService.class));
        XbtManifestId defaultPrimitive = new XbtManifestId(BigInteger.valueOf(5192));

        var handlerClass = Class.forName(BieUpliftingService.class.getName() + "$BieUpliftingHandler");
        var constructor = handlerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object handler = constructor.newInstance(service, null, List.of(),
                new BieUpliftingCustomMappingTable(List.of()), null, document, null,
                List.of(), List.of(), List.of(sourceList), null, List.of(), null);

        Object validation;
        CodeListManifestId actualCodeList;
        XbtManifestId actualPrimitive;
        if (supplementary) {
            DtScManifestId id = new DtScManifestId(BigInteger.ONE);
            DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
            when(dtSc.dtScManifestId()).thenReturn(id);
            when(document.getDtSc(id)).thenReturn(dtSc);
            DtScAwdPriSummaryRecord approved = mock(DtScAwdPriSummaryRecord.class);
            when(approved.codeListManifestId()).thenReturn(allowedId);
            when(approved.xbtManifestId()).thenReturn(defaultPrimitive);
            when(approved.isDefault()).thenReturn(true);
            when(document.getDtScAwdPriList(id)).thenReturn(List.of(approved));
            BbieSc source = new BbieSc();
            source.setCodeListManifestId(sourceList.codeListManifestId());
            BbieSc target = new BbieSc();
            ReflectionTestUtils.invokeMethod(handler, "setValueDomain", source, target, id,
                    List.of(), List.of(sourceList), List.of());
            actualCodeList = target.getCodeListManifestId();
            actualPrimitive = target.getXbtManifestId();
            validation = ReflectionTestUtils.invokeMethod(service, "checkBdtCodeListMappable",
                    sourceList, availableTargets);
        } else {
            DtManifestId id = new DtManifestId(BigInteger.valueOf(36469));
            DtSummaryRecord dt = mock(DtSummaryRecord.class);
            when(dt.dtManifestId()).thenReturn(id);
            when(document.getDt(id)).thenReturn(dt);
            DtAwdPriSummaryRecord approved = mock(DtAwdPriSummaryRecord.class);
            when(approved.codeListManifestId()).thenReturn(allowedId);
            when(approved.xbtManifestId()).thenReturn(defaultPrimitive);
            when(approved.isDefault()).thenReturn(true);
            when(document.getDtAwdPriList(id)).thenReturn(List.of(approved));
            Bbie source = new Bbie();
            source.setCodeListManifestId(sourceList.codeListManifestId());
            Bbie target = new Bbie();
            ReflectionTestUtils.invokeMethod(handler, "setValueDomain", source, target, id,
                    List.of(), List.of(sourceList), List.of());
            actualCodeList = target.getCodeListManifestId();
            actualPrimitive = target.getXbtManifestId();
            validation = ReflectionTestUtils.invokeMethod(service, "checkBdtCodeListMappable",
                    sourceList, availableTargets);
        }

        assertThat(actualCodeList).isEqualTo(retained ? targetList.codeListManifestId() : null);
        assertThat(actualPrimitive).isEqualTo(retained ? null : defaultPrimitive);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(validation, "valid")).isEqualTo(retained);
    }

    private CodeListSummaryRecord codeList(int manifestId) {
        CodeListSummaryRecord list = mock(CodeListSummaryRecord.class);
        when(list.codeListManifestId()).thenReturn(new CodeListManifestId(BigInteger.valueOf(manifestId)));
        when(list.codeListId()).thenReturn(new CodeListId(BigInteger.valueOf(73)));
        when(list.guid()).thenReturn(new Guid("a".repeat(32)));
        when(list.name()).thenReturn("oacl_SystemEnvironmentCode");
        return list;
    }
}
