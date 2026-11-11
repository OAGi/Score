package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The marker on a manifest tells that the record it holds refers to an older record than the manifest of what it refers to
 * does, which a revision of what it refers to causes. It is set on what refers to a component when the component is
 * revised, and it is clear for what the revision makes: the new record refers to the latest. A cancel gives back the
 * record that was there, so the marker follows what that record refers to. Nothing else is changed.
 * <pre>
 *   mvn test -Drevision=3.5.0 -Dtest=CcConflictMarkerDbTest -DargLine=-Dscore.reviseCancel=true
 * </pre>
 */
class CcConflictMarkerDbTest extends CcReviseCancelFixture {

    private static final String ASSOCIATIONS = "exists (select 1 from ascc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id) "
            + "and exists (select 1 from bcc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id)";
    private static final String WITH_SC = "exists (select 1 from dt_sc_manifest s where s.owner_dt_manifest_id = m.dt_manifest_id)";
    private static final String USED_BY_BCCP = "exists (select 1 from bccp_manifest b where b.bdt_manifest_id = m.dt_manifest_id)";

    private long marker(Kind kind, long manifest) {
        return one("select conflict from `" + kind.manifest() + "` where `" + kind.mpk() + "` = ?", manifest);
    }

    private void everythingThatLagsIsMarked() {
        Map<String, Long> invariants = invariants();
        invariants.forEach((name, count) -> {
            if (name.endsWith("without the marker")) {
                assertEquals(0L, count, name);
            }
        });
    }

    // ---- the marker is set on what refers to what is revised ----

    private static final String BASE_OF_DATA_TYPES = "exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)";

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedDataTypeMarksTheBccpsThatUseIt(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP, with supplementary components", WITH_SC + " and " + USED_BY_BCCP);
        List<Long> bccps = ids("select bccp_manifest_id from bccp_manifest where bdt_manifest_id = ?", dt);

        service.reviseDt(requester, new DtManifestId(n(dt)));

        bccps.forEach(e -> assertEquals(1L, marker(Kind.BCCP, e), "the BCCP of the Data Type"));
        assertEquals(0L, marker(Kind.DT, dt), "the Data Type that is revised");
        everythingThatLagsIsMarked();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedDataTypeMarksTheDataTypesAndSupplementaryComponentsBasedOnIt(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "based on by a Data Type, with supplementary components", WITH_SC + " and " + BASE_OF_DATA_TYPES);
        List<Long> derived = ids("select dt_manifest_id from dt_manifest where based_dt_manifest_id = ?", dt);
        List<Long> derivedScs = ids("select d.dt_sc_manifest_id from dt_sc_manifest d join dt_sc_manifest b on b.dt_sc_manifest_id = d.based_dt_sc_manifest_id "
                + "where b.owner_dt_manifest_id = ?", dt);
        assertTrue(!derivedScs.isEmpty(), "a supplementary component is based on one of the Data Type");

        service.reviseDt(requester, new DtManifestId(n(dt)));

        derived.forEach(e -> assertEquals(1L, marker(Kind.DT, e), "the Data Type based on it"));
        derivedScs.forEach(e -> assertEquals(1L, marker(Kind.DT_SC, e), "the supplementary component based on one of it"));
        assertEquals(0L, marker(Kind.DT, dt), "the Data Type that is revised");
        everythingThatLagsIsMarked();
    }

    // ---- and clear for what the revision makes ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedBccpIsNotInConflictAnyMore(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "of a Data Type that is published", "exists (select 1 from dt_manifest d join dt x2 on x2.dt_id = d.dt_id "
                + "where d.dt_manifest_id = m.bdt_manifest_id and x2.state in " + stableStates() + " and x2.next_dt_id is null)");
        long dt = one("select bdt_manifest_id from bccp_manifest where bccp_manifest_id = ?", bccp);
        watch(Kind.DT, dt);

        service.reviseDt(requester, new DtManifestId(n(dt)));
        assertEquals(1L, marker(Kind.BCCP, bccp), "the BCCP refers to the previous record of the Data Type");
        everythingThatLagsIsMarked();

        service.reviseBccp(requester, new BccpManifestId(n(bccp)));
        assertEquals(0L, marker(Kind.BCCP, bccp), "the revision of the BCCP refers to the latest Data Type");
        everythingThatLagsIsMarked();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedAccAndItsAssociationsAreNotInConflictAnyMore(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations, of an ASCCP and a BCCP that are published", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        long asccp = one("select to_asccp_manifest_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc);
        long ascc = one("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ? and to_asccp_manifest_id = ?", acc, asccp);
        long bccp = one("select to_bccp_manifest_id from bcc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc);
        long bcc = one("select bcc_manifest_id from bcc_manifest where from_acc_manifest_id = ? and to_bccp_manifest_id = ?", acc, bccp);
        watch(Kind.ASCCP, asccp);
        watch(Kind.BCCP, bccp);

        service.reviseAsccp(requester, new AsccpManifestId(n(asccp)));
        service.reviseBccp(requester, new BccpManifestId(n(bccp)));
        assertEquals(1L, marker(Kind.ASCC, ascc), "the association refers to the previous record of the ASCCP");
        assertEquals(1L, marker(Kind.BCC, bcc), "the association refers to the previous record of the BCCP");
        everythingThatLagsIsMarked();

        service.reviseAcc(requester, new AccManifestId(n(acc)));
        assertEquals(0L, marker(Kind.ASCC, ascc), "the revision of the association refers to the latest ASCCP");
        assertEquals(0L, marker(Kind.BCC, bcc), "the revision of the association refers to the latest BCCP");
        everythingThatLagsIsMarked();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedDerivedAccIsNotInConflictAnyMore(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        long derived = pick(Kind.ACC, "to be derived", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        deriveAcc(derived, base);

        service.reviseAcc(requester, new AccManifestId(n(base)));
        assertEquals(1L, marker(Kind.ACC, derived), "the ACC refers to the previous record of its base");
        everythingThatLagsIsMarked();

        service.reviseAcc(requester, new AccManifestId(n(derived)));
        assertEquals(0L, marker(Kind.ACC, derived), "the revision refers to the latest base");
        everythingThatLagsIsMarked();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedDerivedDataTypeAndItsSupplementaryComponentsAreNotInConflictAnyMore(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.DT, "with supplementary components and an XBT as allowed primitive", WITH_SC
                + " and exists (select 1 from dt_awd_pri p where p.release_id = m.release_id and p.dt_id = m.dt_id and p.xbt_manifest_id is not null)");
        long derived = factory.dtCommandRepository(requester).create(releaseId(), new DtManifestId(n(base))).value().longValue();
        dsl.execute("update dt set state = ? where dt_id = ?", publishedState(), recordOf(Kind.DT, derived));
        List<Long> scs = ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ?", derived);

        factory.dtCommandRepository(requester).revise(new DtManifestId(n(base)));
        assertEquals(1L, marker(Kind.DT, derived), "the Data Type refers to the previous record of its base");
        scs.forEach(e -> assertEquals(1L, marker(Kind.DT_SC, e), "the supplementary component refers to the previous record of its base"));
        everythingThatLagsIsMarked();

        factory.dtCommandRepository(requester).revise(new DtManifestId(n(derived)));
        assertEquals(0L, marker(Kind.DT, derived), "the revision refers to the latest base");
        scs.forEach(e -> assertEquals(0L, marker(Kind.DT_SC, e), "the revision refers to the latest supplementary component"));
        everythingThatLagsIsMarked();
    }

    // ---- a cancel gives back the record, and the marker follows it ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void cancelledBccpIsInConflictWhileTheDataTypeIsRevisedAndNotAfterwards(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "of a Data Type that is published", "exists (select 1 from dt_manifest d join dt x2 on x2.dt_id = d.dt_id "
                + "where d.dt_manifest_id = m.bdt_manifest_id and x2.state in " + stableStates() + " and x2.next_dt_id is null)");
        long dt = one("select bdt_manifest_id from bccp_manifest where bccp_manifest_id = ?", bccp);
        watch(Kind.DT, dt);

        service.reviseDt(requester, new DtManifestId(n(dt)));
        service.reviseBccp(requester, new BccpManifestId(n(bccp)));
        assertEquals(0L, marker(Kind.BCCP, bccp));

        service.cancelBccp(requester, new BccpManifestId(n(bccp)), null, null);
        assertEquals(1L, marker(Kind.BCCP, bccp), "the BCCP is back, and refers to the previous record of the Data Type that is revised");
        everythingThatLagsIsMarked();

        service.cancelDt(requester, new DtManifestId(n(dt)), null, null);
        assertEquals(0L, marker(Kind.BCCP, bccp), "the Data Type is back too");
        everythingThatLagsIsMarked();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void cancelledDataTypeClearsTheBccpsThatUseIt(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP, with supplementary components", WITH_SC + " and " + USED_BY_BCCP);
        Map<String, Long> before = invariants();

        service.reviseDt(requester, new DtManifestId(n(dt)));
        service.cancelDt(requester, new DtManifestId(n(dt)), null, null);

        assertEquals(0L, one("select count(*) from bccp_manifest where bdt_manifest_id = ? and conflict = 1", dt), "the BCCPs of the Data Type");
        assertEquals(before, invariants(), "the records of the release");
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void cancelledDataTypeClearsTheDataTypesAndSupplementaryComponentsBasedOnIt(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "based on by a Data Type, with supplementary components", WITH_SC + " and " + BASE_OF_DATA_TYPES);
        Map<String, Long> before = invariants();

        service.reviseDt(requester, new DtManifestId(n(dt)));
        service.cancelDt(requester, new DtManifestId(n(dt)), null, null);

        assertEquals(0L, one("select count(*) from dt_manifest where based_dt_manifest_id = ? and conflict = 1", dt), "the Data Types based on it");
        assertEquals(0L, one("select count(*) from dt_sc_manifest d join dt_sc_manifest b on b.dt_sc_manifest_id = d.based_dt_sc_manifest_id "
                + "where b.owner_dt_manifest_id = ? and d.conflict = 1", dt), "the supplementary components based on one of it");
        assertEquals(before, invariants(), "the records of the release");
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void cancelledAccWhoseBaseIsRevisedStaysInConflict(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        long derived = pick(Kind.ACC, "to be derived", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        deriveAcc(derived, base);

        service.reviseAcc(requester, new AccManifestId(n(base)));
        service.reviseAcc(requester, new AccManifestId(n(derived)));
        assertEquals(0L, marker(Kind.ACC, derived));

        service.cancelAcc(requester, new AccManifestId(n(derived)), null, null);
        assertEquals(1L, marker(Kind.ACC, derived), "the ACC is back, and refers to the previous record of its base that is revised");
        everythingThatLagsIsMarked();

        service.cancelAcc(requester, new AccManifestId(n(base)), null, null);
        assertEquals(0L, marker(Kind.ACC, derived), "the base is back too");
        everythingThatLagsIsMarked();
    }

    private org.oagi.score.gateway.http.api.release_management.model.ReleaseId releaseId() {
        return new org.oagi.score.gateway.http.api.release_management.model.ReleaseId(n(release));
    }
}
