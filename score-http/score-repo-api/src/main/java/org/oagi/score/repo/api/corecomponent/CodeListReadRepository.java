package org.oagi.score.repo.api.corecomponent;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.*;

import java.math.BigInteger;
import java.util.Map;

public interface CodeListReadRepository {

    Map<BigInteger, CodeList> getCodeListMap(
            BigInteger ReleaseId) throws ScoreDataAccessException;
}
