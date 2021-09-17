package org.oagi.score.repo.api.agency;

import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public interface AgencyIdListWriteRepository {
    BigInteger createAgencyIdList(ScoreUser user, BigInteger releaseId, BigInteger basedAgencyIdListManifestId) throws ScoreDataAccessException;
    AgencyIdList updateAgencyIdListProperty(ScoreUser user, AgencyIdList agencyIdList) throws ScoreDataAccessException;
    void updateAgencyIdListState(ScoreUser user, BigInteger agencyIdListManifestId, CcState toState) throws ScoreDataAccessException;
    void reviseAgencyIdList(ScoreUser user, BigInteger agencyIdListManifestId) throws ScoreDataAccessException;
    void cancelAgencyIdList(ScoreUser user, BigInteger agencyIdListManifestId) throws ScoreDataAccessException;
    void transferOwnerShipAgencyIdList(ScoreUser user, BigInteger agencyIdListManifestId, String targetLoginId) throws ScoreDataAccessException;
}
