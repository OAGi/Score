package org.oagi.score.repo.api.bie;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.model.CreateBieRequest;
import org.oagi.score.repo.api.bie.model.CreateBieResponse;

public interface BieWriteRepository {

    CreateBieResponse createBie(
            CreateBieRequest request) throws ScoreDataAccessException;

}
