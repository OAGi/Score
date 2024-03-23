package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BiePackageState;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.common.data.AccessPrivilege;

import java.math.BigInteger;
import java.util.Date;

@Data
public class BiePackage {

    private BigInteger biePackageId;

    private String versionId;

    private String versionName;

    private String description;

    private BigInteger releaseId;

    private String releaseNum;

    private BiePackageState state;

    private AccessPrivilege access;

    private ScoreUser owner;

    private ScoreUser createdBy;

    private ScoreUser lastUpdatedBy;

    private Date creationTimestamp;

    private Date lastUpdateTimestamp;

    private BigInteger sourceBiePackageId;

    private String sourceAction;

    private Date sourceTimestamp;

    private String sourceBiePackageVersionId;

    private String sourceBiePackageVersionName;

}
