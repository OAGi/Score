package org.oagi.score.gateway.http.api.bie_management.model.bbie_sc;

import lombok.Data;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

@Data
public class BbieScNode {

    @Data
    public class BdtSc {
        private DtScManifestId dtScManifestId;
        private String guid;
        private int cardinalityMin;
        private int cardinalityMax;
        private String propertyTerm;
        private String representationTerm;
        private String definition;
        private String defaultValue;
        private String fixedValue;
        private CcState state;
        private Collection<String> cdtPrimitives = Collections.emptyList();
    }

    private BdtSc bdtSc = new BdtSc();

    @Data
    public static class BbieSc {
        private Boolean used;
        private String path;
        private String hashPath;
        private String bbiePath;
        private String bbieHashPath;
        private DtScManifestId basedDtScManifestId;

        private TopLevelAsbiepId ownerTopLevelAsbiepId;
        private BbieScId bbieScId;
        private String guid;
        private Integer cardinalityMin;
        private Integer cardinalityMax;
        private BigInteger facetMinLength;
        private BigInteger facetMaxLength;
        private String facetPattern;
        private String remark;
        private String bizTerm;
        private String definition;
        private String displayName;
        private String defaultValue;
        private String fixedValue;
        private String example;
        private Boolean deprecated;

        private XbtManifestId xbtManifestId;
        private CodeListManifestId codeListManifestId;
        private AgencyIdListManifestId agencyIdListManifestId;

        public boolean isEmptyPrimitive() {
            return (xbtManifestId == null && codeListManifestId == null && agencyIdListManifestId == null);
        }
    }

    private BbieSc bbieSc = new BbieSc();

}
