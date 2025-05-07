package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.*;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocCommandRepository;
import org.oagi.score.gateway.http.api.oas_management.repository.criteria.*;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasDocRecord;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.model.ScoreRole.END_USER;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

public class JooqOasDocCommandRepository extends JooqBaseRepository
        implements OasDocCommandRepository {

    public JooqOasDocCommandRepository(DSLContext dslContext,
                                       ScoreUser requester,
                                       RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateOasDocResponse createOasDoc(
            CreateOasDocRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        OasDocRecord record = new OasDocRecord();
        record.setGuid(randomGuid());
        record.setOpenApiVersion(request.getOpenAPIVersion());
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setTermsOfService(request.getTermsOfService());
        record.setVersion(request.getVersion());
        record.setContactName(request.getContactName());
        record.setContactUrl(request.getContactUrl());
        record.setContactEmail(request.getContactEmail());
        record.setLicenseName(request.getLicenseName());
        record.setLicenseUrl(request.getLicenseUrl());
        record.setOwnerUserId(requesterId);
        record.setCreatedBy(requesterId);
        record.setLastUpdatedBy(requesterId);
        record.setCreationTimestamp(timestamp);
        record.setLastUpdateTimestamp(timestamp);

        ULong oasDocId = dslContext().insertInto(OAS_DOC)
                .set(record)
                .returning(OAS_DOC.OAS_DOC_ID)
                .fetchOne().getOasDocId();
        return new CreateOasDocResponse(oasDocId.toBigInteger());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public UpdateOasDocResponse updateOasDoc(
            UpdateOasDocRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        ULong requesterId = ULong.valueOf(requester.userId().value());
        LocalDateTime timestamp = LocalDateTime.now();

        OasDocRecord record = dslContext().selectFrom(OAS_DOC)
                .where(OAS_DOC.OAS_DOC_ID.eq(ULong.valueOf(request.getOasDocId())))
                .fetchOptional().orElse(null);
        if (record == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        List<Field<?>> changedField = new ArrayList();
        if (!StringUtils.equals(request.getTitle(), record.getTitle())) {
            record.setTitle(request.getTitle());
            changedField.add(OAS_DOC.TITLE);
        }
        if (request.getDescription() != null && !StringUtils.equals(request.getDescription(), record.getDescription())) {
            record.setDescription(request.getDescription());
            changedField.add(OAS_DOC.DESCRIPTION);
        }
        if (!StringUtils.equals(request.getVersion(), record.getVersion())) {
            record.setVersion(request.getVersion());
            changedField.add(OAS_DOC.VERSION);
        }
        if (!StringUtils.equals(request.getOpenAPIVersion(), record.getOpenApiVersion())) {
            record.setOpenApiVersion(request.getOpenAPIVersion());
            changedField.add(OAS_DOC.OPEN_API_VERSION);
        }
        if (!StringUtils.equals(request.getTermsOfService(), record.getTermsOfService())) {
            record.setTermsOfService(request.getTermsOfService());
            changedField.add(OAS_DOC.TERMS_OF_SERVICE);
        }
        if (!StringUtils.equals(request.getLicenseName(), record.getLicenseName())) {
            record.setLicenseName(request.getLicenseName());
            changedField.add(OAS_DOC.LICENSE_NAME);
        }
        if (request.getLicenseUrl() != null && !StringUtils.equals(request.getLicenseUrl(), record.getLicenseUrl())) {
            record.setLicenseUrl(request.getLicenseUrl());
            changedField.add(OAS_DOC.LICENSE_URL);
        }
        if (request.getContactName() != null && !StringUtils.equals(request.getContactName(), record.getContactName())) {
            record.setContactName(request.getContactName());
            changedField.add(OAS_DOC.CONTACT_NAME);
        }
        if (request.getContactUrl() != null && !StringUtils.equals(request.getContactUrl(), record.getContactUrl())) {
            record.setContactUrl(request.getContactUrl());
            changedField.add(OAS_DOC.CONTACT_URL);
        }
        if (request.getContactEmail() != null && !StringUtils.equals(request.getContactEmail(), record.getContactEmail())) {
            record.setContactEmail(request.getContactEmail());
            changedField.add(OAS_DOC.CONTACT_EMAIL);
        }
        if (!changedField.isEmpty()) {
            record.setLastUpdatedBy(requesterId);
            changedField.add(OAS_DOC.LAST_UPDATED_BY);

            record.setLastUpdateTimestamp(timestamp);
            changedField.add(OAS_DOC.LAST_UPDATE_TIMESTAMP);

            int affectedRows = record.update(changedField);
            if (affectedRows != 1) {
                throw new ScoreDataAccessException(new IllegalStateException());
            }
        }
        return new UpdateOasDocResponse(
                record.getOasDocId().toBigInteger(),
                !changedField.isEmpty());
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public DeleteOasDocResponse deleteOasDoc(
            DeleteOasDocRequest request) throws ScoreDataAccessException {

        List<BigInteger> oasDocIdList = request.getOasDocIdList();
        if (oasDocIdList == null || oasDocIdList.isEmpty()) {
            return new DeleteOasDocResponse(Collections.emptyList());
        }

        List<ULong> oasResourceIds = dslContext().select(OAS_RESOURCE.OAS_RESOURCE_ID)
                .from(OAS_RESOURCE)
                .where(
                        oasDocIdList.size() == 1 ?
                                OAS_RESOURCE.OAS_DOC_ID.eq(ULong.valueOf(oasDocIdList.get(0))) :
                                OAS_RESOURCE.OAS_DOC_ID.in(oasDocIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                ).fetchInto(ULong.class);

        if (!oasResourceIds.isEmpty()) {
            List<ULong> oasOperationIds = dslContext().select(OAS_OPERATION.OAS_OPERATION_ID)
                    .from(OAS_OPERATION)
                    .where(
                            oasResourceIds.size() == 1 ?
                                    OAS_OPERATION.OAS_RESOURCE_ID.eq(oasResourceIds.get(0)) :
                                    OAS_OPERATION.OAS_RESOURCE_ID.in(oasResourceIds)
                    ).fetchInto(ULong.class);

            if (!oasOperationIds.isEmpty()) {
                deleteOasTagByOperationIdList(oasOperationIds);
                deleteOasRequestByOperationIdList(oasOperationIds);
                deleteOasResponseByOperationIdList(oasOperationIds);

                dslContext().delete(OAS_OPERATION)
                        .where(
                                oasOperationIds.size() == 1 ?
                                        OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationIds.get(0)) :
                                        OAS_OPERATION.OAS_OPERATION_ID.in(oasOperationIds)
                        ).execute();
            }

            dslContext().delete(OAS_RESOURCE)
                    .where(
                            oasResourceIds.size() == 1 ?
                                    OAS_RESOURCE.OAS_RESOURCE_ID.eq(oasResourceIds.get(0)) :
                                    OAS_RESOURCE.OAS_RESOURCE_ID.in(oasResourceIds)
                    ).execute();
        }

        try {
            dslContext().delete(OAS_DOC)
                    .where(
                            oasDocIdList.size() == 1 ?
                                    OAS_DOC.OAS_DOC_ID.eq(ULong.valueOf(oasDocIdList.get(0))) :
                                    OAS_DOC.OAS_DOC_ID.in(oasDocIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList()))
                    )
                    .execute();
        } catch (Exception e) {
            throw new ScoreDataAccessException("It's not possible to delete the used oas doc.", e);
        }

        DeleteOasDocResponse response = new DeleteOasDocResponse(oasDocIdList);
        return response;
    }

    private void deleteOasTagByOperationIdList(List<ULong> oasOperationIdList) {
        List<ULong> oasTagIds = dslContext().select(OAS_RESOURCE_TAG.OAS_TAG_ID)
                .from(OAS_RESOURCE_TAG)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.in(oasOperationIdList)
                ).fetchInto(ULong.class);

        dslContext().deleteFrom(OAS_RESOURCE_TAG)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_RESOURCE_TAG.OAS_OPERATION_ID.in(oasOperationIdList)
                ).execute();

        if (!oasTagIds.isEmpty()) {
            dslContext().deleteFrom(OAS_TAG)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_TAG.OAS_TAG_ID.eq(oasTagIds.get(0)) :
                                    OAS_TAG.OAS_TAG_ID.in(oasTagIds)
                    ).execute();
        }
    }

    private void deleteOasRequestByOperationIdList(List<ULong> oasOperationIdList) {
        List<ULong> oasRequestIds = dslContext().select(OAS_REQUEST.OAS_REQUEST_ID)
                .from(OAS_REQUEST)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_REQUEST.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_REQUEST.OAS_OPERATION_ID.in(oasOperationIdList)
                )
                .fetchInto(ULong.class);

        if (!oasRequestIds.isEmpty()) {
            dslContext().deleteFrom(OAS_REQUEST_PARAMETER)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.eq(oasRequestIds.get(0)) :
                                    OAS_REQUEST_PARAMETER.OAS_REQUEST_ID.in(oasRequestIds)
                    )
                    .execute();

            dslContext().deleteFrom(OAS_REQUEST)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_REQUEST.OAS_REQUEST_ID.eq(oasRequestIds.get(0)) :
                                    OAS_REQUEST.OAS_REQUEST_ID.in(oasRequestIds)
                    )
                    .execute();
        }
    }

    private void deleteOasResponseByOperationIdList(List<ULong> oasOperationIdList) {
        List<ULong> oasResponseIds = dslContext().select(OAS_RESPONSE.OAS_RESPONSE_ID)
                .from(OAS_RESPONSE)
                .where(
                        oasOperationIdList.size() == 1 ?
                                OAS_RESPONSE.OAS_OPERATION_ID.eq(oasOperationIdList.get(0)) :
                                OAS_RESPONSE.OAS_OPERATION_ID.in(oasOperationIdList)
                )
                .fetchInto(ULong.class);

        if (!oasResponseIds.isEmpty()) {
            dslContext().deleteFrom(OAS_RESPONSE_HEADERS)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.eq(oasResponseIds.get(0)) :
                                    OAS_RESPONSE_HEADERS.OAS_RESPONSE_ID.in(oasResponseIds)
                    )
                    .execute();

            dslContext().deleteFrom(OAS_RESPONSE)
                    .where(
                            oasOperationIdList.size() == 1 ?
                                    OAS_RESPONSE.OAS_RESPONSE_ID.eq(oasResponseIds.get(0)) :
                                    OAS_RESPONSE.OAS_RESPONSE_ID.in(oasResponseIds)
                    )
                    .execute();
        }
    }

    @Override
    public ULong insertOasMessageBody(InsertOasMessageBodyArguments arguments) {
        return dslContext().insertInto(OAS_MESSAGE_BODY)
                .set(OAS_MESSAGE_BODY.CREATED_BY, arguments.getUserId())
                .set(OAS_MESSAGE_BODY.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_MESSAGE_BODY.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                .returningResult(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                .fetchOne().value1();
    }

    @Override
    public ULong insertOasResource(InsertOasResourceArguments arguments) {
        return dslContext().insertInto(OAS_RESOURCE)
                .set(OAS_RESOURCE.CREATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE.OAS_DOC_ID, arguments.getOasDocId())
                .set(OAS_RESOURCE.PATH, arguments.getPath())
                .set(OAS_RESOURCE.REF, arguments.getRef())
                .returningResult(OAS_RESOURCE.OAS_RESOURCE_ID)
                .fetchOne().value1();
    }

    @Override
    public ULong insertOasOperation(InsertOasOperationArguments arguments) {
        return dslContext().insertInto(OAS_OPERATION)
                .set(OAS_OPERATION.CREATED_BY, arguments.getUserId())
                .set(OAS_OPERATION.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_OPERATION.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_OPERATION.OAS_RESOURCE_ID, arguments.getOasResourceId())
                .set(OAS_OPERATION.VERB, arguments.getVerb())
                .set(OAS_OPERATION.OPERATION_ID, arguments.getOperationId())
                .set(OAS_OPERATION.SUMMARY, arguments.getSummary())
                .set(OAS_OPERATION.DESCRIPTION, arguments.getDescription())
                .set(OAS_OPERATION.DEPRECATED, (byte) (arguments.isDeprecated() ? 1 : 0))
                .returningResult(OAS_OPERATION.OAS_OPERATION_ID)
                .fetchOne().value1();
    }

    @Override
    public ULong insertOasTag(InsertOasTagArguments arguments) {
        return dslContext().insertInto(OAS_TAG)
                .set(OAS_TAG.CREATED_BY, arguments.getUserId())
                .set(OAS_TAG.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_TAG.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_TAG.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_TAG.NAME, arguments.getName())
                .set(OAS_TAG.DESCRIPTION, arguments.getDescription())
                .set(OAS_TAG.GUID, arguments.getGuid())
                .returningResult(OAS_TAG.OAS_TAG_ID)
                .fetchOne().value1();
    }

    @Override
    public ULong insertOasResourceTag(InsertOasResourceTagArguments arguments) {
        return dslContext().insertInto(OAS_RESOURCE_TAG)
                .set(OAS_RESOURCE_TAG.CREATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE_TAG.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_RESOURCE_TAG.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE_TAG.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESOURCE_TAG.OAS_OPERATION_ID, arguments.getOasOperationId())
                .set(OAS_RESOURCE_TAG.OAS_TAG_ID, arguments.getOasTagId())
                .returningResult(OAS_RESOURCE_TAG.OAS_TAG_ID)
                .fetchOne().value1();
    }

    @Override
    public ULong insertOasRequest(InsertOasRequestArguments arguments) {
        return dslContext().insertInto(OAS_REQUEST)
                .set(OAS_REQUEST.CREATED_BY, arguments.getUserId())
                .set(OAS_REQUEST.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_REQUEST.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_REQUEST.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_REQUEST.OAS_MESSAGE_BODY_ID, arguments.getOasMessageBodyId())
                .set(OAS_REQUEST.OAS_OPERATION_ID, arguments.getOasOperationId())
                .set(OAS_REQUEST.DESCRIPTION, arguments.getDescription())
                .set(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR, (byte) (arguments.isSuppressRootIndicator() ? 1 : 0))
                .set(OAS_REQUEST.MAKE_ARRAY_INDICATOR, (byte) (arguments.isMakeArrayIndicator() ? 1 : 0))
                .set(OAS_REQUEST.IS_CALLBACK, (byte) 0)
                .set(OAS_REQUEST.REQUIRED, (byte) (arguments.isRequired() ? 1 : 0))
                .returningResult(OAS_REQUEST.OAS_REQUEST_ID)
                .fetchOne().value1();
    }

    @Override
    public ULong insertOasResponse(InsertOasResponseArguments arguments) {
        return dslContext().insertInto(OAS_RESPONSE)
                .set(OAS_RESPONSE.CREATED_BY, arguments.getUserId())
                .set(OAS_RESPONSE.LAST_UPDATED_BY, arguments.getUserId())
                .set(OAS_RESPONSE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESPONSE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(OAS_RESPONSE.OAS_MESSAGE_BODY_ID, arguments.getOasMessageBodyId())
                .set(OAS_RESPONSE.OAS_OPERATION_ID, arguments.getOasOperationId())
                .set(OAS_RESPONSE.DESCRIPTION, arguments.getDescription())
                .set(OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR, (byte) (arguments.isSuppressRootIndicator() ? 1 : 0))
                .set(OAS_RESPONSE.MAKE_ARRAY_INDICATOR, (byte) (arguments.isMakeArrayIndicator() ? 1 : 0))
                .set(OAS_RESPONSE.INCLUDE_CONFIRM_INDICATOR, (byte) 0)
                .returningResult(OAS_RESPONSE.OAS_RESPONSE_ID)
                .fetchOne().value1();
    }

}
