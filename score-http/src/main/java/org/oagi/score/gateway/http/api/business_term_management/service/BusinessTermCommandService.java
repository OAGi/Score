package org.oagi.score.gateway.http.api.business_term_management.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.*;
import org.oagi.score.gateway.http.api.business_term_management.model.AsbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.oagi.score.gateway.http.common.util.Utility.isValidURI;
import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional
public class BusinessTermCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private ApplicationConfigurationQueryService applicationConfigurationQueryService;

    /**
     * #1752 - H1: enforce the Business Term feature flag server-side. The Angular UI hides the
     * Business Term area when the flag is off, but the REST endpoints must not trust the UI:
     * a direct API call when the feature is disabled is rejected with 403.
     */
    private void assertBusinessTermEnabled(ScoreUser requester) {
        if (!applicationConfigurationQueryService.isBusinessTermEnabled(requester)) {
            throw new DataAccessForbiddenException("Business Term management is not enabled.");
        }
    }

    /**
     * #1752 - M5: validate the JSON create/update input the same way the CSV import already does
     * (name required and ≤255 chars; external reference URI required and well-formed).
     */
    private void validateBusinessTermInput(String businessTerm, String externalReferenceUri) {
        if (!hasLength(businessTerm)) {
            throw new IllegalArgumentException("The business term is required.");
        }
        if (businessTerm.length() > 255) {
            throw new IllegalArgumentException(businessTerm + " is longer than 255 characters limit.");
        }
        if (!hasLength(externalReferenceUri)) {
            throw new IllegalArgumentException("The external reference URI is required.");
        }
        if (!isValidURI(externalReferenceUri)) {
            throw new IllegalArgumentException(externalReferenceUri + " is not a valid URI.");
        }
    }

    public BusinessTermId create(ScoreUser requester, BusinessTermCreateRequest request) {
        assertBusinessTermEnabled(requester);
        validateBusinessTermInput(request.businessTerm(), request.externalReferenceUri());

        var command = repositoryFactory.businessTermCommandRepository(requester);
        return command.create(
                request.businessTerm(),
                request.externalReferenceId(),
                request.externalReferenceUri(),
                request.definition(),
                request.comment());
    }

    public List<BusinessTermId> create(ScoreUser requester, InputStream inputStream) throws IOException {
        assertBusinessTermEnabled(requester);

        var query = repositoryFactory.businessTermQueryRepository(requester);
        try (Reader reader = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"), ',')) {
            String errorMessage;
            List<BusinessTermId> businessTermIdList = new ArrayList<>();
            BusinessTermTemplateParser templateParser = new BusinessTermTemplateParser(reader);
            while (templateParser.hasNext()) {
                errorMessage = null;
                BusinessTermTemplateRecord record = templateParser.next();
                String businessTerm = record.businessTerm();
                if (!hasLength(businessTerm)) {
                    errorMessage = "The business term is required.";
                } else if (businessTerm.length() > 255) {
                    errorMessage = businessTerm + " is longer than 255 characters limit.";
                }

                String externalReferenceUri = record.externalReferenceUri();
                if (!hasLength(externalReferenceUri)) {
                    errorMessage = "The external reference URI is required.";
                } else if (!isValidURI(externalReferenceUri)) {
                    errorMessage = externalReferenceUri + " is not a valid URI.";
                }

                if (errorMessage != null) {
                    throw new IllegalArgumentException("Fail to parse CSV file: " + errorMessage);
                }

                BusinessTermId businessTermId = create(requester, new BusinessTermCreateRequest(
                        businessTerm,
                        record.externalReferenceId(),
                        externalReferenceUri,
                        record.definition(),
                        record.comment()
                ));
                businessTermIdList.add(businessTermId);
            }

            return businessTermIdList;
        }

    }

    private class BusinessTermTemplateParser {

        private static final String BUSINESS_TERM_HEADER_NAME = "businessTerm";
        private static final String EXTERNAL_REFERENCE_URI_HEADER_NAME = "externalReferenceUri";
        private static final String EXTERNAL_REFERENCE_ID_HEADER_NAME = "externalReferenceId";
        private static final String DEFINITION_HEADER_NAME = "definition";
        private static final String COMMENT_HEADER_NAME = "comment";

        private Iterator<CSVRecord> records;

        BusinessTermTemplateParser(Reader reader) throws IOException {
            records = CSVFormat.DEFAULT
                    .builder()
                    .setEscape('\\')
                    .setQuoteMode(QuoteMode.ALL)
                    .setHeader(BUSINESS_TERM_HEADER_NAME,
                            EXTERNAL_REFERENCE_URI_HEADER_NAME,
                            EXTERNAL_REFERENCE_ID_HEADER_NAME,
                            DEFINITION_HEADER_NAME,
                            COMMENT_HEADER_NAME)
                    .setSkipHeaderRecord(true)
                    .get().parse(reader).iterator();
        }

        public boolean hasNext() {
            return records.hasNext();
        }

        public BusinessTermTemplateRecord next() {
            CSVRecord record = records.next();
            return new BusinessTermTemplateRecord(
                    record.get(BUSINESS_TERM_HEADER_NAME),
                    record.get(EXTERNAL_REFERENCE_URI_HEADER_NAME),
                    record.get(EXTERNAL_REFERENCE_ID_HEADER_NAME),
                    record.get(DEFINITION_HEADER_NAME),
                    record.get(COMMENT_HEADER_NAME));
        }
    }

    private record BusinessTermTemplateRecord(
            String businessTerm,
            String externalReferenceUri,
            String externalReferenceId,
            String definition,
            String comment) {
    }

    public boolean update(ScoreUser requester, BusinessTermUpdateRequest request) {
        assertBusinessTermEnabled(requester);
        validateBusinessTermInput(request.businessTerm(), request.externalReferenceUri());

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
