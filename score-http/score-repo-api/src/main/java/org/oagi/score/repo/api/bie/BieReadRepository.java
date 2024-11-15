package org.oagi.score.repo.api.bie;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.*;

public interface BieReadRepository {

    GetBieSetResponse getBieSet(
            GetBieSetRequest request) throws ScoreDataAccessException;

    GetReuseBieListResponse getReuseBieList(
            GetReuseBieListRequest request) throws ScoreDataAccessException;

    GetBaseBieResponse getBaseBie(
            GetBaseBieRequest request) throws ScoreDataAccessException;

    GetInheritedBieListResponse getInheritedBieList(
            GetInheritedBieListRequest request) throws ScoreDataAccessException;

    GetAssignedBusinessContextResponse getAssignedBusinessContext(
            GetAssignedBusinessContextRequest request) throws ScoreDataAccessException;

    GetUpliftedBieListResponse getUpliftedBieList(
            GetUpliftedBieListRequest request) throws ScoreDataAccessException;

}
