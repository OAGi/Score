package org.oagi.score.repo.api.corecomponent;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.BdtPriRestri;
import org.oagi.score.repo.api.corecomponent.model.BdtScPriRestri;
import org.oagi.score.repo.api.corecomponent.model.CodeList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface ValueDomainReadRepository {

    List<CodeList> getCodeListList(
            BigInteger ReleaseId) throws ScoreDataAccessException;

    Map<BigInteger, BdtPriRestri> getBdtPriRestriMap(
            BigInteger ReleaseId) throws ScoreDataAccessException;

    Map<BigInteger, BdtScPriRestri> getBdtScPriRestriMap(
            BigInteger ReleaseId) throws ScoreDataAccessException;

    Map<BigInteger, List<BdtPriRestri>> getBdtPriRestriBdtIdMap(
            BigInteger ReleaseId) throws ScoreDataAccessException;

    Map<BigInteger, List<BdtScPriRestri>> getBdtScPriRestriBdtScIdMap(
            BigInteger ReleaseId) throws ScoreDataAccessException;

    // TODO: AGENCY_ID_LIST
//    Map<BigInteger, AgencyIdList> getAgencyIdListMap(
//            BigInteger ReleaseId) throws ScoreDataAccessException;
}
