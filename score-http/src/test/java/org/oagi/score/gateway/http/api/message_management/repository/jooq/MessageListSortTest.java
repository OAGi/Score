package org.oagi.score.gateway.http.api.message_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.message_management.repository.criteria.MessageListFilterCriteria;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.model.SortDirection;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sorting the Message list by Subject must produce an ORDER BY on the subject column.
 */
class MessageListSortTest {

    private List<String> listSql(Sort sort) {
        List<String> sql = new ArrayList<>();
        JooqMessageQueryRepository repo = new JooqMessageQueryRepository(
                DSL.using(new MockConnection(ctx -> {
                    sql.add(ctx.sql().toLowerCase(Locale.ROOT));
                    var create = DSL.using(SQLDialect.MYSQL);
                    var count = DSL.field("c", Integer.class);
                    var result = create.newResult(count);
                    var record = create.newRecord(count);
                    record.value1(0);
                    result.add(record);
                    return new MockResult[]{new MockResult(1, result)};
                }), SQLDialect.MYSQL), null, null);

        try {
            repo.getMessageList(new UserId(BigInteger.ONE), new MessageListFilterCriteria(null, null, null),
                    new PageRequest(0, 10, List.of(sort)));
        } catch (RuntimeException ignored) {
            // mapping the mocked rows is not under test; the issued statements are
        }
        return sql;
    }

    @Test
    void sortsBySubjectAscending() {
        List<String> sql = listSql(new Sort("subject", SortDirection.ASC));

        assertTrue(sql.stream().anyMatch(s -> s.contains("order by `oagi`.`message`.`subject` asc")
                || s.contains("order by `message`.`subject` asc")), sql.toString());
    }

    @Test
    void sortsBySubjectDescending() {
        List<String> sql = listSql(new Sort("subject", SortDirection.DESC));

        assertTrue(sql.stream().anyMatch(s -> s.contains("`message`.`subject` desc")), sql.toString());
    }
}
