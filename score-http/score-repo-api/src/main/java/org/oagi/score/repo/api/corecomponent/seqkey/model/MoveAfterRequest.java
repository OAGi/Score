package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

public class MoveAfterRequest extends Request {

    private SeqKey item;

    private SeqKey after;

    public MoveAfterRequest(ScoreUser requester) {
        super(requester);
    }

    public SeqKey getItem() {
        return item;
    }

    public void setItem(SeqKey item) {
        this.item = item;
    }

    public MoveAfterRequest withItem(SeqKey item) {
        setItem(item);
        return this;
    }

    public SeqKey getAfter() {
        return after;
    }

    public void setAfter(SeqKey after) {
        this.after = after;
    }

    public MoveAfterRequest withAfter(SeqKey after) {
        setAfter(after);
        return this;
    }
}
