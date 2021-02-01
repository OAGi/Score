package org.oagi.score.repo.api.corecomponent.model;

import java.math.BigInteger;

public class CcAssociationSequence {

    private BigInteger seqKeyId;

    private BigInteger fromAccManifestId;

    private BigInteger asccManifestId;

    private BigInteger bccManifestId;

    private BigInteger prevSeqKeyId;

    private BigInteger nextSeqKeyId;

    public BigInteger getSeqKeyId() {
        return seqKeyId;
    }

    public void setSeqKeyId(BigInteger seqKeyId) {
        this.seqKeyId = seqKeyId;
    }

    public BigInteger getFromAccManifestId() {
        return fromAccManifestId;
    }

    public void setFromAccManifestId(BigInteger fromAccManifestId) {
        this.fromAccManifestId = fromAccManifestId;
    }

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }

    public void setAsccManifestId(BigInteger asccManifestId) {
        this.asccManifestId = asccManifestId;
    }

    public BigInteger getBccManifestId() {
        return bccManifestId;
    }

    public void setBccManifestId(BigInteger bccManifestId) {
        this.bccManifestId = bccManifestId;
    }

    public BigInteger getPrevSeqKeyId() {
        return prevSeqKeyId;
    }

    public void setPrevSeqKeyId(BigInteger prevSeqKeyId) {
        this.prevSeqKeyId = prevSeqKeyId;
    }

    public BigInteger getNextSeqKeyId() {
        return nextSeqKeyId;
    }

    public void setNextSeqKeyId(BigInteger nextSeqKeyId) {
        this.nextSeqKeyId = nextSeqKeyId;
    }
}
