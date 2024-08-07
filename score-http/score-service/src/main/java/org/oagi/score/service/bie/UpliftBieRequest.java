package org.oagi.score.service.bie;

import lombok.Data;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.service.bie.model.BieUpliftingMapping;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class UpliftBieRequest {

    private ScoreUser requester;

    private BigInteger topLevelAsbiepId;

    private BigInteger targetAsccpManifestId;

    // If `targetAsccpManifestId` does not exist, the logic will use `targetReleaseId` to find it.
    private BigInteger targetReleaseId;

    private List<BieUpliftingMapping> customMappingTable = Collections.emptyList();

}
