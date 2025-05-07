package org.oagi.score.gateway.http.api.bie_management.model.asbiep;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;

@Data
public class AsbiepNode {

    @Data
    public class Asccp {
        private AsccpManifestId asccpManifestId;
        private String guid;
        private String propertyTerm;
        private String den;
        private String definition;
        private CcState state;
        private boolean nillable;
    }

    private Asccp asccp = new Asccp();

    @Data
    public static class Asbiep {
        private boolean used;
        private boolean derived;
        private String path;
        private String hashPath;
        private String roleOfAbiePath;
        private String roleOfAbieHashPath;
        private AsccpManifestId basedAsccpManifestId;
        private TopLevelAsbiepId refTopLevelAsbiepId;

        private TopLevelAsbiepId ownerTopLevelAsbiepId;
        private AsbiepId asbiepId;
        private AbieId roleOfAbieId;
        private String guid;
        private String displayName;
        private String remark;
        private String bizTerm;
        private String definition;
    }

    private Asbiep asbiep = new Asbiep();

}
