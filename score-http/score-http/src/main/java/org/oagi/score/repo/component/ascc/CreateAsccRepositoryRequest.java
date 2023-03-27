package org.oagi.score.repo.component.ascc;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.log.model.LogAction;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class CreateAsccRepositoryRequest extends RepositoryRequest {

    private final BigInteger releaseId;
    private final BigInteger accManifestId;
    private final BigInteger asccpManifestId;
    private int pos = -1;

    private CcState initialState = CcState.WIP;
    private int cardinalityMin = 0;
    private int cardinalityMax = -1;
    private String logHash;
    private LogAction logAction;

    public CreateAsccRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger releaseId,
                                       BigInteger accManifestId,
                                       BigInteger asccpManifestId) {
        super(user);
        this.releaseId = releaseId;
        this.accManifestId = accManifestId;
        this.asccpManifestId = asccpManifestId;
    }

    public CreateAsccRepositoryRequest(AuthenticatedPrincipal user,
                                       LocalDateTime localDateTime,
                                       BigInteger releaseId,
                                       BigInteger accManifestId,
                                       BigInteger asccpManifestId) {
        super(user, localDateTime);
        this.releaseId = releaseId;
        this.accManifestId = accManifestId;
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public CcState getInitialState() {
        return initialState;
    }

    public void setInitialState(CcState initialState) {
        this.initialState = initialState;
    }

    public int getCardinalityMin() {
        return cardinalityMin;
    }

    public void setCardinalityMin(int cardinalityMin) {
        this.cardinalityMin = cardinalityMin;
    }

    public int getCardinalityMax() {
        return cardinalityMax;
    }

    public void setCardinalityMax(int cardinalityMax) {
        this.cardinalityMax = cardinalityMax;
    }

    public String getLogHash() {
        return logHash;
    }

    public void setLogHash(String logHash) {
        this.logHash = logHash;
    }

    public LogAction getLogAction() {
        return logAction;
    }

    public void setLogAction(LogAction logAction) {
        this.logAction = logAction;
    }
}
