package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ReuseBIERequest {

    private BigInteger topLevelAsbiepId;
    private BigInteger asccpManifestId;
    private BigInteger asccManifestId;
    private BigInteger accManifestId;
    private String asbiePath;
    private String asbieHashPath;
    private String fromAbiePath;
    private String fromAbieHashPath;
    private BigInteger reuseTopLevelAsbiepId;

}
