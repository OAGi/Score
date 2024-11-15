package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Auditable;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.Serializable;
import java.math.BigInteger;

public class TopLevelAsbiep extends Auditable implements Serializable {

    private BigInteger topLevelAsbiepId;

    private BigInteger basedTopLevelAsbiepId;

    private ScoreUser owner;

    private BigInteger asbiepId;

    private BigInteger releaseId;

    private String propertyTerm;

    private String guid;

    private BieState state;

    private String status;

    private String version;

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(BigInteger topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public BigInteger getBasedTopLevelAsbiepId() {
        return basedTopLevelAsbiepId;
    }

    public void setBasedTopLevelAsbiepId(BigInteger basedTopLevelAsbiepId) {
        this.basedTopLevelAsbiepId = basedTopLevelAsbiepId;
    }

    public ScoreUser getOwner() {
        return owner;
    }

    public void setOwner(ScoreUser owner) {
        this.owner = owner;
    }

    public BigInteger getAsbiepId() {
        return asbiepId;
    }

    public void setAsbiepId(BigInteger asbiepId) {
        this.asbiepId = asbiepId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(BigInteger releaseId) {
        this.releaseId = releaseId;
    }

    public String getPropertyTerm() {
        return propertyTerm;
    }

    public void setPropertyTerm(String propertyTerm) {
        this.propertyTerm = propertyTerm;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public BieState getState() {
        return state;
    }

    public void setState(BieState state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
