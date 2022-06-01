package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.data.Xbt;

import java.math.BigInteger;
import java.util.List;

@Data
public class CcCreateRestrictionRequest {

    private BigInteger releaseId;
    private BigInteger dtManifestId;
    private String restrictionType;
    private List<primitiveXbtMap> primitiveXbtMapList;
    private BigInteger codeListManifestId;
    private BigInteger agencyIdListManifestId;
    private List<BigInteger> xbtManifestIdList;

    @Data
    public static class primitiveXbtMap {
        private String primitive;
        private List<Xbt> xbtList;
    }
}
