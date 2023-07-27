package org.oagi.score.e2e.obj;

import lombok.Data;
import org.apache.commons.lang3.RandomUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReleaseObject {

    private BigInteger releaseId;

    private String guid;

    private String releaseNumber;

    private String releaseNote;

    private String releaseLicence;

    private BigInteger namespaceId;

    private BigInteger createdby;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    private String state;

    public static ReleaseObject createDraftRelease(AppUserObject creator, NamespaceObject namespace) {
        ReleaseObject release = new ReleaseObject();
        release.setGuid(UUID.randomUUID().toString().replaceAll("-", ""));
        release.setReleaseNumber(String.valueOf((RandomUtils.nextInt(20230519, 20231231))));
        release.setNamespaceId(namespace.getNamespaceId());
        release.setCreatedby(creator.getAppUserId());
        release.setLastUpdatedBy(creator.getAppUserId());
        release.setCreationTimestamp(LocalDateTime.now());
        release.setLastUpdateTimestamp(LocalDateTime.now());
        release.setState("Draft");
        return release;
    }
}
