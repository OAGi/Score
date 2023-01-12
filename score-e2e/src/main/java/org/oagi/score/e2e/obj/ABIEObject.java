package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;
import static org.oagi.score.e2e.obj.ObjectHelper.sha256;

@Data
public class ABIEObject {

    private BigInteger abieId;

    private String guid;

    private BigInteger basedACCManifestId;

    private String path;

    private BigInteger bizCtxId;

    private String definition;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    private int state;

    private String remark;

    private String bizTerm;

    private BigInteger ownerTopLevelAsbiepId;

    public static ABIEObject createRandomABIE(ACCObject acc, AppUserObject creator, TopLevelASBIEPObject topLevelAsbiep) {
        ABIEObject abie = new ABIEObject();
        abie.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        abie.setBasedACCManifestId(acc.getAccManifestId());
        abie.setPath("ACC-" + acc.getAccManifestId());
        abie.setDefinition(randomPrint(50, 100).trim());
        abie.setCreatedBy(creator.getAppUserId());
        abie.setLastUpdatedBy(creator.getAppUserId());
        abie.setCreationTimestamp(LocalDateTime.now());
        abie.setLastUpdateTimestamp(LocalDateTime.now());
        abie.setRemark(randomPrint(50, 100).trim());
        abie.setBizTerm("biz_term_" + randomAlphanumeric(5, 10));
        abie.setOwnerTopLevelAsbiepId(topLevelAsbiep.getTopLevelAsbiepId());
        return abie;
    }

    public String getHashPath() {
        return sha256(getPath());
    }
}
