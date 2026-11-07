package org.oagi.score.gateway.http.api.oas_management.service;

import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextQueryService;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocViolation.Scope;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What the server makes of an operation that a client is about to add to an OpenAPI Document (its Resource Name and its
 * Operation ID, which the user did not give), and whether it can be added: the client shows the answer, and adds. The names
 * are made as they are when a BIE is added ({@link OasOperationNaming}), so what is shown is what is added.
 */
@Service
@Transactional(readOnly = true)
public class OasOperationProposer {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private BusinessContextQueryService businessContextQueryService;

    public OasOperationProposal propose(ScoreUser requester, OasDocId oasDocId, OasOperationCandidate candidate) {
        OasDoc oasDoc = repositoryFactory.oasDocQueryRepository(requester)
                .getOasDoc(new GetOasDocRequest(requester).withOasDocId(oasDocId)).getOasDoc();
        String version = oasDoc == null ? null : oasDoc.getVersion();

        String resourceName = isGiven(candidate.resourceName()) ? candidate.resourceName().trim()
                : deriveResourceName(requester, candidate, version);
        String operationId = isGiven(candidate.operationId()) ? candidate.operationId().trim()
                : isGiven(candidate.propertyTerm())
                ? OasOperationNaming.buildOperationId(candidate.verb(), candidate.propertyTerm(), candidate.arrayIndicator())
                : OasOperationNaming.operationIdFromPath(candidate.verb(), resourceName);

        List<OasDocViolation> violations = new ArrayList<>();
        boolean isRequest = "Request".equals(candidate.messageBody());
        if (isRequest && "GET".equalsIgnoreCase(candidate.verb())) {
            violations.add(violation("messageBody", "A GET operation has no request body."));
        }
        if (resourceName == null || resourceName.isBlank()) {
            violations.add(violation("resourceName", "The Resource Name is required."));
        } else {
            OasOperationNaming.resourcePathError(resourceName).ifPresent(error -> violations.add(violation("resourceName", error)));
        }
        if (operationId == null || operationId.isBlank()) {
            violations.add(violation("operationId", "The Operation ID is required."));
        }
        if (violations.isEmpty()) {
            OasDocCommandRepository command = repositoryFactory.oasDocCommandRepository(requester);
            String verb = candidate.verb().trim().toUpperCase();
            if (!command.findOperationsWithBodyAtEndpoint(oasDocId, resourceName, verb, isRequest).isEmpty()) {
                violations.add(violation("messageBody", "This operation (" + verb + " " + resourceName + ") already has a "
                        + candidate.messageBody() + " body. An operation can have at most one Request and one Response body."));
            }
            Set<OasOperationId> sameEndpoint = new HashSet<>(command.findOasOperationIdsAtEndpoint(oasDocId, resourceName, verb));
            if (!command.findOperationIdsInOasDoc(oasDocId, Set.of(operationId), sameEndpoint).isEmpty()) {
                violations.add(violation("operationId", "The operation ID '" + operationId
                        + "' is already used by another operation of the document."));
            }
        }
        return new OasOperationProposal(resourceName, operationId, List.copyOf(violations));
    }

    /** {@code /<business context>/<version>/<leaf>}, with the first business context of the BIE. */
    private String deriveResourceName(ScoreUser requester, OasOperationCandidate candidate, String version) {
        if (candidate.topLevelAsbiepId() == null || !isGiven(candidate.propertyTerm())) {
            return null;
        }
        var contexts = businessContextQueryService.getBusinessContextSummaryList(requester, candidate.topLevelAsbiepId(), null);
        String contextName = contexts.isEmpty() ? "" : contexts.get(0).name();
        return OasOperationNaming.buildResourcePath(contextName, version, candidate.propertyTerm(), candidate.arrayIndicator());
    }

    private static boolean isGiven(String value) {
        return value != null && !value.isBlank();
    }

    private static OasDocViolation violation(String field, String message) {
        return new OasDocViolation(Scope.OPERATION, null, null, field, message);
    }
}
