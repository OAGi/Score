package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import org.oagi.score.repo.api.bie.model.BieState;

import java.math.BigInteger;
import java.util.List;

@Data
public class BieUpdateStateListRequest {
    private String action;
    private BieState toState;
    private List<BigInteger> topLevelAsbiepIds;
}
