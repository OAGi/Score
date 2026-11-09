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
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationParameterState;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourceId;
import org.oagi.score.gateway.http.common.TestDataSource;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

/**
 * A resource of a document is the Path Item of one path (OpenAPI 3.0.4, 4.7.8: a path is the unique key of the Paths
 * Object), shared by the operations (verbs) of that path. Editing the Resource Name of one row is therefore a MOVE of its
 * operation to the resource of the new path, and must not rename the resource that the other operations of the old path
 * still use. Before, it renamed the shared resource, so editing GET {@code /orders} also moved POST {@code /orders}.
 * Against a development database, in a transaction that is rolled back:
 * <pre>
 *   mvn test -Dtest=BieForOasDocMovePathTest -DargLine=-Dscore.oasMovePath=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "score.oasMovePath", matches = "true")
class BieForOasDocMovePathTest {

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

    private ScoreUser requester() {
        return new ScoreUser(new UserId(USER.toBigInteger()), "tester", "Tester", "tester@example.com", true,
                List.of(ScoreRole.DEVELOPER));
    }

    private record Endpoint(ULong resourceId, ULong operationId, String verb, String path) {
    }

    private ULong resource(String path) {
        LocalDateTime now = LocalDateTime.now();
        return dsl.insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.OAS_DOC_ID, oasDocId).set(OAS_RESOURCE.PATH, path)
                .set(OAS_RESOURCE.CREATED_BY, USER).set(OAS_RESOURCE.LAST_UPDATED_BY, USER)
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, now).set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID).fetchOne().value1();
    }

    private ULong messageBody() {
        LocalDateTime now = LocalDateTime.now();
        return dsl.insertInto(OAS_MESSAGE_BODY)
                .set(OAS_MESSAGE_BODY.CREATED_BY, USER).set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, USER)
                .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, now).set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID).fetchOne().value1();
    }

    /** An operation (with a Response body, and a Request body if asked) in the resource, as Add BIE makes it. */
    private Endpoint operation(ULong resourceId, String path, String verb, boolean withRequest) {
        LocalDateTime now = LocalDateTime.now();
        ULong operationId = dsl.insertInto(OAS_OPERATION)
                .set(OAS_OPERATION.OAS_RESOURCE_ID, resourceId).set(OAS_OPERATION.VERB, verb)
                .set(OAS_OPERATION.OPERATION_ID, verb.toLowerCase() + "Order")
                .set(OAS_OPERATION.CREATED_BY, USER).set(OAS_OPERATION.LAST_UPDATED_BY, USER)
                .set(OAS_OPERATION.CREATION_TIMESTAMP, now).set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_OPERATION.OAS_OPERATION_ID).fetchOne().value1();
        dsl.insertInto(OAS_RESPONSE)
                .set(OAS_RESPONSE.OAS_OPERATION_ID, operationId).set(OAS_RESPONSE.OAS_MESSAGE_BODY_ID, messageBody())
                .set(OAS_RESPONSE.CREATED_BY, USER).set(OAS_RESPONSE.LAST_UPDATED_BY, USER)
                .set(OAS_RESPONSE.CREATION_TIMESTAMP, now).set(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP, now).execute();
        if (withRequest) {
            dsl.insertInto(OAS_REQUEST)
                    .set(OAS_REQUEST.OAS_OPERATION_ID, operationId).set(OAS_REQUEST.OAS_MESSAGE_BODY_ID, messageBody())
                    .set(OAS_REQUEST.CREATED_BY, USER).set(OAS_REQUEST.LAST_UPDATED_BY, USER)
                    .set(OAS_REQUEST.CREATION_TIMESTAMP, now).set(OAS_REQUEST.LAST_UPDATE_TIMESTAMP, now).execute();
        }
        return new Endpoint(resourceId, operationId, verb, path);
    }

    /** One operation alone on its own path, as a legacy document has them. */
    private Endpoint endpoint(String path, String verb) {
        return operation(resource(path), path, verb, false);
    }

    /** The row of the operation as the page sends it back: the path it shows is {@code resourceName}. */
    private BieForOasDoc row(Endpoint endpoint, String messageBody, String resourceName) {
        BieForOasDoc row = new BieForOasDoc();
        row.setMessageBody(messageBody);
        row.setVerb(endpoint.verb());
        row.setResourceName(resourceName);
        row.setOperationId(endpoint.verb().toLowerCase() + "Order");
        row.setOasOperationId(new OasOperationId(endpoint.operationId().toBigInteger()));
        row.setOasResourceId(new OasResourceId(endpoint.resourceId().toBigInteger()));
        return row;
    }

    private BieForOasDoc responseRow(Endpoint endpoint, String resourceName) {
        return row(endpoint, "Response", resourceName);
    }

    private UpdateBieForOasDocResponse update(BieForOasDoc... rows) {
        UpdateBieForOasDocRequest request = new UpdateBieForOasDocRequest(requester());
        request.setOasDocId(new OasDocId(oasDocId.toBigInteger()));
        request.setBieForOasDocList(List.of(rows));
        return new JooqBieForOasDocCommandRepository(dsl, requester(), null).updateBieForOasDoc(request);
    }

    /** "VERB path" of every operation of the document, sorted. */
    private List<String> operations() {
        return dsl.select(OAS_OPERATION.VERB, OAS_RESOURCE.PATH).from(OAS_OPERATION).join(OAS_RESOURCE)
                .on(OAS_RESOURCE.OAS_RESOURCE_ID.eq(OAS_OPERATION.OAS_RESOURCE_ID))
                .where(OAS_RESOURCE.OAS_DOC_ID.eq(oasDocId)).fetch()
                .map(r -> r.get(OAS_OPERATION.VERB) + " " + r.get(OAS_RESOURCE.PATH)).stream().sorted().collect(Collectors.toList());
    }

    private ULong resourceOf(Endpoint endpoint) {
        return dsl.select(OAS_OPERATION.OAS_RESOURCE_ID).from(OAS_OPERATION)
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(endpoint.operationId())).fetchOne(OAS_OPERATION.OAS_RESOURCE_ID);
    }

    private int resources() {
        return dsl.fetchCount(OAS_RESOURCE, OAS_RESOURCE.OAS_DOC_ID.eq(oasDocId));
    }

    private int resourcesWithoutOperation() {
        return dsl.fetchCount(OAS_RESOURCE, OAS_RESOURCE.OAS_DOC_ID.eq(oasDocId)
                .and(DSL.notExists(dsl.selectOne().from(OAS_OPERATION)
                        .where(OAS_OPERATION.OAS_RESOURCE_ID.eq(OAS_RESOURCE.OAS_RESOURCE_ID)))));
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Test
    void editingThePathOfOneVerbDoesNotMoveTheOtherVerbsOfThePath() {
        ULong shared = resource("/orders");
        Endpoint get = operation(shared, "/orders", "GET", false);
        Endpoint post = operation(shared, "/orders", "POST", false);

        // the page: both rows show the path that they share; only the GET row was edited
        UpdateBieForOasDocResponse response = update(responseRow(get, "/items"), responseRow(post, "/orders"));

        assertTrue(response.isChanged());
        assertEquals(List.of("GET /items", "POST /orders"), operations());
        assertEquals(shared, resourceOf(post), "the resource that POST uses is untouched");
        assertNotEquals(shared, resourceOf(get));
        assertEquals(0, resourcesWithoutOperation());
    }

    @Test
    void editingThePathOfEveryVerbOfAPathMovesThemAllToOneResourceAndLeavesNothingBehind() {
        ULong shared = resource("/orders");
        Endpoint get = operation(shared, "/orders", "GET", false);
        Endpoint post = operation(shared, "/orders", "POST", false);

        update(responseRow(get, "/items"), responseRow(post, "/items"));

        assertEquals(List.of("GET /items", "POST /items"), operations());
        assertEquals(resourceOf(get), resourceOf(post), "one path, one resource");
        assertEquals(1, resources());
    }

    @Test
    void theOnlyVerbOfAPathKeepsItsResourceWhenItsPathIsEdited() {
        Endpoint get = endpoint("/orders", "GET");

        update(responseRow(get, "/items"));

        assertEquals(List.of("GET /items"), operations());
        assertEquals(get.resourceId(), resourceOf(get), "renamed in place, not replaced");
        assertEquals(1, resources());
    }

    @Test
    void movingToAPathThatTheDocumentHasJoinsItsResourceAndTheEmptiedResourceGoes() {
        Endpoint get = endpoint("/orders", "GET");
        Endpoint post = endpoint("/items", "POST");

        update(responseRow(get, "/items"), responseRow(post, "/items"));

        assertEquals(List.of("GET /items", "POST /items"), operations());
        assertEquals(post.resourceId(), resourceOf(get));
        assertEquals(1, resources());
        assertEquals(0, resourcesWithoutOperation());
    }

    @Test
    void aLegacyDocumentWithSeveralResourcesOnAPathJoinsTheLowestOne() {
        Endpoint mover = endpoint("/orders", "GET");
        Endpoint first = endpoint("/items", "POST");
        Endpoint second = endpoint("/items", "PATCH");

        update(responseRow(mover, "/items"), responseRow(first, "/items"), responseRow(second, "/items"));

        assertEquals(first.resourceId(), resourceOf(mover));
        assertEquals(second.resourceId(), resourceOf(second), "the other legacy resource is not touched");
    }

    @Test
    void theRequestAndTheResponseRowsOfAnOperationFollowTheOneThatWasEdited() {
        ULong shared = resource("/orders");
        Endpoint post = operation(shared, "/orders", "POST", true);
        Endpoint get = operation(shared, "/orders", "GET", false);

        // only the Response row of POST was edited; its Request row still says the old path, and it comes later
        update(row(post, "Response", "/items"), row(post, "Request", "/orders"), responseRow(get, "/orders"));

        assertEquals(List.of("GET /orders", "POST /items"), operations());
        assertEquals(shared, resourceOf(get));
    }

    @Test
    void theVerbAndTheOperationIdAreStillSavedWhenThePathMovesInTheSameUpdate() {
        ULong shared = resource("/orders");
        Endpoint get = operation(shared, "/orders", "GET", false);
        operation(shared, "/orders", "POST", false);

        BieForOasDoc edited = responseRow(get, "/items");
        edited.setVerb("PUT");
        edited.setOperationId("replaceOrder");
        update(edited);

        assertEquals("PUT", dsl.select(OAS_OPERATION.VERB).from(OAS_OPERATION)
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(get.operationId())).fetchOne(OAS_OPERATION.VERB));
        assertEquals("replaceOrder", dsl.select(OAS_OPERATION.OPERATION_ID).from(OAS_OPERATION)
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(get.operationId())).fetchOne(OAS_OPERATION.OPERATION_ID));
        assertEquals(List.of("POST /orders", "PUT /items"), operations());
    }

    @Test
    void thePathParametersOfTheMovedOperationFollowItAndThoseOfTheOthersStay() {
        ULong shared = resource("/orders/{id}");
        Endpoint get = operation(shared, "/orders/{id}", "GET", false);
        Endpoint patch = operation(shared, "/orders/{id}", "PATCH", false);
        OasOperationParameters.reconcile(dsl, get.operationId(), OasOperationParameterState.NONE, USER, LocalDateTime.now());
        OasOperationParameters.reconcile(dsl, patch.operationId(), OasOperationParameterState.NONE, USER, LocalDateTime.now());

        update(responseRow(get, "/orders/{id}"), responseRow(patch, "/orders/{ids}"));

        List<String> entries = new ArrayList<>();
        OasDocHttpHeaderStore.load(dsl, oasDocId).forEach(h ->
                entries.add(h.getOasOperationId().equals(get.operationId().toBigInteger()) ? "GET " + h.getName() : "PATCH " + h.getName()));
        assertEquals(List.of("GET id", "PATCH ids"), entries.stream().sorted().collect(Collectors.toList()));
    }

    @Test
    void aRowOfAnotherDocumentsResourceIsRefused() {
        Endpoint get = endpoint("/orders", "GET");
        LocalDateTime now = LocalDateTime.now();
        ULong otherDoc = dsl.insertInto(OAS_DOC)
                .set(OAS_DOC.GUID, randomGuid()).set(OAS_DOC.OPEN_API_VERSION, "3.1.1")
                .set(OAS_DOC.TITLE, "other").set(OAS_DOC.VERSION, "1")
                .set(OAS_DOC.OWNER_USER_ID, USER).set(OAS_DOC.CREATED_BY, USER).set(OAS_DOC.LAST_UPDATED_BY, USER)
                .set(OAS_DOC.CREATION_TIMESTAMP, now).set(OAS_DOC.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_DOC.OAS_DOC_ID).fetchOne().value1();
        ULong foreign = dsl.insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.OAS_DOC_ID, otherDoc).set(OAS_RESOURCE.PATH, "/x")
                .set(OAS_RESOURCE.CREATED_BY, USER).set(OAS_RESOURCE.LAST_UPDATED_BY, USER)
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, now).set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID).fetchOne().value1();
        BieForOasDoc row = responseRow(get, "/items");
        row.setOasResourceId(new OasResourceId(foreign.toBigInteger()));

        assertThrows(RuntimeException.class, () -> update(row));
        assertEquals(List.of("GET /orders"), operations());
    }

    @Test
    void anOperationOfAnotherDocumentIsRefusedEvenWhenTheRowNamesAResourceOfThisOne() {
        ULong mine = resource("/mine");
        LocalDateTime now = LocalDateTime.now();
        ULong otherDoc = dsl.insertInto(OAS_DOC)
                .set(OAS_DOC.GUID, randomGuid()).set(OAS_DOC.OPEN_API_VERSION, "3.1.1")
                .set(OAS_DOC.TITLE, "other").set(OAS_DOC.VERSION, "1")
                .set(OAS_DOC.OWNER_USER_ID, USER).set(OAS_DOC.CREATED_BY, USER).set(OAS_DOC.LAST_UPDATED_BY, USER)
                .set(OAS_DOC.CREATION_TIMESTAMP, now).set(OAS_DOC.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_DOC.OAS_DOC_ID).fetchOne().value1();
        ULong theirs = dsl.insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.OAS_DOC_ID, otherDoc).set(OAS_RESOURCE.PATH, "/theirs")
                .set(OAS_RESOURCE.CREATED_BY, USER).set(OAS_RESOURCE.LAST_UPDATED_BY, USER)
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, now).set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, now)
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID).fetchOne().value1();
        Endpoint foreign = operation(theirs, "/theirs", "GET", false);
        BieForOasDoc row = responseRow(foreign, "/stolen");
        row.setOasResourceId(new OasResourceId(mine.toBigInteger()));

        assertThrows(RuntimeException.class, () -> update(row));
        assertEquals("/theirs", dsl.select(OAS_RESOURCE.PATH).from(OAS_RESOURCE)
                .where(OAS_RESOURCE.OAS_RESOURCE_ID.eq(theirs)).fetchOne(OAS_RESOURCE.PATH));
    }
}
