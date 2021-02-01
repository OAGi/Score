package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.base.Response;

public class CreateSeqKeyResponse extends Response {

    private final SeqKey seqKey;

    public CreateSeqKeyResponse(SeqKey seqKey) {
        this.seqKey = seqKey;
    }

    public SeqKey getSeqKey() {
        return seqKey;
    }

}
