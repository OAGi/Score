package org.oagi.score.repo.component.bbiep;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class BbiepNode {

    @Data
    public class Bccp {
        private BigInteger bccpManifestId;
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
        private BigInteger basedBccpManifestId;

        private BigInteger bbiepId;
        private String guid;
        private String remark;
        private String bizTerm;
        private String definition;
        private String displayName;
    }

    private Bbiep bbiep = new Bbiep();

}
