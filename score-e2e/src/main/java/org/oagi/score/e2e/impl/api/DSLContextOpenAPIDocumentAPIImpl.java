package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.OpenAPIDocumentAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasDocRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasMessageBodyRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasOperationRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasRequestRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasResourceRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasResponseRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasServerRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;
import org.oagi.score.e2e.obj.TopLevelASBIEPObject;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_DOC;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_MESSAGE_BODY;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_OPERATION;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_REQUEST;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_RESOURCE;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_RESPONSE;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_SERVER;

public class DSLContextOpenAPIDocumentAPIImpl implements OpenAPIDocumentAPI {

    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextOpenAPIDocumentAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public OpenAPIDocumentObject createRandomOpenAPIDocument(AppUserObject creator) {
        OpenAPIDocumentObject randomOpenApiDocument = OpenAPIDocumentObject.createRandomOpenAPIDocument(creator);

        OasDocRecord oasDocRecord = new OasDocRecord();
        oasDocRecord.setGuid(randomOpenApiDocument.getGuid());
        oasDocRecord.setOpenApiVersion(randomOpenApiDocument.getOpenApiVersion());
        oasDocRecord.setTitle(randomOpenApiDocument.getTitle());
        oasDocRecord.setDescription(randomOpenApiDocument.getDescription());
        oasDocRecord.setTermsOfService(randomOpenApiDocument.getTermsOfService());
        oasDocRecord.setVersion(randomOpenApiDocument.getVersion());
        oasDocRecord.setContactName(randomOpenApiDocument.getContactName());
        oasDocRecord.setContactUrl(randomOpenApiDocument.getContactUrl());
        oasDocRecord.setContactEmail(randomOpenApiDocument.getContactEmail());
        oasDocRecord.setLicenseName(randomOpenApiDocument.getLicenseName());
        oasDocRecord.setLicenseUrl(randomOpenApiDocument.getLicenseUrl());
        oasDocRecord.setOwnerUserId(ULong.valueOf(randomOpenApiDocument.getOwnerUserId()));
        oasDocRecord.setCreatedBy(ULong.valueOf(randomOpenApiDocument.getCreatedBy()));
        oasDocRecord.setLastUpdatedBy(ULong.valueOf(randomOpenApiDocument.getLastUpdatedBy()));
        oasDocRecord.setCreationTimestamp(randomOpenApiDocument.getCreationTimestamp());
        oasDocRecord.setLastUpdateTimestamp(randomOpenApiDocument.getLastUpdateTimestamp());

        randomOpenApiDocument.setOasDocId(
                dslContext.insertInto(OAS_DOC)
                        .set(oasDocRecord)
                        .returning(OAS_DOC.OAS_DOC_ID)
                        .fetchOne().getOasDocId().toBigInteger()
        );
        return randomOpenApiDocument;
    }

    @Override
    public void createRandomServer(OpenAPIDocumentObject openAPIDocument, AppUserObject creator) {
        LocalDateTime now = LocalDateTime.now();
        String serverDomain = randomAlphanumeric(5, 10).toLowerCase();

        OasServerRecord oasServerRecord = new OasServerRecord();
        oasServerRecord.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        oasServerRecord.setOasDocId(ULong.valueOf(openAPIDocument.getOasDocId()));
        oasServerRecord.setDescription("Server for " + openAPIDocument.getTitle());
        oasServerRecord.setUrl("https://" + serverDomain + ".example.com");
        oasServerRecord.setVariables("{}");
        oasServerRecord.setOwnerUserId(ULong.valueOf(creator.getAppUserId()));
        oasServerRecord.setCreatedBy(ULong.valueOf(creator.getAppUserId()));
        oasServerRecord.setLastUpdatedBy(ULong.valueOf(creator.getAppUserId()));
        oasServerRecord.setCreationTimestamp(now);
        oasServerRecord.setLastUpdateTimestamp(now);

        dslContext.insertInto(OAS_SERVER)
                .set(oasServerRecord)
                .execute();
    }

    @Override
    public void seedOpenAPIOperationWithBody(OpenAPIDocumentObject oasDocument, TopLevelASBIEPObject bie,
                                             String path, String verb, String operationId, String messageBody,
                                             AppUserObject creator) {
        LocalDateTime now = LocalDateTime.now();
        ULong oasDocId = ULong.valueOf(oasDocument.getOasDocId());
        ULong topLevelAsbiepId = ULong.valueOf(bie.getTopLevelAsbiepId());
        ULong userId = ULong.valueOf(creator.getAppUserId());

        // Find-or-create the resource by (oas_doc_id, path): a legacy split operation keeps its Request and
        // Response operations under the SAME resource, so the two rows collide on (Resource Name, Verb).
        ULong oasResourceId = dslContext.select(OAS_RESOURCE.OAS_RESOURCE_ID)
                .from(OAS_RESOURCE)
                .where(OAS_RESOURCE.OAS_DOC_ID.eq(oasDocId))
                .and(OAS_RESOURCE.PATH.eq(path))
                .limit(1)
                .fetchOne(OAS_RESOURCE.OAS_RESOURCE_ID);
        if (oasResourceId == null) {
            OasResourceRecord oasResourceRecord = new OasResourceRecord();
            oasResourceRecord.setOasDocId(oasDocId);
            oasResourceRecord.setPath(path);
            oasResourceRecord.setCreatedBy(userId);
            oasResourceRecord.setLastUpdatedBy(userId);
            oasResourceRecord.setCreationTimestamp(now);
            oasResourceRecord.setLastUpdateTimestamp(now);
            oasResourceId = dslContext.insertInto(OAS_RESOURCE)
                    .set(oasResourceRecord)
                    .returning(OAS_RESOURCE.OAS_RESOURCE_ID)
                    .fetchOne().getOasResourceId();
        }

        // A dedicated message body pointing at the BIE, one operation owning exactly one of Request/Response.
        OasMessageBodyRecord oasMessageBodyRecord = new OasMessageBodyRecord();
        oasMessageBodyRecord.setTopLevelAsbiepId(topLevelAsbiepId);
        oasMessageBodyRecord.setCreatedBy(userId);
        oasMessageBodyRecord.setLastUpdatedBy(userId);
        oasMessageBodyRecord.setCreationTimestamp(now);
        oasMessageBodyRecord.setLastUpdateTimestamp(now);
        ULong oasMessageBodyId = dslContext.insertInto(OAS_MESSAGE_BODY)
                .set(oasMessageBodyRecord)
                .returning(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID)
                .fetchOne().getOasMessageBodyId();

        OasOperationRecord oasOperationRecord = new OasOperationRecord();
        oasOperationRecord.setOasResourceId(oasResourceId);
        oasOperationRecord.setVerb(verb);
        oasOperationRecord.setOperationId(operationId);
        oasOperationRecord.setCreatedBy(userId);
        oasOperationRecord.setLastUpdatedBy(userId);
        oasOperationRecord.setCreationTimestamp(now);
        oasOperationRecord.setLastUpdateTimestamp(now);
        ULong oasOperationId = dslContext.insertInto(OAS_OPERATION)
                .set(oasOperationRecord)
                .returning(OAS_OPERATION.OAS_OPERATION_ID)
                .fetchOne().getOasOperationId();

        if ("Request".equals(messageBody)) {
            OasRequestRecord oasRequestRecord = new OasRequestRecord();
            oasRequestRecord.setOasOperationId(oasOperationId);
            oasRequestRecord.setOasMessageBodyId(oasMessageBodyId);
            oasRequestRecord.setRequired((byte) 0);
            oasRequestRecord.setCreatedBy(userId);
            oasRequestRecord.setLastUpdatedBy(userId);
            oasRequestRecord.setCreationTimestamp(now);
            oasRequestRecord.setLastUpdateTimestamp(now);
            dslContext.insertInto(OAS_REQUEST)
                    .set(oasRequestRecord)
                    .execute();
        } else {
            OasResponseRecord oasResponseRecord = new OasResponseRecord();
            oasResponseRecord.setOasOperationId(oasOperationId);
            oasResponseRecord.setOasMessageBodyId(oasMessageBodyId);
            oasResponseRecord.setHttpStatusCode(200);
            oasResponseRecord.setCreatedBy(userId);
            oasResponseRecord.setLastUpdatedBy(userId);
            oasResponseRecord.setCreationTimestamp(now);
            oasResponseRecord.setLastUpdateTimestamp(now);
            dslContext.insertInto(OAS_RESPONSE)
                    .set(oasResponseRecord)
                    .execute();
        }
    }

}
