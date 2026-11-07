package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.DeleteBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.DeleteOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader.Direction;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourceId;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

/**
 * Issue #1791: the HTTP headers of a document are saved, read back, replaced and deleted. Against a development database
 * that has the migration of this issue (it needs a user):
 * <pre>
 *   mvn test -Dtest=OasDocHttpHeaderStoreTest -DargLine=-Dscore.oasHeaders=true
 * </pre>
 * Everything is done inside a transaction that is rolled back.
 */
@EnabledIfSystemProperty(named = "score.oasHeaders", matches = "true")
class OasDocHttpHeaderStoreTest {

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

    private static OasDocHttpHeader header(String name, Direction direction) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setName(name);
        header.setDescription("d");
        header.setSchema(OasSchema.fromJson("{\"type\":\"string\"}"));
        header.setDirection(direction);
        header.setRequired(direction == Direction.RESPONSE);
        return header;
    }

    private void save(List<OasDocHttpHeader> headers) {
        OasDocHttpHeaderStore.save(dsl, oasDocId, headers, USER, LocalDateTime.now());
    }

    private int bindings() {
        return dsl.fetchCount(OAS_DOC_HTTP_HEADER, OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId));
    }

    @Test
    void eachDirectionIsSavedAndReadBackAsItsOwnEntryWithItsOwnRequired() {
        save(List.of(header("traceparent", Direction.REQUEST), header("traceparent", Direction.RESPONSE)));

        assertEquals(2, bindings());
        List<OasDocHttpHeader> loaded = OasDocHttpHeaderStore.load(dsl, oasDocId);
        assertEquals(2, loaded.size());
        OasDocHttpHeader request = loaded.stream().filter(h -> h.getDirection() == Direction.REQUEST).findFirst().orElseThrow();
        OasDocHttpHeader response = loaded.stream().filter(h -> h.getDirection() == Direction.RESPONSE).findFirst().orElseThrow();
        assertEquals("traceparent", request.getName());
        assertFalse(request.isRequired());
        assertTrue(response.isRequired());
        assertEquals(OasSchema.fromJson("{\"type\":\"string\"}"), request.getSchema());
        assertNull(request.getOasOperationId());
    }

    @Test
    void aRequestParameterIsSavedAndReadBackWithItsLocation() {
        List<OasDocHttpHeader> sent = new java.util.ArrayList<>();
        for (String in : List.of("query", "header", "path", "cookie")) {
            OasDocHttpHeader parameter = header("p-" + in, Direction.REQUEST);
            parameter.setIn(in);
            sent.add(parameter);
        }
        sent.add(header("traceparent", Direction.RESPONSE));
        save(sent);

        java.util.Map<String, String> in = new java.util.HashMap<>();
        for (OasDocHttpHeader loaded : OasDocHttpHeaderStore.load(dsl, oasDocId)) {
            in.put(loaded.getName(), loaded.getIn());
        }
        assertEquals("query", in.get("p-query"));
        assertEquals("header", in.get("p-header"));
        assertEquals("path", in.get("p-path"));
        assertEquals("cookie", in.get("p-cookie"));
        assertNull(in.get("traceparent"), "a response header has no location");
        assertEquals("cookie", dsl.select(OAS_PARAMETER.IN).from(OAS_PARAMETER)
                .where(OAS_PARAMETER.NAME.eq("p-cookie")).orderBy(OAS_PARAMETER.OAS_PARAMETER_ID.desc()).limit(1)
                .fetchOne(OAS_PARAMETER.IN));
    }

    @Test
    void theSerializationOfAParameterAndOfAResponseHeaderIsSavedAndReadBack() {
        OasDocHttpHeader filter = header("filter", Direction.REQUEST);
        filter.setIn("query");
        filter.setStyle("deepObject");
        filter.setExplode(true);
        filter.setAllowReserved(true);
        OasDocHttpHeader cookie = header("session", Direction.REQUEST);
        cookie.setIn("cookie");
        cookie.setContentMediaType("text/plain");
        cookie.setExample("abc");
        OasDocHttpHeader plain = header("limit", Direction.REQUEST);
        plain.setIn("query");
        OasDocHttpHeader total = header("x-total", Direction.RESPONSE);
        total.setContentMediaType("text/plain");
        OasDocHttpHeader list = header("x-list", Direction.RESPONSE);
        list.setStyle("simple");
        list.setExplode(true);
        save(List.of(filter, cookie, plain, total, list));

        java.util.Map<String, OasDocHttpHeader> byName = new java.util.HashMap<>();
        OasDocHttpHeaderStore.load(dsl, oasDocId).forEach(h -> byName.put(h.getName(), h));

        assertEquals("deepObject", byName.get("filter").getStyle());
        assertEquals(Boolean.TRUE, byName.get("filter").getExplode());
        assertTrue(byName.get("filter").isAllowReserved());
        assertNull(byName.get("filter").getContentMediaType());
        assertEquals("text/plain", byName.get("session").getContentMediaType());
        assertEquals("abc", byName.get("session").getExample());
        assertNull(byName.get("session").getStyle());
        assertNull(byName.get("limit").getStyle(), "NULL, the default of the specification, stays NULL");
        assertNull(byName.get("limit").getExplode());
        assertFalse(byName.get("limit").isAllowReserved());
        assertEquals("text/plain", byName.get("x-total").getContentMediaType());
        assertEquals("simple", byName.get("x-list").getStyle());
        assertEquals(Boolean.TRUE, byName.get("x-list").getExplode());
        assertNull(byName.get("x-list").getContentMediaType());
    }

    @Test
    void onlyAParameterInAHeaderIsLinkedToAHeaderDefinition() {
        int definitions = dsl.fetchCount(OAS_HTTP_HEADER);
        OasDocHttpHeader query = header("p-query-only", Direction.REQUEST);
        query.setIn("query");
        save(List.of(query));
        assertEquals(definitions, dsl.fetchCount(OAS_HTTP_HEADER));
        assertNull(dsl.select(OAS_PARAMETER.OAS_HTTP_HEADER_ID).from(OAS_PARAMETER)
                .where(OAS_PARAMETER.NAME.eq("p-query-only")).fetchOne(OAS_PARAMETER.OAS_HTTP_HEADER_ID));

        save(List.of(header("p-header-only", Direction.REQUEST)));
        assertEquals(definitions + 1, dsl.fetchCount(OAS_HTTP_HEADER));
        assertEquals("header", OasDocHttpHeaderStore.load(dsl, oasDocId).get(0).getIn());
    }

    @Test
    void theExampleIsKeptOnTheParameterAndOnTheHeaderAndTellsDefinitionsApart() {
        OasDocHttpHeader requestWithExample = header("traceparent", Direction.REQUEST);
        requestWithExample.setExample("abc");
        OasDocHttpHeader responseWithExample = header("traceparent", Direction.RESPONSE);
        responseWithExample.setExample("abc");
        save(List.of(requestWithExample, responseWithExample));

        assertEquals("abc", OasDocHttpHeaderStore.load(dsl, oasDocId).get(0).getExample());
        // the request parameter (optional) and the response header (required) are two definitions
        assertEquals(2, dsl.fetchCount(OAS_HTTP_HEADER, OAS_HTTP_HEADER.HEADER.eq("traceparent")
                .and(OAS_HTTP_HEADER.EXAMPLE.eq("\"abc\""))));
        assertEquals(1, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.NAME.eq("traceparent")
                .and(OAS_PARAMETER.EXAMPLE.eq("\"abc\""))));

        // saving again replaces the definitions: the ones with the example are gone with their entries
        int definitions = dsl.fetchCount(OAS_HTTP_HEADER);
        save(List.of(header("traceparent", Direction.REQUEST), header("traceparent", Direction.RESPONSE)));
        assertEquals(definitions, dsl.fetchCount(OAS_HTTP_HEADER));
        assertEquals(0, dsl.fetchCount(OAS_HTTP_HEADER, OAS_HTTP_HEADER.HEADER.eq("traceparent")
                .and(OAS_HTTP_HEADER.EXAMPLE.eq("\"abc\""))));
        assertNull(OasDocHttpHeaderStore.load(dsl, oasDocId).get(0).getExample());
    }

    @Test
    void eachUseHasItsOwnDefinitionAndSavingAgainReplacesIt() {
        // two operations with the same header have a definition each, so that deleting one entry deletes only its own
        Endpoint first = endpoint("/a", "GET");
        Endpoint second = endpoint("/b", "GET");
        OasDocHttpHeader one = header("traceparent", Direction.RESPONSE);
        one.setOasOperationId(first.operationId().toBigInteger());
        OasDocHttpHeader two = header("traceparent", Direction.RESPONSE);
        two.setOasOperationId(second.operationId().toBigInteger());
        save(List.of(one, two));
        List<ULong> definitionIds = dsl.select(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID).from(OAS_DOC_HTTP_HEADER)
                .where(OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId)).fetch(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID);
        assertEquals(2, definitionIds.stream().distinct().count());

        OasDocHttpHeaderStore.delete(dsl, OAS_DOC_HTTP_HEADER.OAS_OPERATION_ID.eq(first.operationId()));
        assertEquals(0, dsl.fetchCount(OAS_HTTP_HEADER, OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID.eq(definitionIds.get(0))));
        assertEquals(1, dsl.fetchCount(OAS_HTTP_HEADER, OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID.eq(definitionIds.get(1))));

        int definitions = dsl.fetchCount(OAS_HTTP_HEADER);
        save(List.of(two));
        assertEquals(definitions, dsl.fetchCount(OAS_HTTP_HEADER));
        assertEquals(1, dsl.fetchCount(OAS_DOC_HTTP_HEADER, OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId)));
    }

    @Test
    void aDefinitionThatSomethingElseRefersToStaysWhenItsEntryIsDeleted() {
        save(List.of(header("x-kept", Direction.RESPONSE)));
        ULong definitionId = dsl.select(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID).from(OAS_DOC_HTTP_HEADER)
                .where(OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId)).fetchOne(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID);
        // another entry (of another document, as it was when definitions were shared) uses it too
        OasDocHttpHeaderStore.insert(dsl, oasDocId, header("x-other", Direction.REQUEST), USER, LocalDateTime.now());
        dsl.update(OAS_PARAMETER).set(OAS_PARAMETER.OAS_HTTP_HEADER_ID, definitionId)
                .where(OAS_PARAMETER.NAME.eq("x-other")).execute();

        OasDocHttpHeaderStore.delete(dsl, OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID.eq(definitionId));
        assertEquals(1, dsl.fetchCount(OAS_HTTP_HEADER, OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID.eq(definitionId)));
    }

    @Test
    void nullKeepsTheHeadersAndAnEmptyListRemovesThem() {
        save(List.of(header("traceparent", Direction.REQUEST)));
        save(null);
        assertEquals(1, bindings());

        save(List.of());
        assertEquals(0, bindings());
        assertTrue(OasDocHttpHeaderStore.load(dsl, oasDocId).isEmpty());
    }

    @Test
    void deleteRemovesTheBindingsAndTheirParameters() {
        save(List.of(header("traceparent", Direction.REQUEST), header("x-request-id", Direction.REQUEST)));
        List<ULong> parameterIds = dsl.select(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID).from(OAS_DOC_HTTP_HEADER)
                .where(OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId), OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID.isNotNull())
                .fetch(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID);
        assertEquals(2, parameterIds.size());
        List<ULong> definitionIds = dsl.select(OAS_PARAMETER.OAS_HTTP_HEADER_ID).from(OAS_PARAMETER)
                .where(OAS_PARAMETER.OAS_PARAMETER_ID.in(parameterIds)).fetch(OAS_PARAMETER.OAS_HTTP_HEADER_ID);
        assertEquals(2, definitionIds.size());

        OasDocHttpHeaderStore.delete(dsl, OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId));

        assertEquals(0, bindings());
        assertEquals(0, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.OAS_PARAMETER_ID.in(parameterIds)));
        // and their header definitions
        assertEquals(0, dsl.fetchCount(OAS_HTTP_HEADER, OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID.in(definitionIds)));
    }

    // ---------------------------------------------------------------- operations

    private ScoreUser requester() {
        return new ScoreUser(new UserId(USER.toBigInteger()), "tester", "Tester", "tester@example.com", true,
                List.of(ScoreRole.DEVELOPER));
    }

    private record Endpoint(ULong resourceId, ULong operationId, ULong requestId) {
    }

    /** A resource, an operation and a request body of the document, as the Add BIE / Add Operation of the web app make them. */
    private Endpoint endpoint(String path, String verb) {
        LocalDateTime now = LocalDateTime.now();
        ULong resourceId = dsl.insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.OAS_DOC_ID, oasDocId).set(OAS_RESOURCE.PATH, path)
                .set(OAS_RESOURCE.CREATED_BY, USER).set(OAS_RESOURCE.LAST_UPDATED_BY, USER)
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, now).set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID).fetchOne().value1();
        ULong operationId = dsl.insertInto(OAS_OPERATION)
                .set(OAS_OPERATION.OAS_RESOURCE_ID, resourceId).set(OAS_OPERATION.VERB, verb)
                .set(OAS_OPERATION.OPERATION_ID, verb.toLowerCase() + "Order")
                .set(OAS_OPERATION.CREATED_BY, USER).set(OAS_OPERATION.LAST_UPDATED_BY, USER)
                .set(OAS_OPERATION.CREATION_TIMESTAMP, now).set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_OPERATION.OAS_OPERATION_ID).fetchOne().value1();
        ULong messageBodyId = dsl.insertInto(OAS_MESSAGE_BODY)
                .set(OAS_MESSAGE_BODY.CREATED_BY, USER).set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, USER)
                .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, now).set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID).fetchOne().value1();
        ULong requestId = dsl.insertInto(OAS_REQUEST)
                .set(OAS_REQUEST.OAS_OPERATION_ID, operationId).set(OAS_REQUEST.OAS_MESSAGE_BODY_ID, messageBodyId)
                .set(OAS_REQUEST.CREATED_BY, USER).set(OAS_REQUEST.LAST_UPDATED_BY, USER)
                .set(OAS_REQUEST.CREATION_TIMESTAMP, now).set(OAS_REQUEST.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_REQUEST.OAS_REQUEST_ID).fetchOne().value1();
        return new Endpoint(resourceId, operationId, requestId);
    }

    private static OasDocHttpHeader headerOf(ULong operationId, String name) {
        OasDocHttpHeader header = header(name, Direction.REQUEST);
        header.setOasOperationId(operationId.toBigInteger());
        return header;
    }

    @Test
    void aHeaderOfOneOperationIsSavedReadBackAndDeletedAloneWithoutTouchingTheOthers() {
        Endpoint first = endpoint("/orders", "PATCH");
        Endpoint second = endpoint("/orders/{id}", "DELETE");
        save(List.of(header("traceparent", Direction.REQUEST), header("traceparent", Direction.RESPONSE), headerOf(first.operationId(), "x-first"),
                headerOf(second.operationId(), "x-second")));

        List<OasDocHttpHeader> loaded = OasDocHttpHeaderStore.load(dsl, oasDocId);
        assertEquals(4, loaded.size());
        assertEquals(first.operationId().toBigInteger(), loaded.stream()
                .filter(h -> h.getName().equals("x-first")).findFirst().orElseThrow().getOasOperationId());
        assertNull(loaded.stream().filter(h -> h.getName().equals("traceparent")).findFirst().orElseThrow().getOasOperationId());

        OasDocHttpHeaderStore.delete(dsl, OAS_DOC_HTTP_HEADER.OAS_OPERATION_ID.eq(first.operationId()));

        List<String> left = OasDocHttpHeaderStore.load(dsl, oasDocId).stream()
                .map(OasDocHttpHeader::getName).sorted().toList();
        assertEquals(List.of("traceparent", "traceparent", "x-second"), left);
        assertEquals(0, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.NAME.eq("x-first")),
                "the parameter of the deleted binding is gone");
    }

    @Test
    void deletingTheDocumentDeletesItsHeadersBeforeItsOperations() {
        Endpoint endpoint = endpoint("/orders", "PATCH");
        OasDocHttpHeader query = header("sinceLastDateTime", Direction.REQUEST);
        query.setIn("query");
        OasDocHttpHeader path = headerOf(endpoint.operationId(), "id");
        path.setIn("path");
        path.setRequired(true);
        save(List.of(header("traceparent", Direction.REQUEST), header("traceparent", Direction.RESPONSE), headerOf(endpoint.operationId(), "x-own"),
                query, path));
        assertEquals(5, bindings());
        List<ULong> parameterIds = dsl.select(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID).from(OAS_DOC_HTTP_HEADER)
                .where(OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId), OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID.isNotNull())
                .fetch(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID);
        assertEquals(4, parameterIds.size());

        DeleteOasDocRequest request = new DeleteOasDocRequest(requester());
        request.setOasDocIdList(List.of(new OasDocId(oasDocId.toBigInteger())));
        // an operation binding refers to the operation: deleting the operation first would break the foreign key
        new JooqOasDocCommandRepository(dsl, requester(), null).deleteOasDoc(request);

        assertEquals(0, dsl.fetchCount(OAS_DOC, OAS_DOC.OAS_DOC_ID.eq(oasDocId)));
        assertEquals(0, dsl.fetchCount(OAS_OPERATION, OAS_OPERATION.OAS_OPERATION_ID.eq(endpoint.operationId())));
        assertEquals(0, bindings());
        // the parameters of the bindings (header, query and path alike) are gone with them
        assertEquals(0, dsl.fetchCount(OAS_PARAMETER, OAS_PARAMETER.OAS_PARAMETER_ID.in(parameterIds)));
    }

    @Test
    void removingTheLastBodyOfAnOperationDeletesItsHeadersAndNotTheOnesOfTheDocument() {
        Endpoint endpoint = endpoint("/orders", "PATCH");
        save(List.of(header("traceparent", Direction.REQUEST), header("traceparent", Direction.RESPONSE), headerOf(endpoint.operationId(), "x-own")));

        BieForOasDoc body = new BieForOasDoc();
        body.setMessageBody("Request");
        body.setVerb("PATCH");
        body.setResourceName("/orders");
        body.setOasOperationId(new OasOperationId(endpoint.operationId().toBigInteger()));
        body.setOasResourceId(new OasResourceId(endpoint.resourceId().toBigInteger()));
        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(requester());
        request.setOasDocId(new OasDocId(oasDocId.toBigInteger()));
        request.setBieForOasDocList(List.of(body));
        new JooqBieForOasDocCommandRepository(dsl, requester(), null).deleteBieForOasDoc(request);

        assertEquals(0, dsl.fetchCount(OAS_OPERATION, OAS_OPERATION.OAS_OPERATION_ID.eq(endpoint.operationId())));
        // the header of the whole document stays (a request parameter and a response header)
        assertEquals(2, bindings());
        assertEquals(List.of("traceparent", "traceparent"), OasDocHttpHeaderStore.load(dsl, oasDocId).stream().map(OasDocHttpHeader::getName).toList());
    }
}
