package org.oagi.score.gateway.http.api.log_management.repository;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.model.SortDirection;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.LOG;

/**
 * Guards the History list column sort (issue #5): `getLogByReference` used to hard-code
 * `ORDER BY LOG_ID DESC` regardless of the sort the UI requested (`orderFields`).
 *
 * <p>Renders the SQL of an ORDER BY built from {@code orderFields} (no DB) and asserts the
 * requested column, not {@code log_id desc}, drives the sort.
 */
class LogRepositoryTest {

    private static LogRepository repository() {
        DSLContext dslContext = DSL.using(SQLDialect.MYSQL);
        return new LogRepository(dslContext, new RepositoryFactory(dslContext));
    }

    private static PageRequest withSort(String field, SortDirection direction) {
        return new PageRequest(0, 10, List.of(new Sort(field, direction)));
    }

    private static String sql(List<SortField<?>> orderFields) {
        DSLContext dslContext = DSL.using(SQLDialect.MYSQL);
        return dslContext.select(LOG.LOG_ID).from(LOG).orderBy(orderFields).getSQL()
                .toLowerCase().replace("`", "");
    }

    @Test
    void ordersByTheRequestedColumn_insteadOfAlwaysLogIdDesc() {
        String generatedSql = sql(repository().orderFields(withSort("revisionNum", SortDirection.ASC)));

        assertThat(generatedSql).contains("order by").contains("revision_num asc");
        assertThat(generatedSql).doesNotContain("log_id desc");
    }

    @Test
    void mapsEachSortableColumnToItsUnderlyingField() {
        assertThat(sql(repository().orderFields(withSort("commit", SortDirection.DESC)))).contains("hash desc");
        assertThat(sql(repository().orderFields(withSort("revisionAction", SortDirection.ASC)))).contains("log_action asc");
        assertThat(sql(repository().orderFields(withSort("loginId", SortDirection.ASC)))).contains("login_id asc");
        assertThat(sql(repository().orderFields(withSort("creationTimestamp", SortDirection.DESC)))).contains("creation_timestamp desc");
    }

    @Test
    void defaultsToLogIdDesc_whenNoSortOrAnUnknownColumnIsRequested() {
        assertThat(sql(repository().orderFields(new PageRequest(0, 10, List.of())))).contains("log_id desc");
        assertThat(sql(repository().orderFields(withSort("unknown", SortDirection.ASC)))).contains("log_id desc");
    }
}
