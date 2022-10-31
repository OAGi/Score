package org.oagi.score.repo.api.corecomponent;

import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.agency.model.AgencyIdListManifest;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.BdtPriRestri;
import org.oagi.score.repo.api.corecomponent.model.BdtScPriRestri;
import org.oagi.score.repo.api.corecomponent.model.CodeList;
import org.oagi.score.repo.api.corecomponent.model.CodeListManifest;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface ValueDomainReadRepository {

    List<CodeListManifest> getCodeListManifestList(
            BigInteger releaseId) throws ScoreDataAccessException;

    List<CodeList> getCodeListList(
            BigInteger releaseId) throws ScoreDataAccessException;

    Map<BigInteger, BdtPriRestri> getBdtPriRestriMap(
            BigInteger releaseId) throws ScoreDataAccessException;

    Map<BigInteger, BdtScPriRestri> getBdtScPriRestriMap(
            BigInteger releaseId) throws ScoreDataAccessException;

    Map<BigInteger, List<BdtPriRestri>> getBdtPriRestriByBdtManifestIdMap(
            BigInteger releaseId) throws ScoreDataAccessException;

    Map<BigInteger, List<BdtScPriRestri>> getBdtScPriRestriByBdtScManifestIdMap(
            BigInteger releaseId) throws ScoreDataAccessException;

    List<AgencyIdListManifest> getAgencyIdListManifestList(
            BigInteger releaseId) throws ScoreDataAccessException;

    List<AgencyIdList> getAgencyIdListList(
            BigInteger releaseId) throws ScoreDataAccessException;
}
