package org.oagi.score.repo.component.ascc;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.service.log.model.LogAction;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class DeleteAsccRepositoryRequest extends RepositoryRequest {

    private final BigInteger asccManifestId;
    private boolean ignoreState;
    private String logHash;
    private LogAction logAction;

    public DeleteAsccRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger asccManifestId) {
        super(user);
        this.asccManifestId = asccManifestId;
    }

    public DeleteAsccRepositoryRequest(AuthenticatedPrincipal user,
                                       LocalDateTime localDateTime,
                                       BigInteger asccManifestId) {
        super(user, localDateTime);
        this.asccManifestId = asccManifestId;
    }

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }

    public boolean isIgnoreState() {
        return ignoreState;
    }

    public void setIgnoreState(boolean ignoreState) {
        this.ignoreState = ignoreState;
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
