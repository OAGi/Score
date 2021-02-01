package org.oagi.score.repo.component.bbie;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class BbieNode {

    @Data
    public class Bcc {
        private BigInteger bccManifestId;
        private String guid;
        private int cardinalityMin;
        private int cardinalityMax;
        private String den;
        private String definition;
        private String defaultValue;
        private String fixedValue;
        private boolean deprecated;
        private boolean nillable;
        private CcState state;
    }

    private Bcc bcc = new Bcc();

    @Data
    public static class Bbie {
        private Boolean used;
        private String path;
        private String hashPath;
        private String fromAbiePath;
        private String fromAbieHashPath;
        private String toBbiepPath;
        private String toBbiepHashPath;
        private BigInteger basedBccManifestId;

        private BigInteger bbieId;
        private String guid;
        private BigInteger seqKey;
        private Integer cardinalityMin;
        private Integer cardinalityMax;
        private Boolean nillable;
        private String remark;
        private String definition;
        private String defaultValue;
        private String fixedValue;
        private String example;

        private BigInteger bdtPriRestriId;
        private BigInteger codeListId;
        private BigInteger agencyIdListId;

        public boolean isEmptyCardinality() {
            return (cardinalityMin == null && cardinalityMax == null);
        }

        public boolean isEmptyPrimitive() {
            return (bdtPriRestriId == null && codeListId == null && agencyIdListId == null);
        }
    }

    private Bbie bbie = new Bbie();

}
