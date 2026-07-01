package org.oagi.score.gateway.http.api.business_term_management.service;

import org.jooq.exception.DataAccessException;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.*;
import org.oagi.score.gateway.http.api.business_term_management.model.AsbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional
public class BusinessTermCommandService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Upper bound on the number of rows a single batch import may carry. The Angular dialog also
     * guards the source file at 10 MB, but this server-side cap is the authoritative protection for
     * the JSON {@code /batch} payload (which is not bounded by the multipart limits).
     */
    public static final int MAX_IMPORT_ROWS = 50000;

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ApplicationConfigurationQueryService applicationConfigurationQueryService;

    @Autowired
    private BusinessTermRowImporter rowImporter;

    @Autowired
    private BusinessTermImportFileParser importFileParser;

    /**
     * #1752 - H1: enforce the Business Term access policy server-side. The Angular UI hides the
     * Business Term area when the tenant feature flag is off <em>and</em> from developer-role
     * users (navbar + BIE-editor both gate on {@code isBusinessTermEnabled && !isDeveloper}), but
     * the REST endpoints must not trust the UI. A direct API call is rejected with 403 when either
     * (a) the feature is disabled for the tenant, or (b) the requester is a developer — Business
     * Term is an end-user-only feature, so the gate covers reads and writes alike.
     */
    private void assertBusinessTermEnabled(ScoreUser requester) {
        if (!applicationConfigurationQueryService.isBusinessTermEnabled(requester)) {
            throw new DataAccessForbiddenException("Business Term management is not enabled.");
        }
        if (requester.isDeveloper()) {
            throw new DataAccessForbiddenException(
                    "Business Term management is not available to developer-role users.");
        }
    }

    public BusinessTermId create(ScoreUser requester, BusinessTermCreateRequest request) {
        assertBusinessTermEnabled(requester);
        BusinessTermInputValidator.validate(
                request.businessTerm(), request.externalReferenceId(), request.externalReferenceUri());

        var command = repositoryFactory.businessTermCommandRepository(requester);
        return command.create(
                request.businessTerm(),
                request.externalReferenceId(),
                request.externalReferenceUri(),
                request.definition(),
                request.comment());
    }

    /**
     * Parses an uploaded CSV/TSV/XLSX file into headers + rows WITHOUT persisting anything, for the
     * import dialog's column-mapping and preview steps. Gated like the rest of the feature; runs
     * without an ambient transaction since it only reads the uploaded file.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BusinessTermParseResult parse(ScoreUser requester, String filename,
                                         InputStream inputStream, String sheetName) throws IOException {
        assertBusinessTermEnabled(requester);
        return importFileParser.parse(filename, inputStream, sheetName);
    }

    /**
     * Best-effort batch import for the redesigned Business Term import dialog. Each row is validated
     * and upserted in its OWN transaction (via {@link BusinessTermRowImporter}) so a failing row
     * does not roll back the rows that succeeded; the per-row outcome (created / updated / failed)
     * is returned so the dialog can show a summary and let the user fix and retry only the failures.
     *
     * <p>The access gate is applied once up front, and an intra-batch duplicate external-reference
     * URI is rejected (mirroring the dialog's preview), so two rows targeting the same record cannot
     * silently overwrite each other within a single import. This method runs WITHOUT an ambient
     * transaction so the per-row {@code REQUIRES_NEW} boundaries are the only commit points.</p>
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BusinessTermBatchImportResult createBatch(ScoreUser requester, List<BusinessTermImportRow> rows) {
        assertBusinessTermEnabled(requester);
        if (rows == null) {
            throw new IllegalArgumentException("No rows were provided for import.");
        }
        if (rows.size() > MAX_IMPORT_ROWS) {
            throw new IllegalArgumentException(
                    "The import exceeds the maximum of " + MAX_IMPORT_ROWS + " rows.");
        }

        Set<String> urisSeenInThisImport = new HashSet<>();
        List<BusinessTermBatchImportRowResult> results = new ArrayList<>();
        int createdCount = 0;
        int updatedCount = 0;
        int failedCount = 0;

        for (BusinessTermImportRow row : rows) {
            String uri = row.externalReferenceUri();
            if (hasLength(uri) && !urisSeenInThisImport.add(uri)) {
                failedCount++;
                results.add(failed(row, "Duplicate external reference URI in this import."));
                continue;
            }
            try {
                BusinessTermRowImporter.Outcome outcome = rowImporter.importOne(requester, row);
                if (outcome == BusinessTermRowImporter.Outcome.UPDATED) {
                    updatedCount++;
                    results.add(new BusinessTermBatchImportRowResult(
                            row.rowIndex(), row.businessTerm(), uri,
                            BusinessTermBatchImportRowResult.OUTCOME_UPDATED, null));
                } else {
                    createdCount++;
                    results.add(new BusinessTermBatchImportRowResult(
                            row.rowIndex(), row.businessTerm(), uri,
                            BusinessTermBatchImportRowResult.OUTCOME_CREATED, null));
                }
            } catch (IllegalArgumentException e) {
                failedCount++;
                results.add(failed(row, e.getMessage()));
            } catch (DataAccessException e) {
                // A row-level persistence failure (e.g. a value that slipped past validation) must
                // not abort the whole import; report it and move on. Its REQUIRES_NEW transaction
                // has already been rolled back independently.
                logger.warn("Failed to import business term row {} ({}): {}",
                        row.rowIndex(), uri, e.getMessage());
                failedCount++;
                results.add(failed(row, "Could not be saved due to a data error."));
            }
        }

        return new BusinessTermBatchImportResult(createdCount, updatedCount, failedCount, results);
    }

    private static BusinessTermBatchImportRowResult failed(BusinessTermImportRow row, String message) {
        return new BusinessTermBatchImportRowResult(
                row.rowIndex(), row.businessTerm(), row.externalReferenceUri(),
                BusinessTermBatchImportRowResult.OUTCOME_FAILED, message);
    }

    public boolean update(ScoreUser requester, BusinessTermUpdateRequest request) {
        assertBusinessTermEnabled(requester);
        BusinessTermInputValidator.validate(
                request.businessTerm(), request.externalReferenceId(), request.externalReferenceUri());

        var command = repositoryFactory.businessTermCommandRepository(requester);
        return command.update(
                request.businessTermId(),
                request.businessTerm(),
                request.externalReferenceId(),
                request.externalReferenceUri(),
                request.definition(),
                request.comment());
    }

    public boolean discard(ScoreUser requester, BusinessTermId businessTermId) {
        assertBusinessTermEnabled(requester);
        assertBusinessTermNotInUse(requester, businessTermId);

        var command = repositoryFactory.businessTermCommandRepository(requester);
        return command.delete(businessTermId);
    }

    /**
     * #1752 - M9: discard a batch of catalog business terms in a single transaction so a mid-batch
     * failure rolls the whole operation back instead of leaving partial commits.
     */
    public void discard(ScoreUser requester, List<BusinessTermId> businessTermIdList) {
        assertBusinessTermEnabled(requester);
        if (businessTermIdList == null || businessTermIdList.isEmpty()) {
            return;
        }
        for (BusinessTermId businessTermId : businessTermIdList) {
            assertBusinessTermNotInUse(requester, businessTermId);
        }

        var command = repositoryFactory.businessTermCommandRepository(requester);
        for (BusinessTermId businessTermId : businessTermIdList) {
            command.delete(businessTermId);
        }
    }

    /**
     * #1752 - H2: reject discarding a business term that is still assigned (referenced by
     * ascc_bizterm/bcc_bizterm) with a clear 400 instead of letting the unconditional delete
     * hit a foreign-key violation that surfaces as a misleading HTTP 500.
     */
    private void assertBusinessTermNotInUse(ScoreUser requester, BusinessTermId businessTermId) {
        var query = repositoryFactory.businessTermQueryRepository(requester);
        if (query.isBusinessTermUsed(businessTermId)) {
            throw new IllegalArgumentException(
                    "The business term is in use by one or more components and cannot be discarded.");
        }
    }

    public List<BigInteger> assignBusinessTerm(
            ScoreUser requester, BusinessTermId businessTermId, AssignBusinessTermRequest request) {
        assertBusinessTermEnabled(requester);

        var command = repositoryFactory.businessTermCommandRepository(requester);
        return command.assignBusinessTerm(businessTermId, request);
    }

    public boolean updateAssignment(
            ScoreUser requester, AsbieBusinessTermId asbieBusinessTermId, AssignedBusinessTermUpdateRequest request) {
        assertBusinessTermEnabled(requester);

        var command = repositoryFactory.businessTermCommandRepository(requester);
        return command.updateAssignment(asbieBusinessTermId,
                request.typeCode(),
                request.primaryIndicator());
    }

    public boolean updateAssignment(
            ScoreUser requester, BbieBusinessTermId bbieBusinessTermId, AssignedBusinessTermUpdateRequest request) {
        assertBusinessTermEnabled(requester);

        var command = repositoryFactory.businessTermCommandRepository(requester);
        return command.updateAssignment(bbieBusinessTermId,
                request.typeCode(),
                request.primaryIndicator());
    }

    public void deleteBusinessTermAssignment(
            ScoreUser requester, AssignedBusinessTermDeleteRequest request) {
        assertBusinessTermEnabled(requester);

        var command = repositoryFactory.businessTermCommandRepository(requester);
        for (AsbieBusinessTermId asbieBusinessTermId : request.assignedAsbieBizTermIdList()) {
            command.delete(asbieBusinessTermId);
        }
        for (BbieBusinessTermId bbieBusinessTermId : request.assignedBbieBizTermIdList()) {
            command.delete(bbieBusinessTermId);
        }
    }

}
