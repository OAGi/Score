package org.oagi.score.gateway.http.api.oas_management.service;

import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.business_context.service.BusinessContextQueryService;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocQueryRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.*;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.tables.OasDoc.OAS_DOC;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

@Service
@Transactional(readOnly = true)
public class OpenAPIDocService {

    private static final int OPEN_API_VERSION_MAX_LENGTH = OAS_DOC.OPEN_API_VERSION.getDataType().length();
    private static final int TERMS_OF_SERVICE_MAX_LENGTH = OAS_DOC.TERMS_OF_SERVICE.getDataType().length();
    private static final int VERSION_MAX_LENGTH = OAS_DOC.VERSION.getDataType().length();
    private static final int CONTACT_URL_MAX_LENGTH = OAS_DOC.CONTACT_URL.getDataType().length();
    private static final int LICENSE_NAME_MAX_LENGTH = OAS_DOC.LICENSE_NAME.getDataType().length();
    private static final int LICENSE_URL_MAX_LENGTH = OAS_DOC.LICENSE_URL.getDataType().length();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RepositoryFactory repositoryFactory;

    private OasDocCommandRepository command(ScoreUser requester) {
        return repositoryFactory.oasDocCommandRepository(requester);
    };

    private OasDocQueryRepository query(ScoreUser requester) {
        return repositoryFactory.oasDocQueryRepository(requester);
    };

    private BieForOasDocCommandRepository bieForOasDocCommand(ScoreUser requester) {
        return repositoryFactory.bieForOasDocCommandRepository(requester);
    }

    private BieForOasDocQueryRepository bieForOasDocQuery(ScoreUser requester) {
        return repositoryFactory.bieForOasDocQueryRepository(requester);
    }

    @Autowired
    private BusinessContextQueryService businessContextQueryService;

    public GetOasDocResponse getOasDoc(ScoreUser requester, GetOasDocRequest request) {
        GetOasDocResponse response = query(requester).getOasDoc(request);
        return response;
    }

    public GetOasDocListResponse getOasDocList(ScoreUser requester, GetOasDocListRequest request) {
        GetOasDocListResponse response = query(requester).getOasDocList(request);
        return response;
    }

    @Transactional
    public CreateOasDocResponse createOasDoc(ScoreUser requester, CreateOasDocRequest request) {
        validateOasDocRequest(
                request.getOpenAPIVersion(),
                request.getTermsOfService(),
                request.getVersion(),
                request.getContactUrl(),
                request.getLicenseName(),
                request.getLicenseUrl());
        CreateOasDocResponse response = command(requester).createOasDoc(request);
        return response;
    }

    @Transactional
    public UpdateOasDocResponse updateOasDoc(ScoreUser requester, UpdateOasDocRequest request) {
        validateOasDocRequest(
                request.getOpenAPIVersion(),
                request.getTermsOfService(),
                request.getVersion(),
                request.getContactUrl(),
                request.getLicenseName(),
                request.getLicenseUrl());
        UpdateOasDocResponse response = command(requester).updateOasDoc(request);
        return response;
    }

    @Transactional
    public DeleteOasDocResponse deleteOasDoc(ScoreUser requester, DeleteOasDocRequest request) {
        DeleteOasDocResponse response = command(requester).deleteOasDoc(request);
        return response;
    }

    public GetBieForOasDocResponse getBieForOasDoc(ScoreUser requester, GetBieForOasDocRequest request) {
        GetBieForOasDocResponse response = bieForOasDocQuery(requester).getBieForOasDoc(request);
        return response;
    }

    public PageResponse<BieForOasDoc> selectBieForOasDoc(ScoreUser requester, BieForOasDocListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        PageResponse<BieForOasDoc> result = new SelectBieForOasDocListArguments(
                repositoryFactory.oasDocQueryRepository(requester))
                .setOasDocId(request.getOasDocId())
                .setDen(request.getDen())
                .setPropertyTerm(request.getPropertyTerm())
                .setBusinessContext(request.getBusinessContext())
                .setVersion(request.getVersion())
                .setRemark(request.getRemark())
                .setAsccpManifestId(request.getAsccpManifestId())
                .setExcludePropertyTerms(request.getExcludePropertyTerms())
                .setExcludeTopLevelAsbiepIds(request.getExcludeTopLevelAsbiepIds())
                .setStates(request.getStates())
                .setLibraryId(request.getLibraryId())
                .setReleaseId(request.getReleaseId())
                .setOwnerLoginIdList(request.getOwnerLoginIdList())
                .setUpdaterLoginIdList(request.getUpdaterLoginIdList())
                .setUpdateDate(request.getUpdateStartDate(), request.getUpdateEndDate())
                .setAccess(ULong.valueOf(requester.userId().value()), request.getAccess())
                .setOwnedByDeveloper(request.getOwnedByDeveloper())
                .setSort((pageRequest.sorts().isEmpty()) ? null : pageRequest.sorts().get(0).field(),
                        (pageRequest.sorts().isEmpty()) ? null : pageRequest.sorts().get(0).direction().name())
                .setOffset(pageRequest.pageOffset(), pageRequest.pageSize())
                .fetch();

        List<BieForOasDoc> bieForOasDocList = result.getList();
        bieForOasDocList.forEach(bieForOasDoc -> {
            bieForOasDoc.setBusinessContexts(
                    businessContextQueryService.getBusinessContextSummaryList(
                            requester, bieForOasDoc.getTopLevelAsbiepId(), request.getBusinessContext())
            );
        });

        PageResponse<BieForOasDoc> response = new PageResponse();
        response.setList(bieForOasDocList);
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(result.getLength());
        return response;
    }

    @Transactional
    public AddBieForOasDocResponse addBieForOasDoc(ScoreUser requester, AddBieForOasDocRequest request) {
        UserId userId = requester.userId();
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }
        if (request.getOasDocId() == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }
        if (request.getTopLevelAsbiepId() == null) {
            throw new IllegalArgumentException("`TopLevelAsbiepId` parameter must not be null.");
        }

        long millis = System.currentTimeMillis();

        var command = repositoryFactory.oasDocCommandRepository(requester);

        OasMessageBodyId oasMessageBodyId = new InsertOasMessageBodyArguments(command)
                .setUserId(userId)
                .setTopLevelAsbiepId(request.getTopLevelAsbiepId())
                .setTimestamp(millis)
                .execute();

        OasResourceId oasResourceId = new InsertOasResourceArguments(command)
                .setUserId(userId)
                .setOasDocId(request.getOasDocId())
                .setPath(request.getPath())
                .setRef(request.getRef())
                .setTimestamp(millis)
                .execute();

        OasOperationId oasOperationId = new InsertOasOperationArguments(command)
                .setUserId(userId)
                .setOperationId(request.getOperationId())
                .setOasResourceId(oasResourceId)
                .setVerb(request.getVerb())
                .setSummary(request.getSummary())
                .setDescription(request.getDescriptionForOperation())
                .setDeprecated(request.isDeprecatedForOperation())
                .setTimestamp(millis)
                .execute();

        if (request.getTagName() != null) {
            OasTagId oasTagId = new InsertOasTagArguments(command)
                    .setUserId(userId)
                    .setGuid(randomGuid())
                    .setName(request.getTagName())
                    .execute();
            new InsertOasResourceTagArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasTagId(oasTagId)
                    .execute();
        }
        OasRequestId oasRequestId = null;
        OasResponseId oasResponseId = null;
        if (request.isOasRequest()) {
            oasRequestId = new InsertOasRequestArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setMakeArrayIndicator(request.isMakeArrayIndicator())
                    .setSuppressRootIndicator(request.isSuppressRootIndicator())
                    .setIncludePaginationIndicator(request.isIncludePaginationIndicator())
                    .setIncludeMetaHeaderIndicator(request.isIncludeMetaHeaderIndicator())
                    .setRequired(request.isRequiredForRequestBody())
                    .setTimestamp(millis)
                    .execute();
        } else {
            oasResponseId = new InsertOasResponseArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setMakeArrayIndicator(request.isMakeArrayIndicator())
                    .setSuppressRootIndicator(request.isSuppressRootIndicator())
                    .setIncludePaginationIndicator(request.isIncludePaginationIndicator())
                    .setIncludeMetaHeaderIndicator(request.isIncludeMetaHeaderIndicator())
                    .setTimestamp(millis)
                    .execute();
        }
        return new AddBieForOasDocResponse(oasRequestId != null ? oasRequestId : null,
                oasResponseId != null ? oasResponseId : null);
    }

    /**
     * Issue #1730: Adds an API operation (endpoint) that does NOT reference a BIE.
     *
     * A BIE-less message body (top_level_asbiep_id = NULL) is created and linked to either an
     * {@code oas_request} (Request-type message body, no schema) or an {@code oas_response}
     * (Response-type, carrying the chosen HTTP status code such as 202/204) without any content
     * schema, so the OpenAPI generator can emit a bodyless operation.
     */
    @Transactional
    public AddBieForOasDocResponse addOperationForOasDoc(ScoreUser requester, AddOperationForOasDocRequest request) {
        UserId userId = requester.userId();
        if (userId == null) {
            throw new IllegalArgumentException("`userId` parameter must not be null.");
        }
        if (request.getOasDocId() == null) {
            throw new IllegalArgumentException("`oasDocId` parameter must not be null.");
        }
        if (request.getVerb() == null) {
            throw new IllegalArgumentException("`verb` parameter must not be null.");
        }

        long millis = System.currentTimeMillis();

        var command = repositoryFactory.oasDocCommandRepository(requester);

        // Issue #1730: BIE-less endpoint -> empty message body (top_level_asbiep_id = NULL).
        OasMessageBodyId oasMessageBodyId = new InsertOasMessageBodyArguments(command)
                .setUserId(userId)
                .setTopLevelAsbiepId(null)
                .setTimestamp(millis)
                .execute();

        OasResourceId oasResourceId = new InsertOasResourceArguments(command)
                .setUserId(userId)
                .setOasDocId(request.getOasDocId())
                .setPath(request.getPath())
                .setRef(request.getRef())
                .setTimestamp(millis)
                .execute();

        OasOperationId oasOperationId = new InsertOasOperationArguments(command)
                .setUserId(userId)
                .setOperationId(request.getOperationId())
                .setOasResourceId(oasResourceId)
                .setVerb(request.getVerb())
                .setSummary(request.getSummary())
                .setDescription(request.getDescription())
                .setDeprecated(false)
                .setTimestamp(millis)
                .execute();

        if (request.getTagName() != null) {
            OasTagId oasTagId = new InsertOasTagArguments(command)
                    .setUserId(userId)
                    .setGuid(randomGuid())
                    .setName(request.getTagName())
                    .execute();
            new InsertOasResourceTagArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasTagId(oasTagId)
                    .execute();
        }

        OasRequestId oasRequestId = null;
        OasResponseId oasResponseId = null;
        if (request.isOasRequest()) {
            // Request-type message body: no BIE schema and no status code; the generator omits
            // (empties) the requestBody.
            oasRequestId = new InsertOasRequestArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setMakeArrayIndicator(false)
                    // Issue #1730: Suppress Root defaults to checked for added (BIE-less) operations.
                    .setSuppressRootIndicator(true)
                    .setIncludePaginationIndicator(false)
                    .setIncludeMetaHeaderIndicator(false)
                    .setRequired(false)
                    .setTimestamp(millis)
                    .execute();
        } else {
            // Response-type message body: carries the HTTP status code (e.g. 202/204), no content schema.
            String httpStatusCode = (request.getHttpStatusCode() != null)
                    ? String.valueOf(request.getHttpStatusCode()) : null;
            oasResponseId = new InsertOasResponseArguments(command)
                    .setUserId(userId)
                    .setOasOperationId(oasOperationId)
                    .setOasMessageBodyId(oasMessageBodyId)
                    .setDescription(request.getDescription())
                    .setHttpStatusCode(httpStatusCode)
                    .setMakeArrayIndicator(false)
                    // Issue #1730: Suppress Root defaults to checked for added (BIE-less) operations.
                    .setSuppressRootIndicator(true)
                    .setIncludePaginationIndicator(false)
                    .setIncludeMetaHeaderIndicator(false)
                    .setTimestamp(millis)
                    .execute();
        }

        return new AddBieForOasDocResponse(oasRequestId, oasResponseId);
    }

    private void validateOasDocRequest(String openApiVersion,
                                       String termsOfService,
                                       String version,
                                       String contactUrl,
                                       String licenseName,
                                       String licenseUrl) {
        validateMaxLength("openAPIVersion", openApiVersion, OPEN_API_VERSION_MAX_LENGTH);
        validateMaxLength("termsOfService", termsOfService, TERMS_OF_SERVICE_MAX_LENGTH);
        validateMaxLength("version", version, VERSION_MAX_LENGTH);
        validateMaxLength("contactUrl", contactUrl, CONTACT_URL_MAX_LENGTH);
        validateMaxLength("licenseName", licenseName, LICENSE_NAME_MAX_LENGTH);
        validateMaxLength("licenseUrl", licenseUrl, LICENSE_URL_MAX_LENGTH);
    }

    private void validateMaxLength(String fieldName, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    String.format("`%s` must not exceed %d characters.", fieldName, maxLength));
        }
    }

    @Transactional
    public DeleteBieForOasDocResponse deleteBieForOasDoc(ScoreUser requester, DeleteBieForOasDocRequest request) {
        DeleteBieForOasDocResponse response =
                bieForOasDocCommand(requester).deleteBieForOasDoc(request);
        return response;

    }

    public boolean checkOasDocUniqueness(ScoreUser requester, OasDoc oasDoc) {
        var query = repositoryFactory.oasDocQueryRepository(requester);
        return query.checkOasDocUniqueness(oasDoc);
    }

    public boolean checkOasDocTitleUniqueness(ScoreUser requester, OasDoc oasDoc) {
        var query = repositoryFactory.oasDocQueryRepository(requester);
        return query.checkOasDocTitleUniqueness(oasDoc);
    }

    @Transactional
    public UpdateBieForOasDocResponse updateDetails(ScoreUser requester, UpdateBieForOasDocRequest request) {

        UpdateBieForOasDocResponse response =
                bieForOasDocCommand(requester).updateBieForOasDoc(request);
        return response;
    }

    @Transactional
    public GetOasOperationResponse getOasOperation(ScoreUser requester, GetOasOperationRequest request) {
        GetOasOperationResponse response = query(requester).getOasOperation(request);
        return response;
    }

    @Transactional
    public GetOasRequestTableResponse getOasRequestTable(ScoreUser requester, GetOasRequestTableRequest request) {
        GetOasRequestTableResponse response = query(requester).getOasRequestTable(request);
        return response;
    }

    @Transactional
    public GetOasResponseTableResponse getOasResponseTable(ScoreUser requester, GetOasResponseTableRequest request) {
        GetOasResponseTableResponse response = query(requester).getOasResponseTable(request);
        return response;
    }

    @Transactional
    public GetAssignedOasTagResponse getAssignedOasTag(ScoreUser requester, GetAssignedOasTagRequest request) {
        GetAssignedOasTagResponse response = bieForOasDocQuery(requester).getAssignedOasTag(request);
        return response;
    }
}
