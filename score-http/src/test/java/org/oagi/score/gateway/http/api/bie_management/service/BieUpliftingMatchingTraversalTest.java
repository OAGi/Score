package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTable;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BieUpliftingMatchingTraversalTest {

    private static final String ROOT_PATH = "ASCCP-10>ACC-100";
    private static final String SOURCE_ASCC_PATH = ROOT_PATH + ">ASCC-1";
    private static final String TARGET_ASCC_PATH = ROOT_PATH + ">ASCC-2";

    @Test
    void analysisTraversalUsesSharedAutomaticAsccMatching() throws Exception {
        Fixture fixture = fixture();
        Class<?> diffType = Class.forName(BieUpliftingService.class.getName() + "$BieDiff");
        Constructor<?> constructor = diffType.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        BieVisitor visitor = (BieVisitor) constructor.newInstance(fixture.service, fixture.source,
                fixture.target, new AsccpManifestId(BigInteger.TEN));
        BieUpliftingListener listener = mock(BieUpliftingListener.class);
        ReflectionTestUtils.invokeMethod(visitor, "addListener", listener);

        visitor.visitAsbiep(new Asbiep(), context(fixture.source, "ASCCP-10", null));
        visitor.visitAbie(new Abie(), context(fixture.source, ROOT_PATH, "ASCCP-10"));
        visitor.visitAsbie(sourceAsbie(), context(fixture.source, SOURCE_ASCC_PATH, ROOT_PATH));

        verify(listener).foundBestMatchedAsbie(
                org.mockito.ArgumentMatchers.any(Asbie.class),
                eq(fixture.sourceAscc),
                eq(SOURCE_ASCC_PATH),
                isNull(),
                eq(fixture.targetAscc),
                eq(TARGET_ASCC_PATH));
    }

    @Test
    void generationTraversalUsesSharedAutomaticAsccMatching() throws Exception {
        Fixture fixture = fixture();
        Class<?> handlerType = Class.forName(BieUpliftingService.class.getName() + "$BieUpliftingHandler");
        Constructor<?> constructor = handlerType.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        BieVisitor visitor = (BieVisitor) constructor.newInstance(fixture.service, mock(ScoreUser.class),
                List.of(), new BieUpliftingCustomMappingTable(List.of()), fixture.source, fixture.target,
                new AsccpManifestId(BigInteger.TEN), List.of(), List.of(), List.of(), null, List.of(), null);

        visitor.visitAsbiep(new Asbiep(), context(fixture.source, "ASCCP-10", null));
        visitor.visitAbie(new Abie(), context(fixture.source, ROOT_PATH, "ASCCP-10"));
        assertThat(visitor.visitAsbie(sourceAsbie(), context(fixture.source, SOURCE_ASCC_PATH, ROOT_PATH)))
                .isEqualTo(BieVisitResult.CONTINUE);

        Map<?, ?> occurrences = (Map<?, ?>) ReflectionTestUtils.getField(visitor, "occurrences");
        Object occurrence = occurrences.get(SOURCE_ASCC_PATH);
        assertThat(ReflectionTestUtils.getField(occurrence, "targetPath")).isEqualTo(TARGET_ASCC_PATH);
    }

    private Fixture fixture() {
        CcDocument sourceCc = mock(CcDocument.class);
        CcDocument targetCc = mock(CcDocument.class);
        BieDocument source = mock(BieDocument.class);
        when(source.getCcDocument()).thenReturn(sourceCc);

        AsccSummaryRecord sourceAscc = ascc(1, "a");
        AsccSummaryRecord targetAscc = ascc(2, "a");
        when(sourceCc.getAscc(new AsccManifestId(BigInteger.ONE))).thenReturn(sourceAscc);
        when(targetCc.getAscc(new AsccManifestId(BigInteger.valueOf(2)))).thenReturn(targetAscc);

        AsccpSummaryRecord rootAsccp = mock(AsccpSummaryRecord.class);
        when(rootAsccp.asccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.TEN));
        when(rootAsccp.roleOfAccManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(100)));
        when(targetCc.getAsccp(new AsccpManifestId(BigInteger.TEN))).thenReturn(rootAsccp);
        when(targetAscc.toAsccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(20)));
        AsccpSummaryRecord targetAssociationAsccp = mock(AsccpSummaryRecord.class);
        when(targetAssociationAsccp.roleOfAccManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(200)));
        when(targetCc.getAsccp(new AsccpManifestId(BigInteger.valueOf(20))))
                .thenReturn(targetAssociationAsccp);
        when(targetCc.getAcc(new AccManifestId(BigInteger.valueOf(200))))
                .thenReturn(mock(AccSummaryRecord.class));

        AccSummaryRecord rootAcc = mock(AccSummaryRecord.class);
        when(rootAcc.accManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(100)));
        when(targetCc.getAcc(new AccManifestId(BigInteger.valueOf(100)))).thenReturn(rootAcc);
        when(targetCc.getAssociations(rootAcc)).thenReturn(List.of(targetAscc));

        return new Fixture(new BieUpliftingService(new CcMatchingService()), source, targetCc, sourceAscc, targetAscc);
    }

    private Asbie sourceAsbie() {
        Asbie asbie = new Asbie();
        asbie.setBasedAsccManifestId(new AsccManifestId(BigInteger.ONE));
        return asbie;
    }

    private AsccSummaryRecord ascc(int manifestId, String guidCharacter) {
        AsccSummaryRecord ascc = mock(AsccSummaryRecord.class);
        when(ascc.isAscc()).thenReturn(true);
        when(ascc.asccManifestId()).thenReturn(new AsccManifestId(BigInteger.valueOf(manifestId)));
        when(ascc.guid()).thenReturn(new Guid(guidCharacter.repeat(32)));
        return ascc;
    }

    private BieVisitContext context(BieDocument document, String path, String parent) {
        return new BieVisitContext() {
            public BieDocument getBieDocument() { return document; }
            public String getOccurrencePath() { return path; }
            public String getParentOccurrencePath() { return parent; }
        };
    }

    private record Fixture(BieUpliftingService service, BieDocument source, CcDocument target,
                           AsccSummaryRecord sourceAscc, AsccSummaryRecord targetAscc) {
    }
}
