package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class OpenAPIDocumentObject {

    private BigInteger oasDocId;

    private String guid;

    private String openApiVersion;

    private String title;

    private String description;

    private String termsOfService;

    private String version;

    private String contactName;

    private String contactUrl;

    private String contactEmail;

    private String licenseName;

    private String licenseUrl;

    private BigInteger ownerUserId;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    public static OpenAPIDocumentObject createRandomOpenAPIDocument(AppUserObject creator) {
        OpenAPIDocumentObject openAPIDocument = new OpenAPIDocumentObject();
        openAPIDocument.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        openAPIDocument.setOpenApiVersion("3.0.3");
        openAPIDocument.setTitle(randomAlphanumeric(5, 10));
        openAPIDocument.setDescription(randomPrint(50, 100).trim());
        String contactDomain = randomAlphanumeric(5, 10);
        openAPIDocument.setTermsOfService("https://" + contactDomain + ".com" + "/terms_of_service");
        openAPIDocument.setVersion("oas_doc_ver_" + randomAlphanumeric(3, 7));
        openAPIDocument.setContactName(randomAlphanumeric(5, 10));
        openAPIDocument.setContactUrl("https://" + contactDomain + ".com");
        openAPIDocument.setContactEmail(randomAlphanumeric(5, 10) + "@" + contactDomain + ".com");
        String licenseDomain = randomAlphanumeric(5, 10);
        openAPIDocument.setLicenseName("Test License " + licenseDomain);
        openAPIDocument.setLicenseUrl("https://" + licenseDomain + ".com");
        openAPIDocument.setOwnerUserId(creator.getAppUserId());
        openAPIDocument.setCreatedBy(creator.getAppUserId());
        openAPIDocument.setLastUpdatedBy(creator.getAppUserId());
        openAPIDocument.setCreationTimestamp(LocalDateTime.now());
        openAPIDocument.setLastUpdateTimestamp(LocalDateTime.now());
        return openAPIDocument;
    }

}
