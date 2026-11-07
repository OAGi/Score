package org.oagi.score.gateway.http.api.oas_management.service;

import org.oagi.score.gateway.http.api.oas_management.controller.payload.AddBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.AddOperationForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.DeleteBieForOasDocRequest;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    @Autowired
    private OasOperationProposer proposer;

    /** The document and its operations are saved or checked by their own services; the entries here. */
    public OasDocChangeResult apply(ScoreUser requester, OasDocId oasDocId, OasDocChangeSet changeSet, boolean dryRun) {
        List<OasDocChangeResult.DerivedOperation> derived = deriveNames(changeSet.operationsOrEmpty());
        List<OasDocChangeResult.AddedOperation> added = new ArrayList<>();
        try {
            return applyChanges(requester, oasDocId, changeSet, dryRun, derived, added);
        } catch (OasDocViolationException e) {
            // what was made of the names is what the user sees next to what is wrong
            throw e.withDerived(derived).withAdded(List.copyOf(added));
        }
    }

    private OasDocChangeResult applyChanges(ScoreUser requester, OasDocId oasDocId, OasDocChangeSet changeSet, boolean dryRun,
                                            List<OasDocChangeResult.DerivedOperation> derived,
                                            List<OasDocChangeResult.AddedOperation> added) {
        // Every rule that is broken is said at once. A step is judged as it would be saved, so when the document itself cannot be
        // saved the steps that refer to it are not judged: what they would say is not about what the user did.
        List<OasDocViolation> violations = new ArrayList<>(rowViolations(changeSet.operationsOrEmpty()));
        boolean documentFailed = false;
        if (changeSet.document() != null) {
            UpdateOasDocRequest request = documentRequest(requester, oasDocId, changeSet.document());
            try {
                step(Scope.DOCUMENT, () -> oasDocService.updateOasDoc(requester, request));
            } catch (OasDocViolationException e) {
                violations.addAll(e.violations());
                documentFailed = true;
            }
        }
        List<OasDocHttpHeader> headers = List.of();
        Map<BigInteger, List<OasDocChangeResult.AddedOperation>> createdOperations = new LinkedHashMap<>();
        if (!documentFailed) {
            if (!changeSet.removedOrEmpty().isEmpty()) {
                DeleteBieForOasDocRequest request = new DeleteBieForOasDocRequest(requester);
                request.setOasDocId(oasDocId);
                request.setBieForOasDocList(changeSet.removedOrEmpty());
                try {
                    step(Scope.OPERATION, () -> oasDocService.deleteBieForOasDoc(requester, request));
                } catch (OasDocViolationException e) {
                    violations.addAll(e.violations());
                }
            }
            violations.addAll(addOperations(requester, oasDocId, changeSet.addedOrEmpty(), added, createdOperations));
            boolean operationsFailed = false;
            if (!changeSet.operationsOrEmpty().isEmpty()) {
                UpdateBieForOasDocRequest request = new UpdateBieForOasDocRequest(requester);
                request.setOasDocId(oasDocId);
                request.setBieForOasDocList(changeSet.operationsOrEmpty());
                try {
                    step(Scope.OPERATION, () -> oasDocService.updateDetails(requester, request));
                } catch (OasDocViolationException e) {
                    violations.addAll(e.violations());
                    operationsFailed = true;
                    if (dryRun) {
                        followOperations(requester, request);
                    }
                }
            }
            OasDocCommandRepository command = repositoryFactory.oasDocCommandRepository(requester);
            headers = command.applyHeaderChanges(oasDocId, changeSet.headersOrNone());
            // the entries of an operation that this change set creates are the page's after it is saved: what it reads then has them
            headers = headers.stream().filter(h -> h.getOasOperationId() == null
                    || !createdOperations.containsKey(h.getOasOperationId())).toList();
            OasDoc saved = repositoryFactory.oasDocQueryRepository(requester)
                    .getOasDoc(new GetOasDocRequest(requester).withOasDocId(oasDocId)).getOasDoc();
            violations.addAll(OasDocHttpHeaderPolicy.violations(headers, saved.getOpenAPIVersion()));
            violations.addAll(pathParameterViolations(headers, command.findResolvedOperationPaths(oasDocId)));
            if (!operationsFailed) {
                violations.addAll(documentWideViolations(command.findOperationEndpoints(oasDocId)));
            }
        }
        try {
            assertAll(new ArrayList<>(new LinkedHashSet<>(forAddedRows(violations, createdOperations))));
        } catch (OasDocViolationException e) {
            // the entries that follow the operations do so whether or not the change can be saved: the page shows them next to what is wrong
            throw e.withHttpHeaders(documentFailed ? null : headers);
        }

        if (dryRun) {
            markRollbackOnly();
        }
        return OasDocChangeResult.valid(headers, derived, List.copyOf(added));
    }

    /**
     * The service refuses to write the operations when a rule is broken, and the entries that the system registers for them are made
     * by that write. They follow the operations whether or not the change can be saved, and a check keeps nothing, so the operations
     * are written here as they are, for the entries that the document would have; if they cannot be written either, the entries are
     * left as they are.
     */
    private void followOperations(ScoreUser requester, UpdateBieForOasDocRequest request) {
        try {
            repositoryFactory.bieForOasDocCommandRepository(requester).updateBieForOasDoc(request);
        } catch (RuntimeException e) {
            // the entries are those of before
        }
    }

    /**
     * Adds the operations that the user added in the page, in the order of the page. Their Operation IDs are judged with the whole
     * change set (see {@link #documentWideViolations}), so that every row that has one that another has is said, not only the later.
     */
    private List<OasDocViolation> addOperations(ScoreUser requester, OasDocId oasDocId, List<OasOperationAddition> additions,
                                                List<OasDocChangeResult.AddedOperation> added,
                                                Map<BigInteger, List<OasDocChangeResult.AddedOperation>> createdOperations) {
        List<OasDocViolation> violations = new ArrayList<>();
        OasDocCommandRepository command = repositoryFactory.oasDocCommandRepository(requester);
        for (OasOperationAddition addition : additions) {
            OasOperationProposal names = proposer.names(requester, oasDocId, addition.candidate());
            OasDocChangeResult.AddedOperation addedOperation = new OasDocChangeResult.AddedOperation(addition.clientKey(),
                    names.operationId(), names.resourceName(), addition.messageBody());
            added.add(addedOperation);
            List<OasDocViolation> rowViolations = new ArrayList<>();
            OasOperationNaming.resourcePathError(names.resourceName()).ifPresent(error ->
                    rowViolations.add(new OasDocViolation(Scope.OPERATION, null, addition.messageBody(), "resourceName", error)));
            boolean operationIdCleared = addition.operationId() != null && addition.operationId().isBlank(); // given, and blank
            if (operationIdCleared || names.operationId() == null || names.operationId().isBlank()) {
                rowViolations.add(new OasDocViolation(Scope.OPERATION, null, addition.messageBody(), "operationId", "The Operation ID is required."));
            }
            if ("Request".equals(addition.messageBody()) && "GET".equalsIgnoreCase(addition.verb())) {
                rowViolations.add(new OasDocViolation(Scope.OPERATION, null, addition.messageBody(), "messageBody", "A GET operation has no request body."));
            }
            if (rowViolations.isEmpty()) {
                try {
                    step(Scope.OPERATION, () -> addOne(requester, oasDocId, addition, names));
                    OasResourceId resource = command.findOasResourceId(oasDocId, names.resourceName());
                    OasOperationId created = resource == null ? null : command.findOasOperationId(resource, addition.verb().trim().toUpperCase());
                    if (created != null) {
                        createdOperations.computeIfAbsent(created.value(), k -> new ArrayList<>()).add(addedOperation);
                    }
                } catch (OasDocViolationException e) {
                    e.violations().forEach(v -> rowViolations.add(new OasDocViolation(v.scope(), null,
                            v.messageBody() != null ? v.messageBody() : addition.messageBody(), v.field() != null ? v.field() : "messageBody", v.message())));
                }
            }
            rowViolations.forEach(v -> violations.add(v.forAddedRow(addition.clientKey())));
        }
        return violations;
    }

    private void addOne(ScoreUser requester, OasDocId oasDocId, OasOperationAddition addition, OasOperationProposal names) {
        if (addition.hasBie()) {
            AddBieForOasDocRequest request = new AddBieForOasDocRequest(requester);
            request.setOasDocId(oasDocId);
            request.setTopLevelAsbiepId(addition.topLevelAsbiepId());
            request.setOasRequest("Request".equals(addition.messageBody()));
            request.setPath(names.resourceName());
            request.setVerb(addition.verb());
            request.setOperationId(names.operationId());
            request.setMakeArrayIndicator(addition.arrayIndicator());
            request.setSuppressRootIndicator(addition.suppressRootIndicator());
            request.setTagName(addition.tagName());
            request.setDeprecatedForOperation(false);
            request.setOperationIdJudgedLater(true);
            oasDocService.addBieForOasDoc(requester, request);
        } else {
            AddOperationForOasDocRequest request = new AddOperationForOasDocRequest(requester);
            request.setOasDocId(oasDocId);
            request.setOasRequest("Request".equals(addition.messageBody()));
            request.setPath(names.resourceName());
            request.setVerb(addition.verb());
            request.setOperationId(names.operationId());
            request.setTagName(addition.tagName());
            request.setHttpStatusCode(addition.httpStatusCode());
            request.setSummary(addition.summary());
            request.setDescription(addition.description());
            request.setOperationIdJudgedLater(true);
            oasDocService.addOperationForOasDoc(requester, request);
        }
    }

    /**
     * A violation of an operation that was created by this change set is about the row that the user added (the operation has no
     * id for the page): it is said for each added row of the operation that it is about.
     */
    private static List<OasDocViolation> forAddedRows(List<OasDocViolation> violations,
                                                      Map<BigInteger, List<OasDocChangeResult.AddedOperation>> createdOperations) {
        List<OasDocViolation> result = new ArrayList<>();
        for (OasDocViolation violation : violations) {
            List<OasDocChangeResult.AddedOperation> rows = violation.oasOperationId() == null ? null
                    : createdOperations.get(violation.oasOperationId());
            if (rows == null) {
                result.add(violation);
                continue;
            }
            rows.stream().filter(row -> violation.messageBody() == null || violation.messageBody().equals(row.messageBody()))
                    .forEach(row -> result.add(violation.forAddedRow(row.clientKey())));
        }
        return result;
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

    /**
     * What is wrong with the rows as they are, before anything is saved: an Operation ID that is not given (it is the one thing of an
     * operation that has to be), and a Resource Name that is not a path (a key of {@code paths} of the generated document). All of
     * them, so that the user sees every row that is wrong at once.
     */
    private static List<OasDocViolation> rowViolations(List<BieForOasDoc> operations) {
        List<OasDocViolation> violations = new ArrayList<>();
        for (BieForOasDoc row : operations) {
            if (row == null) {
                continue;
            }
            BigInteger oasOperationId = row.getOasOperationId() == null ? null : row.getOasOperationId().value();
            if (row.getOperationId() == null || row.getOperationId().isBlank()) {
                violations.add(new OasDocViolation(Scope.OPERATION, oasOperationId, row.getMessageBody(), "operationId",
                        "The Operation ID is required."));
            }
            OasOperationNaming.resourcePathError(row.getResourceName()).ifPresent(error ->
                    violations.add(new OasDocViolation(Scope.OPERATION, oasOperationId, row.getMessageBody(), "resourceName", error)));
        }
        return violations;
    }

    /**
     * What is wrong with the operations of the document as they are, whether the user has touched them or not: an Operation ID
     * that operations of two endpoints have (the Request and the Response of one endpoint share theirs), and a second Request or
     * Response body of an endpoint. The user sees them as soon as the document is opened.
     */
    private static List<OasDocViolation> documentWideViolations(List<OasOperationEndpoint> operations) {
        List<OasDocViolation> violations = new ArrayList<>();
        Map<String, List<OasOperationEndpoint>> byOperationId = new LinkedHashMap<>();
        Map<String, List<OasOperationEndpoint>> byEndpoint = new LinkedHashMap<>();
        for (OasOperationEndpoint operation : operations) {
            if (operation.operationId() != null && !operation.operationId().isBlank()) {
                byOperationId.computeIfAbsent(operation.operationId(), k -> new ArrayList<>()).add(operation); // exact: case-sensitive
            }
            byEndpoint.computeIfAbsent(operation.path() + "|" + operation.verb(), k -> new ArrayList<>()).add(operation);
        }
        byOperationId.forEach((operationId, sharing) -> {
            long endpoints = sharing.stream().map(o -> o.path() + "|" + o.verb()).distinct().count();
            if (endpoints > 1) {
                sharing.forEach(o -> violations.add(OasDocViolation.ofOperation(o.oasOperationId(), "operationId",
                        "Operation ID must be unique within the document.")));
            }
        });
        byEndpoint.values().forEach(atEndpoint -> {
            for (boolean request : new boolean[]{true, false}) {
                List<OasOperationEndpoint> withBody = atEndpoint.stream()
                        .filter(o -> request ? o.hasRequestBody() : o.hasResponseBody()).toList();
                if (withBody.size() > 1) {
                    String body = request ? "Request" : "Response";
                    withBody.forEach(o -> violations.add(new OasDocViolation(Scope.OPERATION, o.oasOperationId(), body, "messageBody",
                            "This endpoint already has a " + body + " body.")));
                }
            }
        });
        return violations;
    }

    private static void assertAll(List<OasDocViolation> violations) {
        if (!violations.isEmpty()) {
            throw new OasDocViolationException(violations);
        }
    }

    /**
     * A path parameter given for an operation has to be a template expression of the path of that operation. (The generator
     * refuses the document otherwise, so it is better said here, where it is made.)
     */
    private static List<OasDocViolation> pathParameterViolations(List<OasDocHttpHeader> headers, Map<OasOperationId, String> pathOfOperation) {
        List<OasDocViolation> violations = new ArrayList<>();
        for (OasDocHttpHeader header : headers) {
            if (header.getDirection() != OasDocHttpHeader.Direction.REQUEST || !"path".equals(header.getIn())
                    || header.getOasOperationId() == null) {
                continue;
            }
            String path = pathOfOperation.get(new OasOperationId(header.getOasOperationId()));
            if (path != null && !OasResourcePaths.parameterNames(path).contains(header.getName())) {
                violations.add(new OasDocViolation(Scope.HEADER, header.getOasOperationId(), null, "name",
                        "The path parameter '" + header.getName() + "' is not in the path '" + path + "' of its operation."));
            }
        }
        return violations;
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
