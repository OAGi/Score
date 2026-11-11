package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.oagi.score.gateway.http.api.bie_management.service.BieRepository;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What refers to a component by the column of a record (BCCP.BDT_ID, DT.BASED_DT_ID, ...) refers to the revision it was
 * made with, and does not follow a revision of what it refers to: the manifest does. So what the code reads and what a
 * revision is made with must be what the manifest holds, which is always the latest.
 * <pre>
 *   mvn test -Drevision=3.5.0 -Dtest=CcRowReferenceDbTest -DargLine=-Dscore.reviseCancel=true
 * </pre>
 */
class CcRowReferenceDbTest extends CcReviseCancelFixture {

    private static final String ASSOCIATIONS = "exists (select 1 from ascc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id) "
            + "and exists (select 1 from bcc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id)";
    private static final String WITH_SC = "exists (select 1 from dt_sc_manifest s where s.owner_dt_manifest_id = m.dt_manifest_id)";

    private long column(String sql, Object... bindings) {
        return one(sql, bindings);
    }

    // ---- a revision is made with the latest of what it refers to ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedAccIsBasedOnTheLatestBase(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        long derived = pick(Kind.ACC, "to be derived", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        deriveAcc(derived, base);

        service.reviseAcc(requester, new AccManifestId(n(base)));
        long latestBase = recordOf(Kind.ACC, base);
        long oldDerived = recordOf(Kind.ACC, derived);
        service.reviseAcc(requester, new AccManifestId(n(derived)));

        assertEquals(latestBase, column("select based_acc_id from acc where acc_id = ?", recordOf(Kind.ACC, derived)),
                "the base of the revised ACC");
        assertTrue(latestBase != column("select based_acc_id from acc where acc_id = ?", oldDerived),
                "the revised record keeps the base it was made with");
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedAsccpIsTheRoleOfTheLatestAcc(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "the role of an ACC that is published", "exists (select 1 from acc_manifest r join acc a on a.acc_id = r.acc_id "
                + "where r.acc_manifest_id = m.role_of_acc_manifest_id and a.state in " + stableStates() + " and a.next_acc_id is null and "
                + "exists (select 1 from ascc_manifest x where x.from_acc_manifest_id = r.acc_manifest_id))");
        long acc = column("select role_of_acc_manifest_id from asccp_manifest where asccp_manifest_id = ?", asccp);
        watch(Kind.ACC, acc);

        service.reviseAcc(requester, new AccManifestId(n(acc)));
        long latestAcc = recordOf(Kind.ACC, acc);
        service.reviseAsccp(requester, new AsccpManifestId(n(asccp)));

        assertEquals(latestAcc, column("select role_of_acc_id from asccp where asccp_id = ?", recordOf(Kind.ASCCP, asccp)),
                "the ACC the revised ASCCP is the role of");
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedBccpHasTheLatestDataType(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "of a Data Type that is published", "exists (select 1 from dt_manifest d join dt x2 on x2.dt_id = d.dt_id "
                + "where d.dt_manifest_id = m.bdt_manifest_id and x2.state in " + stableStates() + " and x2.next_dt_id is null)");
        long dt = column("select bdt_manifest_id from bccp_manifest where bccp_manifest_id = ?", bccp);
        watch(Kind.DT, dt);

        service.reviseDt(requester, new DtManifestId(n(dt)));
        long latestDt = recordOf(Kind.DT, dt);
        service.reviseBccp(requester, new BccpManifestId(n(bccp)));

        assertEquals(latestDt, column("select bdt_id from bccp where bccp_id = ?", recordOf(Kind.BCCP, bccp)),
                "the Data Type of the revised BCCP");
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void revisedDtHasTheLatestBaseAndSupplementaryComponentsOfIt(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.DT, "with supplementary components and an XBT as allowed primitive", WITH_SC
                + " and exists (select 1 from dt_awd_pri p where p.release_id = m.release_id and p.dt_id = m.dt_id and p.xbt_manifest_id is not null)");
        long derived = factory.dtCommandRepository(requester).create(releaseId(), new DtManifestId(n(base))).value().longValue();
        watch(Kind.DT, derived);
        publish(Kind.DT, derived);

        factory.dtCommandRepository(requester).revise(new DtManifestId(n(base)));
        long latestBase = recordOf(Kind.DT, base);
        factory.dtCommandRepository(requester).revise(new DtManifestId(n(derived)));
        long revised = recordOf(Kind.DT, derived);

        assertEquals(latestBase, column("select based_dt_id from dt where dt_id = ?", revised), "the base of the revised Data Type");
        for (long sc : ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ?", derived)) {
            assertEquals(column("select b.dt_sc_id from dt_sc_manifest m join dt_sc_manifest b on b.dt_sc_manifest_id = m.based_dt_sc_manifest_id "
                            + "where m.dt_sc_manifest_id = ?", sc),
                    column("select based_dt_sc_id from dt_sc where dt_sc_id = ?", recordOf(Kind.DT_SC, sc)),
                    "the base of the revised supplementary component of the manifest#" + sc);
        }
    }

    // ---- what is read is what the manifest holds ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void allowedPrimitiveIsInheritedFromTheLatestBase(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.DT, "with supplementary components and an XBT as allowed primitive", WITH_SC
                + " and exists (select 1 from dt_awd_pri p where p.release_id = m.release_id and p.dt_id = m.dt_id and p.xbt_manifest_id is not null)");
        long derived = factory.dtCommandRepository(requester).create(releaseId(), new DtManifestId(n(base))).value().longValue();

        var before = factory.dtQueryRepository(requester).getDtDetails(new DtManifestId(n(derived))).dtAwdPriList();
        assertTrue(!before.isEmpty() && before.stream().allMatch(e -> e.inherited()), "what a new Data Type has is inherited from its base");

        factory.dtCommandRepository(requester).revise(new DtManifestId(n(base)));
        long latestBase = recordOf(Kind.DT, base);
        for (long p : ids("select dt_awd_pri_id from dt_awd_pri where release_id = ? and dt_id = ?", release, latestBase)) {
            factory.dtCommandRepository(requester).deleteDtAwdPri(new DtAwdPriId(n(p)));
        }

        var after = factory.dtQueryRepository(requester).getDtDetails(new DtManifestId(n(derived))).dtAwdPriList();
        assertTrue(after.stream().noneMatch(e -> e.inherited()), "nothing is inherited, the latest base has no allowed primitive");
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bbieTakesTheDefaultValueOfTheLatestBccp(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "of a BCC without default and fixed values, and of a Data Type with allowed primitives",
                "exists (select 1 from bcc_manifest b join bcc c on c.bcc_id = b.bcc_id where b.to_bccp_manifest_id = m.bccp_manifest_id "
                        + "and c.default_value is null and c.fixed_value is null) and exists (select 1 from bccp x3 where x3.bccp_id = m.bccp_id "
                        + "and x3.default_value is null and x3.fixed_value is null)");
        long bcc = ids("select b.bcc_manifest_id from bcc_manifest b join bcc c on c.bcc_id = b.bcc_id where b.to_bccp_manifest_id = ? "
                + "and c.default_value is null and c.fixed_value is null order by 1 limit 1", bccp).get(0);
        long dt = column("select bdt_manifest_id from bccp_manifest where bccp_manifest_id = ?", bccp);
        watch(Kind.DT, dt);

        service.reviseBccp(requester, new BccpManifestId(n(bccp)));
        dsl.execute("update bccp set default_value = 'LATEST' where bccp_id = ?", recordOf(Kind.BCCP, bccp));

        BieRepository bieRepository = new BieRepository();
        ReflectionTestUtils.setField(bieRepository, "dslContext", dsl);
        ReflectionTestUtils.setField(bieRepository, "repositoryFactory", factory);
        // the BBIE is made without the path columns, which the application fills in afterwards
        String sqlMode = dsl.fetchOne("select @@sql_mode").get(0, String.class);
        dsl.execute("set foreign_key_checks = 0");
        dsl.execute("set sql_mode = ''");
        try {
            var bbie = bieRepository.createBbie(requester, new AbieId(n(1)), new BbiepId(n(1)), new BccManifestId(n(bcc)),
                    new DtManifestId(n(dt)), 1, new TopLevelAsbiepId(n(1)));
            assertEquals("LATEST", bbie.getDefaultValue(), "the default value of the BBIE");
        } finally {
            dsl.execute("set foreign_key_checks = 1");
            dsl.execute("set sql_mode = '" + sqlMode + "'");
        }
    }

    private void publish(Kind kind, long manifestId) {
        dsl.execute("update `" + kind.table + "` set state = ? where `" + kind.pk() + "` = ?", publishedState(), recordOf(kind, manifestId));
    }

    private org.oagi.score.gateway.http.api.release_management.model.ReleaseId releaseId() {
        return new org.oagi.score.gateway.http.api.release_management.model.ReleaseId(n(release));
    }
}
