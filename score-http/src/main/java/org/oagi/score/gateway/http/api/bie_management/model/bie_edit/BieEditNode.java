package org.oagi.score.gateway.http.api.bie_management.model.bie_edit;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.math.BigInteger;
import java.util.List;

@Data
public class BieEditNode {

    private TopLevelAsbiepId topLevelAsbiepId;
    private LibraryId libraryId;
    private ReleaseId releaseId;

    private String type;
    private String guid;
    private String hashPath;
    private String name;
    private String displayName;
    private boolean used;
    private boolean required;
    private boolean locked;
    private boolean derived;
    private boolean hasChild;

    private String version;
    private String status;

    private String libraryName;
    private String releaseNum;
    private BieState topLevelAsbiepState;
    private String ownerLoginId;

    private boolean deprecated;
    private String deprecatedReason;
    private String deprecatedRemark;

    // Issue #1635
    private BigInteger basedTopLevelAsbiepId;

    // Issue #1519: a BIE may bind to several OpenAPI Documents/operations (M:N). The BIE-root editor
    // surfaces the full cross-document union of bindings, not just the first one.
    private List<BieForOasDoc> bieForOasDocList;

}
