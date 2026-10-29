package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASBIE_BIZTERM;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BBIE_BIZTERM;

/**
 * Discarding a BIE must remove its business term assignments, and the ASCC/BCC business terms
 * left without an assignment, before the ASBIE/BBIE rows are deleted with foreign key checks off.
 */
class DiscardBieBusinessTermAssignmentsTest {

    private static int indexOf(List<String> sql, String prefix) {
        for (int i = 0; i < sql.size(); i++) {
            if (sql.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    void assignmentsAreDeletedBeforeTheirAsbieAndBbieRows() {
        List<String> sql = new ArrayList<>();
        var create = DSL.using(SQLDialect.MYSQL);
        JooqBieCommandRepository repo = new JooqBieCommandRepository(
                DSL.using(new MockConnection(ctx -> {
                    String s = ctx.sql().toLowerCase(Locale.ROOT);
                    sql.add(s);
                    if (s.startsWith("select")) {
                        var field = s.contains("`ascc_bizterm_id`") ? ASBIE_BIZTERM.ASCC_BIZTERM_ID : BBIE_BIZTERM.BCC_BIZTERM_ID;
                        var result = create.newResult(field);
                        var record = create.newRecord(field);
                        record.set(field, ULong.valueOf(7));
                        result.add(record);
                        return new MockResult[]{new MockResult(1, result)};
                    }
                    return new MockResult[]{new MockResult(1, null)};
                }), SQLDialect.MYSQL), null, mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));

        repo.deleteByTopLevelAsbiepIdList(List.of(new TopLevelAsbiepId(BigInteger.TEN)));

        int asbieBizterm = indexOf(sql, "delete from `asbie_bizterm`");
        int asccBizterm = indexOf(sql, "delete from `ascc_bizterm`");
        int asbie = indexOf(sql, "delete from `asbie`");
        int bbieBizterm = indexOf(sql, "delete from `bbie_bizterm`");
        int bccBizterm = indexOf(sql, "delete from `bcc_bizterm`");
        int bbie = indexOf(sql, "delete from `bbie`");

        assertTrue(asbieBizterm >= 0 && asbieBizterm < asccBizterm, sql.toString());
        assertTrue(bbieBizterm >= 0 && bbieBizterm < bccBizterm, sql.toString());
        assertTrue(asccBizterm < asbie && bccBizterm < bbie, sql.toString());
    }
}
