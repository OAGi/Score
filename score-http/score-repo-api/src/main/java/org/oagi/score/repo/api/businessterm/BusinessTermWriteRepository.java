package org.oagi.score.repo.api.businessterm;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businessterm.model.*;

public interface BusinessTermWriteRepository {

    CreateBusinessTermResponse createBusinessTerm(
            CreateBusinessTermRequest request) throws ScoreDataAccessException;

    CreateBulkBusinessTermResponse createBusinessTermsFromFile(CreateBulkBusinessTermRequest request) throws ScoreDataAccessException;

    UpdateBusinessTermResponse updateBusinessTerm(
            UpdateBusinessTermRequest request) throws ScoreDataAccessException;

    DeleteBusinessTermResponse deleteBusinessTerm(
            DeleteBusinessTermRequest request) throws ScoreDataAccessException;
    
}
