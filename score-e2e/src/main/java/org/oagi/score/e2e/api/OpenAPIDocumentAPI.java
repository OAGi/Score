package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;

/**
 * APIs for the OpenAPI document management.
 */
public interface OpenAPIDocumentAPI {

    /**
     * Create a random OpenAPI document.
     *
     * @param creator account who creates this OpenAPI document
     * @return a created OpenAPI document object
     */
    OpenAPIDocumentObject createRandomOpenAPIDocument(AppUserObject creator);

    /**
     * Create a random server record for the given OpenAPI document.
     *
     * @param openAPIDocument the target OpenAPI document
     * @param creator         account who creates this server record
     */
    void createRandomServer(OpenAPIDocumentObject openAPIDocument, AppUserObject creator);

    /**
     * Seed a single raw OpenAPI operation (one Request OR one Response body) directly into the database,
     * bypassing the UI.
     *
     * <p>The OpenAPI Document editor (since Issue #1492, Option 2) find-or-creates exactly one
     * {@code oas_operation} per {@code (path, verb)}, so it can no longer produce the legacy "split
     * operation" shape in which one endpoint's Request and Response live on two separate
     * {@code oas_operation} rows that share a single {@code operation_id}. That shape still exists in
     * databases imported before Issue #1492, and Issue #1757 is precisely the false
     * {@code Operation ID must be unique within the document.} error it triggered on the BIE-root
     * {@code OpenAPI Document Information} panel (whose duplicate check keyed on the {@code oas_operation_id}
     * row PK rather than the {@code (Resource Name, Verb)} operation identity).</p>
     *
     * <p>This inserts one {@code oas_operation} — find-or-creating its {@code oas_resource} by
     * {@code (oas_doc_id, path)} so repeated calls on one {@code path} share the same resource — carrying
     * exactly one {@code oas_request} (when {@code messageBody} is {@code "Request"}) or one
     * {@code oas_response} (otherwise), whose {@code oas_message_body} points at the given BIE. Compose two
     * calls with the SAME {@code (path, verb, operationId)} — one {@code "Request"} and one
     * {@code "Response"} — to reproduce the legacy split operation; compose two calls with DIFFERENT
     * {@code path}s but the SAME {@code operationId} to reproduce a genuine cross-operation
     * {@code Operation ID} collision.</p>
     *
     * @param oasDocument the OpenAPI document the operation belongs to
     * @param bie         the top-level BIE the message body references
     * @param path        the resource path (surfaced as the {@code Resource Name})
     * @param verb        the HTTP verb, stored verbatim in the app's canonical upper case (e.g. {@code POST})
     * @param operationId the OpenAPI {@code operationId} string stored on the operation
     * @param messageBody {@code "Request"} to attach an {@code oas_request}, otherwise an {@code oas_response}
     * @param creator     the account recorded as the creator of the seeded rows
     */
    void seedOpenAPIOperationWithBody(OpenAPIDocumentObject oasDocument, TopLevelASBIEPObject bie,
                                      String path, String verb, String operationId, String messageBody,
                                      AppUserObject creator);

}
