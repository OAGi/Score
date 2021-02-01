package org.oagi.score.repo.api.corecomponent.seqkey;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.seqkey.model.*;

public interface SeqKeyWriteRepository {

    CreateSeqKeyResponse createSeqKey(CreateSeqKeyRequest request) throws ScoreDataAccessException;

    MoveAfterResponse moveAfter(MoveAfterRequest request) throws ScoreDataAccessException;

    UpdateSeqKeyResponse updateSeqKey(UpdateSeqKeyRequest request) throws ScoreDataAccessException;

    DeleteSeqKeyResponse deleteSeqKey(DeleteSeqKeyRequest request) throws ScoreDataAccessException;

}
