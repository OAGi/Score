package org.oagi.score.repo.component.abie;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class AbieNode {

    @Data
    public class Acc {
        private BigInteger accManifestId;
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
        private BigInteger basedAccManifestId;

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
