package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;

/**
 * Cancelling a revision (developers) or an amendment (end-users): the component goes back to the record it was revised
 * from, and what it was revised with is discarded. A cancel must leave the records as they were before the revision,
 * whatever has been done to the component, and to what depends on it, in the meantime.
 * <pre>
 *   mvn test -Drevision=3.5.0 -Dtest=CcCancelDbTest -DargLine=-Dscore.reviseCancel=true
 * </pre>
 * Each test takes a snapshot of the rows of the release, revises, does what a user can do to a revised component, cancels,
 * and requires that the rows are those of the snapshot, but for what the user has created and that stays, and that
 * every reference and every history link is as valid as it was.
 */
class CcCancelDbTest extends CcReviseCancelFixture {

    private static final String ASSOCIATIONS = "exists (select 1 from ascc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id) "
            + "and exists (select 1 from bcc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id)";
    private static final String ROLE_OF = "exists (select 1 from asccp_manifest p where p.role_of_acc_manifest_id = m.acc_manifest_id)";
    private static final String NO_BASE = "m.based_acc_manifest_id is null";
    private static final String WITH_SC = "exists (select 1 from dt_sc_manifest s where s.owner_dt_manifest_id = m.dt_manifest_id)";
    private static final String USED_ASCCP = "exists (select 1 from ascc_manifest x where x.to_asccp_manifest_id = m.asccp_manifest_id)";
    private static final String USED_BCCP = "exists (select 1 from bcc_manifest x where x.to_bccp_manifest_id = m.bccp_manifest_id)";
    private static final String CROSS_LIBRARY_BASE = "m.based_dt_manifest_id is not null and (select b.release_id from dt_manifest b "
            + "where b.dt_manifest_id = m.based_dt_manifest_id) <> m.release_id";

    int serial;

    // ---- the steps of a user ----

    void revise(Kind kind, long m) {
        switch (kind) {
            case ACC -> service.reviseAcc(requester, new AccManifestId(n(m)));
            case ASCCP -> service.reviseAsccp(requester, new AsccpManifestId(n(m)));
            case BCCP -> service.reviseBccp(requester, new BccpManifestId(n(m)));
            case DT -> service.reviseDt(requester, new DtManifestId(n(m)));
            default -> throw new IllegalArgumentException();
        }
    }

    void cancel(Kind kind, long m) {
        switch (kind) {
            case ACC -> service.cancelAcc(requester, new AccManifestId(n(m)), null, null);
            case ASCCP -> service.cancelAsccp(requester, new AsccpManifestId(n(m)), null, null);
            case BCCP -> service.cancelBccp(requester, new BccpManifestId(n(m)), null, null);
            case DT -> service.cancelDt(requester, new DtManifestId(n(m)), null, null);
            default -> throw new IllegalArgumentException();
        }
    }

    /** What the revision of a component ends as, when the user publishes it, so that it can be revised again. */
    void publish(Kind kind, long m) {
        dsl.execute("update `" + kind.table + "` set state = ? where `" + kind.pk() + "` = ?", publishedState(), recordOf(kind, m));
    }

    ReleaseId releaseId() {
        return new ReleaseId(n(release));
    }

    NamespaceId namespaceId() {
        List<Long> found = ids("select namespace_id from `release` where release_id = ? and namespace_id is not null", release);
        return found.isEmpty() ? null : new NamespaceId(n(found.get(0)));
    }

    long newAcc(Long base) {
        return factory.accCommandRepository(requester).create(releaseId(), base == null ? null : new AccManifestId(n(base)),
                "Cancel " + (++serial), null, null, null, namespaceId()).value().longValue();
    }

    long newAsccp(long roleOf) {
        return factory.asccpCommandRepository(requester).create(releaseId(), new AccManifestId(n(roleOf)), "Cancel " + (++serial),
                AsccpType.Default, true, null, namespaceId(), null).value().longValue();
    }

    long newBccp(long dt) {
        return factory.bccpCommandRepository(requester).create(releaseId(), new DtManifestId(n(dt)), "Cancel " + (++serial))
                .value().longValue();
    }

    long newDt(long base) {
        return factory.dtCommandRepository(requester).create(releaseId(), new DtManifestId(n(base))).value().longValue();
    }

    // ---- the engine ----

    /** Runs the steps, and requires that the rows are what they were, but for what stays. */
    void roundTrip(Runnable steps, Expect stays) {
        // the keys of the sequence are made again by a cancel; what matters is the order of the associations
        stays.ignoreAdded("seq_key").changeAny("ascc_manifest", "seq_key_id").changeAny("bcc_manifest", "seq_key_id");
        if (Boolean.getBoolean("score.ignoreConflict")) {
            for (Kind kind : Kind.values()) {
                stays.changeAny(kind.manifest(), "conflict");
            }
        }
        Snapshot before = snapshot();
        Map<String, Long> invariantsBefore = invariants();
        Map<Long, List<String>> orderBefore = associationOrder();
        try {
            steps.run();
        } catch (Throwable t) {
            Throwable root = t;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String where = java.util.Arrays.stream(t.getStackTrace()).filter(f -> f.getClassName().contains("oagi.score")
                    && !f.getClassName().contains("CcCancelDbTest")).limit(3).map(f -> f.getClassName().replaceAll(".*\\.", "")
                    + "." + f.getMethodName() + ":" + f.getLineNumber()).collect(java.util.stream.Collectors.joining(" < "));
            problems.add("a step failed at " + where + ": " + t.getClass().getSimpleName() + ": " + t.getMessage()
                    + (root != t ? " <- " + root.getClass().getSimpleName() + ": " + root.getMessage() : ""));
            report();
            return;
        }
        Snapshot after = snapshot();
        Map<String, Long> invariantsAfter = invariants();
        Map<Long, List<String>> orderAfter = associationOrder();
        for (var e : orderBefore.entrySet()) {
            if (!e.getValue().equals(orderAfter.get(e.getKey()))) {
                problems.add("the order of the associations of the ACC manifest#" + e.getKey() + " is not what it was: "
                        + e.getValue().size() + " before, " + (orderAfter.get(e.getKey()) == null ? "none" : orderAfter.get(e.getKey()).size()) + " after");
            }
        }

        problems.addAll(stays.unexplained(diff(before, after)));
        for (var e : invariantsAfter.entrySet()) {
            long was = invariantsBefore.getOrDefault(e.getKey(), 0L);
            if (e.getValue() != was) {
                problems.add("invariant '" + e.getKey() + "': " + was + " rows violate it before, " + e.getValue() + " after");
            }
        }
        for (var e : after.counts.entrySet()) {
            String table = e.getKey();
            long delta = e.getValue() - before.counts.get(table);
            if (!stays.isFree(table) && delta != stays.net(table)) {
                problems.add("the table " + table + " has " + delta + " more rows, expected " + stays.net(table));
            }
        }
        report();
    }

    /** The associations of every ACC of the release in the order of their sequence, as what identifies them. */
    Map<Long, List<String>> associationOrder() {
        Map<Long, Map<Long, List<Object>>> rows = new java.util.LinkedHashMap<>();
        for (var r : dsl.fetch("select s.from_acc_manifest_id, s.seq_key_id, s.prev_seq_key_id, s.next_seq_key_id, "
                + "coalesce(a.guid, b.guid, 'HEAD') from seq_key s join acc_manifest m on m.acc_manifest_id = s.from_acc_manifest_id "
                + "left join ascc_manifest am on am.ascc_manifest_id = s.ascc_manifest_id left join ascc a on a.ascc_id = am.ascc_id "
                + "left join bcc_manifest bm on bm.bcc_manifest_id = s.bcc_manifest_id left join bcc b on b.bcc_id = bm.bcc_id "
                + "where m.release_id = ? and " + w(Kind.ACC, "m"), release)) {
            rows.computeIfAbsent(r.get(0, Long.class), k -> new java.util.LinkedHashMap<>())
                    .put(r.get(1, Long.class), java.util.Arrays.asList(r.get(2, Long.class), r.get(3, Long.class), r.get(4, String.class)));
        }
        Map<Long, List<String>> order = new java.util.LinkedHashMap<>();
        for (var acc : rows.entrySet()) {
            Map<Long, List<Object>> keys = acc.getValue();
            List<String> chain = new java.util.ArrayList<>();
            Long head = keys.entrySet().stream().filter(e -> e.getValue().get(0) == null).map(Map.Entry::getKey).findFirst().orElse(null);
            for (Long k = head; k != null && chain.size() <= keys.size(); k = (Long) keys.get(k).get(1)) {
                chain.add(String.valueOf(keys.get(k).get(2)));
            }
            if (chain.size() != keys.size()) {
                chain.add("BROKEN CHAIN " + chain.size() + "/" + keys.size());
            }
            order.put(acc.getKey(), chain);
        }
        return order;
    }

    /** What identifies the associations of an ACC, whatever manifest holds them. */
    List<String> associationGuids(long acc) {
        return dsl.fetch("select a.guid from ascc_manifest m join ascc a on a.ascc_id = m.ascc_id where m.from_acc_manifest_id = ? "
                + "union all select b.guid from bcc_manifest m join bcc b on b.bcc_id = m.bcc_id where m.from_acc_manifest_id = ? order by 1",
                acc, acc).map(r -> r.get(0, String.class));
    }

    /** What identifies the supplementary components of a Data Type, whatever manifest holds them. */
    List<String> supplementaryComponentGuids(long dt) {
        return dsl.fetch("select s.guid from dt_sc_manifest m join dt_sc s on s.dt_sc_id = m.dt_sc_id where m.owner_dt_manifest_id = ? order by 1", dt)
                .map(r -> r.get(0, String.class));
    }

    /** The base of each supplementary component of a Data Type, by what identifies the component. */
    List<String> supplementaryComponentBases(long dt) {
        return dsl.fetch("select concat(s.guid, ' -> ', ifnull(m.based_dt_sc_manifest_id, '-')) from dt_sc_manifest m join dt_sc s on s.dt_sc_id = m.dt_sc_id "
                + "where m.owner_dt_manifest_id = ? order by 1", dt).map(r -> r.get(0, String.class));
    }

    /** Makes a published ACC one that has been made since the last release: nothing of the previous release links to it. */
    void makeNewSinceTheLastRelease(long acc) {
        for (Kind kind : List.of(Kind.ACC, Kind.ASCC, Kind.BCC)) {
            String m = "`" + kind.manifest() + "`";
            String id = "`" + kind.mpk() + "`";
            String where = kind == Kind.ACC ? "x.acc_manifest_id = " + acc : "x.from_acc_manifest_id = " + acc;
            dsl.execute("update " + m + " p join " + m + " x on x.`prev_" + kind.mpk() + "` = p." + id + " set p.`next_" + kind.mpk() + "` = null where " + where);
            dsl.execute("update " + m + " x set x.`prev_" + kind.mpk() + "` = null where " + where);
        }
    }

    Expect nothingStays() {
        return new Expect();
    }

    // ---- ACC ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void acc(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations, used as a role", NO_BASE + " and " + ASSOCIATIONS + " and " + ROLE_OF);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithAssociationsAdded(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        long asccp = pick(Kind.ASCCP, "to add", "m.role_of_acc_manifest_id <> " + acc);
        long bccp = pick(Kind.BCCP, "to add", "1 = 1");
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            factory.accCommandRepository(requester).createAscc(new AccManifestId(n(acc)), new AsccpManifestId(n(asccp)), null, null, true);
            factory.accCommandRepository(requester).createBcc(new AccManifestId(n(acc)), new BccpManifestId(n(bccp)), null, null, true);
            cancel(Kind.ACC, acc);
        }, nothingStays().ignoreAdded("seq_key"));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithAssociationsChanged(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        long ascc = ids("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        long bcc = ids("select bcc_manifest_id from bcc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            factory.accCommandRepository(requester).update(new AsccManifestId(n(ascc)), new Cardinality(1, 3), true, null);
            factory.accCommandRepository(requester).update(new BccManifestId(n(bcc)), null, new Cardinality(1, 3), true, null, null, null);
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithAssociationsDeleted(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        long ascc = ids("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        long bcc = ids("select bcc_manifest_id from bcc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        List<String> associations = associationGuids(acc);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            service.discard(requester, new AsccManifestId(n(ascc)));
            service.discard(requester, new BccManifestId(n(bcc)));
            assertEquals(associations.size() - 2, associationGuids(acc).size(), "the associations are deleted");
            cancel(Kind.ACC, acc);
            assertEquals(associations, associationGuids(acc), "the associations of the ACC after the cancel");
        }, nothingStays().added("ascc_manifest", 1).removed("ascc_manifest", 1).added("bcc_manifest", 1).removed("bcc_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accMadeSinceTheLastReleaseWithAssociationsDeleted(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        makeNewSinceTheLastRelease(acc);
        long ascc = ids("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        long bcc = ids("select bcc_manifest_id from bcc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        List<String> associations = associationGuids(acc);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            service.discard(requester, new AsccManifestId(n(ascc)));
            service.discard(requester, new BccManifestId(n(bcc)));
            cancel(Kind.ACC, acc);
            assertEquals(associations, associationGuids(acc), "the associations of the ACC after the cancel");
        }, nothingStays().added("ascc_manifest", 1).removed("ascc_manifest", 1).added("bcc_manifest", 1).removed("bcc_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void associationDeletedAndRestoredGetsTheViewOrderOfThePreviousRelease(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        long ascc = ids("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        Long prevAcc = dsl.fetchOne("select prev_acc_manifest_id from acc_manifest where acc_manifest_id = ?", acc).get(0, Long.class);
        Long prevAscc = dsl.fetchOne("select prev_ascc_manifest_id from ascc_manifest where ascc_manifest_id = ?", ascc).get(0, Long.class);
        long user = requester.userId().value().longValue();
        String insert = "insert into bie_view_order (from_acc_manifest_id, ascc_manifest_id, weight, created_by, last_updated_by, "
                + "creation_timestamp, last_update_timestamp) values (?, ?, ?, ?, ?, now(6), now(6))";
        if (prevAcc != null && prevAscc != null) {
            dsl.execute(insert, prevAcc, prevAscc, 5, user, user);
        }
        dsl.execute(insert, acc, ascc, 7, user, user);
        String guid = dsl.fetchOne("select a.guid from ascc_manifest m join ascc a on a.ascc_id = m.ascc_id where m.ascc_manifest_id = ?", ascc).get(0, String.class);

        revise(Kind.ACC, acc);
        service.discard(requester, new AsccManifestId(n(ascc)));
        assertEquals(0L, one("select count(*) from bie_view_order where ascc_manifest_id = ?", ascc), "deleting the association removes its weight");
        cancel(Kind.ACC, acc);

        long restored = one("select m.ascc_manifest_id from ascc_manifest m join ascc a on a.ascc_id = m.ascc_id "
                + "where m.from_acc_manifest_id = ? and a.guid = ?", acc, guid);
        List<Long> weights = ids("select weight from bie_view_order where from_acc_manifest_id = ? and ascc_manifest_id = ?", acc, restored);
        if (prevAcc != null && prevAscc != null) {
            assertEquals(List.of(5L), weights, "the weight of the same association in the previous release");
        } else {
            assertEquals(List.of(), weights, "there is no previous release to take the weight from");
        }
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithEveryAssociationDeleted(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        List<String> associations = associationGuids(acc);
        List<Long> asccs = ids("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ?", acc);
        List<Long> bccs = ids("select bcc_manifest_id from bcc_manifest where from_acc_manifest_id = ?", acc);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            asccs.forEach(e -> service.discard(requester, new AsccManifestId(n(e))));
            bccs.forEach(e -> service.discard(requester, new BccManifestId(n(e))));
            assertEquals(0, associationGuids(acc).size(), "the associations are deleted");
            cancel(Kind.ACC, acc);
            assertEquals(associations, associationGuids(acc), "the associations of the ACC after the cancel");
        }, nothingStays().added("ascc_manifest", asccs.size()).removed("ascc_manifest", asccs.size())
                .added("bcc_manifest", bccs.size()).removed("bcc_manifest", bccs.size()));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithAnAssociationDeletedAndOneAdded(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        long bccp = pick(Kind.BCCP, "to add", "1 = 1");
        long ascc = ids("select ascc_manifest_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        List<String> associations = associationGuids(acc);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            service.discard(requester, new AsccManifestId(n(ascc)));
            factory.accCommandRepository(requester).createBcc(new AccManifestId(n(acc)), new BccpManifestId(n(bccp)), null, null, true);
            cancel(Kind.ACC, acc);
            assertEquals(associations, associationGuids(acc), "the associations of the ACC after the cancel");
        }, nothingStays().added("ascc_manifest", 1).removed("ascc_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accRevisedAgainWithAnAssociationDeleted(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        revise(Kind.ACC, acc);
        publish(Kind.ACC, acc);
        long bcc = ids("select bcc_manifest_id from bcc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        List<String> associations = associationGuids(acc);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            service.discard(requester, new BccManifestId(n(bcc)));
            cancel(Kind.ACC, acc);
            assertEquals(associations, associationGuids(acc), "the associations of the ACC after the cancel");
        }, nothingStays().added("bcc_manifest", 1).removed("bcc_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithBaseChanged(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", NO_BASE + " and " + ASSOCIATIONS);
        long other = pick(Kind.ACC, "to be the other base, without ASCCs", NO_BASE
                + " and not exists (select 1 from ascc_manifest x where x.from_acc_manifest_id = m.acc_manifest_id)");
        long acc = pick(Kind.ACC, "to be derived", NO_BASE + " and " + ASSOCIATIONS);
        deriveAcc(acc, base);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            factory.accCommandRepository(requester).updateBasedAccManifestId(new AccManifestId(n(acc)), new AccManifestId(n(other)));
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithBaseRemoved(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", NO_BASE + " and " + ASSOCIATIONS);
        long acc = pick(Kind.ACC, "to be derived", NO_BASE + " and " + ASSOCIATIONS);
        deriveAcc(acc, base);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            factory.accCommandRepository(requester).updateBasedAccManifestId(new AccManifestId(n(acc)), null);
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accWithBaseSet(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", NO_BASE + " and " + ASSOCIATIONS);
        long acc = pick(Kind.ACC, "without a base", NO_BASE + " and " + ASSOCIATIONS);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            factory.accCommandRepository(requester).updateBasedAccManifestId(new AccManifestId(n(acc)), new AccManifestId(n(base)));
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accBecomesTheBaseOfANewAcc(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            newAcc(acc);
            cancel(Kind.ACC, acc);
        }, nothingStays().added("acc", 1).added("acc_manifest", 1).ignoreAdded("seq_key", "ascc", "bcc", "ascc_manifest", "bcc_manifest"));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accBecomesTheRoleOfANewAsccp(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            newAsccp(acc);
            cancel(Kind.ACC, acc);
        }, nothingStays().added("asccp", 1).added("asccp_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void baseAccIsCancelledAfterTheDerivedAccIsRevised(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", NO_BASE + " and " + ASSOCIATIONS + " and " + ROLE_OF);
        long derived = pick(Kind.ACC, "to be derived", NO_BASE + " and " + ASSOCIATIONS);
        deriveAcc(derived, base);
        roundTrip(() -> {
            revise(Kind.ACC, base);
            revise(Kind.ACC, derived);
            cancel(Kind.ACC, base);
            cancel(Kind.ACC, derived);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void derivedAccIsCancelledBeforeTheBaseAcc(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", NO_BASE + " and " + ASSOCIATIONS + " and " + ROLE_OF);
        long derived = pick(Kind.ACC, "to be derived", NO_BASE + " and " + ASSOCIATIONS);
        deriveAcc(derived, base);
        roundTrip(() -> {
            revise(Kind.ACC, base);
            revise(Kind.ACC, derived);
            cancel(Kind.ACC, derived);
            cancel(Kind.ACC, base);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void derivedAccIsRevisedFirstAndBaseAccLater(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.ACC, "to be the base", NO_BASE + " and " + ASSOCIATIONS + " and " + ROLE_OF);
        long derived = pick(Kind.ACC, "to be derived", NO_BASE + " and " + ASSOCIATIONS);
        deriveAcc(derived, base);
        roundTrip(() -> {
            revise(Kind.ACC, derived);
            revise(Kind.ACC, base);
            cancel(Kind.ACC, base);
            cancel(Kind.ACC, derived);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void accRevisedAgain(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS + " and " + ROLE_OF);
        revise(Kind.ACC, acc);
        publish(Kind.ACC, acc);
        roundTrip(() -> {
            revise(Kind.ACC, acc);
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    // ---- ASCCP ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccp(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "used by an association", USED_ASCCP);
        roundTrip(() -> {
            revise(Kind.ASCCP, asccp);
            cancel(Kind.ASCCP, asccp);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccpIsAddedToANewAcc(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "used by an association", USED_ASCCP);
        roundTrip(() -> {
            revise(Kind.ASCCP, asccp);
            long acc = newAcc(null);
            factory.accCommandRepository(requester).createAscc(new AccManifestId(n(acc)), new AsccpManifestId(n(asccp)), null, null, true);
            cancel(Kind.ASCCP, asccp);
        }, nothingStays().added("acc", 1).added("acc_manifest", 1).added("ascc", 1).added("ascc_manifest", 1).ignoreAdded("seq_key"));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccpRoleChanged(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "used by an association", USED_ASCCP);
        long other = pick(Kind.ACC, "to be the other role", NO_BASE + " and " + ASSOCIATIONS);
        roundTrip(() -> {
            revise(Kind.ASCCP, asccp);
            factory.asccpCommandRepository(requester).updateRoleOfAcc(new AsccpManifestId(n(asccp)), new AccManifestId(n(other)));
            cancel(Kind.ASCCP, asccp);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccpIsCancelledAfterTheAccThatHasItIsRevised(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        long asccp = ids("select to_asccp_manifest_id from ascc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        watch(Kind.ASCCP, asccp);
        roundTrip(() -> {
            revise(Kind.ASCCP, asccp);
            revise(Kind.ACC, acc);
            cancel(Kind.ASCCP, asccp);
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccpRevisedAgain(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "used by an association", USED_ASCCP);
        revise(Kind.ASCCP, asccp);
        publish(Kind.ASCCP, asccp);
        roundTrip(() -> {
            revise(Kind.ASCCP, asccp);
            cancel(Kind.ASCCP, asccp);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void asccpIsRenamed(Branch branch) {
        useBranch(branch);
        long asccp = pick(Kind.ASCCP, "used by an association", USED_ASCCP);
        roundTrip(() -> {
            revise(Kind.ASCCP, asccp);
            factory.asccpCommandRepository(requester).update(new AsccpManifestId(n(asccp)), "Renamed In Revision", null, null, null, null, null);
            cancel(Kind.ASCCP, asccp);
        }, nothingStays());
    }

    // ---- BCCP ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccp(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "used by an association", USED_BCCP);
        roundTrip(() -> {
            revise(Kind.BCCP, bccp);
            cancel(Kind.BCCP, bccp);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccpIsAddedToANewAcc(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "used by an association", USED_BCCP);
        roundTrip(() -> {
            revise(Kind.BCCP, bccp);
            long acc = newAcc(null);
            factory.accCommandRepository(requester).createBcc(new AccManifestId(n(acc)), new BccpManifestId(n(bccp)), null, null, true);
            cancel(Kind.BCCP, bccp);
        }, nothingStays().added("acc", 1).added("acc_manifest", 1).added("bcc", 1).added("bcc_manifest", 1).ignoreAdded("seq_key"));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccpIsRenamed(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "used by an association", USED_BCCP);
        roundTrip(() -> {
            revise(Kind.BCCP, bccp);
            factory.bccpCommandRepository(requester).update(new BccpManifestId(n(bccp)), "Renamed In Revision", null, null, null, null, null);
            cancel(Kind.BCCP, bccp);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccpDataTypeChanged(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "used by an association", USED_BCCP);
        long other = pick(Kind.DT, "to be the other Data Type", WITH_SC + " and m.based_dt_manifest_id is not null");
        roundTrip(() -> {
            revise(Kind.BCCP, bccp);
            factory.bccpCommandRepository(requester).updateDt(new BccpManifestId(n(bccp)), new DtManifestId(n(other)));
            cancel(Kind.BCCP, bccp);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccpIsCancelledAfterTheAccThatHasItIsRevised(Branch branch) {
        useBranch(branch);
        long acc = pick(Kind.ACC, "with associations", NO_BASE + " and " + ASSOCIATIONS);
        long bccp = ids("select to_bccp_manifest_id from bcc_manifest where from_acc_manifest_id = ? order by 1 limit 1", acc).get(0);
        watch(Kind.BCCP, bccp);
        roundTrip(() -> {
            revise(Kind.BCCP, bccp);
            revise(Kind.ACC, acc);
            cancel(Kind.BCCP, bccp);
            cancel(Kind.ACC, acc);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void bccpRevisedAgain(Branch branch) {
        useBranch(branch);
        long bccp = pick(Kind.BCCP, "used by an association", USED_BCCP);
        revise(Kind.BCCP, bccp);
        publish(Kind.BCCP, bccp);
        roundTrip(() -> {
            revise(Kind.BCCP, bccp);
            cancel(Kind.BCCP, bccp);
        }, nothingStays());
    }

    // ---- Data Type ----

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dt(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP", WITH_SC
                + " and exists (select 1 from bccp_manifest b where b.bdt_manifest_id = m.dt_manifest_id)");
        roundTrip(() -> {
            revise(Kind.DT, dt);
            cancel(Kind.DT, dt);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtUsedByANewBccp(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP", WITH_SC
                + " and exists (select 1 from bccp_manifest b where b.bdt_manifest_id = m.dt_manifest_id)");
        roundTrip(() -> {
            revise(Kind.DT, dt);
            newBccp(dt);
            cancel(Kind.DT, dt);
        }, nothingStays().added("bccp", 1).added("bccp_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtIsTheBaseOfANewDt(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with a base of another library", WITH_SC + " and " + CROSS_LIBRARY_BASE);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            newDt(dt);
            cancel(Kind.DT, dt);
        }, nothingStays().added("dt", 1).added("dt_manifest", 1)
                .ignoreAdded("dt_sc", "dt_sc_manifest", "dt_awd_pri", "dt_sc_awd_pri"));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtGetsASupplementaryComponent(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with a base of another library", WITH_SC + " and " + CROSS_LIBRARY_BASE);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            factory.dtCommandRepository(requester).appendDtSc(new DtManifestId(n(dt)));
            cancel(Kind.DT, dt);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtCannotBeCancelledWhileADtIsBasedOnASupplementaryComponentAddedInTheRevision(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with a base of another library", WITH_SC + " and " + CROSS_LIBRARY_BASE);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            factory.dtCommandRepository(requester).appendDtSc(new DtManifestId(n(dt)));
            long derived = newDt(dt);
            // the derived Data Type has a supplementary component that is based on the added one
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> cancel(Kind.DT, dt));
            // when the derived Data Type is gone, the cancel goes through
            factory.dtCommandRepository(requester).delete(new DtManifestId(n(derived)));
            cancel(Kind.DT, dt);
        }, nothingStays().ignoreAdded("dt_sc", "dt_sc_manifest", "dt_awd_pri", "dt_sc_awd_pri"));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtIsCancelledWhenTheDtBasedOnItOnlyHasSupplementaryComponentsOfTheRevisedOnes(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with a base of another library", WITH_SC + " and " + CROSS_LIBRARY_BASE);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            long derived = newDt(dt);
            cancel(Kind.DT, dt);
            // the derived Data Type is still there, based on the Data Type that is back
            org.junit.jupiter.api.Assertions.assertEquals(1L, one("select count(*) from dt_manifest where dt_manifest_id = ? and based_dt_manifest_id = ?", derived, dt));
        }, nothingStays().added("dt", 1).added("dt_manifest", 1).ignoreAdded("dt_sc", "dt_sc_manifest", "dt_awd_pri", "dt_sc_awd_pri"));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtLosesASupplementaryComponent(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with supplementary components that nothing is based on", WITH_SC
                + " and not exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        long sc = ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ? order by 1 limit 1", dt).get(0);
        List<String> supplementaryComponents = supplementaryComponentGuids(dt);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            service.discard(requester, new DtScManifestId(n(sc)), false);
            assertEquals(supplementaryComponents.size() - 1, supplementaryComponentGuids(dt).size(), "the supplementary component is deleted");
            cancel(Kind.DT, dt);
            assertEquals(supplementaryComponents, supplementaryComponentGuids(dt), "the supplementary components after the cancel");
        }, nothingStays().added("dt_sc_manifest", 1).removed("dt_sc_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtWithABaseLosesASupplementaryComponentThatIsBasedOnTheOneOfTheBase(Branch branch) {
        useBranch(branch);
        // the supplementary components of the base and of the Data Types based on it share their GUIDs in the release
        long dt = pick(Kind.DT, "with a base and supplementary components that nothing is based on",
                WITH_SC + " and m.based_dt_manifest_id is not null and not exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        long sc = ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ? and based_dt_sc_manifest_id is not null order by 1 limit 1", dt).get(0);
        List<String> bases = supplementaryComponentBases(dt);
        assertTrue(bases.stream().anyMatch(e -> !e.endsWith("-")), "a supplementary component is based on one of the base");
        roundTrip(() -> {
            revise(Kind.DT, dt);
            service.discard(requester, new DtScManifestId(n(sc)), false);
            cancel(Kind.DT, dt);
            assertEquals(bases, supplementaryComponentBases(dt), "what the supplementary components are based on after the cancel");
        }, nothingStays().added("dt_sc_manifest", 1).removed("dt_sc_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtLosesEverySupplementaryComponent(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with several supplementary components that nothing is based on",
                "(select count(*) from dt_sc_manifest s where s.owner_dt_manifest_id = m.dt_manifest_id) >= 2 "
                        + "and not exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        List<Long> scs = ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ?", dt);
        List<String> supplementaryComponents = supplementaryComponentGuids(dt);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            scs.forEach(e -> service.discard(requester, new DtScManifestId(n(e)), false));
            assertEquals(0, supplementaryComponentGuids(dt).size(), "the supplementary components are deleted");
            cancel(Kind.DT, dt);
            assertEquals(supplementaryComponents, supplementaryComponentGuids(dt), "the supplementary components after the cancel");
        }, nothingStays().added("dt_sc_manifest", scs.size()).removed("dt_sc_manifest", scs.size()));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void deletedSupplementaryComponentLeavesTheRecordsLinked(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with supplementary components that nothing is based on", WITH_SC
                + " and not exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        long sc = ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ? order by 1 limit 1", dt).get(0);
        revise(Kind.DT, dt);
        long revised = recordOf(Kind.DT_SC, sc);
        long original = one("select prev_dt_sc_id from dt_sc where dt_sc_id = ?", revised);

        service.discard(requester, new DtScManifestId(n(sc)), false);

        assertEquals(0L, one("select count(*) from dt_sc where dt_sc_id = ?", revised), "the revision of the deleted supplementary component");
        assertEquals(1L, one("select count(*) from dt_sc where dt_sc_id = ? and next_dt_sc_id is null", original),
                "the supplementary component it was revised from is not linked to a next revision");
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtRevisedAgainWithASupplementaryComponentDeleted(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with supplementary components that nothing is based on", WITH_SC
                + " and not exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        revise(Kind.DT, dt);
        publish(Kind.DT, dt);
        long sc = ids("select dt_sc_manifest_id from dt_sc_manifest where owner_dt_manifest_id = ? order by 1 limit 1", dt).get(0);
        List<String> supplementaryComponents = supplementaryComponentGuids(dt);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            service.discard(requester, new DtScManifestId(n(sc)), false);
            cancel(Kind.DT, dt);
            assertEquals(supplementaryComponents, supplementaryComponentGuids(dt), "the supplementary components after the cancel");
        }, nothingStays().added("dt_sc_manifest", 1).removed("dt_sc_manifest", 1));
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtAllowedPrimitivesChanged(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "with a base of another library, allowed primitives of an XBT", WITH_SC + " and " + CROSS_LIBRARY_BASE
                + " and exists (select 1 from dt_awd_pri p where p.release_id = m.release_id and p.dt_id = m.dt_id and p.xbt_manifest_id is not null)");
        long oldRecord = recordOf(Kind.DT, dt);
        Map<String, Object> pri = dsl.fetchOne("select p.cdt_pri_id, c.name, p.xbt_manifest_id from dt_awd_pri p join cdt_pri c on c.cdt_pri_id = p.cdt_pri_id "
                + "where p.release_id = ? and p.dt_id = ? and p.xbt_manifest_id is not null order by p.dt_awd_pri_id limit 1", release, oldRecord).intoMap();
        roundTrip(() -> {
            revise(Kind.DT, dt);
            long newRecord = recordOf(Kind.DT, dt);
            // one more allowed primitive, and one less
            factory.dtCommandRepository(requester).createDtAwdPri(releaseId(), new DtId(n(newRecord)), String.valueOf(pri.get("name")),
                    new XbtManifestId(n(Long.parseLong(String.valueOf(pri.get("xbt_manifest_id"))))), null, null, false);
            long first = ids("select dt_awd_pri_id from dt_awd_pri where release_id = ? and dt_id = ? order by 1 limit 1", release, newRecord).get(0);
            factory.dtCommandRepository(requester).deleteDtAwdPri(new DtAwdPriId(n(first)));
            cancel(Kind.DT, dt);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void baseDtIsCancelledAfterTheDerivedDtIsRevised(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.DT, "with a base of another library and a Data Type based on it", WITH_SC + " and " + CROSS_LIBRARY_BASE
                + " and exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        long derived = ids("select dt_manifest_id from dt_manifest where based_dt_manifest_id = ? order by 1 limit 1", base).get(0);
        watch(Kind.DT, derived);
        roundTrip(() -> {
            revise(Kind.DT, base);
            revise(Kind.DT, derived);
            cancel(Kind.DT, base);
            cancel(Kind.DT, derived);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void derivedDtIsCancelledBeforeTheBaseDt(Branch branch) {
        useBranch(branch);
        long base = pick(Kind.DT, "with a base of another library and a Data Type based on it", WITH_SC + " and " + CROSS_LIBRARY_BASE
                + " and exists (select 1 from dt_manifest d where d.based_dt_manifest_id = m.dt_manifest_id)");
        long derived = ids("select dt_manifest_id from dt_manifest where based_dt_manifest_id = ? order by 1 limit 1", base).get(0);
        watch(Kind.DT, derived);
        roundTrip(() -> {
            revise(Kind.DT, base);
            revise(Kind.DT, derived);
            cancel(Kind.DT, derived);
            cancel(Kind.DT, base);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtIsCancelledAfterTheBccpThatUsesItIsRevised(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP", WITH_SC + " and exists (select 1 from bccp_manifest b where b.bdt_manifest_id = m.dt_manifest_id)");
        long bccp = ids("select bccp_manifest_id from bccp_manifest where bdt_manifest_id = ? order by 1 limit 1", dt).get(0);
        watch(Kind.BCCP, bccp);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            revise(Kind.BCCP, bccp);
            cancel(Kind.DT, dt);
            cancel(Kind.BCCP, bccp);
        }, nothingStays());
    }

    @ParameterizedTest
    @EnumSource(Branch.class)
    void dtRevisedAgain(Branch branch) {
        useBranch(branch);
        long dt = pick(Kind.DT, "used by a BCCP", WITH_SC + " and exists (select 1 from bccp_manifest b where b.bdt_manifest_id = m.dt_manifest_id)");
        revise(Kind.DT, dt);
        publish(Kind.DT, dt);
        roundTrip(() -> {
            revise(Kind.DT, dt);
            cancel(Kind.DT, dt);
        }, nothingStays());
    }
}
