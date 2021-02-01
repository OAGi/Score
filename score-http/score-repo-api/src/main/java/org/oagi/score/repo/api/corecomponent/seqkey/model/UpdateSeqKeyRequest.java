package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

public class UpdateSeqKeyRequest extends Request {

    private SeqKey seqKey;

    public UpdateSeqKeyRequest(ScoreUser requester) {
        super(requester);
    }

    public SeqKey getSeqKey() {
        return seqKey;
    }

    public void setSeqKey(SeqKey seqKey) {
        this.seqKey = seqKey;
    }

    public UpdateSeqKeyRequest withSeqKey(SeqKey seqKey) {
        setSeqKey(seqKey);
        return this;
    }

}
