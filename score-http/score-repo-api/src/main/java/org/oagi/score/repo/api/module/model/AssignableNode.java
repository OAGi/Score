package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.corecomponent.model.CcState;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class AssignableNode {
    public BigInteger getManifestId() {
        return manifestId;
    }

    public void setManifestId(BigInteger manifestId) {
        this.manifestId = manifestId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CcState getState() {
        return state;
    }

    public void setState(CcState state) {
        this.state = state;
    }

    public String getDen() {
        return den;
    }

    public void setDen(String den) {
        this.den = den;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigInteger getRevision() {
        return revision;
    }

    public void setRevision(BigInteger revision) {
        this.revision = revision;
    }

    private BigInteger manifestId;
    private String type;
    private CcState state;
    private String den;
    private String ownerUserId;
    private LocalDateTime timestamp;
    private BigInteger revision;
}
