package org.oagi.score.gateway.http.api.bie_management.model.asbie;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;

import java.math.BigInteger;

@Data
public class AsbieNode {

    @Data
    public class Ascc {
        private AsccManifestId asccManifestId;
        private String guid;
        private int cardinalityMin;
        private int cardinalityMax;
        private String den;
        private String definition;
        private CcState state;
    }

    private Ascc ascc = new Ascc();

    @Data
    public static class Asbie {
        private Boolean used;
        private String path;
        private String hashPath;
        private String fromAbiePath;
        private String fromAbieHashPath;
        private String toAsbiepPath;
        private String toAsbiepHashPath;
        private AsccManifestId basedAsccManifestId;

        private TopLevelAsbiepId ownerTopLevelAsbiepId;
        private AsbieId asbieId;
        private AsbiepId toAsbiepId;
        private String guid;
        private BigInteger seqKey = BigInteger.ZERO;
        private Integer cardinalityMin;
        private Integer cardinalityMax;
        private Boolean nillable;
        private String remark;
        private String definition;
        private Boolean deprecated;
    }

    private Asbie asbie = new Asbie();

}
