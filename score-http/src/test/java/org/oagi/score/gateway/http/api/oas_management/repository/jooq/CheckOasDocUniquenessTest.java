package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasDoc;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * An OpenAPI Document is unique on (Title, OpenAPI Version, Document Version); the License Name is not
 * part of the key, and an empty Document Version (stored as NULL) still takes part in the check.
 */
class CheckOasDocUniquenessTest {

    private String uniquenessSql(OasDoc oasDoc, int existing, boolean expectUnique) {
        List<String> sql = new ArrayList<>();
        var create = DSL.using(SQLDialect.MYSQL);
        JooqOasDocQueryRepository repo = new JooqOasDocQueryRepository(
                DSL.using(new MockConnection(ctx -> {
                    sql.add(ctx.sql().toLowerCase(Locale.ROOT));
                    var count = DSL.field("c", Integer.class);
                    var result = create.newResult(count);
                    var record = create.newRecord(count);
                    record.value1(existing);
                    result.add(record);
                    return new MockResult[]{new MockResult(1, result)};
                }), SQLDialect.MYSQL), null, mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));
        assertTrue(repo.checkOasDocUniqueness(oasDoc) == expectUnique);
        return sql.get(0);
    }

    private OasDoc oasDoc(String version, String licenseName) {
        OasDoc oasDoc = new OasDoc();
        oasDoc.setTitle("dup title");
        oasDoc.setOpenAPIVersion("3.1.0");
        oasDoc.setVersion(version);
        oasDoc.setLicenseName(licenseName);
        return oasDoc;
    }

    @Test
    void licenseNameIsNotPartOfTheKey() {
        assertFalse(uniquenessSql(oasDoc("1.0", "MIT"), 0, true).contains("license_name"));
        assertFalse(uniquenessSql(oasDoc("1.0", null), 0, true).contains("license_name"));
    }

    @Test
    void duplicateOnTitleOpenApiVersionAndVersionIsReported() {
        String sql = uniquenessSql(oasDoc("1.0", "MIT"), 1, false);

        assertTrue(sql.contains("`title` = ?"), sql);
        assertTrue(sql.contains("`open_api_version` = ?"), sql);
        assertTrue(sql.contains("`version` = ?"), sql);
    }

    @Test
    void emptyDocumentVersionIsComparedAsNull() {
        String sql = uniquenessSql(oasDoc(null, null), 1, false);

        assertTrue(sql.contains("`version` is null"), sql);
    }

    @Test
    void createDoesNotExcludeAnyRowButUpdateExcludesItself() {
        OasDoc create = oasDoc("1.0", null);
        OasDoc update = oasDoc("1.0", null);
        update.setOasDocId(BigInteger.valueOf(7));

        assertFalse(uniquenessSql(create, 0, true).contains("`oas_doc_id` <>"));
        assertTrue(uniquenessSql(update, 0, true).contains("`oas_doc_id` <>"));
    }
}
