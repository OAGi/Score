package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.OasDocRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.OasDocWriteRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.ScoreGuidUtils.randomGuid;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqOasDocWriteRepository extends JooqScoreRepository
        implements OasDocWriteRepository {

    public JooqOasDocWriteRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public CreateOasDocResponse createOasDoc(
            CreateOasDocRequest request) throws ScoreDataAccessException {

        ScoreUser requester = request.getRequester();
        BigInteger requesterUserId = requester.getUserId();
        ULong userId = ULong.valueOf(requesterUserId);
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
        record.setOwnerUserId(userId);
        record.setCreatedBy(ULong.valueOf(requesterUserId));
        record.setLastUpdatedBy(ULong.valueOf(requesterUserId));
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
        BigInteger requesterUserId = requester.getUserId();
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
            record.setLastUpdatedBy(ULong.valueOf(requesterUserId));
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

}
