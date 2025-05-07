package org.oagi.score.gateway.http.api.bie_management.model.abie;

import lombok.Data;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;

import java.math.BigInteger;

@Data
public class AbieNode {

    @Data
    public class Acc {
        private AccManifestId accManifestId;
        private String guid;
        private String objectClassTerm;
        private String den;
        private String definition;
        private CcState state;
    }

    private Acc acc = new Acc();

    @Data
    public static class Abie {
        private boolean used;
        private String path;
        private String hashPath;
        private AccManifestId basedAccManifestId;

        private TopLevelAsbiepId ownerTopLevelAsbiepId;
        private BigInteger abieId;
        private String guid;
        private String version;
        private BigInteger clientId;
        private String status;
        private String remark;
        private String bizTerm;
        private String definition;
    }

    private Abie abie = new Abie();
}
