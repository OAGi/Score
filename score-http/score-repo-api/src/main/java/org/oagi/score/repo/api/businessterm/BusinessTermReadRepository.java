package org.oagi.score.repo.api.businessterm;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businessterm.model.GetBusinessTermListRequest;
import org.oagi.score.repo.api.businessterm.model.GetBusinessTermListResponse;
import org.oagi.score.repo.api.businessterm.model.GetBusinessTermRequest;
import org.oagi.score.repo.api.businessterm.model.GetBusinessTermResponse;

public interface BusinessTermReadRepository {

    GetBusinessTermResponse getBusinessTerm(
            GetBusinessTermRequest request) throws ScoreDataAccessException;

    GetBusinessTermListResponse getBusinessTermList(
            GetBusinessTermListRequest request) throws ScoreDataAccessException;

    GetBusinessTermListResponse getBusinessTermListByAssignedBie(
            GetBusinessTermListRequest request) throws ScoreDataAccessException;
    
}
