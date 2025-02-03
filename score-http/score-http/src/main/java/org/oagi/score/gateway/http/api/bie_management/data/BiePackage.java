package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.release_management.data.SimpleRelease;
import org.oagi.score.repo.api.bie.model.BiePackageState;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.common.data.AccessPrivilege;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class BiePackage {

    private BigInteger biePackageId;

    private BigInteger libraryId;

    private String versionId;

    private String versionName;

    private String description;

    private List<SimpleRelease> releases = Collections.emptyList();

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

    private String sourceBiePackageVersionName;

    private String sourceBiePackageVersionId;

}
