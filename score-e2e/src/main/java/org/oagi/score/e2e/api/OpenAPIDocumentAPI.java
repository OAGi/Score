package org.oagi.score.e2e.api;

import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;

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

}
