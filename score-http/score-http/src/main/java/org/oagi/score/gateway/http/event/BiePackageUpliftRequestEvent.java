package org.oagi.score.gateway.http.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.redis.event.Event;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiePackageUpliftRequestEvent implements Event {

    private BigInteger requestUserId;

    private BigInteger targetReleaseId;

    private BigInteger upliftedBiePackageId;

    private List<BigInteger> sourceTopLevelAsbiepIdList;

}
