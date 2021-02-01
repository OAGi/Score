package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class DeleteSeqKeyRequest extends Request {

    private BigInteger seqKeyId;

    public DeleteSeqKeyRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getSeqKeyId() {
        return seqKeyId;
    }

    public void setSeqKeyId(BigInteger seqKeyId) {
        this.seqKeyId = seqKeyId;
    }

    public DeleteSeqKeyRequest withSeqKeyId(BigInteger seqKeyId) {
        setSeqKeyId(seqKeyId);
        return this;
    }

}
