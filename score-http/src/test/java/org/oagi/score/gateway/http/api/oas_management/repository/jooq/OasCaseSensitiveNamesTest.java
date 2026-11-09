package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

/**
 * Issue #1891: the names that the OpenAPI Specification defines as case-sensitive (a Security Scheme name, a scope name, an
 * Operation ID, a path, a parameter name) are compared case-sensitively by the database, so {@code Auth} and {@code auth}
 * are two names. Against a development database that has the V3_6_1 migration, in a transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=OasCaseSensitiveNamesTest -DargLine=-Dscore.oasCaseSensitive=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.oasCaseSensitive", matches = "true")
class OasCaseSensitiveNamesTest {

    private static TestDataSource dataSource;
    private static Connection connection;
    private static DSLContext dsl;
    private static ULong USER;

    private ULong oasDocId;

    @BeforeAll
    static void connect() throws SQLException {
        dataSource = TestDataSource.open(2);
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        dsl = DSL.using(connection, SQLDialect.MARIADB);
        USER = dsl.select(APP_USER.APP_USER_ID).from(APP_USER).orderBy(APP_USER.APP_USER_ID).limit(1)
                .fetchOne(APP_USER.APP_USER_ID);
    }

    @AfterAll
    static void disconnect() throws SQLException {
        connection.rollback();
        connection.close();
        dataSource.close();
    }

    @BeforeEach
    void createDocument() {
        LocalDateTime now = LocalDateTime.now();
        oasDocId = dsl.insertInto(OAS_DOC)
                .set(OAS_DOC.GUID, randomGuid()).set(OAS_DOC.OPEN_API_VERSION, "3.1.1")
                .set(OAS_DOC.TITLE, "t").set(OAS_DOC.VERSION, "1")
                .set(OAS_DOC.OWNER_USER_ID, USER).set(OAS_DOC.CREATED_BY, USER).set(OAS_DOC.LAST_UPDATED_BY, USER)
                .set(OAS_DOC.CREATION_TIMESTAMP, now).set(OAS_DOC.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_DOC.OAS_DOC_ID).fetchOne().value1();
    }

    @AfterEach
    void discardChanges() throws SQLException {
        connection.rollback();
    }

    private JooqOasDocCommandRepository repository() {
        ScoreUser requester = new ScoreUser(new UserId(USER.toBigInteger()), "tester", "Tester", "tester@example.com",
                true, List.of(ScoreRole.DEVELOPER));
        return new JooqOasDocCommandRepository(dsl, requester, new RepositoryFactory(dsl));
    }

    private ULong securityScheme(String name) {
        LocalDateTime now = LocalDateTime.now();
        return dsl.insertInto(OAS_SECURITY_SCHEME)
                .set(OAS_SECURITY_SCHEME.GUID, randomGuid()).set(OAS_SECURITY_SCHEME.OAS_DOC_ID, oasDocId)
                .set(OAS_SECURITY_SCHEME.SCHEME_NAME, name).set(OAS_SECURITY_SCHEME.TYPE, "oauth2")
                .set(OAS_SECURITY_SCHEME.CREATED_BY, USER).set(OAS_SECURITY_SCHEME.LAST_UPDATED_BY, USER)
                .set(OAS_SECURITY_SCHEME.CREATION_TIMESTAMP, now).set(OAS_SECURITY_SCHEME.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID).fetchOne().value1();
    }

    private ULong resource(String path) {
        LocalDateTime now = LocalDateTime.now();
        return dsl.insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.OAS_DOC_ID, oasDocId).set(OAS_RESOURCE.PATH, path)
                .set(OAS_RESOURCE.CREATED_BY, USER).set(OAS_RESOURCE.LAST_UPDATED_BY, USER)
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, now).set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID).fetchOne().value1();
    }

    private ULong operation(ULong resourceId, String verb, String operationId) {
        LocalDateTime now = LocalDateTime.now();
        return dsl.insertInto(OAS_OPERATION)
                .set(OAS_OPERATION.OAS_RESOURCE_ID, resourceId).set(OAS_OPERATION.VERB, verb)
                .set(OAS_OPERATION.OPERATION_ID, operationId)
                .set(OAS_OPERATION.CREATED_BY, USER).set(OAS_OPERATION.LAST_UPDATED_BY, USER)
                .set(OAS_OPERATION.CREATION_TIMESTAMP, now).set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_OPERATION.OAS_OPERATION_ID).fetchOne().value1();
    }

    private void parameter(String in, String name) {
        LocalDateTime now = LocalDateTime.now();
        dsl.insertInto(OAS_PARAMETER)
                .set(OAS_PARAMETER.GUID, randomGuid()).set(OAS_PARAMETER.NAME, name).set(OAS_PARAMETER.IN, in)
                .set(OAS_PARAMETER.SCHEMA_TYPE_REFERENCE, "{\"type\":\"string\"}")
                .set(OAS_PARAMETER.CREATED_BY, USER).set(OAS_PARAMETER.LAST_UPDATED_BY, USER)
                .set(OAS_PARAMETER.CREATION_TIMESTAMP, now).set(OAS_PARAMETER.LAST_UPDATE_TIMESTAMP, now)
                .execute();
    }

    // ---- Security Scheme name ----

    @Test
    void securitySchemeNames_differingInCase_areTwoNames() {
        securityScheme("Auth");
        securityScheme("auth");
        assertEquals(2, dsl.fetchCount(OAS_SECURITY_SCHEME, OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId)));
        assertEquals(1, dsl.fetchCount(OAS_SECURITY_SCHEME, OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId)
                .and(OAS_SECURITY_SCHEME.SCHEME_NAME.eq("Auth"))));
    }

    @Test
    void securitySchemeName_exactDuplicate_isRejected() {
        securityScheme("Auth");
        assertThrows(DataAccessException.class, () -> securityScheme("Auth"));
    }

    // ---- scope names (OAuth flow, document security, operation security) ----

    private ULong oauthFlow() {
        LocalDateTime now = LocalDateTime.now();
        return dsl.insertInto(OAS_OAUTH_FLOW)
                .set(OAS_OAUTH_FLOW.GUID, randomGuid()).set(OAS_OAUTH_FLOW.OAS_SECURITY_SCHEME_ID, securityScheme("OAuth2"))
                .set(OAS_OAUTH_FLOW.FLOW_TYPE, "implicit")
                .set(OAS_OAUTH_FLOW.CREATED_BY, USER).set(OAS_OAUTH_FLOW.LAST_UPDATED_BY, USER)
                .set(OAS_OAUTH_FLOW.CREATION_TIMESTAMP, now).set(OAS_OAUTH_FLOW.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_OAUTH_FLOW.OAS_OAUTH_FLOW_ID).fetchOne().value1();
    }

    private void oauthScope(ULong flowId, String name) {
        LocalDateTime now = LocalDateTime.now();
        dsl.insertInto(OAS_OAUTH_SCOPE)
                .set(OAS_OAUTH_SCOPE.GUID, randomGuid()).set(OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID, flowId)
                .set(OAS_OAUTH_SCOPE.SCOPE_NAME, name)
                .set(OAS_OAUTH_SCOPE.CREATED_BY, USER).set(OAS_OAUTH_SCOPE.LAST_UPDATED_BY, USER)
                .set(OAS_OAUTH_SCOPE.CREATION_TIMESTAMP, now).set(OAS_OAUTH_SCOPE.LAST_UPDATE_TIMESTAMP, now)
                .execute();
    }

    @Test
    void oauthScopeNames_differingInCase_areTwoNames() {
        ULong flowId = oauthFlow();
        oauthScope(flowId, "read");
        oauthScope(flowId, "Read");
        assertEquals(2, dsl.fetchCount(OAS_OAUTH_SCOPE, OAS_OAUTH_SCOPE.OAS_OAUTH_FLOW_ID.eq(flowId)));
        assertThrows(DataAccessException.class, () -> oauthScope(flowId, "read"));
    }

    private void documentSecurityScope(ULong documentSecurityId, String name) {
        LocalDateTime now = LocalDateTime.now();
        dsl.insertInto(OAS_DOC_SECURITY_SCOPE)
                .set(OAS_DOC_SECURITY_SCOPE.GUID, randomGuid())
                .set(OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID, documentSecurityId)
                .set(OAS_DOC_SECURITY_SCOPE.SCOPE_NAME, name)
                .set(OAS_DOC_SECURITY_SCOPE.CREATED_BY, USER).set(OAS_DOC_SECURITY_SCOPE.LAST_UPDATED_BY, USER)
                .set(OAS_DOC_SECURITY_SCOPE.CREATION_TIMESTAMP, now).set(OAS_DOC_SECURITY_SCOPE.LAST_UPDATE_TIMESTAMP, now)
                .execute();
    }

    private ULong documentSecurity() {
        LocalDateTime now = LocalDateTime.now();
        return dsl.insertInto(OAS_DOC_SECURITY)
                .set(OAS_DOC_SECURITY.GUID, randomGuid()).set(OAS_DOC_SECURITY.OAS_DOC_ID, oasDocId)
                .set(OAS_DOC_SECURITY.OAS_SECURITY_SCHEME_ID, securityScheme("DocOAuth2"))
                .set(OAS_DOC_SECURITY.CREATED_BY, USER).set(OAS_DOC_SECURITY.LAST_UPDATED_BY, USER)
                .set(OAS_DOC_SECURITY.CREATION_TIMESTAMP, now).set(OAS_DOC_SECURITY.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_DOC_SECURITY.OAS_DOC_SECURITY_ID).fetchOne().value1();
    }

    @Test
    void documentSecurityScopeNames_differingInCase_areTwoNames() {
        ULong documentSecurityId = documentSecurity();
        documentSecurityScope(documentSecurityId, "read");
        documentSecurityScope(documentSecurityId, "Read");
        assertEquals(2, dsl.fetchCount(OAS_DOC_SECURITY_SCOPE,
                OAS_DOC_SECURITY_SCOPE.OAS_DOC_SECURITY_ID.eq(documentSecurityId)));
        assertThrows(DataAccessException.class, () -> documentSecurityScope(documentSecurityId, "read"));
    }

    private void operationSecurityScope(ULong operationSecurityId, String name) {
        LocalDateTime now = LocalDateTime.now();
        dsl.insertInto(OAS_OPERATION_SECURITY_SCOPE)
                .set(OAS_OPERATION_SECURITY_SCOPE.GUID, randomGuid())
                .set(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID, operationSecurityId)
                .set(OAS_OPERATION_SECURITY_SCOPE.SCOPE_NAME, name)
                .set(OAS_OPERATION_SECURITY_SCOPE.CREATED_BY, USER).set(OAS_OPERATION_SECURITY_SCOPE.LAST_UPDATED_BY, USER)
                .set(OAS_OPERATION_SECURITY_SCOPE.CREATION_TIMESTAMP, now)
                .set(OAS_OPERATION_SECURITY_SCOPE.LAST_UPDATE_TIMESTAMP, now)
                .execute();
    }

    private ULong operationSecurity() {
        LocalDateTime now = LocalDateTime.now();
        ULong operationId = operation(resource("/orders-security"), "get", "getOrderSecurity");
        return dsl.insertInto(OAS_OPERATION_SECURITY)
                .set(OAS_OPERATION_SECURITY.GUID, randomGuid()).set(OAS_OPERATION_SECURITY.OAS_OPERATION_ID, operationId)
                .set(OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID, securityScheme("OperationOAuth2"))
                .set(OAS_OPERATION_SECURITY.CREATED_BY, USER).set(OAS_OPERATION_SECURITY.LAST_UPDATED_BY, USER)
                .set(OAS_OPERATION_SECURITY.CREATION_TIMESTAMP, now).set(OAS_OPERATION_SECURITY.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID).fetchOne().value1();
    }

    @Test
    void operationSecurityScopeNames_differingInCase_areTwoNames() {
        ULong operationSecurityId = operationSecurity();
        operationSecurityScope(operationSecurityId, "read");
        operationSecurityScope(operationSecurityId, "Read");
        assertEquals(2, dsl.fetchCount(OAS_OPERATION_SECURITY_SCOPE,
                OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID.eq(operationSecurityId)));
        assertThrows(DataAccessException.class, () -> operationSecurityScope(operationSecurityId, "read"));
    }

    // ---- Operation ID ----

    @Test
    void operationIds_differingInCase_areNotTheSameOperationId() {
        operation(resource("/users"), "get", "getUser");
        operation(resource("/users2"), "get", "getuser");
        assertEquals(1, dsl.fetchCount(OAS_OPERATION, OAS_OPERATION.OPERATION_ID.eq("getUser")));
        assertEquals(Set.of("getUser"),
                repository().findOperationIdsInOasDoc(new OasDocId(oasDocId.toBigInteger()), List.of("getUser"), null));
        assertEquals(Set.of("getuser"),
                repository().findOperationIdsInOasDoc(new OasDocId(oasDocId.toBigInteger()), List.of("getuser"), null));
        assertEquals(Set.of(),
                repository().findOperationIdsInOasDoc(new OasDocId(oasDocId.toBigInteger()), List.of("GETUSER"), null));
    }

    // ---- path ----

    @Test
    void paths_differingInCase_areTwoResources() {
        ULong upper = operation(resource("/Pets"), "get", "getPets1");
        ULong lower = operation(resource("/pets"), "get", "getPets2");
        OasDocId docId = new OasDocId(oasDocId.toBigInteger());
        assertEquals(Set.of(new OasOperationId(upper.toBigInteger())), repository().findOasOperationIdsAtEndpoint(docId, "/Pets", "get"));
        assertEquals(Set.of(new OasOperationId(lower.toBigInteger())), repository().findOasOperationIdsAtEndpoint(docId, "/pets", "get"));
        assertEquals(Set.of(), repository().findOasOperationIdsAtEndpoint(docId, "/PETS", "get"));
    }

    // ---- parameter name ----

    @Test
    void parameterNames_differingInCase_areTwoParameters() {
        // names that no row of a development database has, since the rows of that database stay in place
        parameter("path", "case1891id");
        parameter("path", "CASE1891ID");
        assertEquals(1, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.IN.eq("path").and(OAS_PARAMETER.NAME.eq("case1891id"))));
        assertEquals(1, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.IN.eq("path").and(OAS_PARAMETER.NAME.eq("CASE1891ID"))));
    }

    // ---- character sets of the names ----
    // The Security Scheme name and the scope names are ASCII (the Components Object, RFC 6749). The Operation ID, the path and the
    // name of a parameter are not limited by the specification, and the path is made from names that are not limited to ASCII, so
    // they keep utf8mb3: a letter of any script is stored, and is compared exactly. (Emoji, outside utf8mb3, are Issue #1892.)

    private static final String SMILE = "\uD83D\uDE00"; // U+1F600
    private static final String HANGUL_A = "\uC778\uC99D";  // two Hangul syllables
    private static final String HANGUL_B = "\uC778\uC99E";  // the second syllable differs

    @Test
    void securitySchemeNames_areAsciiOnly() {
        // the key of components.securitySchemes is ^[a-zA-Z0-9\\.\\-_]+$ (the Components Object), so the column is ASCII
        securityScheme("Api-Key_2.v1");
        assertEquals(1, dsl.fetchCount(OAS_SECURITY_SCHEME, OAS_SECURITY_SCHEME.OAS_DOC_ID.eq(oasDocId)
                .and(OAS_SECURITY_SCHEME.SCHEME_NAME.eq("Api-Key_2.v1"))));
        assertThrows(DataAccessException.class, () -> securityScheme("Auth" + SMILE));
        assertThrows(DataAccessException.class, () -> securityScheme(HANGUL_A));
    }

    @Test
    void scopeNames_areAsciiOnly() {
        // a scope is an OAuth 2.0 scope token (RFC 6749, 3.3): printable ASCII, so the columns are ASCII
        ULong flowId = oauthFlow();
        oauthScope(flowId, "https://example.com/auth/read:pets");
        assertThrows(DataAccessException.class, () -> oauthScope(flowId, "read" + SMILE));
        assertThrows(DataAccessException.class, () -> oauthScope(flowId, HANGUL_A));
        assertThrows(DataAccessException.class, () -> oauthScope(flowId, "caf\u00e9"));

        ULong documentSecurityId = documentSecurity();
        documentSecurityScope(documentSecurityId, "read:pets");
        assertThrows(DataAccessException.class, () -> documentSecurityScope(documentSecurityId, "read" + SMILE));

        ULong operationSecurityId = operationSecurity();
        operationSecurityScope(operationSecurityId, "read:pets");
        assertThrows(DataAccessException.class, () -> operationSecurityScope(operationSecurityId, "read" + SMILE));
    }

    @Test
    void operationIdsAndPaths_ofAnyScript_areStoredAndFoundExactly() {
        ULong first = operation(resource("/" + HANGUL_A), "get", "get" + HANGUL_A);
        ULong second = operation(resource("/" + HANGUL_B), "get", "get" + HANGUL_B);
        OasDocId docId = new OasDocId(oasDocId.toBigInteger());
        assertEquals(Set.of("get" + HANGUL_A),
                repository().findOperationIdsInOasDoc(docId, List.of("get" + HANGUL_A), null));
        assertEquals(Set.of("get" + HANGUL_B),
                repository().findOperationIdsInOasDoc(docId, List.of("get" + HANGUL_B), null));
        assertEquals(Set.of(new OasOperationId(first.toBigInteger())),
                repository().findOasOperationIdsAtEndpoint(docId, "/" + HANGUL_A, "get"));
        assertEquals(Set.of(new OasOperationId(second.toBigInteger())),
                repository().findOasOperationIdsAtEndpoint(docId, "/" + HANGUL_B, "get"));
    }

    @Test
    void parameterNames_ofAnyScript_areStoredAndAreTwoParameters() {
        parameter("query", "case1891" + HANGUL_A);
        parameter("query", "case1891" + HANGUL_B);
        assertEquals(1, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.IN.eq("query")
                .and(OAS_PARAMETER.NAME.eq("case1891" + HANGUL_A))));
        assertEquals(1, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.IN.eq("query")
                .and(OAS_PARAMETER.NAME.eq("case1891" + HANGUL_B))));
    }

    // ---- what stays case-insensitive ----

    @Test
    void httpHeaderNames_stayCaseInsensitive() {
        LocalDateTime now = LocalDateTime.now();
        dsl.insertInto(OAS_HTTP_HEADER)
                .set(OAS_HTTP_HEADER.GUID, randomGuid()).set(OAS_HTTP_HEADER.HEADER, "CASE1891HEADER")
                .set(OAS_HTTP_HEADER.SCHEMA_TYPE_REFERENCE, "{\"type\":\"string\"}")
                .set(OAS_HTTP_HEADER.OWNER_USER_ID, USER)
                .set(OAS_HTTP_HEADER.CREATED_BY, USER).set(OAS_HTTP_HEADER.LAST_UPDATED_BY, USER)
                .set(OAS_HTTP_HEADER.CREATION_TIMESTAMP, now).set(OAS_HTTP_HEADER.LAST_UPDATE_TIMESTAMP, now)
                .execute();
        assertTrue(dsl.fetchCount(OAS_HTTP_HEADER, OAS_HTTP_HEADER.HEADER.eq("Case1891Header")) >= 1);
    }

    @Test
    void collations_matchTheSpecification() {
        List<String> unicode = dsl.fetch("""
                        SELECT CONCAT(table_name, '.', column_name) FROM information_schema.columns
                        WHERE table_schema = DATABASE() AND collation_name = 'utf8mb3_bin'
                          AND CONCAT(table_name, '.', column_name) IN (
                            'oas_operation.operation_id', 'oas_parameter.name', 'oas_resource.path')""")
                .into(String.class);
        assertEquals(3, unicode.size(), "case-sensitive utf8mb3 columns: " + unicode);
        List<String> ascii = dsl.fetch("""
                        SELECT CONCAT(table_name, '.', column_name) FROM information_schema.columns
                        WHERE table_schema = DATABASE() AND collation_name = 'ascii_bin'
                          AND CONCAT(table_name, '.', column_name) IN (
                            'oas_security_scheme.scheme_name', 'oas_oauth_scope.scope_name',
                            'oas_doc_security_scope.scope_name', 'oas_operation_security_scope.scope_name')""")
                .into(String.class);
        assertEquals(4, ascii.size(), "case-sensitive ASCII columns: " + ascii);
        assertEquals("utf8mb3_general_ci", dsl.fetchValue("""
                SELECT collation_name FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'oas_http_header' AND column_name = 'header'"""));
    }
}
