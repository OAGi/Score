package org.oagi.score.repo.api.agency;

import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.agency.model.AgencyIdListValue;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListRequest;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListResponse;
import org.oagi.score.repo.api.base.ScoreDataAccessException;

import java.math.BigInteger;
import java.util.List;

public interface AgencyIdListReadRepository {

    GetAgencyIdListListResponse getAgencyIdListList(GetAgencyIdListListRequest request) throws ScoreDataAccessException;

    AgencyIdList getAgencyIdListByAgencyIdListManifestId(BigInteger agencyIdListManifestId) throws ScoreDataAccessException;

    AgencyIdList getAgencyIdListByAgencyIdListId(BigInteger agencyIdListId) throws ScoreDataAccessException;

    List<AgencyIdListValue> getAgencyIdListValueListByAgencyIdListManifestId(BigInteger agencyIdListManifestId) throws ScoreDataAccessException;
    
}
