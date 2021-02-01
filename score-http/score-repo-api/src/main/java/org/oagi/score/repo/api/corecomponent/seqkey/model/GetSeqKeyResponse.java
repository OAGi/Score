package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.base.Response;

public class GetSeqKeyResponse extends Response {

    private final SeqKey seqKey;

    public GetSeqKeyResponse(SeqKey seqKey) {
        this.seqKey = seqKey;
    }

    public SeqKey getSeqKey() {
        return seqKey;
    }

}
