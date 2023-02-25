package org.oagi.score.repo.api.businessterm;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businessterm.model.*;

public interface BusinessTermAssignmentWriteRepository {

    AssignBusinessTermResponse assignBusinessTerm(
            AssignBusinessTermRequest request) throws ScoreDataAccessException;

//    todo
    UpdateBusinessTermAssignmentResponse updateBusinessTermAssignment(
            UpdateBusinessTermAssignmentRequest request) throws ScoreDataAccessException;

    DeleteAssignedBusinessTermResponse deleteBusinessTermAssignment(
            DeleteAssignedBusinessTermRequest request) throws ScoreDataAccessException;
    
}
