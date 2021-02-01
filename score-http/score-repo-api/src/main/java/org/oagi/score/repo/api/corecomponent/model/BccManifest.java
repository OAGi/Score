package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class BccManifest implements CcManifest, CcAssociation, Serializable {

    private BigInteger bccManifestId;

    private BigInteger releaseId;

    private BigInteger bccId;

    private BigInteger seqKeyId;

    private BigInteger fromAccManifestId;

    private BigInteger toBccpManifestId;

    private boolean conflict;

    private BigInteger prevBccManifestId;

    private BigInteger nextBccManifestId;

    public BigInteger getBccManifestId() {
        return bccManifestId;
    }

    public void setBccManifestId(BigInteger bccManifestId) {
        this.bccManifestId = bccManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return getBccManifestId();
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return getBccId();
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getBccId() {
        return bccId;
    }

    public void setBccId(BigInteger bccId) {
        this.bccId = bccId;
    }

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

    public BigInteger getToBccpManifestId() {
        return toBccpManifestId;
    }

    public void setToBccpManifestId(BigInteger toBccpManifestId) {
        this.toBccpManifestId = toBccpManifestId;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public BigInteger getPrevBccManifestId() {
        return prevBccManifestId;
    }

    public void setPrevBccManifestId(BigInteger prevBccManifestId) {
        this.prevBccManifestId = prevBccManifestId;
    }

    public BigInteger getNextBccManifestId() {
        return nextBccManifestId;
    }

    public void setNextBccManifestId(BigInteger nextBccManifestId) {
        this.nextBccManifestId = nextBccManifestId;
    }

    @Override
    public boolean isManifest() {
        return true;
    }

    @Override
    public boolean isAscc() {
        return false;
    }

    @Override
    public boolean isBcc() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BccManifest that = (BccManifest) o;
        return isManifest() == that.isManifest() &&
                isAscc() == that.isAscc() &&
                isBcc() == that.isBcc() &&
                Objects.equals(bccManifestId, that.bccManifestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bccManifestId, isManifest(), isAscc(), isBcc());
    }
}
