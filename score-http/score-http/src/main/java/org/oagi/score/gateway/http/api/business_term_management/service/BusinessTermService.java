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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.gateway.http.helper.Utility.isValidURI;

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

    @Transactional
    public CreateBulkBusinessTermResponse createBusinessTermsFromFile(CreateBulkBusinessTermRequest request)
            throws ScoreDataAccessException {
        List<String> formatCheckExceptions = new ArrayList<>();
        try (CSVReader reader = new CSVReader(
                new BufferedReader(
                        new InputStreamReader(request.getInputStream(), "UTF-8"), ','))) {
            List<BusinessTerm> businessTerms = new ArrayList<BusinessTerm>();
            List<String[]> list = reader.readAll();
            list.remove(0); // remove header with column names
            for (String[] recordStr : list) {
                BusinessTerm term = new BusinessTerm();
                if (recordStr[0].length() > 255) {
                    formatCheckExceptions.add(recordStr[0] + " is longer than 255 characters limit.");
                }
                term.setBusinessTerm(recordStr[0]);
                if (!isValidURI(recordStr[1])) {
                    formatCheckExceptions.add(recordStr[1] + " is not a valid URI.");
                }
                term.setExternalReferenceUri(recordStr[1]);
                term.setExternalReferenceId(recordStr[2]);
                term.setDefinition(recordStr[3]);
                term.setComment(recordStr[4]);
                if (formatCheckExceptions.isEmpty() && term.getExternalReferenceUri() != null && !term.getExternalReferenceUri().equals("")
                        && checkBusinessTermUniqueness(term)) {
                    businessTerms.add(term);
                }
            }
            request.setBusinessTermList(businessTerms);
        } catch (IOException e) {
            throw new ScoreDataAccessException("Fail to parse CSV file: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new ScoreDataAccessException("Fail to parse CSV file: " + e.getMessage());
        }
        if (!formatCheckExceptions.isEmpty()) {
            throw new ScoreDataAccessException("Fail to parse CSV file: " + String.join(" and ", formatCheckExceptions));
        }

        CreateBulkBusinessTermResponse response =
                scoreRepositoryFactory.createBusinessTermWriteRepository()
                        .createBusinessTermsFromFile(request);
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
