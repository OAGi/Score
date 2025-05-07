package org.oagi.score.gateway.http.api.bie_management.model.bbie;

import lombok.Data;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

@Data
public class BbieNode {

    @Data
    public class Bcc {
        private BccManifestId bccManifestId;
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
        private Collection<String> cdtPrimitives = Collections.emptyList();
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
        private BccManifestId basedBccManifestId;

        private TopLevelAsbiepId ownerTopLevelAsbiepId;
        private BbieId bbieId;
        private BbiepId toBbiepId;
        private String guid;
        private BigInteger seqKey;
        private Integer cardinalityMin;
        private Integer cardinalityMax;
        private BigInteger facetMinLength;
        private BigInteger facetMaxLength;
        private String facetPattern;
        private Boolean nillable;
        private String remark;
        private String definition;
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

    private Bbie bbie = new Bbie();

}
