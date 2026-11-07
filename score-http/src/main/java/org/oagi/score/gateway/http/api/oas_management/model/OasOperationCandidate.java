package org.oagi.score.gateway.http.api.oas_management.model;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

/**
 * An operation that a client is about to add to an OpenAPI Document, as far as the user has said it. What the user did not say
 * (the Resource Name and the Operation ID of a BIE that is added) the server makes.
 *
 * @param topLevelAsbiepId the BIE, when the operation has one
 * @param propertyTerm     the property term of the BIE
 * @param verb             the HTTP verb
 * @param messageBody      {@code Request} or {@code Response}
 * @param arrayIndicator   whether the BIE is a list
 * @param resourceName     the Resource Name when the user gave one (an operation without a BIE), else it is made from the BIE
 * @param operationId      the Operation ID when the user gave one, else it is made from the verb and the BIE
 */
public record OasOperationCandidate(TopLevelAsbiepId topLevelAsbiepId, String propertyTerm, String verb, String messageBody,
                                    boolean arrayIndicator, String resourceName, String operationId) {
}
