package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BieUpliftingServiceTest {

    @Test
    void resolvesTargetPrimitiveBySharedXbtIdAcrossReleaseManifests() {
        XbtSummaryRecord sourceXbt = xbt(101, 7, 'a');
        XbtSummaryRecord targetXbt = xbt(202, 7, 'b');

        XbtSummaryRecord result = service()
                .getTargetXbtManifest(sourceXbt, List.of(targetXbt));

        assertThat(result).isSameAs(targetXbt);
    }

    @Test
    void doesNotResolveTargetPrimitiveWhenXbtIdDiffersEvenIfGuidMatches() {
        XbtSummaryRecord sourceXbt = xbt(101, 7, 'a');
        XbtSummaryRecord targetXbt = xbt(202, 8, 'a');

        XbtSummaryRecord result = service()
                .getTargetXbtManifest(sourceXbt, List.of(targetXbt));

        assertThat(result).isNull();
    }

    @Test
    void resolvesTargetCodeListByGuidBeforeFallbackIdentifiers() {
        CodeListSummaryRecord source = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Source", "LIST", "1");
        CodeListSummaryRecord target = codeList(202, 8, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Different name", "OTHER", "2");

        CodeListSummaryRecord result = service()
                .getTargetCodeListManifest(source, List.of(target));

        assertThat(result).isSameAs(target);
    }

    @Test
    void resolvesUpliftedCodeListByNameListIdAndVersion() {
        CodeListSummaryRecord source = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Code List", "TEST", "1");
        CodeListSummaryRecord target = codeList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Code List", "TEST", "1");

        CodeListSummaryRecord result = service()
                .getTargetCodeListManifest(source, List.of(target));

        assertThat(result).isSameAs(target);
    }

    @Test
    void resolvesUpliftedAgencyIdListByItsIdentifiers() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Agency List", "TEST", "1");
        AgencyIdListSummaryRecord target = agencyIdList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Agency List", "TEST", "1");

        AgencyIdListSummaryRecord result = service()
                .getTargetAgencyIdListManifest(source, List.of(target));

        assertThat(result).isSameAs(target);
    }

    @Test
    void doesNotSelectMatchingCodeListWhenTargetNodeDoesNotAllowIt() {
        CodeListSummaryRecord source = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Code List", "TEST", "1");
        CodeListSummaryRecord target = codeList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Code List", "TEST", "1");

        CodeListSummaryRecord result = service()
                .findTargetCodeListMatch(source, List.of()).target();

        assertThat(result).isNull();
    }

    @Test
    void rejectsCodeListWhenApprovalSetIsExplicitlyEmpty() {
        CodeListSummaryRecord source = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Code List", "TEST", "1");
        CodeListSummaryRecord target = codeList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Code List", "TEST", "1");

        assertThat(service().findTargetCodeListMatch(source, List.of(target), Set.of()).target()).isNull();
    }

    @Test
    void selectsMatchingCodeListWhenTargetNodeHasNoExplicitRestriction() {
        CodeListSummaryRecord source = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Code List", "TEST", "1");
        CodeListSummaryRecord target = codeList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Code List", "TEST", "1");

        CodeListSummaryRecord result = service()
                .findTargetCodeListMatch(source, List.of(target)).target();

        assertThat(result).isSameAs(target);
    }

    @Test
    void selectsMatchingAgencyIdListWhenTargetNodeAllowsIt() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Agency List", "TEST", "1");
        AgencyIdListSummaryRecord target = agencyIdList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Agency List", "TEST", "1");

        AgencyIdListSummaryRecord result = service()
                .findTargetAgencyIdListMatch(source, List.of(target)).target();

        assertThat(result).isSameAs(target);
    }

    @Test
    void rejectsAgencyIdListWhenApprovalSetIsExplicitlyEmpty() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Agency List", "TEST", "1");
        AgencyIdListSummaryRecord target = agencyIdList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Agency List", "TEST", "1");

        assertThat(service().findTargetAgencyIdListMatch(source, List.of(target), Set.of()).target()).isNull();
    }

    @Test
    void selectsMatchingAgencyIdListWhenTargetNodeHasNoExplicitRestriction() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Agency List", "TEST", "1");
        AgencyIdListSummaryRecord target = agencyIdList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Agency List", "TEST", "1");

        AgencyIdListSummaryRecord result = service()
                .findTargetAgencyIdListMatch(source, List.of(target)).target();

        assertThat(result).isSameAs(target);
    }

    @Test
    void backendReviewUsesTheBieAvailableCodeListSelection() {
        CodeListSummaryRecord source = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Code List", "TEST", "1");
        CodeListSummaryRecord target = codeList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Code List", "TEST", "1");
        BieUpliftingService service = service();

        Object validation = ReflectionTestUtils.invokeMethod(service, "checkBdtCodeListMappable",
                source, List.of(target));

        assertThat((Boolean) ReflectionTestUtils.invokeMethod(validation, "valid")).isTrue();
        assertThat((String) ReflectionTestUtils.invokeMethod(validation, "issue")).isEmpty();
    }

    @Test
    void backendReviewUsesTheBieAvailableAgencyIdListSelection() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Test Agency List", "TEST", "1");
        AgencyIdListSummaryRecord target = agencyIdList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Test Agency List", "TEST", "1");
        BieUpliftingService service = service();

        Object validation = ReflectionTestUtils.invokeMethod(service, "checkBdtAgencyIdListMappable",
                source, List.of(target));

        assertThat((Boolean) ReflectionTestUtils.invokeMethod(validation, "valid")).isTrue();
        assertThat((String) ReflectionTestUtils.invokeMethod(validation, "issue")).isEmpty();
    }

    @Test
    void selectsTheAllowedTargetManifestForAGuidMatchedCodeList() {
        CodeListSummaryRecord source = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Source", "SOURCE", "1");
        CodeListSummaryRecord guidMatched = codeList(202, 8, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Target", "TARGET", "2");
        CodeListSummaryRecord allowedManifest = codeList(203, 8, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Target", "TARGET", "2");

        CodeListSummaryRecord result = service()
                .findTargetCodeListMatch(source, List.of(allowedManifest)).target();

        assertThat(result).isSameAs(allowedManifest);
    }

    @Test
    void selectsTheAllowedTargetManifestForAGuidMatchedAgencyIdList() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Source", "SOURCE", "1");
        AgencyIdListSummaryRecord guidMatched = agencyIdList(202, 8, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Target", "TARGET", "2");
        AgencyIdListSummaryRecord allowedManifest = agencyIdList(203, 8, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Target", "TARGET", "2");

        AgencyIdListSummaryRecord result = service()
                .findTargetAgencyIdListMatch(source, List.of(allowedManifest)).target();

        assertThat(result).isSameAs(allowedManifest);
    }

    @Test
    void doesNotTreatNullGuidsAsAnExactMatch() {
        CodeListSummaryRecord source = codeList(101, 7, null, "Source", "SOURCE", "1");
        CodeListSummaryRecord target = codeList(202, 8, null, "Target", "TARGET", "1");

        CodeListSummaryRecord result = service()
                .getTargetCodeListManifest(source, List.of(target));

        assertThat(result).isNull();
    }

    @Test
    void usesCcMatchingServiceForCodeAndAgencyIdentityRecords() {
        CcMatchingService matchingService = new CcMatchingService();
        CodeListSummaryRecord sourceCodeList = codeList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Code", "CODE", "1");
        CodeListSummaryRecord targetCodeList = codeList(202, 8, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Code", "CODE", "1");
        AgencyIdListSummaryRecord sourceAgencyIdList = agencyIdList(301, 9, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Agency", "AGENCY", "1");
        AgencyIdListSummaryRecord targetAgencyIdList = agencyIdList(302, 10, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Agency", "AGENCY", "1");

        assertThat(matchingService.score(sourceCodeList, targetCodeList)).isEqualTo(1.0d);
        assertThat(matchingService.score(sourceAgencyIdList, targetAgencyIdList)).isEqualTo(1.0d);
    }

    @Test
    void doesNotSelectAnUnrelatedTargetWhenSharedCodeListIdIsNull() {
        CodeListSummaryRecord source = codeList(101, null, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Source", "SOURCE", "1");
        CodeListSummaryRecord target = codeList(202, null, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Target", "TARGET", "1");

        assertThat(service().getTargetCodeListManifest(source, List.of(target))).isNull();
    }

    @Test
    void doesNotMatchCodeListOrAgencyIdListWhenTargetCollectionIsNull() {
        CodeListSummaryRecord sourceCodeList = codeList(101, 7,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Code", "CODE", "1");
        AgencyIdListSummaryRecord sourceAgencyIdList = agencyIdList(101, 7,
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Agency", "AGENCY", "1");

        assertThat(service().getTargetCodeListManifest(sourceCodeList, null)).isNull();
        assertThat(service().getTargetAgencyIdListManifest(sourceAgencyIdList, null)).isNull();
    }

    @Test
    void keepsCandidateOrderWhenFallbackCandidatesAreEquivalent() {
        CodeListSummaryRecord source = codeList(101, 7,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Code", "CODE", "1");
        CodeListSummaryRecord first = codeList(202, 8,
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Code", "CODE", "1");
        CodeListSummaryRecord second = codeList(203, 8,
                "cccccccccccccccccccccccccccccccc", "Code", "CODE", "1");

        assertThat(service().getTargetCodeListManifest(source, List.of(first, second)))
                .isSameAs(first);
    }

    @Test
    void rejectsExistingButUnrelatedTargetPathComponents() {
        CcDocument targetDocument = mock(CcDocument.class);
        when(targetDocument.getAsccp(new AsccpManifestId(BigInteger.ONE)))
                .thenReturn(mock(AsccpSummaryRecord.class));
        when(targetDocument.getBcc(new BccManifestId(BigInteger.TWO)))
                .thenReturn(mock(BccSummaryRecord.class));

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
                service(), "validateTargetPathComponents", targetDocument, "ASCCP-1>BCC-2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target mapping path contains unrelated components.");
    }

    @Test
    void acceptsTargetPathThroughInheritedAccChain() {
        CcDocument targetDocument = mock(CcDocument.class);
        AccSummaryRecord baseAcc = mock(AccSummaryRecord.class);
        AccSummaryRecord inheritedAcc = mock(AccSummaryRecord.class);
        BccSummaryRecord bcc = mock(BccSummaryRecord.class);

        when(targetDocument.getAcc(new AccManifestId(BigInteger.ONE))).thenReturn(baseAcc);
        when(targetDocument.getAcc(new AccManifestId(BigInteger.TWO))).thenReturn(inheritedAcc);
        when(inheritedAcc.basedAccManifestId()).thenReturn(new AccManifestId(BigInteger.ONE));
        when(targetDocument.getBcc(new BccManifestId(BigInteger.TEN))).thenReturn(bcc);
        when(bcc.fromAccManifestId()).thenReturn(new AccManifestId(BigInteger.TWO));

        assertThatCode(() -> ReflectionTestUtils.invokeMethod(
                service(), "validateTargetPathComponents", targetDocument, "ACC-1>ACC-2>BCC-10"))
                .doesNotThrowAnyException();

        BccSummaryRecord reverseOrderBcc = mock(BccSummaryRecord.class);
        when(targetDocument.getBcc(new BccManifestId(BigInteger.valueOf(11)))).thenReturn(reverseOrderBcc);
        when(reverseOrderBcc.fromAccManifestId()).thenReturn(new AccManifestId(BigInteger.ONE));
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(
                service(), "validateTargetPathComponents", targetDocument, "ACC-2>ACC-1>BCC-11"))
                .doesNotThrowAnyException();

        AccSummaryRecord unrelatedLeftAcc = mock(AccSummaryRecord.class);
        AccSummaryRecord unrelatedRightAcc = mock(AccSummaryRecord.class);
        when(targetDocument.getAcc(new AccManifestId(BigInteger.valueOf(3)))).thenReturn(unrelatedLeftAcc);
        when(targetDocument.getAcc(new AccManifestId(BigInteger.valueOf(4)))).thenReturn(unrelatedRightAcc);
        BccSummaryRecord unrelatedBcc = mock(BccSummaryRecord.class);
        when(targetDocument.getBcc(new BccManifestId(BigInteger.valueOf(12)))).thenReturn(unrelatedBcc);
        when(unrelatedBcc.fromAccManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(4)));
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
                service(), "validateTargetPathComponents", targetDocument, "ACC-3>ACC-4>BCC-12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target mapping path contains unrelated components.");
    }

    @Test
    void doesNotResolveAgencyIdListWhenItsValueNameDiffers() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Agency", "AGENCY", "1", "Source value");
        AgencyIdListSummaryRecord target = agencyIdList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Agency", "AGENCY", "1", "Target value");

        assertThat(service().getTargetAgencyIdListManifest(source, List.of(target))).isNull();
    }

    @Test
    void resolvesAgencyIdListWhenAllFallbackIdentifiersIncludingValueNameMatch() {
        AgencyIdListSummaryRecord source = agencyIdList(101, 7, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Agency", "AGENCY", "1", "Value");
        AgencyIdListSummaryRecord target = agencyIdList(202, 8, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "Agency", "AGENCY", "1", "Value");

        assertThat(service().getTargetAgencyIdListManifest(source, List.of(target))).isSameAs(target);
    }

    @Test
    void doesNotSelectAnUnrelatedTargetWhenSharedAgencyIdListIdIsNull() {
        AgencyIdListSummaryRecord source = agencyIdList(101, null, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Source", "SOURCE", "1");
        AgencyIdListSummaryRecord target = agencyIdList(202, null, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "Target", "TARGET", "1");

        assertThat(service().getTargetAgencyIdListManifest(source, List.of(target))).isNull();
    }

    private static BieUpliftingService service() {
        return new BieUpliftingService(new CcMatchingService());
    }

    private static CodeListSummaryRecord codeList(int manifestId, Integer codeListId, String guid,
                                                   String name, String listId, String versionId) {
        return new CodeListSummaryRecord(
                new CodeListManifestId(BigInteger.valueOf(manifestId)),
                codeListId != null ? new CodeListId(BigInteger.valueOf(codeListId)) : null,
                guid != null ? new Guid(guid) : null, null, null, null,
                name, listId, versionId, null, null, false,
                CcState.Published, null, null, null, null);
    }

    private static AgencyIdListSummaryRecord agencyIdList(int manifestId, int agencyIdListId, String guid,
                                                           String name, String listId, String versionId) {
        return agencyIdList(manifestId, agencyIdListId, guid, name, listId, versionId, null);
    }

    private static AgencyIdListSummaryRecord agencyIdList(int manifestId, Integer agencyIdListId, String guid,
                                                           String name, String listId, String versionId) {
        return agencyIdList(manifestId, agencyIdListId, guid, name, listId, versionId, null);
    }

    private static AgencyIdListSummaryRecord agencyIdList(int manifestId, Integer agencyIdListId, String guid,
                                                           String name, String listId, String versionId,
                                                           String valueName) {
        return new AgencyIdListSummaryRecord(
                new AgencyIdListManifestId(BigInteger.valueOf(manifestId)),
                agencyIdListId != null ? new AgencyIdListId(BigInteger.valueOf(agencyIdListId)) : null,
                new Guid(guid), null,
                null,
                name, listId, versionId, null, null, null, valueName,
                false, CcState.Published, null, null, null, null);
    }

    private static XbtSummaryRecord xbt(int manifestId, int xbtId, char guidCharacter) {
        return new XbtSummaryRecord(
                new XbtManifestId(BigInteger.valueOf(manifestId)),
                new XbtId(BigInteger.valueOf(xbtId)),
                null,
                new Guid(String.valueOf(guidCharacter).repeat(32)),
                "primitive-" + xbtId,
                "xsd:string",
                null, null, null, null, null, null);
    }
}
