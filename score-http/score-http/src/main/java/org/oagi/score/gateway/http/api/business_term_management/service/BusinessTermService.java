package org.oagi.score.gateway.http.api.business_term_management.service;

import org.jooq.DSLContext;
import org.jooq.tools.csv.CSVReader;
import org.oagi.score.gateway.http.api.business_term_management.data.AssignedBusinessTermListRecord;
import org.oagi.score.gateway.http.api.business_term_management.data.AssignedBusinessTermListRequest;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.BusinessTermRepository;
import org.oagi.score.repo.PaginationResponse;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListRequest;
import org.oagi.score.repo.api.businesscontext.model.GetBusinessContextListResponse;
import org.oagi.score.repo.api.businessterm.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.oagi.score.service.businesscontext.BusinessContextService;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.gateway.http.helper.Utility.isValidURI;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

@Service
@Transactional(readOnly = true)
public class BusinessTermService {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BusinessContextService businessContextService;

    @Autowired
    private ScoreRepositoryFactory scoreRepositoryFactory;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BusinessTermRepository businessTermRepository;

    public GetBusinessTermResponse getBusinessTerm(GetBusinessTermRequest request) {
        GetBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermReadRepository()
                        .getBusinessTerm(request);
        return response;
    }

    public GetBusinessTermListResponse getBusinessTermList(GetBusinessTermListRequest request) {
        GetBusinessTermListResponse response;
        if (request.getAssignedBies() != null && !request.getAssignedBies().isEmpty()) {
            response = scoreRepositoryFactory.createBusinessTermReadRepository()
                    .getBusinessTermListByAssignedBie(request);
        } else {
            response = scoreRepositoryFactory.createBusinessTermReadRepository()
                    .getBusinessTermList(request);
        }
        return response;
    }

    @Transactional
    public CreateBusinessTermResponse createBusinessTerm(CreateBusinessTermRequest request) {
        CreateBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermWriteRepository().createBusinessTerm(request);
        return response;
    }

    private class BusinessTermTemplateParser {

        private static final String BUSINESS_TERM_HEADER_NAME = "businessTerm";
        private static final String EXTERNAL_REFERENCE_URI_HEADER_NAME = "externalReferenceUri";
        private static final String EXTERNAL_REFERENCE_ID_HEADER_NAME = "externalReferenceId";
        private static final String DEFINITION_HEADER_NAME = "definition";
        private static final String COMMENT_HEADER_NAME = "comment";
        private static final int MAX_RECORD_INDEX = 5; // the template should have at most five values for each record.

        private List<String[]> list;
        private int recordIndex;
        private int businessTermHeaderIndex = -1;
        private int externalReferenceUriHeaderIndex = -1;
        private int externalReferenceIdHeaderIndex = -1;
        private int definitionHeaderIndex = -1;
        private int commentHeaderIndex = -1;

        BusinessTermTemplateParser(CSVReader reader) throws IOException {
            list = reader.readAll();
            if (list == null || list.isEmpty()) {
                throw new ScoreDataAccessException("No data in the template.");
            }

            String[] header = list.get(0);
            if (header == null || header.length == 0) {
                throw new ScoreDataAccessException("No header(s) in the template.");
            }

            int headerIndex = 0;
            for (String name : header) {
                switch (name) {
                    case BUSINESS_TERM_HEADER_NAME:
                        businessTermHeaderIndex = headerIndex;
                        break;

                    case EXTERNAL_REFERENCE_URI_HEADER_NAME:
                        externalReferenceUriHeaderIndex = headerIndex;
                        break;

                    case EXTERNAL_REFERENCE_ID_HEADER_NAME:
                        externalReferenceIdHeaderIndex = headerIndex;
                        break;

                    case DEFINITION_HEADER_NAME:
                        definitionHeaderIndex = headerIndex;
                        break;

                    case COMMENT_HEADER_NAME:
                        commentHeaderIndex = headerIndex;
                        break;

                    default:
                        throw new ScoreDataAccessException("Unknown header in the template: " + name);
                }

                headerIndex++;
            }
            recordIndex = 1;
        }

        public boolean hasNext() {
            return recordIndex < list.size();
        }

        public BusinessTermTemplateRecord next() {
            String[] values = list.get(recordIndex);
            BusinessTermTemplateRecord record = new BusinessTermTemplateRecord();
            if (businessTermHeaderIndex >= 0 || businessTermHeaderIndex < MAX_RECORD_INDEX) {
                record.setBusinessTerm(values[businessTermHeaderIndex]);
            }
            if (externalReferenceUriHeaderIndex >= 0 || externalReferenceUriHeaderIndex < MAX_RECORD_INDEX) {
                record.setExternalReferenceUri(values[externalReferenceUriHeaderIndex]);
            }
            if (externalReferenceIdHeaderIndex >= 0 || externalReferenceIdHeaderIndex < MAX_RECORD_INDEX) {
                record.setExternalReferenceId(values[externalReferenceIdHeaderIndex]);
            }
            if (definitionHeaderIndex >= 0 || definitionHeaderIndex < MAX_RECORD_INDEX) {
                record.setDefinition(values[definitionHeaderIndex]);
            }
            if (commentHeaderIndex >= 0 || commentHeaderIndex < MAX_RECORD_INDEX) {
                record.setComment(values[commentHeaderIndex]);
            }
            recordIndex++;
            return record;
        }

    }

    private class BusinessTermTemplateRecord {

        private String businessTerm;

        private String externalReferenceUri;

        private String externalReferenceId;

        private String definition;

        private String comment;

        public String getBusinessTerm() {
            return businessTerm;
        }

        public void setBusinessTerm(String businessTerm) {
            this.businessTerm = businessTerm;
        }

        public String getExternalReferenceUri() {
            return externalReferenceUri;
        }

        public void setExternalReferenceUri(String externalReferenceUri) {
            this.externalReferenceUri = externalReferenceUri;
        }

        public String getExternalReferenceId() {
            return externalReferenceId;
        }

        public void setExternalReferenceId(String externalReferenceId) {
            this.externalReferenceId = externalReferenceId;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    @Transactional
    public CreateBulkBusinessTermResponse createBusinessTermsFromFile(CreateBulkBusinessTermRequest request)
            throws ScoreDataAccessException {
        List<String> formatCheckExceptions = new ArrayList<>();
        try (CSVReader reader = new CSVReader(
                new BufferedReader(
                        new InputStreamReader(request.getInputStream(), "UTF-8"), ','))) {
            String errorMessage;
            List<BusinessTerm> businessTerms = new ArrayList<>();
            BusinessTermTemplateParser templateParser = new BusinessTermTemplateParser(reader);
            while (templateParser.hasNext()) {
                errorMessage = null;
                BusinessTermTemplateRecord record = templateParser.next();
                BusinessTerm term = new BusinessTerm();
                String businessTerm = record.getBusinessTerm();
                if (!hasLength(businessTerm)) {
                    errorMessage = "The business term is required.";
                } else if (businessTerm.length() > 255) {
                    errorMessage = businessTerm + " is longer than 255 characters limit.";
                } else {
                    term.setBusinessTerm(businessTerm);
                }

                String externalReferenceUri = record.getExternalReferenceUri();
                if (!hasLength(externalReferenceUri)) {
                    errorMessage = "The external reference URI is required.";
                } else if (!isValidURI(externalReferenceUri)) {
                    errorMessage = externalReferenceUri + " is not a valid URI.";
                } else {
                    term.setExternalReferenceUri(externalReferenceUri);
                }

                term.setExternalReferenceId(record.getExternalReferenceId());
                term.setDefinition(record.getDefinition());
                term.setComment(record.getComment());

                if (errorMessage == null && checkBusinessTermUniqueness(term)) {
                    businessTerms.add(term);
                } else {
                    formatCheckExceptions.add(errorMessage);
                }
            }

            request.setBusinessTermList(businessTerms);
        } catch (IOException e) {
            throw new ScoreDataAccessException("Fail to parse CSV file: " + e.getMessage());
        }

        CreateBulkBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermWriteRepository()
                        .createBusinessTermsFromFile(request);
        response.setFormatCheckExceptions(formatCheckExceptions);
        return response;
    }

    @Transactional
    public UpdateBusinessTermResponse updateBusinessTerm(UpdateBusinessTermRequest request) {
        UpdateBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermWriteRepository()
                        .updateBusinessTerm(request);

        return response;
    }

    @Transactional
    public DeleteBusinessTermResponse deleteBusinessTerm(DeleteBusinessTermRequest request) throws ScoreDataAccessException {
        DeleteBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermWriteRepository()
                        .deleteBusinessTerm(request);
        return response;
    }

    public AssignedBusinessTerm getBusinessTermAssignment(GetAssignedBusinessTermRequest request) {
        AssignedBusinessTerm response = businessTermRepository.getBusinessTermAssignment(request);
        return response;
    }

    public PageResponse<AssignedBusinessTermListRecord> getBusinessTermAssignmentList(AuthenticatedPrincipal user, AssignedBusinessTermListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        AppUser requester = sessionService.getAppUserByUsername(user);

        PaginationResponse<AssignedBusinessTermListRecord> result = businessTermRepository
                .getBieBiztermList(request, AssignedBusinessTermListRecord.class);

        List<AssignedBusinessTermListRecord> assignedBtList = result.getResult();
        assignedBtList.forEach(assignedBt -> {

            GetBusinessContextListRequest getBusinessContextListRequest =
                    new GetBusinessContextListRequest(authenticationService.asScoreUser(user))
                            .withName(request.getBusinessContext());

            getBusinessContextListRequest.setPageIndex(-1);
            getBusinessContextListRequest.setPageSize(-1);

            GetBusinessContextListResponse getBusinessContextListResponse = businessContextService
                    .getBusinessContextList(getBusinessContextListRequest, false);

            assignedBt.setBusinessContexts(getBusinessContextListResponse.getResults());
            assignedBt.setPrimary(assignedBt.isPrimary());
        });

        PageResponse<AssignedBusinessTermListRecord> response = new PageResponse();
        response.setList(assignedBtList);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(result.getPageCount());
        return response;
    }

    @Transactional
    public AssignBusinessTermResponse assignBusinessTerm(AssignBusinessTermRequest request) {
        AssignBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermAssignmentWriteRepository()
                        .assignBusinessTerm(request);
        return response;
    }

    @Transactional
    public UpdateBusinessTermAssignmentResponse updateBusinessTermAssignment(UpdateBusinessTermAssignmentRequest request) {
        UpdateBusinessTermAssignmentResponse response =
                scoreRepositoryFactory.createBusinessTermAssignmentWriteRepository()
                        .updateBusinessTermAssignment(request);
        return response;
    }

    @Transactional
    public DeleteAssignedBusinessTermResponse deleteBusinessTermAssignment(DeleteAssignedBusinessTermRequest request) {
        DeleteAssignedBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermAssignmentWriteRepository()
                        .deleteBusinessTermAssignment(request);
        return response;
    }

    @Transactional
    public boolean checkAssignmentUniqueness(AssignBusinessTermRequest assignBusinessTermRequest) {
        return businessTermRepository.checkAssignmentUniqueness(assignBusinessTermRequest);
    }

    public boolean checkBusinessTermUniqueness(BusinessTerm businessTerm) {
        return businessTermRepository.checkBusinessTermUniqueness(businessTerm);
    }

    public boolean checkBusinessTermNameUniqueness(BusinessTerm businessTerm) {
        return businessTermRepository.checkBusinessTermNameUniqueness(businessTerm);
    }
}
