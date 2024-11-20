package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomPrint;

@Data
public class TopLevelASBIEPObject {

    private BigInteger topLevelAsbiepId;

    private BigInteger asbiepId;

    private BigInteger ownerUserId;

    private LocalDateTime lastUpdateTimestamp;

    private BigInteger lastUpdatedBy;

    private BigInteger releaseId;

    private String version;

    private String status;

    private String state;

    private boolean inverseMode;

    private String propertyTerm;

    private String den;

    private String releaseNumber;

    public static TopLevelASBIEPObject createRandomTopLevelAsbiepInRelease(ReleaseObject release, AppUserObject user) {
        return createRandomTopLevelAsbiepInRelease(release, user, "WIP");
    }

    public static TopLevelASBIEPObject createRandomTopLevelAsbiepInRelease(ReleaseObject release, AppUserObject user,
                                                                           String state) {
        TopLevelASBIEPObject topLevelAsbiep = new TopLevelASBIEPObject();
        topLevelAsbiep.setOwnerUserId(user.getAppUserId());
        topLevelAsbiep.setLastUpdateTimestamp(LocalDateTime.now());
        topLevelAsbiep.setLastUpdatedBy(user.getAppUserId());
        topLevelAsbiep.setReleaseId(release.getReleaseId());
        topLevelAsbiep.setVersion("version_" + randomAlphanumeric(5, 10));
        topLevelAsbiep.setStatus("status_" + randomAlphanumeric(5, 10));
        topLevelAsbiep.setState(state);
        topLevelAsbiep.setInverseMode(false);
        return topLevelAsbiep;
    }


}
