package org.oagi.score.repo.api.corecomponent.seqkey.model;

import org.oagi.score.repo.api.corecomponent.model.EntityType;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Objects;

public class SeqKey implements Iterable<SeqKey>, Serializable {

    private BigInteger seqKeyId;

    private BigInteger fromAccManifestId;

    private BigInteger asccManifestId;

    private BigInteger bccManifestId;

    private EntityType entityType;

    private SeqKey prevSeqKey;

    private SeqKey nextSeqKey;

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

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public SeqKey getPrevSeqKey() {
        return prevSeqKey;
    }

    public void setPrevSeqKey(SeqKey prevSeqKey) {
        if (prevSeqKey != null && prevSeqKey.equals(this.prevSeqKey)) {
            return;
        }

        this.prevSeqKey = prevSeqKey;
        if (prevSeqKey != null) {
            prevSeqKey.setNextSeqKey(this);
        }

        if (this.nextSeqKey != null && this.nextSeqKey.equals(this.prevSeqKey)) {
            throw new IllegalStateException();
        }
    }

    public SeqKey getNextSeqKey() {
        return nextSeqKey;
    }

    public void setNextSeqKey(SeqKey nextSeqKey) {
        if (nextSeqKey != null && nextSeqKey.equals(this.nextSeqKey)) {
            return;
        }

        this.nextSeqKey = nextSeqKey;
        if (nextSeqKey != null) {
            nextSeqKey.setPrevSeqKey(this);
        }

        if (this.prevSeqKey != null && this.prevSeqKey.equals(this.nextSeqKey)) {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeqKey seqKey = (SeqKey) o;
        return Objects.equals(seqKeyId, seqKey.seqKeyId) &&
                Objects.equals(fromAccManifestId, seqKey.fromAccManifestId) &&
                Objects.equals(asccManifestId, seqKey.asccManifestId) &&
                Objects.equals(bccManifestId, seqKey.bccManifestId) &&
                entityType == seqKey.entityType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seqKeyId, fromAccManifestId, asccManifestId, bccManifestId, entityType);
    }

    @Override
    public String toString() {
        return "SeqKey{" +
                "seqKeyId=" + seqKeyId +
                ", fromAccManifestId=" + fromAccManifestId +
                ", asccManifestId=" + asccManifestId +
                ", bccManifestId=" + bccManifestId +
                ", entityType=" + entityType +
                '}';
    }

    @Override
    public Iterator<SeqKey> iterator() {
        return new SeqKeyIterator(this);
    }

    private class SeqKeyIterator implements Iterator<SeqKey> {

        private SeqKey seqKey;

        public SeqKeyIterator(SeqKey seqKey) {
            this.seqKey = seqKey;
        }

        @Override
        public boolean hasNext() {
            return (this.seqKey != null);
        }

        @Override
        public SeqKey next() {
            SeqKey thisSeqKey = this.seqKey;
            this.seqKey = this.seqKey.getNextSeqKey();
            return thisSeqKey;
        }
    }
}
