package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** A BIE that an operation emits as its error ConfirmMessage counts as used in an OpenAPI document. */
class OpenApiDocumentErrorConfirmReferenceTest {

    private final List<String> sql = new ArrayList<>();

    private JooqOpenApiDocumentQueryRepository repository() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            String statement = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(statement);
            // only the operation's error ConfirmMessage column references the BIE
            int count = statement.contains("error_confirm_top_level_asbiep_id") ? 1 : 0;
            var field = DSL.field("c", Integer.class);
            var result = DSL.using(SQLDialect.MYSQL).newResult(field);
            result.add(DSL.using(SQLDialect.MYSQL).newRecord(field).values(count));
            return new MockResult[]{new MockResult(1, result)};
        };
        return new JooqOpenApiDocumentQueryRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, null);
    }

    @Test
    void anErrorConfirmMessageBieIsAReference() {
        assertTrue(repository().hasTopLevelAsbiepReference(new TopLevelAsbiepId(BigInteger.TEN)));
        assertTrue(sql.stream().anyMatch(s -> s.contains("error_confirm_top_level_asbiep_id")), sql.toString());
    }
}
