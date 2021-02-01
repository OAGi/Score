package org.oagi.score.service.bie;

import lombok.Data;

import java.math.BigInteger;

@Data
public class FindTargetAsccpManifestResponse {

    private BigInteger asccpManifestId;

    private BieDocument bieDocument;

}
