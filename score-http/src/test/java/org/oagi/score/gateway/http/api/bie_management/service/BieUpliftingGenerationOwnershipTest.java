package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBieRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.CreateBieResponse;
import org.oagi.score.gateway.http.api.bie_management.model.*;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.repository.BieCommandRepository;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.*;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.*;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.*;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.*;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.*;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BieUpliftingGenerationOwnershipTest {
    private static final String SOURCE_ROOT = "ASCCP-1";
    private static final String SOURCE_OWNER = SOURCE_ROOT + ">ACC-1";
    private static final String TARGET_ROOT = "ASCCP-100>ACC-100";
    private static final String TARGET_BRANCH = TARGET_ROOT + ">ASCC-90>ASCCP-90>ACC-90";

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void customMappingsResolveActualLaterTargetOwnersAndSelectedReuseSkipsInlineCopy(boolean mapSourceBbie) throws Exception {
        CcDocument sourceCc = mock(CcDocument.class);
        CcDocument targetCc = mock(CcDocument.class);
        BieDocument source = mock(BieDocument.class);
        when(source.getCcDocument()).thenReturn(sourceCc);
        AsccpSummaryRecord root = asccp(100, 100);
        AsccpSummaryRecord branch = asccp(90, 90);
        when(targetCc.getAsccp(new AsccpManifestId(BigInteger.valueOf(100)))).thenReturn(root);
        when(targetCc.getAsccp(new AsccpManifestId(BigInteger.valueOf(90)))).thenReturn(branch);
        AccSummaryRecord rootAcc = acc(100);
        AccSummaryRecord branchAcc = acc(90);
        when(targetCc.getAcc(new AccManifestId(BigInteger.valueOf(100)))).thenReturn(rootAcc);
        when(targetCc.getAcc(new AccManifestId(BigInteger.valueOf(90)))).thenReturn(branchAcc);
        AsccSummaryRecord selectedAscc = ascc(10, 10);
        AsccSummaryRecord branchAscc = ascc(90, 90);
        when(targetCc.getAscc(new AsccManifestId(BigInteger.TEN))).thenReturn(selectedAscc);
        when(targetCc.getAscc(new AsccManifestId(BigInteger.valueOf(90)))).thenReturn(branchAscc);
        AsccSummaryRecord firstSourceAscc = ascc(1, 1);
        AsccSummaryRecord secondSourceAscc = ascc(2, 2);
        when(sourceCc.getAscc(new AsccManifestId(BigInteger.ONE))).thenReturn(firstSourceAscc);
        when(sourceCc.getAscc(new AsccManifestId(BigInteger.TWO))).thenReturn(secondSourceAscc);

        BccpSummaryRecord bccp = mock(BccpSummaryRecord.class);
        when(bccp.bccpManifestId()).thenReturn(new BccpManifestId(BigInteger.valueOf(30)));
        when(bccp.dtManifestId()).thenReturn(new DtManifestId(BigInteger.valueOf(40)));
        when(targetCc.getBccp(any())).thenReturn(bccp);
        BccSummaryRecord firstBcc = bcc(20);
        BccSummaryRecord secondBcc = bcc(21);
        BccSummaryRecord automaticCandidate = bcc(22);
        when(targetCc.getBcc(new BccManifestId(BigInteger.valueOf(20)))).thenReturn(firstBcc);
        when(targetCc.getBcc(new BccManifestId(BigInteger.valueOf(21)))).thenReturn(secondBcc);
        when(targetCc.getBcc(new BccManifestId(BigInteger.valueOf(22)))).thenReturn(automaticCandidate);
        BccSummaryRecord firstSourceBcc = bcc(1);
        BccSummaryRecord secondSourceBcc = bcc(2);
        BccSummaryRecord suppressedSourceBcc = bcc(3);
        Guid suppressedGuid = new Guid("3".repeat(32));
        when(suppressedSourceBcc.guid()).thenReturn(suppressedGuid);
        when(automaticCandidate.guid()).thenReturn(suppressedGuid);
        when(sourceCc.getBcc(new BccManifestId(BigInteger.ONE))).thenReturn(firstSourceBcc);
        when(sourceCc.getBcc(new BccManifestId(BigInteger.TWO))).thenReturn(secondSourceBcc);
        when(sourceCc.getBcc(new BccManifestId(BigInteger.valueOf(3)))).thenReturn(suppressedSourceBcc);
        when(targetCc.getAssociations(rootAcc)).thenReturn(List.of(automaticCandidate));
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        when(dt.dtManifestId()).thenReturn(new DtManifestId(BigInteger.valueOf(40)));
        when(targetCc.getDt(any())).thenReturn(dt);
        DtAwdPriSummaryRecord primitive = mock(DtAwdPriSummaryRecord.class);
        when(primitive.isDefault()).thenReturn(true);
        when(targetCc.getDtAwdPriList(any())).thenReturn(List.of(primitive));
        DtScSummaryRecord sc = mock(DtScSummaryRecord.class);
        when(sc.dtScManifestId()).thenReturn(new DtScManifestId(BigInteger.valueOf(50)));
        when(targetCc.getDtSc(any())).thenReturn(sc);
        DtScSummaryRecord sourceScRecord = mock(DtScSummaryRecord.class);
        when(sourceScRecord.dtScManifestId()).thenReturn(new DtScManifestId(BigInteger.ONE));
        when(sourceScRecord.ownerDtManifestId()).thenReturn(new DtManifestId(BigInteger.ONE));
        when(sourceCc.getDtSc(new DtScManifestId(BigInteger.ONE))).thenReturn(sourceScRecord);
        DtScAwdPriSummaryRecord scPrimitive = mock(DtScAwdPriSummaryRecord.class);
        when(scPrimitive.isDefault()).thenReturn(true);
        when(targetCc.getDtScAwdPriList(any())).thenReturn(List.of(scPrimitive));

        String sourceBbie = SOURCE_OWNER + ">BCC-1";
        String sourceSc = sourceBbie + ">BCCP-1>DT-1>DT_SC-1";
        BieUpliftingMapping selected = mapping("ASBIE", SOURCE_OWNER + ">ASCC-1", TARGET_BRANCH + ">ASCC-10", 10);
        TopLevelAsbiepId reuseId = new TopLevelAsbiepId(BigInteger.valueOf(999));
        selected.setRefTopLevelAsbiepId(reuseId);
        BieUpliftingMapping explicitlyUnmatched = mapping("BBIE", SOURCE_OWNER + ">BCC-3", null, 3);
        explicitlyUnmatched.setSuppressAutoMapping(true);
        List<BieUpliftingMapping> mappingList = new ArrayList<>(List.of(
                selected,
                explicitlyUnmatched,
                mapping("BBIE_SC", sourceSc, TARGET_BRANCH + ">BCC-21>BCCP-30>DT-40>DT_SC-50", 50),
                mapping("BBIE", SOURCE_OWNER + ">BCC-2", TARGET_BRANCH + ">BCC-21", 21),
                mapping("ASBIE", SOURCE_OWNER + ">ASCC-2", TARGET_ROOT + ">ASCC-90", 90)));
        if (mapSourceBbie) {
            mappingList.add(mapping("BBIE", sourceBbie, TARGET_BRANCH + ">BCC-20", 20));
        }
        BieUpliftingCustomMappingTable mappings = new BieUpliftingCustomMappingTable(mappingList);

        ScoreUser requester = mock(ScoreUser.class);
        RepositoryFactory repositories = mock(RepositoryFactory.class, RETURNS_DEEP_STUBS);
        BieCommandRepository command = mock(BieCommandRepository.class);
        when(repositories.bieCommandRepository(requester)).thenReturn(command);
        when(command.createBie(any())).thenReturn(new CreateBieResponse(new TopLevelAsbiepId(BigInteger.valueOf(1000))));
        BieUpliftingService service = new BieUpliftingService(new CcMatchingService());
        ReflectionTestUtils.setField(service, "repositoryFactory", repositories);
        BieVisitor visitor = handler(service, requester, source, targetCc, mappings);

        // Callback order deliberately creates mapped owners after the children referencing them.
        visitor.visitAsbiep(new Asbiep(), context(source, SOURCE_ROOT, null));
        visitor.visitAbie(new Abie(), context(source, SOURCE_OWNER, SOURCE_ROOT));
        assertThat(visitor.visitAsbie(sourceAsbie(1), context(source, SOURCE_OWNER + ">ASCC-1", SOURCE_OWNER)))
                .isEqualTo(BieVisitResult.SKIP_SUBTREE);
        visitor.visitBbie(sourceBbie(1), context(source, sourceBbie, SOURCE_OWNER));
        visitor.visitBbiep(new Bbiep(), context(source, sourceBbie, SOURCE_OWNER));
        BbieSc sourceSupplementaryComponent = new BbieSc();
        sourceSupplementaryComponent.setBasedDtScManifestId(new DtScManifestId(BigInteger.ONE));
        visitor.visitBbieSc(sourceSupplementaryComponent, context(source, sourceSc, sourceBbie));
        visitor.visitBbie(sourceBbie(2), context(source, SOURCE_OWNER + ">BCC-2", SOURCE_OWNER));
        visitor.visitBbiep(new Bbiep(), context(source, SOURCE_OWNER + ">BCC-2", SOURCE_OWNER));
        visitor.visitBbie(sourceBbie(3), context(source, SOURCE_OWNER + ">BCC-3", SOURCE_OWNER));
        String inlineSource = SOURCE_OWNER + ">ASCC-2";
        assertThat(visitor.visitAsbie(sourceAsbie(2), context(source, inlineSource, SOURCE_OWNER)))
                .isEqualTo(BieVisitResult.CONTINUE);
        visitor.visitAsbiep(new Asbiep(), context(source, inlineSource + ">ASCCP-2", inlineSource));
        visitor.visitAbie(new Abie(), context(source, inlineSource + ">ASCCP-2>ACC-2", inlineSource + ">ASCCP-2"));
        visitor.visitEnd(mock(TopLevelAsbiepSummaryRecord.class), context(source, null, null));

        ArgumentCaptor<CreateBieRequest> capture = ArgumentCaptor.forClass(CreateBieRequest.class);
        verify(command).createBie(capture.capture());
        CreateBieRequest request = capture.getValue();
        assertThat(request.getAsbieList()).hasSize(2);
        var reference = request.getAsbieList().get(0);
        var inline = request.getAsbieList().get(1);
        Abie actualBranch = inline.getToAsbiep().getRoleOfAbie();
        assertThat(actualBranch.getPath()).isEqualTo(TARGET_BRANCH);
        assertThat(reference.getFromAbie()).isSameAs(actualBranch);
        assertThat(reference.getRefTopLevelAsbiepId()).isEqualTo(reuseId);
        assertThat(reference.getToAsbiep()).isNull();
        assertThat(request.getBbieList()).hasSize(mapSourceBbie ? 2 : 1).allSatisfy(bbie ->
                assertThat(bbie.getFromAbie()).isSameAs(actualBranch));
        assertThat(request.getBbieList()).noneMatch(bbie -> bbie.getBbie().getBasedBccManifestId()
                .equals(new BccManifestId(BigInteger.valueOf(22))));
        assertThat(request.getBbieScList()).hasSize(1);
        Bbie actualScOwner = request.getBbieList().get(mapSourceBbie ? 1 : 0).getBbie();
        assertThat(actualScOwner.getPath()).isEqualTo(TARGET_BRANCH + ">BCC-21");
        assertThat(request.getBbieScList().get(0).getBbie()).isSameAs(actualScOwner);
        if (mapSourceBbie) {
            assertThat(actualScOwner).isNotSameAs(request.getBbieList().get(0).getBbie());
        }
    }

    private Asbie sourceAsbie(int manifestId) {
        Asbie asbie = new Asbie();
        asbie.setBasedAsccManifestId(new AsccManifestId(BigInteger.valueOf(manifestId)));
        return asbie;
    }

    private Bbie sourceBbie(int manifestId) {
        Bbie bbie = new Bbie();
        bbie.setBasedBccManifestId(new BccManifestId(BigInteger.valueOf(manifestId)));
        return bbie;
    }

    private BieVisitor handler(BieUpliftingService service, ScoreUser requester, BieDocument source,
                               CcDocument target, BieUpliftingCustomMappingTable mappings) throws Exception {
        Class<?> type = Class.forName(BieUpliftingService.class.getName() + "$BieUpliftingHandler");
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return (BieVisitor) constructor.newInstance(service, requester, List.of(), mappings, source, target,
                new AsccpManifestId(BigInteger.valueOf(100)), List.of(), List.of(), List.of(), null, List.of(), null);
    }

    private BieVisitContext context(BieDocument document, String path, String owner) {
        return new BieVisitContext() {
            public BieDocument getBieDocument() { return document; }
            public String getOccurrencePath() { return path; }
            public String getParentOccurrencePath() { return owner; }
        };
    }

    private BieUpliftingMapping mapping(String type, String source, String target, int id) {
        BieUpliftingMapping mapping = new BieUpliftingMapping();
        mapping.setBieType(type);
        mapping.setSourcePath(source);
        mapping.setTargetPath(target);
        mapping.setTargetManifestId(BigInteger.valueOf(id));
        return mapping;
    }

    private AccSummaryRecord acc(int id) {
        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(acc.accManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(id)));
        return acc;
    }

    private AsccpSummaryRecord asccp(int id, int acc) {
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.asccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(id)));
        when(asccp.roleOfAccManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(acc)));
        return asccp;
    }

    private AsccSummaryRecord ascc(int id, int asccp) {
        AsccSummaryRecord ascc = mock(AsccSummaryRecord.class);
        when(ascc.isAscc()).thenReturn(true);
        when(ascc.asccManifestId()).thenReturn(new AsccManifestId(BigInteger.valueOf(id)));
        when(ascc.toAsccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(asccp)));
        return ascc;
    }

    private BccSummaryRecord bcc(int id) {
        BccSummaryRecord bcc = mock(BccSummaryRecord.class);
        when(bcc.isBcc()).thenReturn(true);
        when(bcc.bccManifestId()).thenReturn(new BccManifestId(BigInteger.valueOf(id)));
        when(bcc.toBccpManifestId()).thenReturn(new BccpManifestId(BigInteger.valueOf(30)));
        return bcc;
    }
}
