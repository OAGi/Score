package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcListTypes;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.repository.criteria.CcListFilterCriteria;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseQueryRepository;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A Component Type filter applies to ACCs only, so the other kinds of component must not be listed.
 */
class CcListComponentTypeFilterTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId(BigInteger.ONE);

    private List<String> listSql(List<OagisComponentType> componentTypes) {
        List<String> sql = new ArrayList<>();
        RepositoryFactory repositoryFactory = mock(RepositoryFactory.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        ReleaseQueryRepository releaseQuery = mock(ReleaseQueryRepository.class);
        ReleaseSummaryRecord release = mock(ReleaseSummaryRecord.class);
        when(release.releaseId()).thenReturn(RELEASE_ID);
        when(releaseQuery.getIncludedReleaseSummaryList(any())).thenReturn(Set.of(release));
        when(repositoryFactory.releaseQueryRepository(any())).thenReturn(releaseQuery);

        JooqCcQueryRepository repo = new JooqCcQueryRepository(
                DSL.using(new MockConnection(ctx -> {
                    sql.add(ctx.sql().toLowerCase(Locale.ROOT));
                    var create = DSL.using(SQLDialect.MYSQL);
                    var count = DSL.field("c", Integer.class);
                    var result = create.newResult(count);
                    var record = create.newRecord(count);
                    record.value1(0);
                    result.add(record);
                    return new MockResult[]{new MockResult(1, result)};
                }), SQLDialect.MYSQL), null, repositoryFactory);

        try {
            repo.getCcList(CcListFilterCriteria.builder(RELEASE_ID)
                    .types(CcListTypes.fromString(null))
                    .componentTypes(componentTypes)
                    .build(), new PageRequest(0, 10, List.of()));
        } catch (RuntimeException ignored) {
            // mapping the mocked rows is not under test; the issued statements are
        }
        return sql;
    }

    @Test
    void componentTypeFilterQueriesAccsOnly() {
        List<String> sql = listSql(List.of(OagisComponentType.Base));

        assertTrue(sql.stream().anyMatch(s -> s.contains("`acc_manifest`")), sql.toString());
        assertTrue(sql.stream().noneMatch(s -> s.contains("`asccp_manifest`") || s.contains("`bccp_manifest`")
                || s.contains("`ascc_manifest`") || s.contains("`bcc_manifest`") || s.contains("`dt_manifest`")), sql.toString());
    }

    @Test
    void withoutComponentTypeAllKindsAreQueried() {
        List<String> sql = listSql(List.of());

        assertFalse(sql.isEmpty());
        assertTrue(sql.stream().anyMatch(s -> s.contains("`asccp_manifest`")), sql.toString());
        assertTrue(sql.stream().anyMatch(s -> s.contains("`bccp_manifest`")), sql.toString());
    }
}
