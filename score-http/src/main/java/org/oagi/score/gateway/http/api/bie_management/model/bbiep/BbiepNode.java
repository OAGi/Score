package org.oagi.score.gateway.http.api.bie_management.model.bbiep;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;

@Data
public class BbiepNode {

    @Data
    public class Bccp {
        private BccpManifestId bccpManifestId;
        private String guid;
        private String propertyTerm;
        private String den;
        private String definition;
        private CcState state;
        private boolean nillable;
        private String defaultValue;
        private String fixedValue;
    }

    private Bccp bccp = new Bccp();

    @Data
    public class Bdt {
        private String guid;
        private String dataTypeTerm;
        private String den;
        private String definition;
        private CcState state;
    }

    private Bdt bdt = new Bdt();

    @Data
    public static class Bbiep {
        private boolean used;
        private String path;
        private String hashPath;
        private BccpManifestId basedBccpManifestId;

        private TopLevelAsbiepId ownerTopLevelAsbiepId;
        private BbiepId bbiepId;
        private String guid;
        private String remark;
        private String bizTerm;
        private String definition;
        private String displayName;
    }

    private Bbiep bbiep = new Bbiep();

}
