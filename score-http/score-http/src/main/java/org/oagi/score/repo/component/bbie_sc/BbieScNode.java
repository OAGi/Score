package org.oagi.score.repo.component.bbie_sc;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class BbieScNode {

    @Data
    public class BdtSc {
        private BigInteger dtScManifestId;
        private String guid;
        private int cardinalityMin;
        private int cardinalityMax;
        private String propertyTerm;
        private String representationTerm;
        private String definition;
        private String defaultValue;
        private String fixedValue;
        private CcState state;
    }

    private BdtSc bdtSc = new BdtSc();

    @Data
    public static class BbieSc {
        private Boolean used;
        private String path;
        private String hashPath;
        private String bbiePath;
        private String bbieHashPath;
        private BigInteger basedDtScManifestId;

        private BigInteger bbieScId;
        private String guid;
        private Integer cardinalityMin;
        private Integer cardinalityMax;
        private String remark;
        private String bizTerm;
        private String definition;
        private String defaultValue;
        private String fixedValue;
        private String example;

        private BigInteger bdtScPriRestriId;
        private BigInteger codeListId;
        private BigInteger agencyIdListId;

        public boolean isEmptyPrimitive() {
            return (bdtScPriRestriId == null && codeListId == null && agencyIdListId == null);
        }
    }

    private BbieSc bbieSc = new BbieSc();

}
