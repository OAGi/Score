package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.openapidoc.model.BieForOasDoc;

import java.math.BigInteger;

@Data
public class BieEditNode {

    private BigInteger topLevelAsbiepId;
    private BigInteger releaseId;

    private String type;
    private String guid;
    private String hashPath;
    private String name;
    private boolean used;
    private boolean required;
    private boolean locked;
    private boolean derived;
    private boolean hasChild;

    private String version;
    private String status;

    private String releaseNum;
    private BieState topLevelAsbiepState;
    private String ownerLoginId;

    // Issue #1519
    private BieForOasDoc bieForOasDoc;

}
