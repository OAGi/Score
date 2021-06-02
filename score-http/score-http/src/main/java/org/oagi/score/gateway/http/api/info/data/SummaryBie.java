package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.api.businesscontext.model.BusinessContext;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SummaryBie {

    private BigInteger topLevelAsbiepId;
    private LocalDateTime lastUpdateTimestamp;
    private BieState state;

    private BigInteger ownerUserId;
    private String ownerUsername;

    private String propertyTerm;

    private List<BusinessContext> businessContexts;

}
