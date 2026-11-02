package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;

import java.math.BigInteger;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A small graph of core components in memory, the way a {@link CcDocument} answers for it. */
class AccFixture {

    static final ReleaseSummaryRecord RELEASE = new ReleaseSummaryRecord(new ReleaseId(BigInteger.ONE), null, "1", null);

    private final Map<AccManifestId, AccSummaryRecord> accs = new HashMap<>();
    private final Map<AccManifestId, List<CcAssociation>> associations = new HashMap<>();
    private final Map<AsccpManifestId, AsccpSummaryRecord> asccps = new HashMap<>();
    private final Map<BccpManifestId, BccpSummaryRecord> bccps = new HashMap<>();

    AccSummaryRecord acc(long id, String objectClass, OagisComponentType type, Long basedId) {
        AccSummaryRecord acc = new AccSummaryRecord(null, RELEASE, new AccManifestId(BigInteger.valueOf(id)), null, null, "Default",
                basedId == null ? null : new AccManifestId(BigInteger.valueOf(basedId)),
                objectClass + ". Details", objectClass, null, type, false, false, CcState.WIP, null, null, 1, null, null, null);
        accs.put(acc.accManifestId(), acc);
        associations.put(acc.accManifestId(), new ArrayList<>());
        return acc;
    }

    void bcc(AccSummaryRecord from, long bccId, long bccpId, String term) {
        BccpSummaryRecord bccp = new BccpSummaryRecord(null, RELEASE, new BccpManifestId(BigInteger.valueOf(bccpId)), null, null, null,
                term + ". Text", term, "Text", false, false, CcState.WIP, null, null, null, 1, null, null, null);
        bccps.put(bccp.bccpManifestId(), bccp);
        associations.get(from.accManifestId()).add(new BccSummaryRecord(null, RELEASE, new BccManifestId(BigInteger.valueOf(bccId)), null, null,
                from.accManifestId(), bccp.bccpManifestId(), null, null, null, null, null, null, false, false, CcState.WIP,
                null, null, 1, null, null, null));
    }

    void ascc(AccSummaryRecord from, long asccId, long asccpId, String term, AccSummaryRecord roleOf) {
        AsccpSummaryRecord asccp = new AsccpSummaryRecord(null, RELEASE, new AsccpManifestId(BigInteger.valueOf(asccpId)), null, null, null,
                roleOf.accManifestId(), term + ". " + roleOf.objectClassTerm(), term, true, false, false, CcState.WIP,
                null, null, 1, null, null, null);
        asccps.put(asccp.asccpManifestId(), asccp);
        associations.get(from.accManifestId()).add(new AsccSummaryRecord(null, RELEASE, new AsccManifestId(BigInteger.valueOf(asccId)), null, null,
                from.accManifestId(), asccp.asccpManifestId(), null, null, null, null, null, false, CcState.WIP,
                null, 1, null, null, null));
    }

    CcDocument document() {
        CcDocument document = mock(CcDocument.class);
        when(document.getAcc(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> accs.get(i.<AccManifestId>getArgument(0)));
        when(document.getAsccp(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> asccps.get(i.<AsccpManifestId>getArgument(0)));
        when(document.getBccp(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> bccps.get(i.<BccpManifestId>getArgument(0)));
        when(document.getAssociations(org.mockito.ArgumentMatchers.any())).thenAnswer(i ->
                associations.getOrDefault(i.<AccSummaryRecord>getArgument(0).accManifestId(), List.of()));
        when(document.getAccListByBasedAccManifestId(org.mockito.ArgumentMatchers.any())).thenAnswer(i ->
                accs.values().stream().filter(a -> i.<AccManifestId>getArgument(0).equals(a.basedAccManifestId())).toList());
        when(document.getAsccpListByRoleOfAccManifestId(org.mockito.ArgumentMatchers.any())).thenAnswer(i ->
                asccps.values().stream().filter(a -> i.<AccManifestId>getArgument(0).equals(a.roleOfAccManifestId())).toList());
        when(document.getAccListByToAsccpManifestId(org.mockito.ArgumentMatchers.any())).thenAnswer(i ->
                associations.entrySet().stream()
                        .filter(e -> e.getValue().stream().anyMatch(x -> x instanceof AsccSummaryRecord a
                                && a.toAsccpManifestId().equals(i.<AsccpManifestId>getArgument(0))))
                        .map(e -> accs.get(e.getKey())).toList());
        return document;
    }
}
