package org.oagi.score.gateway.http.api.info_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.Guid;

import java.util.Date;

@Data
public class SummaryCcExt {

    private AccId accId;

    private AccManifestId accManifestId;

    private ReleaseId releaseId;

    private String releaseNum;

    private Guid guid;

    private String objectClassTerm;

    private CcState state;

    private Date lastUpdateTimestamp;

    private String lastUpdateUser;

    private String ownerUsername;

    private UserId ownerUserId;

    private TopLevelAsbiepId topLevelAsbiepId;

    private BieState bieState;

    private String den;

    private String associationDen;

    private int seqKey;

}
