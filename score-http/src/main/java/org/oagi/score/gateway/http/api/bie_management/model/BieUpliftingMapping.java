package org.oagi.score.gateway.http.api.bie_management.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieUpliftingMapping {

    public final static BieUpliftingMapping NULL_INSTANCE = new BieUpliftingMapping();

    private String bieType;
    private BigInteger bieId;
    private BigInteger sourceManifestId;
    private String sourcePath;
    private BigInteger targetManifestId;
    private String targetPath;
    private TopLevelAsbiepId refTopLevelAsbiepId;

}
