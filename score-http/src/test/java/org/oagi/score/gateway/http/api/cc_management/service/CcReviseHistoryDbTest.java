package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Revising ('Working' branch, developers) and amending (a release that is not 'Working', end-users) a component: the
 * record of the component is copied, the copy and the original are linked as the previous and the next revision, the
 * manifest takes the copy, and the same is done for what belongs to the component (the associations of an ACC, the
 * supplementary components and the allowed primitives of a Data Type).
 * <pre>
 *   mvn test -Drevision=3.5.0 -Dtest=CcReviseHistoryDbTest -DargLine=-Dscore.reviseCancel=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.reviseCancel", matches = "true")
class CcReviseHistoryDbTest extends CcReviseCancelFixture {

    private static final String ASSOCIATIONS = "exists (select 1 from ascc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id) "
            + "and exists (select 1 from bcc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id)";

    private static final String ROLE_OF = "exists (select 1 from asccp_manifest p where p.role_of_acc_manifest_id = m.acc_manifest_id)";

    // ---- ACC ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithoutBase(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "without a base, with associations", "m.based_acc_manifest_id is null and " + ASSOCIATIONS + " and " + ROLE_OF);
        reviseAcc(acc);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithBase(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        long acc = pick(Kind.ACC, "to be derived", "m.based_acc_manifest_id is null and " + ASSOCIATIONS + " and " + ROLE_OF);
        deriveAcc(acc, base);
        reviseAcc(acc);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accThatIsTheBaseOfAnotherAcc(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", "m.based_acc_manifest_id is null and " + ASSOCIATIONS + " and " + ROLE_OF);
        long derived = pick(Kind.ACC, "to be derived", "m.based_acc_manifest_id is null and " + ASSOCIATIONS);
        deriveAcc(derived, base);
        reviseAcc(base);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accRevisedAgain(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", ASSOCIATIONS + " and " + ROLE_OF);
        reviseAcc(acc);
        publish(Kind.ACC, acc);
        reviseAcc(acc);
        report();
    }

    // ---- ASCCP, BCCP ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccp(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "used by an association",
                "exists (select 1 from ascc_manifest x where x.to_asccp_manifest_id = m.asccp_manifest_id)");
        reviseAsccp(asccp);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccpRevisedAgain(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "used by an association",
                "exists (select 1 from ascc_manifest x where x.to_asccp_manifest_id = m.asccp_manifest_id)");
        reviseAsccp(asccp);
        publish(Kind.ASCCP, asccp);
        reviseAsccp(asccp);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccp(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "used by an association",
                "exists (select 1 from bcc_manifest x where x.to_bccp_manifest_id = m.bccp_manifest_id)");
        reviseBccp(bccp);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccpRevisedAgain(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "used by an association",
                "exists (select 1 from bcc_manifest x where x.to_bccp_manifest_id = m.bccp_manifest_id)");
        reviseBccp(bccp);
        publish(Kind.BCCP, bccp);
        reviseBccp(bccp);
        report();
    }

    // ---- Data Type ----

    private static final String WITH_SC = "exists (select 1 from dt_sc_manifest s where s.owner_dt_manifest_id = m.dt_manifest_id)";

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtWithBaseOfAnotherLibraryAndDataTypesBasedOnIt(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with a base of a release the library depends on, supplementary components, and a Data Type based on it",
                "m.based_dt_manifest_id is not null and " + WITH_SC + " and (select b.release_id from dt_manifest b "
                        + "where b.dt_manifest_id = m.based_dt_manifest_id) <> m.release_id "
                        + "and exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        reviseDt(dt);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtWithBaseOfTheSameRelease(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with a base of the release and supplementary components",
                "m.based_dt_manifest_id is not null and " + WITH_SC + " and (select b.release_id from dt_manifest b "
                        + "where b.dt_manifest_id = m.based_dt_manifest_id) = m.release_id");
        reviseDt(dt);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtUsedByBccpWithCodeListAllowedPrimitive(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP, with a Code List as an allowed primitive",
                "exists (select 1 from bccp_manifest b where b.bdt_manifest_id = m.dt_manifest_id) "
                        + "and exists (select 1 from dt_awd_pri p where p.release_id = m.release_id and p.dt_id = m.dt_id "
                        + "and p.code_list_manifest_id is not null)");
        reviseDt(dt);
        report();
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtRevisedAgain(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP, with supplementary components",
                WITH_SC + " and exists (select 1 from bccp_manifest b where b.bdt_manifest_id = m.dt_manifest_id)");
        reviseDt(dt);
        publish(Kind.DT, dt);
        reviseDt(dt);
        report();
    }

    // ---- what a revision is ----

    /** What the previous revision becomes when the user publishes the revision, so that it can be revised again. */
    private void publish(Kind kind, long manifestId) {
        dsl.execute("update `" + kind.table + "` set state = ? where `" + kind.pk() + "` = ?", publishedState(), recordOf(kind, manifestId));
    }

    private void reviseAcc(long m) {
        long oldAcc = recordOf(Kind.ACC, m);
        List<Long> asccs = ids("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ?", m);
        List<Long> bccs = ids("select bcc_manifest_id from bcc_manifest where from_acc_manifest_id = ?", m);
        Revision r = revise(Kind.ACC, m, () -> service.reviseAcc(requester, new AccManifestId(n(m))));

        // the associations are revised with the ACC, and belong to the new record
        for (long c : asccs) {
            associationRevised(r, Kind.ASCC, c, "from_acc_id", oldAcc, "to_asccp_id");
        }
        for (long c : bccs) {
            associationRevised(r, Kind.BCC, c, "from_acc_id", oldAcc, "to_bccp_id");
        }
        r.expect.added("ascc", asccs.size()).added("bcc", bccs.size());
        // what refers to the ACC is marked as in conflict, and keeps the record it referred to
        for (long p : ids("select asccp_manifest_id from asccp_manifest where role_of_acc_manifest_id = ? and release_id = ?", m, release)) {
            r.expect.changeManifest(Kind.ASCCP, p, "conflict");
        }
        for (long d : ids("select acc_manifest_id from acc_manifest where based_acc_manifest_id = ? and release_id = ?", m, release)) {
            r.expect.changeManifest(Kind.ACC, d, "conflict");
        }
        r.finish();
    }

    private void reviseAsccp(long m) {
        Revision r = revise(Kind.ASCCP, m, () -> service.reviseAsccp(requester, new AsccpManifestId(n(m))));
        for (long c : ids("select ascc_manifest_id from ascc_manifest where to_asccp_manifest_id = ? and release_id = ?", m, release)) {
            r.expect.changeManifest(Kind.ASCC, c, "conflict");
        }
        r.finish();
    }

    private void reviseBccp(long m) {
        Revision r = revise(Kind.BCCP, m, () -> service.reviseBccp(requester, new BccpManifestId(n(m))));
        for (long c : ids("select bcc_manifest_id from bcc_manifest where to_bccp_manifest_id = ? and release_id = ?", m, release)) {
            r.expect.changeManifest(Kind.BCC, c, "conflict");
        }
        r.finish();
    }

    private void reviseDt(long m) {
        long oldDt = recordOf(Kind.DT, m);
        List<Long> scs = ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ?", m);
        List<String> awdPri = awdPri("dt_awd_pri", "dt_id", oldDt);
        Map<Long, List<String>> scAwdPri = new java.util.LinkedHashMap<>();
        Map<Long, Long> oldSc = new java.util.LinkedHashMap<>();
        for (long s : scs) {
            long record = recordOf(Kind.DT_SC, s);
            oldSc.put(s, record);
            scAwdPri.put(s, awdPri("dt_sc_awd_pri", "dt_sc_id", record));
        }
        Revision r = revise(Kind.DT, m, () -> service.reviseDt(requester, new DtManifestId(n(m))));
        long newDt = r.newRecord;

        checkEquals(awdPri, awdPri("dt_awd_pri", "dt_id", newDt), "allowed primitives of the new Data Type");
        checkEquals(awdPri, awdPri("dt_awd_pri", "dt_id", oldDt), "allowed primitives of the previous Data Type");
        r.expect.added("dt_awd_pri", awdPri.size());
        int scAwdPriCount = 0;
        for (long s : scs) {
            long oldRecord = oldSc.get(s);
            long newRecord = recordOf(Kind.DT_SC, s);
            Map<String, Object> before = r.before.row(Kind.DT_SC, oldRecord);
            Map<String, Object> after = r.after.row(Kind.DT_SC, newRecord);
            check(oldRecord != newRecord, Kind.DT_SC + " manifest#" + s + " still holds the record it held");
            revisionLinks(r, Kind.DT_SC, oldRecord, newRecord);
            checkEquals(newDt, toLong(after.get("owner_dt_id")), "owner of the supplementary component of the manifest#" + s);
            checkEquals(awdPri("dt_sc_awd_pri", "dt_sc_id", oldRecord), awdPri("dt_sc_awd_pri", "dt_sc_id", newRecord),
                    "allowed primitives of the revised supplementary component of the manifest#" + s);
            check(before != null && after != null, "supplementary component rows of the manifest#" + s);
            scAwdPriCount += scAwdPri.get(s).size();
            r.expect.changeManifest(Kind.DT_SC, s, "dt_sc_id", "conflict").change(Kind.DT_SC, oldRecord, "next_dt_sc_id");
            for (long d : ids("select dt_sc_manifest_id from dt_sc_manifest where based_dt_sc_manifest_id = ? and release_id = ?", s, release)) {
                r.expect.changeManifest(Kind.DT_SC, d, "conflict");
            }
        }
        r.expect.added("dt_sc", scs.size()).added("dt_sc_awd_pri", scAwdPriCount);
        // what refers to the Data Type is marked as in conflict
        for (long b : ids("select bccp_manifest_id from bccp_manifest where bdt_manifest_id = ? and release_id = ?", m, release)) {
            r.expect.changeManifest(Kind.BCCP, b, "conflict");
        }
        for (long d : ids("select dt_manifest_id from dt_manifest where based_dt_manifest_id = ? and release_id = ?", m, release)) {
            r.expect.changeManifest(Kind.DT, d, "conflict");
        }
        r.finish();
    }

    /** The allowed primitives of a record, as what identifies them without the key. */
    private List<String> awdPri(String table, String owner, long record) {
        List<String> rows = new ArrayList<>(dsl.fetch("select concat_ws('|', cdt_pri_id, ifnull(xbt_manifest_id, '-'), "
                + "ifnull(code_list_manifest_id, '-'), ifnull(agency_id_list_manifest_id, '-'), is_default) from `" + table
                + "` where release_id = ? and `" + owner + "` = ?", release, record).map(r -> r.get(0, String.class)));
        rows.sort(null);
        return rows;
    }

    // ---- the engine ----

    private static long toLong(Object value) {
        return value == null ? -1 : Long.parseLong(String.valueOf(value));
    }

    /** The revision of one component: the state before and after, and what a revision may change. */
    private final class Revision {
        final Kind kind;
        final long manifest;
        final long oldRecord;
        long newRecord;
        Snapshot before;
        Snapshot after;
        Map<String, Long> invariantsBefore;
        Map<String, Long> invariantsAfter;
        Expect expect = new Expect();

        Revision(Kind kind, long manifest) {
            this.kind = kind;
            this.manifest = manifest;
            this.oldRecord = recordOf(kind, manifest);
        }

        void finish() {
            List<Change> diff = diff(before, after);
            problems.addAll(expect.unexplained(diff));
            Set<String> lag = new TreeSet<>();
            for (var e : invariantsAfter.entrySet()) {
                long was = invariantsBefore.getOrDefault(e.getKey(), 0L);
                if (e.getValue() == was) {
                    continue;
                }
                String message = "invariant '" + e.getKey() + "': " + was + " rows violate it before, " + e.getValue() + " after";
                if (isReferenceLag(e.getKey())) {
                    lag.add(e.getKey() + " " + was + "->" + e.getValue());
                } else {
                    problems.add(message);
                }
            }
            System.out.println("[" + branch + "] revised " + kind + " manifest#" + manifest + " record#" + oldRecord + " -> #" + newRecord
                    + ", rows that refer to the previous record and are not changed: " + lag);
        }
    }

    /** What refers to a revised component keeps referring to the record it was revised from; the manifest is what follows. */
    private static boolean isReferenceLag(String invariant) {
        return invariant.endsWith(" exact") && !invariant.endsWith("from_acc exact") && !invariant.contains("owner");
    }

    private Revision revise(Kind kind, long manifest, Runnable action) {
        Revision r = new Revision(kind, manifest);
        r.before = snapshot();
        r.invariantsBefore = invariants();
        action.run();
        r.after = snapshot();
        r.invariantsAfter = invariants();
        r.newRecord = recordOf(kind, manifest);

        check(r.newRecord != r.oldRecord, kind + " manifest#" + manifest + " still holds the record it held");
        revisionLinks(r, kind, r.oldRecord, r.newRecord);
        r.expect.changeManifest(kind, manifest, kind.pk(), "log_id", "conflict").change(kind, r.oldRecord, kind.next());
        r.expect.added(kind.table, 1).added("log", 1).changeAny("log", "next_log_id");
        return r;
    }

    /** An association of the revised ACC is revised with it. */
    private void associationRevised(Revision r, Kind kind, long manifest, String ownerColumn, long oldOwner, String targetColumn) {
        long oldRecord = toLong(r.before.manifest(kind, manifest).get(kind.pk()));
        long newRecord = toLong(r.after.manifest(kind, manifest).get(kind.pk()));
        check(oldRecord != newRecord, kind + " manifest#" + manifest + " still holds the record it held");
        revisionLinks(r, kind, oldRecord, newRecord);
        checkEquals(r.newRecord, toLong(r.after.get(kind, newRecord, ownerColumn)), kind + " manifest#" + manifest + " owner ACC");
        checkEquals(oldOwner, toLong(r.before.get(kind, oldRecord, ownerColumn)), kind + " manifest#" + manifest + " previous owner ACC");
        // a revision is made with the record that the manifest of its target holds, which is the one the old record refers
        // to, unless the target was revised after it (then the old record is marked as in conflict)
        String targetManifest = targetColumn.substring("to_".length()).replaceAll("_id$", "_manifest");
        checkEquals(one("select t.`" + targetColumn.substring("to_".length()) + "` from `" + kind.manifest() + "` m join `"
                        + targetManifest + "` t on t.`" + targetManifest + "_id` = m.`to_" + targetManifest + "_id` where m.`"
                        + kind.mpk() + "` = ?", manifest),
                toLong(r.after.get(kind, newRecord, targetColumn)), kind + " manifest#" + manifest + " " + targetColumn);
        r.expect.changeManifest(kind, manifest, kind.pk(), "conflict").change(kind, oldRecord, kind.next());
    }

    /** The new record is the next revision of the old one, and is the old one but for what a revision changes. */
    private void revisionLinks(Revision r, Kind kind, long oldRecord, long newRecord) {
        String what = kind + " #" + oldRecord + " -> #" + newRecord;
        Map<String, Object> oldBefore = r.before.row(kind, oldRecord);
        Map<String, Object> oldAfter = r.after.row(kind, oldRecord);
        Map<String, Object> created = r.after.row(kind, newRecord);
        if (oldBefore == null || oldAfter == null || created == null) {
            problems.add(what + ": a row is not there (before " + (oldBefore != null) + ", old after " + (oldAfter != null)
                    + ", new " + (created != null) + ")");
            return;
        }
        checkEquals(oldRecord, toLong(created.get(kind.prev())), what + ": prev of the new");
        checkEquals(newRecord, toLong(oldAfter.get(kind.next())), what + ": next of the old");
        check(created.get(kind.next()) == null, what + ": the new has a next revision");
        checkEquals(oldBefore.get(kind.prev()), oldAfter.get(kind.prev()), what + ": prev of the old is kept");
        if (kind.hasState()) {
            checkEquals("WIP", created.get("state"), what + ": state of the new");
            checkEquals(oldBefore.get("state"), oldAfter.get("state"), what + ": state of the old is kept");
            checkEquals(requester.userId().value().longValue(), toLong(created.get("owner_user_id")), what + ": owner of the new");
        }
        Set<String> except = new HashSet<>(Set.of(kind.pk(), kind.prev(), kind.next()));
        if (kind.hasState()) {
            except.addAll(Set.of("state", "owner_user_id", "created_by", "last_updated_by", "creation_timestamp", "last_update_timestamp"));
        }
        if (kind == Kind.ASCC || kind == Kind.BCC) {
            except.add("from_acc_id");
            // checked against the manifest of its target, where the association is revised
            except.add(kind == Kind.ASCC ? "to_asccp_id" : "to_bccp_id");
        }
        if (kind == Kind.DT_SC) {
            except.add("owner_dt_id");
        }
        Set<String> different = new TreeSet<>();
        for (var column : oldBefore.entrySet()) {
            if (!except.contains(column.getKey()) && !java.util.Objects.equals(column.getValue(), created.get(column.getKey()))) {
                different.add(column.getKey());
            }
        }
        check(different.isEmpty(), what + ": the new differs from the old in " + different);
    }
}
