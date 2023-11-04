package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.OpenAPIDocumentAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.OasDocRecord;
import org.oagi.score.e2e.obj.AppUserObject;
import org.oagi.score.e2e.obj.OpenAPIDocumentObject;

import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.OAS_DOC;

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

}
