package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.oagi.score.e2e.obj.ObjectHelper.sha256;

@Data
public class ASBIEPObject {

    private BigInteger asbiepId;

    private String guid;

    private BigInteger basedAsccpManifestId;

    private String path;

    private BigInteger roleOfAbieId;

    private String definition;

    private String remark;

    private String bizTerm;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    private BigInteger ownerTopLevelAsbiepId;

    public static ASBIEPObject createRandomAsbiep(ASCCPObject asccp, ABIEObject roleOfAbie,
                                                  AppUserObject user, TopLevelASBIEPObject topLevelAsbiep) {
        ASBIEPObject asbiep = new ASBIEPObject();
        asbiep.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        asbiep.setBasedAsccpManifestId(asccp.getAsccpManifestId());
        asbiep.setPath("ASCCP-" + asccp.getAsccpManifestId());
        asbiep.setRoleOfAbieId(roleOfAbie.getAbieId());
        asbiep.setDefinition(randomPrint(50, 100).trim());
        asbiep.setBizTerm("biz_term_" + randomAlphanumeric(5, 10));
        asbiep.setRemark("remark_" + randomAlphanumeric(5, 10));
        asbiep.setCreatedBy(user.getAppUserId());
        asbiep.setLastUpdatedBy(user.getAppUserId());
        asbiep.setCreationTimestamp(LocalDateTime.now());
        asbiep.setLastUpdateTimestamp(LocalDateTime.now());
        asbiep.setOwnerTopLevelAsbiepId(topLevelAsbiep.getAsbiepId());
        return asbiep;
    }

    public String getHashPath() {
        return sha256(getPath());
    }
}
