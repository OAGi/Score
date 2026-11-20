package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ValueListCandidate;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
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
        ValueListCandidate<CodeListManifestId> sourceList = codeList(4291);
        ValueListCandidate<CodeListManifestId> targetList = codeList(4633);
        List<ValueListCandidate<CodeListManifestId>> targets = targetExists ? List.of(targetList) : List.of();
        CodeListManifestId allowedId = switch (restriction) {
            case "allowed" -> targetList.manifestId();
            case "disallowed" -> new CodeListManifestId(BigInteger.valueOf(999));
            default -> null;
        };
        XbtManifestId defaultPrimitive = new XbtManifestId(BigInteger.valueOf(5192));

        var handlerClass = Class.forName(BieUpliftingService.class.getName() + "$BieUpliftingHandler");
        var constructor = handlerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object handler = constructor.newInstance(service, developer(), List.of(),
                new BieUpliftingCustomMappingTable(List.of()), null, document, null,
                List.of(), List.of(), List.of(sourceList), targets, List.of(), List.of());

        Object validation;
        CodeListManifestId actualCodeList;
        XbtManifestId actualPrimitive;
        if (supplementary) {
            DtScManifestId id = new DtScManifestId(BigInteger.ONE);
            DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
            when(dtSc.dtScManifestId()).thenReturn(id);
            when(document.getDtSc(id)).thenReturn(dtSc);
            // A row of the allowed primitives holds one value domain: the default primitive, or an allowed list.
            DtScAwdPriSummaryRecord approved = mock(DtScAwdPriSummaryRecord.class);
            when(approved.codeListManifestId()).thenReturn(allowedId);
            DtScAwdPriSummaryRecord defaultRow = mock(DtScAwdPriSummaryRecord.class);
            when(defaultRow.xbtManifestId()).thenReturn(defaultPrimitive);
            when(defaultRow.isDefault()).thenReturn(true);
            when(document.getDtScAwdPriList(id)).thenReturn(List.of(approved, defaultRow));
            BbieSc source = new BbieSc();
            source.setCodeListManifestId(sourceList.manifestId());
            BbieSc target = new BbieSc();
            ReflectionTestUtils.invokeMethod(handler, "setValueDomain", source, target, id,
                    List.of(), List.of(sourceList), List.of());
            actualCodeList = target.getCodeListManifestId();
            actualPrimitive = target.getXbtManifestId();
            List<ValueListCandidate<CodeListManifestId>> availableTargets = ReflectionTestUtils.invokeMethod(service, "availableCodeLists",
                    developer(), targets, List.of(approved, defaultRow), (java.util.function.Function<DtScAwdPriSummaryRecord, CodeListManifestId>) DtScAwdPriSummaryRecord::codeListManifestId);
            validation = ReflectionTestUtils.invokeMethod(service, "checkBdtCodeListMappable",
                    sourceList, availableTargets);
        } else {
            DtManifestId id = new DtManifestId(BigInteger.valueOf(36469));
            DtSummaryRecord dt = mock(DtSummaryRecord.class);
            when(dt.dtManifestId()).thenReturn(id);
            when(document.getDt(id)).thenReturn(dt);
            // A row of the allowed primitives holds one value domain: the default primitive, or an allowed list.
            DtAwdPriSummaryRecord approved = mock(DtAwdPriSummaryRecord.class);
            when(approved.codeListManifestId()).thenReturn(allowedId);
            DtAwdPriSummaryRecord defaultRow = mock(DtAwdPriSummaryRecord.class);
            when(defaultRow.xbtManifestId()).thenReturn(defaultPrimitive);
            when(defaultRow.isDefault()).thenReturn(true);
            when(document.getDtAwdPriList(id)).thenReturn(List.of(approved, defaultRow));
            Bbie source = new Bbie();
            source.setCodeListManifestId(sourceList.manifestId());
            Bbie target = new Bbie();
            ReflectionTestUtils.invokeMethod(handler, "setValueDomain", source, target, id,
                    List.of(), List.of(sourceList), List.of());
            actualCodeList = target.getCodeListManifestId();
            actualPrimitive = target.getXbtManifestId();
            List<ValueListCandidate<CodeListManifestId>> availableTargets = ReflectionTestUtils.invokeMethod(service, "availableCodeLists",
                    developer(), targets, List.of(approved, defaultRow), (java.util.function.Function<DtAwdPriSummaryRecord, CodeListManifestId>) DtAwdPriSummaryRecord::codeListManifestId);
            validation = ReflectionTestUtils.invokeMethod(service, "checkBdtCodeListMappable",
                    sourceList, availableTargets);
        }

        assertThat(actualCodeList).isEqualTo(retained ? targetList.manifestId() : null);
        assertThat(actualPrimitive).isEqualTo(retained ? null : defaultPrimitive);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(validation, "valid")).isEqualTo(retained);
    }

    @ParameterizedTest(name = "requester developer={0}, target {1}, end-user owned={2}, retained={3}")
    @CsvSource({
            "true, Published, false, true",
            "true, Production, false, false",
            "true, Published, true, false",
            "true, WIP, false, false",
            "false, Published, false, true",
            "false, Production, true, true",
            "false, WIP, false, true"
    })
    void uplifteeGetsOnlyWhatTheRequesterCouldAssignWhenEditingTheNode(
            boolean developer, CcState state, boolean endUserOwned, boolean retained) throws Exception {
        BieUpliftingService service = new BieUpliftingService(new CcMatchingService());
        CcDocument document = mock(CcDocument.class);
        ValueListCandidate<CodeListManifestId> sourceList = codeList(4291);
        ValueListCandidate<CodeListManifestId> targetList = new ValueListCandidate<>(
                new CodeListManifestId(BigInteger.valueOf(4633)), new Guid("a".repeat(32)),
                "oacl_SystemEnvironmentCode", "LIST", "1", null, state, null, endUserOwned);
        ScoreUser requester = developer ? developer()
                : new ScoreUser(null, "user", "User", null, false, List.of(ScoreRole.END_USER));
        XbtManifestId defaultPrimitive = new XbtManifestId(BigInteger.valueOf(5192));

        var handlerClass = Class.forName(BieUpliftingService.class.getName() + "$BieUpliftingHandler");
        var constructor = handlerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object handler = constructor.newInstance(service, requester, List.of(),
                new BieUpliftingCustomMappingTable(List.of()), null, document, null,
                List.of(), List.of(), List.of(sourceList), List.of(targetList), List.of(), List.of());

        DtManifestId id = new DtManifestId(BigInteger.valueOf(36469));
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        when(dt.dtManifestId()).thenReturn(id);
        when(document.getDt(id)).thenReturn(dt);
        DtAwdPriSummaryRecord approved = mock(DtAwdPriSummaryRecord.class);
        when(approved.codeListManifestId()).thenReturn(targetList.manifestId());
        DtAwdPriSummaryRecord defaultRow = mock(DtAwdPriSummaryRecord.class);
        when(defaultRow.xbtManifestId()).thenReturn(defaultPrimitive);
        when(defaultRow.isDefault()).thenReturn(true);
        when(document.getDtAwdPriList(id)).thenReturn(List.of(approved, defaultRow));
        Bbie source = new Bbie();
        source.setCodeListManifestId(sourceList.manifestId());
        Bbie target = new Bbie();

        ReflectionTestUtils.invokeMethod(handler, "setValueDomain", source, target, id,
                List.of(), List.of(sourceList), List.of());

        assertThat(target.getCodeListManifestId()).isEqualTo(retained ? targetList.manifestId() : null);
        assertThat(target.getXbtManifestId()).isEqualTo(retained ? null : defaultPrimitive);
    }

    private ValueListCandidate<CodeListManifestId> codeList(int manifestId) {
        return new ValueListCandidate<>(new CodeListManifestId(BigInteger.valueOf(manifestId)),
                new Guid("a".repeat(32)), "oacl_SystemEnvironmentCode", "LIST", "1", null,
                CcState.Published, null, false);
    }

    private static ScoreUser developer() {
        return new ScoreUser(null, "developer", "Developer", null, true, List.of(ScoreRole.DEVELOPER));
    }
}
