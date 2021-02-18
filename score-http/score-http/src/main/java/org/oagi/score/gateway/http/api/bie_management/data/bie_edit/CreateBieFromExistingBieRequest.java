package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CreateBieFromExistingBieRequest {

    private String asbieHashPath;
    private BigInteger topLevelAsbiepId;
    private BigInteger asccpManifestId;
}