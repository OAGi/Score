package org.oagi.score.gateway.http.api.bie_management.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitContext;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitResult;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitor;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;

import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BieDocumentImplTest {

    @Test
    void assignsDistinctOccurrenceContextToRepeatedAsbiepReferences() {
        Fixture fixture = fixture();
        List<Visit> visits = new ArrayList<>();
        assertThat(fixture.document.getAssociations(fixture.reusedAbie)).containsExactly(fixture.bbie);

        fixture.document.accept(new BieVisitor() {
            @Override
            public BieVisitResult visitAsbiep(Asbiep asbiep, BieVisitContext context) {
                visits.add(new Visit("ASBIEP", asbiep.getAsbiepId(), context.getOccurrencePath(),
                        context.getParentOccurrencePath()));
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitAbie(Abie abie, BieVisitContext context) {
                visits.add(new Visit("ABIE", abie.getAbieId(), context.getOccurrencePath(),
                        context.getParentOccurrencePath()));
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitAsbie(Asbie asbie, BieVisitContext context) {
                visits.add(new Visit("ASBIE", asbie.getAsbieId(), context.getOccurrencePath(),
                        context.getParentOccurrencePath()));
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitBbie(Bbie bbie, BieVisitContext context) {
                visits.add(new Visit("BBIE", bbie.getBbieId(), context.getOccurrencePath(),
                        context.getParentOccurrencePath()));
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitBbiep(Bbiep bbiep, BieVisitContext context) {
                visits.add(new Visit("BBIEP", bbiep.getBbiepId(), context.getOccurrencePath(),
                        context.getParentOccurrencePath()));
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
                visits.add(new Visit("BBIE_SC", bbieSc.getBbieScId(), context.getOccurrencePath(),
                        context.getParentOccurrencePath()));
                return BieVisitResult.CONTINUE;
            }
        });

        assertThat(visits).extracting(Visit::type)
                .containsExactly("ASBIEP", "ABIE", "ASBIE", "ASBIEP", "ABIE", "BBIE",
                        "BBIEP", "BBIE_SC", "ASBIE", "ASBIEP", "ABIE", "BBIE", "BBIEP",
                        "BBIE_SC");
        assertThat(visits).filteredOn(visit -> visit.type().equals("ASBIEP"))
                .extracting(Visit::id)
                .containsExactly(new AsbiepId(BigInteger.valueOf(1)),
                        new AsbiepId(BigInteger.valueOf(2)), new AsbiepId(BigInteger.valueOf(2)));
        assertThat(visits).filteredOn(visit -> visit.type().equals("BBIE_SC"))
                .extracting(Visit::id)
                .containsExactly(new BbieScId(BigInteger.valueOf(30)), new BbieScId(BigInteger.valueOf(30)));
        String root = "ASCCP-1";
        String rootAbie = root + ">ACC-1";
        String firstReference = rootAbie + ">ASCC-1";
        String firstAsbiep = firstReference + ">ASCCP-2";
        String firstAbie = firstAsbiep + ">ACC-2";
        String firstBbie = firstAbie + ">BCC-3";
        String secondReference = rootAbie + ">ASCC-2";
        String secondAsbiep = secondReference + ">ASCCP-2";
        String secondAbie = secondAsbiep + ">ACC-2";
        String secondBbie = secondAbie + ">BCC-3";
        String scSuffix = ">BCCP-21>DT-40>DT_SC-30";
        assertThat(visits).extracting(Visit::occurrencePath)
                .containsExactly(root, rootAbie, firstReference, firstAsbiep, firstAbie,
                        firstBbie, firstBbie, firstBbie + scSuffix, secondReference,
                        secondAsbiep, secondAbie, secondBbie, secondBbie, secondBbie + scSuffix);
        assertThat(visits).extracting(Visit::parentOccurrencePath)
                .containsExactly(null, root, rootAbie, firstReference, firstAsbiep,
                        firstAbie, firstAbie, firstBbie, rootAbie, secondReference,
                        secondAsbiep, secondAbie, secondAbie, secondBbie);
    }

    @Test
    void skippingAReusedAsbieReferenceDoesNotTraverseItsSubtree() {
        Fixture fixture = fixture();
        List<String> visitedTypes = new ArrayList<>();

        fixture.document.accept(new BieVisitor() {
            @Override
            public BieVisitResult visitAsbie(Asbie asbie, BieVisitContext context) {
                visitedTypes.add("ASBIE-" + asbie.getAsbieId());
                return asbie.getAsbieId().equals(new AsbieId(BigInteger.valueOf(1)))
                        ? BieVisitResult.SKIP_SUBTREE
                        : BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitAsbiep(Asbiep asbiep, BieVisitContext context) {
                visitedTypes.add("ASBIEP-" + asbiep.getAsbiepId());
                return BieVisitResult.CONTINUE;
            }
        });

        assertThat(visitedTypes).containsExactly("ASBIEP-1", "ASBIE-1", "ASBIE-2",
                "ASBIEP-2");
    }

    @Test
    void nestedReuseKeepsGroupPathsAndSupplementaryComponentOwnersDistinct() {
        Fixture fixture = fixture(true);
        List<BieVisitContext> bbieContexts = new ArrayList<>();
        List<BieVisitContext> scContexts = new ArrayList<>();
        fixture.document.accept(new BieVisitor() {
            @Override
            public BieVisitResult visitBbie(Bbie bbie, BieVisitContext context) {
                bbieContexts.add(context);
                return BieVisitResult.CONTINUE;
            }

            @Override
            public BieVisitResult visitBbieSc(BbieSc sc, BieVisitContext context) {
                scContexts.add(context);
                return BieVisitResult.CONTINUE;
            }
        });

        // Inspect retained contexts after traversal, so mutable traversal state cannot pass this check.
        List<String> expectedOwners = new ArrayList<>();
        List<String> expectedBbies = new ArrayList<>();
        for (int outer : List.of(1, 2)) {
            for (int inner : List.of(3, 4)) {
                String owner = "ASCCP-1>ACC-1>ASCC-" + outer
                        + ">ASCCP-2>ACC-2>ASCC-" + inner + ">ASCCP-3>ACC-3";
                expectedOwners.add(owner);
                expectedBbies.add(owner + ">ASCC-5>ASCCP-5>ACC-4>BCC-3");
            }
        }
        assertThat(bbieContexts).extracting(BieVisitContext::getOccurrencePath)
                .containsExactlyElementsOf(expectedBbies);
        assertThat(bbieContexts).extracting(BieVisitContext::getParentOccurrencePath)
                .containsExactlyElementsOf(expectedOwners);
        assertThat(scContexts).extracting(BieVisitContext::getParentOccurrencePath)
                .containsExactlyElementsOf(expectedBbies);
        assertThat(scContexts).extracting(BieVisitContext::getOccurrencePath)
                .containsExactlyElementsOf(expectedBbies.stream()
                        .map(path -> path + ">BCCP-21>DT-40>DT_SC-30").toList());
    }

    private Fixture fixture() {
        return fixture(false);
    }

    private Fixture fixture(boolean nestedReuse) {
        TopLevelAsbiepId topLevelId = new TopLevelAsbiepId(BigInteger.valueOf(99));
        AsbiepId rootAsbiepId = new AsbiepId(BigInteger.ONE);
        AsbiepId reusedAsbiepId = new AsbiepId(BigInteger.TWO);
        AbieId rootAbieId = new AbieId(BigInteger.ONE);
        AbieId reusedAbieId = new AbieId(BigInteger.TWO);

        Asbiep rootAsbiep = asbiep(rootAsbiepId, rootAbieId, topLevelId);
        Asbiep reusedAsbiep = asbiep(reusedAsbiepId, reusedAbieId, topLevelId);
        Abie rootAbie = abie(rootAbieId, new AccManifestId(BigInteger.ONE), topLevelId);
        Abie reusedAbie = abie(reusedAbieId, new AccManifestId(BigInteger.TWO), topLevelId);

        Asbie firstReference = asbie(1, rootAbieId, reusedAsbiepId, topLevelId, 1);
        Asbie secondReference = asbie(2, rootAbieId, reusedAsbiepId, topLevelId, 2);
        Bbie bbie = new Bbie();
        bbie.setBbieId(new BbieId(BigInteger.valueOf(20)));
        bbie.setFromAbieId(reusedAbieId);
        bbie.setToBbiepId(new BbiepId(BigInteger.valueOf(21)));
        bbie.setBasedBccManifestId(new BccManifestId(BigInteger.valueOf(3)));
        Bbiep bbiep = new Bbiep();
        bbiep.setBbiepId(new BbiepId(BigInteger.valueOf(21)));
        bbiep.setBasedBccpManifestId(new BccpManifestId(BigInteger.valueOf(21)));
        BbieSc bbieSc = new BbieSc();
        bbieSc.setBbieScId(new BbieScId(BigInteger.valueOf(30)));
        bbieSc.setBbieId(bbie.getBbieId());
        bbieSc.setBasedDtScManifestId(new DtScManifestId(BigInteger.valueOf(30)));

        AccSummaryRecord rootAcc = mock(AccSummaryRecord.class);
        AccSummaryRecord reusedAcc = mock(AccSummaryRecord.class);
        when(rootAcc.accManifestId()).thenReturn(new AccManifestId(BigInteger.ONE));
        when(reusedAcc.accManifestId()).thenReturn(new AccManifestId(BigInteger.TWO));
        when(rootAcc.componentType()).thenReturn(OagisComponentType.Base);
        when(reusedAcc.componentType()).thenReturn(OagisComponentType.Base);
        AsccSummaryRecord firstAscc = ascc(1);
        AsccSummaryRecord secondAscc = ascc(2);
        BccSummaryRecord bcc = mock(BccSummaryRecord.class);
        when(bcc.isBcc()).thenReturn(true);
        when(bcc.bccManifestId()).thenReturn(new BccManifestId(BigInteger.valueOf(3)));
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.roleOfAccManifestId()).thenReturn(new AccManifestId(BigInteger.TWO));

        BccpSummaryRecord bccp = mock(BccpSummaryRecord.class);
        when(bccp.bccpManifestId()).thenReturn(new BccpManifestId(BigInteger.valueOf(21)));
        DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
        when(dtSc.dtScManifestId()).thenReturn(new DtScManifestId(BigInteger.valueOf(30)));
        when(dtSc.ownerDtManifestId()).thenReturn(new DtManifestId(BigInteger.valueOf(40)));
        CcDocument ccDocument = mock(CcDocument.class);
        when(ccDocument.getBccp(new BccpManifestId(BigInteger.valueOf(21)))).thenReturn(bccp);
        when(ccDocument.getDtSc(new DtScManifestId(BigInteger.valueOf(30)))).thenReturn(dtSc);
        when(ccDocument.getAcc(new AccManifestId(BigInteger.ONE))).thenReturn(rootAcc);
        when(ccDocument.getAcc(new AccManifestId(BigInteger.TWO))).thenReturn(reusedAcc);
        when(ccDocument.getAsccp(any(AsccpManifestId.class))).thenReturn(asccp);
        when(ccDocument.getBcc(any(BccManifestId.class))).thenReturn(bcc);
        when(ccDocument.getAssociations(rootAcc)).thenReturn(List.of(firstAscc, secondAscc));
        when(ccDocument.getAssociations(reusedAcc)).thenReturn(List.of(bcc));

        List<Asbiep> asbieps = new ArrayList<>(List.of(rootAsbiep, reusedAsbiep));
        List<Abie> abies = new ArrayList<>(List.of(rootAbie, reusedAbie));
        List<Asbie> asbies = new ArrayList<>(List.of(firstReference, secondReference));
        if (nestedReuse) {
            AbieId nestedAbieId = new AbieId(BigInteger.valueOf(3));
            AsbiepId nestedAsbiepId = new AsbiepId(BigInteger.valueOf(3));
            asbieps.add(asbiep(nestedAsbiepId, nestedAbieId, topLevelId));
            abies.add(abie(nestedAbieId, new AccManifestId(BigInteger.valueOf(3)), topLevelId));
            asbies.add(asbie(3, reusedAbieId, nestedAsbiepId, topLevelId, 3));
            asbies.add(asbie(4, reusedAbieId, nestedAsbiepId, topLevelId, 4));
            bbie.setFromAbieId(nestedAbieId);

            AccSummaryRecord nestedAcc = mock(AccSummaryRecord.class);
            when(nestedAcc.accManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(3)));
            AccSummaryRecord groupAcc = mock(AccSummaryRecord.class);
            when(groupAcc.accManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(4)));
            when(groupAcc.isGroup()).thenReturn(true);
            AsccpSummaryRecord nestedAsccp = mock(AsccpSummaryRecord.class);
            when(nestedAsccp.roleOfAccManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(3)));
            AsccpSummaryRecord groupAsccp = mock(AsccpSummaryRecord.class);
            when(groupAsccp.asccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(5)));
            when(groupAsccp.roleOfAccManifestId()).thenReturn(new AccManifestId(BigInteger.valueOf(4)));
            when(ccDocument.getAcc(new AccManifestId(BigInteger.valueOf(3)))).thenReturn(nestedAcc);
            when(ccDocument.getAcc(new AccManifestId(BigInteger.valueOf(4)))).thenReturn(groupAcc);
            when(ccDocument.getAsccp(new AsccpManifestId(BigInteger.valueOf(3)))).thenReturn(nestedAsccp);
            when(ccDocument.getAsccp(new AsccpManifestId(BigInteger.valueOf(4)))).thenReturn(nestedAsccp);
            when(ccDocument.getAsccp(new AsccpManifestId(BigInteger.valueOf(5)))).thenReturn(groupAsccp);
            AsccSummaryRecord firstNestedAscc = ascc(3);
            AsccSummaryRecord secondNestedAscc = ascc(4);
            AsccSummaryRecord groupAscc = ascc(5);
            when(ccDocument.getAssociations(reusedAcc)).thenReturn(List.of(firstNestedAscc, secondNestedAscc));
            when(ccDocument.getAssociations(nestedAcc)).thenReturn(List.of(groupAscc));
            when(ccDocument.getAssociations(groupAcc)).thenReturn(List.of(bcc));
        }

        BieSet bieSet = new BieSet();
        bieSet.setTopLevelAsbiep(null);
        bieSet.setAsbiepList(asbieps);
        bieSet.setAbieList(abies);
        bieSet.setAsbieList(asbies);
        bieSet.setBbieList(List.of(bbie));
        bieSet.setBbiepList(List.of(bbiep));
        bieSet.setBbieScList(List.of(bbieSc));

        BieDocumentImpl document = new BieDocumentImpl(bieSet);
        document.setTopLevelAsbiep(new TopLevelAsbiepSummaryRecord(
                null, null, topLevelId, null, rootAsbiepId, null, null, null, null, null,
                null, null, false, false, null, null, null));
        document.with(ccDocument);
        return new Fixture(document, reusedAbie, bbie);
    }

    private Asbiep asbiep(AsbiepId id, AbieId roleOfAbieId, TopLevelAsbiepId ownerId) {
        Asbiep asbiep = new Asbiep();
        asbiep.setAsbiepId(id);
        asbiep.setBasedAsccpManifestId(new AsccpManifestId(id.value()));
        asbiep.setRoleOfAbieId(roleOfAbieId);
        asbiep.setOwnerTopLevelAsbiepId(ownerId);
        return asbiep;
    }

    private Abie abie(AbieId id, AccManifestId basedAccManifestId, TopLevelAsbiepId ownerId) {
        Abie abie = new Abie();
        abie.setAbieId(id);
        abie.setBasedAccManifestId(basedAccManifestId);
        abie.setOwnerTopLevelAsbiepId(ownerId);
        return abie;
    }

    private Asbie asbie(int id, AbieId fromAbieId, AsbiepId toAsbiepId,
                        TopLevelAsbiepId ownerId, int basedAsccId) {
        Asbie asbie = new Asbie();
        asbie.setAsbieId(new AsbieId(BigInteger.valueOf(id)));
        asbie.setFromAbieId(fromAbieId);
        asbie.setToAsbiepId(toAsbiepId);
        asbie.setBasedAsccManifestId(new AsccManifestId(BigInteger.valueOf(basedAsccId)));
        asbie.setOwnerTopLevelAsbiepId(ownerId);
        return asbie;
    }

    private AsccSummaryRecord ascc(int id) {
        AsccSummaryRecord ascc = mock(AsccSummaryRecord.class);
        when(ascc.isAscc()).thenReturn(true);
        when(ascc.asccManifestId()).thenReturn(new AsccManifestId(BigInteger.valueOf(id)));
        when(ascc.toAsccpManifestId()).thenReturn(new AsccpManifestId(BigInteger.valueOf(id)));
        return ascc;
    }

    private record Fixture(BieDocumentImpl document, Abie reusedAbie, Bbie bbie) {}

    private record Visit(String type, Object id, String occurrencePath, String parentOccurrencePath) {}
}
