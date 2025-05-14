package org.oagi.score.gateway.http.api.bie_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class BiePackage {

    private BigInteger biePackageId;

    private LibraryId libraryId;

    private String versionId;

    private String versionName;

    private String description;

    private List<ReleaseSummaryRecord> releases = Collections.emptyList();

    private BieState state;

    private AccessPrivilege access;

    private UserSummaryRecord owner;

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
