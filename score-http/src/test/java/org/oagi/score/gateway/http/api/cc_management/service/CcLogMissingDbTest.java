package org.oagi.score.gateway.http.api.cc_management.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.log_management.model.LogNotFoundException;

import java.sql.Savepoint;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A revision goes on from the log a component is in, and a cancel goes back to the log it was in before. The logs are
 * never missing unless somebody removed them from the database, so this removes them by hand:
 * <ul>
 *   <li>a log that does not say what component it is of is still the log to go back to;</li>
 *   <li>when there is no log to go back to, the cancel says so and changes nothing, and when the user lets a new history
 *   be written, the component has revision 1 and nothing else;</li>
 *   <li>a component that has no log cannot be revised until the user lets a new history be written, which makes revision 1,
 *   and the revision is revision 2.</li>
 * </ul>
 * <pre>
 *   mvn test -Drevision=3.5.0 -Dtest=CcLogMissingDbTest -DargLine=-Dscore.reviseCancel=true
 * </pre>
 */
class CcLogMissingDbTest extends CcReviseCancelFixture {

    static Stream<Arguments> components() {
        return Stream.of(Branch.values()).flatMap(branch ->
                Stream.of(Kind.ACC, Kind.ASCCP, Kind.BCCP, Kind.DT).map(kind -> Arguments.of(branch, kind)));
    }

    void revise(Kind kind, long m, boolean recreateLog) {
        switch (kind) {
            case ACC -> service.reviseAcc(requester, new AccManifestId(n(m)), recreateLog);
            case ASCCP -> service.reviseAsccp(requester, new AsccpManifestId(n(m)), recreateLog);
            case BCCP -> service.reviseBccp(requester, new BccpManifestId(n(m)), recreateLog);
            case DT -> service.reviseDt(requester, new DtManifestId(n(m)), recreateLog);
            default -> throw new IllegalArgumentException();
        }
    }

    void cancel(Kind kind, long m, boolean recreateLog) {
        switch (kind) {
            case ACC -> service.cancelAcc(requester, new AccManifestId(n(m)), null, null, recreateLog);
            case ASCCP -> service.cancelAsccp(requester, new AsccpManifestId(n(m)), null, null, recreateLog);
            case BCCP -> service.cancelBccp(requester, new BccpManifestId(n(m)), null, null, recreateLog);
            case DT -> service.cancelDt(requester, new DtManifestId(n(m)), null, null, recreateLog);
            default -> throw new IllegalArgumentException();
        }
    }

    Long logOf(Kind kind, long m) {
        return dsl.fetchOne("select log_id from `" + kind.manifest() + "` where `" + kind.mpk() + "` = ?", m).get(0, Long.class);
    }

    String guidOf(Kind kind, long m) {
        return dsl.fetchOne("select guid from `" + kind.table + "` where `" + kind.pk() + "` = ?", recordOf(kind, m))
                .get(0, String.class);
    }

    List<Long> logsOf(String guid) {
        return ids("select log_id from log where reference = ? order by log_id", guid);
    }

    /** What an operator does: the logs go, and what refers to them lets go first. */
    void removeLogs(Kind kind, String guid, String where) {
        List<Long> gone = ids("select log_id from log where reference = ? and " + where, guid);
        if (gone.isEmpty()) {
            return;
        }
        String list = gone.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        dsl.execute("update `" + kind.manifest() + "` set log_id = null where log_id in (" + list + ")");
        dsl.execute("update log set prev_log_id = null where prev_log_id in (" + list + ")");
        dsl.execute("update log set next_log_id = null where next_log_id in (" + list + ")");
        dsl.execute("delete from log where log_id in (" + list + ")");
    }

    // ---- a log that does not say what it is of ----

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("components")
    void theLogThatDoesNotSayWhatItIsOfIsTheLogToGoBackTo(Branch branch, Kind kind) {
        useBranch(branch);
        long m = pick(kind, "revisable", "1 = 1");
        Long stable = logOf(kind, m);
        String guid = guidOf(kind, m);
        revise(kind, m, false);
        dsl.execute("update log set snapshot = '{}' where log_id = ?", stable);

        cancel(kind, m, false);

        assertEquals(stable, logOf(kind, m), "the manifest is in the log it was in");
        assertEquals(0L, one("select count(*) from log where reference = ? and revision_num > (select revision_num from log where log_id = ?)",
                guid, stable), "the logs of the revision are deleted");
    }

    // ---- the log to go back to is gone ----

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("components")
    void cancelWithoutTheLogToGoBackToIsRefusedAndWritesNewHistoryWhenLetTo(Branch branch, Kind kind) throws Exception {
        useBranch(branch);
        long m = pick(kind, "revisable", "1 = 1");
        String guid = guidOf(kind, m);
        long record = recordOf(kind, m);
        revise(kind, m, false);
        long revised = recordOf(kind, m);
        removeLogs(kind, guid, "revision_num < (select revision_num from (select revision_num from log where log_id = " + logOf(kind, m) + ") r)");
        Assertions.assertFalse(logsOf(guid).isEmpty());

        // refused, and nothing is changed (the service is a transaction: the changes up to the refusal are taken back)
        Savepoint beforeCancel = connection.setSavepoint();
        assertThrows(LogNotFoundException.class, () -> cancel(kind, m, false));
        connection.rollback(beforeCancel);
        assertEquals(revised, recordOf(kind, m), "the revision is still there");

        // let to write the new history: revision 1 is all that is left
        cancel(kind, m, true);
        assertEquals(record, recordOf(kind, m), "the component is back");
        List<Long> logs = logsOf(guid);
        assertEquals(1, logs.size(), "one log is left");
        assertEquals(logs.get(0), logOf(kind, m), "the manifest is in it");
        assertEquals(1L, one("select revision_num from log where log_id = ?", logs.get(0)), "revision 1");
        assertEquals(1L, one("select revision_tracking_num from log where log_id = ?", logs.get(0)));

        // and it can be revised again, as revision 2
        revise(kind, m, false);
        assertEquals(2L, one("select revision_num from log where log_id = ?", logOf(kind, m)));
    }

    // ---- a component without a log ----

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("components")
    void aComponentWithoutALogIsRevisedWhenLetToWriteNewHistory(Branch branch, Kind kind) throws Exception {
        useBranch(branch);
        long m = pick(kind, "revisable", "1 = 1");
        String guid = guidOf(kind, m);
        long record = recordOf(kind, m);
        removeLogs(kind, guid, "1 = 1");
        assertEquals(0, logsOf(guid).size());
        dsl.execute("update `" + kind.manifest() + "` set log_id = null where `" + kind.mpk() + "` = ?", m);

        assertThrows(LogNotFoundException.class, () -> revise(kind, m, false));
        assertEquals(record, recordOf(kind, m), "nothing is revised");
        assertEquals(0, logsOf(guid).size(), "nothing is written");

        revise(kind, m, true);
        Assertions.assertNotEquals(record, recordOf(kind, m), "the revision is made");
        List<Long> revisions = ids("select revision_num from log where reference = ? order by log_id", guid);
        assertEquals(List.of(1L, 2L), revisions, "revision 1 is the component as it was, and revision 2 the revision");
        assertEquals(2L, one("select revision_num from log where log_id = ?", logOf(kind, m)));

        // the revision is cancelled to revision 1
        cancel(kind, m, false);
        assertEquals(record, recordOf(kind, m));
        assertEquals(List.of(1L), ids("select revision_num from log where reference = ?", guid));
    }
}
