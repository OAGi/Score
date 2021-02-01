package org.oagi.score.repo.api.corecomponent.model;

import org.oagi.score.repo.api.corecomponent.CcManifest;

import java.io.Serializable;
import java.math.BigInteger;

public class CodeListManifest implements CcManifest, Serializable {

    private BigInteger CodeListManifestId;

    private BigInteger releaseId;

    private BigInteger CodeListId;

    private BigInteger basedCodeListManifestId;

    private boolean conflict;

    private BigInteger logId;

    private BigInteger prevBccpManifestId;

    private BigInteger nextBccpManifestId;

    public BigInteger getCodeListManifestId() {
        return CodeListManifestId;
    }

    public void setCodeListManifestId(BigInteger codeListManifestId) {
        CodeListManifestId = codeListManifestId;
    }

    @Override
    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public BigInteger getCodeListId() {
        return CodeListId;
    }

    public void setCodeListId(BigInteger codeListId) {
        CodeListId = codeListId;
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

    public BigInteger getPrevBccpManifestId() {
        return prevBccpManifestId;
    }

    public void setPrevBccpManifestId(BigInteger prevBccpManifestId) {
        this.prevBccpManifestId = prevBccpManifestId;
    }

    public BigInteger getNextBccpManifestId() {
        return nextBccpManifestId;
    }

    public void setNextBccpManifestId(BigInteger nextBccpManifestId) {
        this.nextBccpManifestId = nextBccpManifestId;
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
