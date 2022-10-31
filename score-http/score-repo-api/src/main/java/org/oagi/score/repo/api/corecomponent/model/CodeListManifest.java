package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class CodeListManifest implements CcManifest, Serializable {

    private BigInteger codeListManifestId;

    private BigInteger releaseId;

    private BigInteger codeListId;

    private BigInteger basedCodeListManifestId;

    private boolean conflict;

    private BigInteger logId;

    private BigInteger prevCodeListManifestId;

    private BigInteger nextCodeListManifestId;

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }

    public void setCodeListManifestId(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(BigInteger codeListId) {
        this.codeListId = codeListId;
    }

    public BigInteger getBasedCodeListManifestId() {
        return basedCodeListManifestId;
    }

    public void setBasedCodeListManifestId(BigInteger basedCodeListManifestId) {
        this.basedCodeListManifestId = basedCodeListManifestId;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public BigInteger getLogId() {
        return logId;
    }

    public void setLogId(BigInteger logId) {
        this.logId = logId;
    }

    public BigInteger getPrevCodeListManifestId() {
        return prevCodeListManifestId;
    }

    public void setPrevCodeListManifestId(BigInteger prevCodeListManifestId) {
        this.prevCodeListManifestId = prevCodeListManifestId;
    }

    public BigInteger getNextCodeListManifestId() {
        return nextCodeListManifestId;
    }

    public void setNextCodeListManifestId(BigInteger nextCodeListManifestId) {
        this.nextCodeListManifestId = nextCodeListManifestId;
    }

    @Override
    public BigInteger getManifestId() {
        return this.getCodeListManifestId();
    }

    @Override
    public BigInteger getBasedCcId() {
        return this.getBasedCodeListManifestId();
    }
}
