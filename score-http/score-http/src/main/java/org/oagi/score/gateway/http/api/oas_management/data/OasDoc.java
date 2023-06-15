package org.oagi.score.gateway.http.api.oas_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class OasDoc {
    private BigInteger oasDocId;
    private String guid;
    private String openAPIVersion;
    private String title;
    private String description;
    private String termsOfService;
    private String version;
    private String contactName;
    private String contactUrl;
    private String contactEmail;
    private String licenseName;
    private String licenseUrl;
    private String owner;
    private BigInteger ownerUserId;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;
}
