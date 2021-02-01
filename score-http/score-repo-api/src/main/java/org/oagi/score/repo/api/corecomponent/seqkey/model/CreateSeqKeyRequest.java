package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;

public class CreateSeqKeyRequest extends Request {

    private BigInteger fromAccManifestId;

    private SeqKeyType type;

    private BigInteger manifestId;

    public CreateSeqKeyRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getFromAccManifestId() {
        return fromAccManifestId;
    }

    public void setFromAccManifestId(BigInteger fromAccManifestId) {
        this.fromAccManifestId = fromAccManifestId;
    }

    public CreateSeqKeyRequest withFromAccManifestId(BigInteger fromAccManifestId) {
        setFromAccManifestId(fromAccManifestId);
        return this;
    }

    public SeqKeyType getType() {
        return type;
    }

    public void setType(SeqKeyType type) {
        this.type = type;
    }

    public CreateSeqKeyRequest withType(SeqKeyType type) {
        setType(type);
        return this;
    }

    public BigInteger getManifestId() {
        return manifestId;
    }

    public void setManifestId(BigInteger manifestId) {
        this.manifestId = manifestId;
    }

    public CreateSeqKeyRequest withManifestId(BigInteger manifestId) {
        setManifestId(manifestId);
        return this;
    }

}
