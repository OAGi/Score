package org.oagi.score.gateway.http.api.code_list_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.repository.AgencyIdListQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A code list without an Agency ID List Value is compared with the ones that have none, not with a NULL equality. */
class CodeListSameWithoutAgencyTest {

    private final List<String> sql = new ArrayList<>();

    private JooqCodeListQueryRepository repository() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            var result = DSL.using(SQLDialect.MYSQL).newResult(DSL.field("c", Integer.class));
            result.add(DSL.using(SQLDialect.MYSQL).newRecord(DSL.field("c", Integer.class)).values(0));
            return new MockResult[]{new MockResult(1, result)};
        };
        RepositoryFactory repositories = mock(RepositoryFactory.class);
        when(repositories.agencyIdListQueryRepository(any())).thenReturn(mock(AgencyIdListQueryRepository.class));
        return new JooqCodeListQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, repositories);
    }

    @Test
    void withoutAnAgencyIdListValueTheQueryTestsForNull() {
        repository().hasSameCodeList(new ReleaseId(BigInteger.ONE), null, null, "list", "1.0");

        assertTrue(sql.get(0).contains("`agency_id_list_value_manifest_id` is null"), sql.toString());
    }

    @Test
    void withAnAgencyIdListValueTheQueryComparesTheManifest() {
        repository().hasSameCodeList(new ReleaseId(BigInteger.ONE), null,
                new AgencyIdListValueManifestId(BigInteger.TEN), "list", "1.0");

        assertTrue(sql.get(0).contains("`agency_id_list_value_manifest_id` = ?"), sql.toString());
    }
}
