package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSupportDocId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSupportDocRecord;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ASBIEP_SUPPORT_DOC;

/** Saving a BIE changes only the support documents of that BIE's ASBIEP. */
class AsbiepSupportDocOwnershipTest {

    private static final AsbiepId ASBIEP = new AsbiepId(BigInteger.valueOf(5));
    private static final AsbiepSupportDocId OWN_DOC = new AsbiepSupportDocId(BigInteger.valueOf(100));
    private static final AsbiepSupportDocId OTHER_DOC = new AsbiepSupportDocId(BigInteger.valueOf(123));

    private final List<String> sql = new ArrayList<>();

    /** The ASBIEP owns one support document (100); everything else the repository asks for is empty. */
    private JooqAsbiepCommandRepository repository() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            String statement = ctx.sql().toLowerCase(Locale.ROOT);
            sql.add(statement);
            Result<?> result;
            if (statement.startsWith("select `asbiep_support_doc`.`asbiep_support_doc_id` from")) {
                var rows = DSL.using(SQLDialect.MYSQL).newResult(ASBIEP_SUPPORT_DOC.ASBIEP_SUPPORT_DOC_ID);
                rows.add(DSL.using(SQLDialect.MYSQL).newRecord(ASBIEP_SUPPORT_DOC.ASBIEP_SUPPORT_DOC_ID)
                        .values(ULong.valueOf(100)));
                result = rows;
            } else {
                result = DSL.using(SQLDialect.MYSQL).newResult();
            }
            return new MockResult[]{new MockResult(0, result)};
        };
        return new JooqAsbiepCommandRepository(
                DSL.using(new MockConnection(provider), SQLDialect.MYSQL), null, mock(RepositoryFactory.class));
    }

    private static AsbiepSupportDocRecord doc(AsbiepSupportDocId id) {
        return new AsbiepSupportDocRecord(id, ASBIEP, "content", "description");
    }

    @Test
    void aSupportDocumentOfAnotherAsbiepIsRejectedBeforeAnythingIsWritten() {
        JooqAsbiepCommandRepository repo = repository();

        assertThrows(IllegalArgumentException.class,
                () -> repo.applyAsbiepSupportDocListChanges(ASBIEP, List.of(doc(OTHER_DOC))));

        assertTrue(sql.stream().noneMatch(s -> s.startsWith("update") || s.startsWith("delete") || s.startsWith("insert")),
                sql.toString());
    }

    @Test
    void aSupportDocumentOfTheAsbiepIsStillUpdated() {
        JooqAsbiepCommandRepository repo = repository();

        repo.applyAsbiepSupportDocListChanges(ASBIEP, List.of(doc(OWN_DOC)));

        assertTrue(sql.stream().anyMatch(s -> s.contains("`asbiep_support_doc_id` = ?")), sql.toString());
    }
}
