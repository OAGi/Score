package org.oagi.score.repo.component.asbiep;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;

@Data
public class AsbiepNode {

    @Data
    public class Asccp {
        private BigInteger asccpManifestId;
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
        private BigInteger basedAsccpManifestId;
        private BigInteger refTopLevelAsbiepId;

        private BigInteger ownerTopLevelAsbiepId;
        private BigInteger asbiepId;
        private BigInteger roleOfAbieId;
        private String guid;
        private String displayName;
        private String remark;
        private String bizTerm;
        private String definition;
    }

    private Asbiep asbiep = new Asbiep();

}
