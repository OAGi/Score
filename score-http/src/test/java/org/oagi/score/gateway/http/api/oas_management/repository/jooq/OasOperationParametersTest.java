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
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationParameterState;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourceId;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

/**
 * Issue #1791: the request parameters that the system registers for an operation follow the operation: {@code
 * sinceLastDateTime} for a GET that returns an array, a path parameter for each {@code {name}} of the path. Against a
 * development database that has the migration of this issue, in a transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=OasOperationParametersTest -DargLine=-Dscore.oasHeaders=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.oasHeaders", matches = "true")
class OasOperationParametersTest {

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
        createDocument("1");
    }

    private void createDocument(String version) {
        LocalDateTime now = LocalDateTime.now();
        oasDocId = dsl.insertInto(OAS_DOC)
                .set(OAS_DOC.GUID, randomGuid()).set(OAS_DOC.OPEN_API_VERSION, "3.1.1")
                .set(OAS_DOC.TITLE, "t").set(OAS_DOC.VERSION, version)
                .set(OAS_DOC.OWNER_USER_ID, USER).set(OAS_DOC.CREATED_BY, USER).set(OAS_DOC.LAST_UPDATED_BY, USER)
                .set(OAS_DOC.CREATION_TIMESTAMP, now).set(OAS_DOC.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_DOC.OAS_DOC_ID).fetchOne().value1();
    }

    @AfterEach
    void discardChanges() throws SQLException {
        connection.rollback();
    }

    private ScoreUser requester() {
        return new ScoreUser(new UserId(USER.toBigInteger()), "tester", "Tester", "tester@example.com", true,
                List.of(ScoreRole.DEVELOPER));
    }

    private record Endpoint(ULong resourceId, ULong operationId, ULong responseId) {
    }

    /** A resource, an operation and a response body, as Add BIE makes them. */
    private Endpoint endpoint(String path, String verb, boolean array) {
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
        ULong responseId = dsl.insertInto(OAS_RESPONSE)
                .set(OAS_RESPONSE.OAS_OPERATION_ID, operationId).set(OAS_RESPONSE.OAS_MESSAGE_BODY_ID, messageBodyId)
                .set(OAS_RESPONSE.MAKE_ARRAY_INDICATOR, (byte) (array ? 1 : 0))
                .set(OAS_RESPONSE.CREATED_BY, USER).set(OAS_RESPONSE.LAST_UPDATED_BY, USER)
                .set(OAS_RESPONSE.CREATION_TIMESTAMP, now).set(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_RESPONSE.OAS_RESPONSE_ID).fetchOne().value1();
        return new Endpoint(resourceId, operationId, responseId);
    }

    private void reconcile(ULong operationId, OasOperationParameterState before) {
        OasOperationParameters.reconcile(dsl, operationId, before, USER, LocalDateTime.now());
    }

    private void setArray(Endpoint endpoint, boolean array) {
        dsl.update(OAS_RESPONSE).set(OAS_RESPONSE.MAKE_ARRAY_INDICATOR, (byte) (array ? 1 : 0))
                .where(OAS_RESPONSE.OAS_RESPONSE_ID.eq(endpoint.responseId())).execute();
    }

    private void setPath(Endpoint endpoint, String path) {
        dsl.update(OAS_RESOURCE).set(OAS_RESOURCE.PATH, path).where(OAS_RESOURCE.OAS_RESOURCE_ID.eq(endpoint.resourceId())).execute();
    }

    /** "in name" of what the document has for the operation (or the whole document), in the order saved. */
    private List<String> entries() {
        return OasDocHttpHeaderStore.load(dsl, oasDocId).stream()
                .map(h -> (h.getOasOperationId() == null ? "doc " : "op ") + h.getDirection() + " " + h.getIn() + " " + h.getName())
                .collect(Collectors.toList());
    }

    @Test
    void aNewGetThatReturnsAnArrayGetsSinceLastDateTimeAndItsPathParameters() {
        Endpoint endpoint = endpoint("/facility/{facilityId}/orders/{id}", "GET", true);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);

        assertEquals(List.of("op REQUEST query sinceLastDateTime", "op REQUEST path facilityId", "op REQUEST path id"), entries());
        OasDocHttpHeader since = OasDocHttpHeaderStore.load(dsl, oasDocId).get(0);
        assertFalse(since.isRequired());
        assertEquals("{\"type\":\"string\",\"format\":\"date-time\"}", since.getSchema().toJson().replace(" ", ""));
        assertTrue(OasDocHttpHeaderStore.load(dsl, oasDocId).get(1).isRequired());
    }

    @Test
    void aNewPostGetsOnlyItsPathParametersAndNoSinceLastDateTime() {
        Endpoint endpoint = endpoint("/orders/{id}", "POST", true);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        assertEquals(List.of("op REQUEST path id"), entries());
    }

    @Test
    void aPathWithoutNamesAndNoArrayGetsNothing() {
        Endpoint endpoint = endpoint("/orders", "GET", false);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        assertEquals(List.of(), entries());
    }

    @Test
    void sinceLastDateTimeComesWhenAGetBecomesAnArrayAndGoesWhenItStopsBeingOne() {
        Endpoint endpoint = endpoint("/orders", "GET", false);
        OasOperationParameterState single = OasOperationParameters.capture(dsl, endpoint.operationId());

        setArray(endpoint, true);
        reconcile(endpoint.operationId(), single);
        assertEquals(List.of("op REQUEST query sinceLastDateTime"), entries());

        OasOperationParameterState array = OasOperationParameters.capture(dsl, endpoint.operationId());
        setArray(endpoint, false);
        reconcile(endpoint.operationId(), array);
        assertEquals(List.of(), entries());
    }

    @Test
    void sinceLastDateTimeGoesWhenTheVerbIsNoLongerGetAndComesBackWhenItIsAgain() {
        Endpoint endpoint = endpoint("/orders", "GET", true);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        assertEquals(1, entries().size());

        OasOperationParameterState get = OasOperationParameters.capture(dsl, endpoint.operationId());
        dsl.update(OAS_OPERATION).set(OAS_OPERATION.VERB, "POST").where(OAS_OPERATION.OAS_OPERATION_ID.eq(endpoint.operationId())).execute();
        reconcile(endpoint.operationId(), get);
        assertEquals(List.of(), entries());

        OasOperationParameterState post = OasOperationParameters.capture(dsl, endpoint.operationId());
        dsl.update(OAS_OPERATION).set(OAS_OPERATION.VERB, "GET").where(OAS_OPERATION.OAS_OPERATION_ID.eq(endpoint.operationId())).execute();
        reconcile(endpoint.operationId(), post);
        assertEquals(List.of("op REQUEST query sinceLastDateTime"), entries());
    }

    @Test
    void anEntryTheUserDeletedDoesNotComeBackUntilTheOperationChangesIntoThatCaseAgain() {
        Endpoint endpoint = endpoint("/orders/{id}", "GET", true);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        OasDocHttpHeaderStore.save(dsl, oasDocId, List.of(), USER, LocalDateTime.now()); // the user deleted them all

        // a change that is none of the two cases
        OasOperationParameterState state = OasOperationParameters.capture(dsl, endpoint.operationId());
        reconcile(endpoint.operationId(), state);
        assertEquals(List.of(), entries());
    }

    @Test
    void anEntryOfTheWholeDocumentWithTheSameNameIsNotRegisteredAgain() {
        OasDocHttpHeader own = new OasDocHttpHeader();
        own.setDirection(OasDocHttpHeader.Direction.REQUEST);
        own.setIn("query");
        own.setName("sinceLastDateTime");
        own.setSchema(OasSchema.fromJson("{\"type\":\"string\"}"));
        OasDocHttpHeader id = new OasDocHttpHeader();
        id.setDirection(OasDocHttpHeader.Direction.REQUEST);
        id.setIn("path");
        id.setName("id");
        id.setRequired(true);
        id.setSchema(OasSchema.fromJson("{\"type\":\"integer\"}"));
        OasDocHttpHeaderStore.save(dsl, oasDocId, List.of(own, id), USER, LocalDateTime.now());

        Endpoint endpoint = endpoint("/orders/{id}", "GET", true);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        assertEquals(List.of("doc REQUEST query sinceLastDateTime", "doc REQUEST path id"), entries());
    }

    @Test
    void renamingAPathNameRenamesItsParameterAndKeepsWhatTheUserMadeOfIt() {
        Endpoint endpoint = endpoint("/orders/{id}", "DELETE", false);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        dsl.update(OAS_PARAMETER).set(OAS_PARAMETER.DESCRIPTION, "mine").set(OAS_PARAMETER.SCHEMA_TYPE_REFERENCE, "{\"type\":\"integer\"}")
                .where(OAS_PARAMETER.NAME.eq("id"), OAS_PARAMETER.IN.eq("path")).execute();

        OasOperationParameterState before = OasOperationParameters.capture(dsl, endpoint.operationId());
        setPath(endpoint, "/orders/{orderId}");
        reconcile(endpoint.operationId(), before);

        assertEquals(List.of("op REQUEST path orderId"), entries());
        OasDocHttpHeader renamed = OasDocHttpHeaderStore.load(dsl, oasDocId).get(0);
        assertEquals("mine", renamed.getDescription());
        assertEquals("integer", renamed.getSchema().toMap().get("type"));
    }

    @Test
    void anAddedNameGetsAParameterAndARemovedNameLosesItsOwn() {
        Endpoint endpoint = endpoint("/orders/{id}", "DELETE", false);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);

        OasOperationParameterState before = OasOperationParameters.capture(dsl, endpoint.operationId());
        setPath(endpoint, "/facility/{facilityId}/orders/{id}");
        reconcile(endpoint.operationId(), before);
        assertEquals(List.of("op REQUEST path id", "op REQUEST path facilityId"), entries());

        before = OasOperationParameters.capture(dsl, endpoint.operationId());
        setPath(endpoint, "/orders");
        reconcile(endpoint.operationId(), before);
        assertEquals(List.of(), entries());
    }

    @Test
    void severalNamesChangedAtOnceAreRenamedInOrder() {
        Endpoint endpoint = endpoint("/a/{x}/b/{y}", "GET", false);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        OasOperationParameterState before = OasOperationParameters.capture(dsl, endpoint.operationId());
        setPath(endpoint, "/a/{p}/b/{q}/c/{r}");
        reconcile(endpoint.operationId(), before);
        assertEquals(List.of("op REQUEST path p", "op REQUEST path q", "op REQUEST path r"), entries());
    }

    @Test
    void theVersionOfThePathIsNotAPathParameterWhenTheDocumentHasAVersion() {
        Endpoint endpoint = endpoint("/{version}/orders/{id}", "GET", false);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        assertEquals(List.of("op REQUEST path id"), entries());
    }

    @Test
    void whenTheDocumentLosesItsVersionTheVersionOfThePathBecomesAParameter() {
        Endpoint endpoint = endpoint("/{version}/orders", "GET", false);
        OasOperationParameterState withVersion = OasOperationParameters.capture(dsl, endpoint.operationId(), "GET", "/{version}/orders", "1");
        OasOperationParameterState without = OasOperationParameters.capture(dsl, endpoint.operationId(), "GET", "/{version}/orders", null);
        OasOperationParameters.apply(dsl, oasDocId, endpoint.operationId(), withVersion, without, USER, LocalDateTime.now());
        assertEquals(List.of("op REQUEST path version"), entries());

        OasOperationParameters.apply(dsl, oasDocId, endpoint.operationId(), without, withVersion, USER, LocalDateTime.now());
        assertEquals(List.of(), entries());
    }

    @Test
    void theEndpointDetailsUpdateOfAPathVerbOrArrayChangeRegistersThemInTheSameStep() {
        Endpoint endpoint = endpoint("/orders/{id}", "GET", false);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        assertEquals(List.of("op REQUEST path id"), entries());

        BieForOasDoc row = new BieForOasDoc();
        row.setMessageBody("Response");
        row.setVerb("GET");
        row.setResourceName("/orders/{orderId}/lines");
        row.setOperationId("getOrder");
        row.setArrayIndicator(true);
        row.setOasOperationId(new OasOperationId(endpoint.operationId().toBigInteger()));
        row.setOasResourceId(new OasResourceId(endpoint.resourceId().toBigInteger()));
        UpdateBieForOasDocRequest request = new UpdateBieForOasDocRequest(requester());
        request.setOasDocId(new OasDocId(oasDocId.toBigInteger()));
        request.setBieForOasDocList(List.of(row));
        new JooqBieForOasDocCommandRepository(dsl, requester(), null).updateBieForOasDoc(request);

        assertEquals(List.of("op REQUEST path orderId", "op REQUEST query sinceLastDateTime"), entries());
    }

    @Test
    void removingTheArrayBodyOfAGetThatKeepsAnotherBodyTakesSinceLastDateTimeAway() {
        Endpoint endpoint = endpoint("/orders", "GET", true);
        reconcile(endpoint.operationId(), OasOperationParameterState.NONE);
        // a request body too, so that the operation stays
        LocalDateTime now = LocalDateTime.now();
        ULong messageBodyId = dsl.insertInto(OAS_MESSAGE_BODY)
                .set(OAS_MESSAGE_BODY.CREATED_BY, USER).set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, USER)
                .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, now).set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID).fetchOne().value1();
        dsl.insertInto(OAS_REQUEST)
                .set(OAS_REQUEST.OAS_OPERATION_ID, endpoint.operationId()).set(OAS_REQUEST.OAS_MESSAGE_BODY_ID, messageBodyId)
                .set(OAS_REQUEST.CREATED_BY, USER).set(OAS_REQUEST.LAST_UPDATED_BY, USER)
                .set(OAS_REQUEST.CREATION_TIMESTAMP, now).set(OAS_REQUEST.LAST_UPDATE_TIMESTAMP, now).execute();
        assertEquals(List.of("op REQUEST query sinceLastDateTime"), entries());

        BieForOasDoc body = new BieForOasDoc();
        body.setMessageBody("Response");
        body.setVerb("GET");
        body.setResourceName("/orders");
        body.setOasOperationId(new OasOperationId(endpoint.operationId().toBigInteger()));
        body.setOasResourceId(new OasResourceId(endpoint.resourceId().toBigInteger()));
        DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(requester());
        request.setOasDocId(new OasDocId(oasDocId.toBigInteger()));
        request.setBieForOasDocList(List.of(body));
        new JooqBieForOasDocCommandRepository(dsl, requester(), null).deleteBieForOasDoc(request);

        assertEquals(1, dsl.fetchCount(OAS_OPERATION, OAS_OPERATION.OAS_OPERATION_ID.eq(endpoint.operationId())));
        assertEquals(List.of(), entries());
    }
}
