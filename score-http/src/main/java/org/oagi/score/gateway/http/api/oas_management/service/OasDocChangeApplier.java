package org.oagi.score.gateway.http.api.oas_management.service;

import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.UpdateOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocViolation.Scope;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies a change set to an OpenAPI Document in one transaction: the document, then its operations (which register the
 * request parameters that follow from them), then the entries the user added, changed and deleted. The rules of each step
 * are those of saving that part on its own, so a change set is judged as it would be saved.
 *
 * A rule that is broken throws an {@link OasDocViolationException} and the whole transaction is rolled back, so that
 * nothing of a change set is saved without the rest. With {@code dryRun} it is rolled back when it is fine too: what was
 * asked is answered, and nothing is kept. It is a different bean from {@link OasDocChangeService} so that the exception
 * leaves the transaction before it is turned into an answer.
 */
@Service
@Transactional
public class OasDocChangeApplier {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private OpenAPIDocService oasDocService;

    /** The document and its operations are saved or checked by their own services; the entries here. */
    public OasDocChangeResult apply(ScoreUser requester, OasDocId oasDocId, OasDocChangeSet changeSet, boolean dryRun) {
        List<OasDocChangeResult.DerivedOperation> derived = deriveNames(changeSet.operationsOrEmpty());
        try {
            return applyChanges(requester, oasDocId, changeSet, dryRun, derived);
        } catch (OasDocViolationException e) {
            throw e.withDerived(derived); // what was made of the names is what the user sees next to what is wrong
        }
    }

    private OasDocChangeResult applyChanges(ScoreUser requester, OasDocId oasDocId, OasDocChangeSet changeSet, boolean dryRun,
                                            List<OasDocChangeResult.DerivedOperation> derived) {
        assertOperationIdsGiven(changeSet.operationsOrEmpty());
        assertResourcePaths(changeSet.operationsOrEmpty());
        if (changeSet.document() != null) {
            UpdateOasDocRequest request = documentRequest(requester, oasDocId, changeSet.document());
            step(Scope.DOCUMENT, () -> oasDocService.updateOasDoc(requester, request));
        }
        if (!changeSet.operationsOrEmpty().isEmpty()) {
            UpdateBieForOasDocRequest request = new UpdateBieForOasDocRequest(requester);
            request.setOasDocId(oasDocId);
            request.setBieForOasDocList(changeSet.operationsOrEmpty());
            step(Scope.OPERATION, () -> oasDocService.updateDetails(requester, request));
        }

        OasDocCommandRepository command = repositoryFactory.oasDocCommandRepository(requester);
        List<OasDocHttpHeader> headers = command.applyHeaderChanges(oasDocId, changeSet.headersOrNone());
        OasDoc saved = repositoryFactory.oasDocQueryRepository(requester)
                .getOasDoc(new GetOasDocRequest(requester).withOasDocId(oasDocId)).getOasDoc();
        step(Scope.HEADER, () -> OasDocHttpHeaderPolicy.validate(headers, saved.getOpenAPIVersion()));
        assertPathParametersInPaths(headers, command.findResolvedOperationPaths(oasDocId));

        if (dryRun) {
            markRollbackOnly();
        }
        return OasDocChangeResult.valid(headers, derived);
    }

    /** The transaction ends in a rollback whatever happens next. */
    protected void markRollbackOnly() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    /**
     * Make what follows from the verb or the array indicator that the user changed ({@code touched}): the Operation ID, the
     * Resource Name, and a Request body that a GET cannot have. The rows are changed as they are, so what is saved is what is
     * answered. The other row of the operation, when it is sent too, is of the same operation and gets the same.
     */
    private static List<OasDocChangeResult.DerivedOperation> deriveNames(List<BieForOasDoc> operations) {
        Map<String, OasDocChangeResult.DerivedOperation> derived = new LinkedHashMap<>();
        for (BieForOasDoc row : operations) {
            if (row == null || row.getTouched().isEmpty()) {
                continue;
            }
            String sentMessageBody = row.getMessageBody();
            boolean verbTouched = row.getTouched().contains("verb");
            boolean arrayTouched = row.getTouched().contains("arrayIndicator");
            if (verbTouched && "GET".equalsIgnoreCase(row.getVerb()) && "Request".equals(row.getMessageBody())) {
                row.setMessageBody("Response"); // a GET has no request body
            }
            if (verbTouched || arrayTouched) {
                row.setOperationId(OasOperationNaming.recomputeOperationId(
                        row.getVerb(), row.getOperationId(), row.isArrayIndicator(), row.getPropertyTerm()));
            }
            if (arrayTouched) {
                row.setResourceName(OasOperationNaming.applyResourceArrayMarker(
                        row.getResourceName(), row.getPropertyTerm(), row.isArrayIndicator()));
            }
            for (BieForOasDoc twin : operations) {
                if (twin != null && twin != row && twin.getOasOperationId() != null
                        && twin.getOasOperationId().equals(row.getOasOperationId()) && twin.getTouched().isEmpty()) {
                    String twinSent = twin.getMessageBody();
                    twin.setOperationId(row.getOperationId());
                    twin.setResourceName(row.getResourceName());
                    derived.put(keyOf(twin.getOasOperationId(), twinSent), derivedOf(twin, twinSent));
                }
            }
            derived.put(keyOf(row.getOasOperationId(), sentMessageBody), derivedOf(row, sentMessageBody));
        }
        return List.copyOf(derived.values());
    }

    private static String keyOf(OasOperationId oasOperationId, String messageBody) {
        return (oasOperationId == null ? "" : oasOperationId.value()) + "|" + messageBody;
    }

    private static OasDocChangeResult.DerivedOperation derivedOf(BieForOasDoc row, String sentMessageBody) {
        return new OasDocChangeResult.DerivedOperation(row.getOasOperationId() == null ? null : row.getOasOperationId().value(),
                sentMessageBody, row.getOperationId(), row.getResourceName(), row.getMessageBody());
    }

    /** The Resource Name is a key of {@code paths} of the generated document, so it has to be a path. */
    private static void assertResourcePaths(List<BieForOasDoc> operations) {
        for (BieForOasDoc row : operations) {
            String error = row == null ? null : OasOperationNaming.resourcePathError(row.getResourceName()).orElse(null);
            if (error != null) {
                throw new OasDocViolationException(new OasDocViolation(Scope.OPERATION,
                        row.getOasOperationId() == null ? null : row.getOasOperationId().value(), row.getMessageBody(),
                        "resourceName", error));
            }
        }
    }

    /** The Operation ID is the one thing of an operation that has to be given. */
    private static void assertOperationIdsGiven(List<BieForOasDoc> operations) {
        for (BieForOasDoc operation : operations) {
            if (operation != null && (operation.getOperationId() == null || operation.getOperationId().isBlank())) {
                throw new OasDocViolationException(new OasDocViolation(Scope.OPERATION,
                        operation.getOasOperationId() == null ? null : operation.getOasOperationId().value(),
                        operation.getMessageBody(), "operationId", "The Operation ID is required."));
            }
        }
    }

    /**
     * A path parameter given for an operation has to be a template expression of the path of that operation. (The generator
     * refuses the document otherwise, so it is better said here, where it is made.)
     */
    private static void assertPathParametersInPaths(List<OasDocHttpHeader> headers, Map<OasOperationId, String> pathOfOperation) {
        for (OasDocHttpHeader header : headers) {
            if (header.getDirection() != OasDocHttpHeader.Direction.REQUEST || !"path".equals(header.getIn())
                    || header.getOasOperationId() == null) {
                continue;
            }
            String path = pathOfOperation.get(new OasOperationId(header.getOasOperationId()));
            if (path != null && !OasResourcePaths.parameterNames(path).contains(header.getName())) {
                throw new OasDocViolationException(new OasDocViolation(Scope.HEADER, header.getOasOperationId(), null, "name",
                        "The path parameter '" + header.getName() + "' is not in the path '" + path + "' of its operation."));
            }
        }
    }

    private static UpdateOasDocRequest documentRequest(ScoreUser requester, OasDocId oasDocId, OasDoc document) {
        UpdateOasDocRequest request = new UpdateOasDocRequest(requester).withOasDocId(oasDocId);
        request.setOpenAPIVersion(document.getOpenAPIVersion());
        request.setTitle(document.getTitle());
        request.setTermsOfService(document.getTermsOfService());
        request.setVersion(document.getVersion());
        request.setDescription(document.getDescription());
        request.setContactEmail(document.getContactEmail());
        request.setContactName(document.getContactName());
        request.setContactUrl(document.getContactUrl());
        request.setLicenseName(document.getLicenseName());
        request.setLicenseUrl(document.getLicenseUrl());
        request.setSecuritySchemes(document.getSecuritySchemes());
        request.setSecurityRequirements(document.getSecurityRequirements());
        // the entries are not the document's own: they are saved from what was added, changed and deleted
        request.setHttpHeaders(null);
        return request;
    }

    /** Runs a step; a rule it breaks (an {@link IllegalArgumentException}) is a violation of that part of the change set. */
    private static void step(Scope scope, Runnable step) {
        try {
            step.run();
        } catch (OasDocViolationException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new OasDocViolationException(OasDocViolation.of(scope, e.getMessage()));
        }
    }
}
