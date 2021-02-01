package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class AsccManifest implements CcManifest, CcAssociation, Serializable {

    private BigInteger asccManifestId;

    private BigInteger releaseId;

    private BigInteger asccId;

    private BigInteger seqKeyId;

    private BigInteger fromAccManifestId;

    private BigInteger toAsccpManifestId;

    private boolean conflict;

    private BigInteger prevAsccManifestId;

    private BigInteger nextAsccManifestId;

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }

    public void setAsccManifestId(BigInteger asccManifestId) {
        this.asccManifestId = asccManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return getAsccManifestId();
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    @Override
    public BigInteger getBasedCcId() {
        return getAsccId();
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getAsccId() {
        return asccId;
    }

    public void setAsccId(BigInteger asccId) {
        this.asccId = asccId;
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

    public BigInteger getToAsccpManifestId() {
        return toAsccpManifestId;
    }

    public void setToAsccpManifestId(BigInteger toAsccpManifestId) {
        this.toAsccpManifestId = toAsccpManifestId;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public BigInteger getPrevAsccManifestId() {
        return prevAsccManifestId;
    }

    public void setPrevAsccManifestId(BigInteger prevAsccManifestId) {
        this.prevAsccManifestId = prevAsccManifestId;
    }

    public BigInteger getNextAsccManifestId() {
        return nextAsccManifestId;
    }

    public void setNextAsccManifestId(BigInteger nextAsccManifestId) {
        this.nextAsccManifestId = nextAsccManifestId;
    }

    @Override
    public boolean isManifest() {
        return true;
    }

    @Override
    public boolean isAscc() {
        return true;
    }

    @Override
    public boolean isBcc() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsccManifest that = (AsccManifest) o;
        return isManifest() == that.isManifest() &&
                isAscc() == that.isAscc() &&
                isBcc() == that.isBcc() &&
                Objects.equals(asccManifestId, that.asccManifestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asccManifestId, isManifest(), isAscc(), isBcc());
    }
}
