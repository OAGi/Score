package org.oagi.score.gateway.http.api.oas_management.model;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

/**
 * An operation that the user added in the page and has not saved. What the user did not say (the Resource Name and the Operation ID
 * of a BIE that is added) the server makes when it checks or saves the change set.
 *
 * @param clientKey        what the client calls the row until it is saved (the row has no id yet)
 * @param topLevelAsbiepId the BIE, or {@code null} for an operation without a BIE
 * @param propertyTerm     the property term of the BIE
 * @param verb             the HTTP verb
 * @param messageBody      {@code Request} or {@code Response}
 * @param arrayIndicator   whether the BIE is a list
 * @param suppressRootIndicator whether the root of the BIE is left out
 * @param resourceName     the Resource Name when the user gave one, else it is made from the BIE
 * @param operationId      the Operation ID when the user gave one, else it is made from the verb and the BIE
 * @param tagName          the tag of the operation
 * @param httpStatusCode   the status code of the Response of an operation without a BIE
 * @param summary          the summary of an operation without a BIE
 * @param description      the description of an operation without a BIE
 */
public record OasOperationAddition(String clientKey, TopLevelAsbiepId topLevelAsbiepId, String propertyTerm, String verb,
                                   String messageBody, boolean arrayIndicator, boolean suppressRootIndicator,
                                   String resourceName, String operationId, String tagName, Integer httpStatusCode,
                                   String summary, String description) {

    public boolean hasBie() {
        return topLevelAsbiepId != null;
    }

    public OasOperationCandidate candidate() {
        return new OasOperationCandidate(topLevelAsbiepId, propertyTerm, verb, messageBody, arrayIndicator, resourceName, operationId);
    }
}
