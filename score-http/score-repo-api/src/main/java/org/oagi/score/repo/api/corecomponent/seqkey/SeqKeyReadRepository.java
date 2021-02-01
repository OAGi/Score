package org.oagi.score.repo.api.corecomponent.seqkey;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.seqkey.model.GetSeqKeyRequest;
import org.oagi.score.repo.api.corecomponent.seqkey.model.GetSeqKeyResponse;

public interface SeqKeyReadRepository {

    GetSeqKeyResponse getSeqKey(GetSeqKeyRequest request) throws ScoreDataAccessException;

}
