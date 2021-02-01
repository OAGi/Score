package org.oagi.score.service.codelist.model;

import lombok.Data;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

@Data
public class CodeListUpliftingRequest {

    private ScoreUser requester;

    private BigInteger codeListManifestId;

    private BigInteger targetReleaseId;

}
