package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;

public class UpdateSeqKeyResponse extends Response {

    private final BigInteger seqKeyId;

    public UpdateSeqKeyResponse(BigInteger seqKeyId) {
        this.seqKeyId = seqKeyId;
    }

    public BigInteger getSeqKeyId() {
        return seqKeyId;
    }

}
