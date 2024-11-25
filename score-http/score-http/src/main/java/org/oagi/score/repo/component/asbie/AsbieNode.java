package org.oagi.score.repo.component.asbie;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class AsbieNode {

    @Data
    public class Ascc {
        private BigInteger asccManifestId;
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
        private BigInteger basedAsccManifestId;

        private BigInteger ownerTopLevelAsbiepId;
        private BigInteger asbieId;
        private BigInteger toAsbiepId;
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
