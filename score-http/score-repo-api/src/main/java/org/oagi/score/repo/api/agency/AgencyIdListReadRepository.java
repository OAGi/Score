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
    AgencyIdList getAgencyIdList(BigInteger agencyIdListManifestId) throws ScoreDataAccessException;
    AgencyIdList getAgencyIdListById(BigInteger agencyIdListId) throws ScoreDataAccessException;
    List<AgencyIdListValue> getAgencyIdListValueList(BigInteger agencyIdListManifestId) throws ScoreDataAccessException;
}
