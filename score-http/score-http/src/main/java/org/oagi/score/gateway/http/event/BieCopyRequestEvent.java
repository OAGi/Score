package org.oagi.score.gateway.http.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.redis.event.Event;

import java.math.BigInteger;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieCopyRequestEvent implements Event {

    private BigInteger sourceTopLevelAsbiepId;
    private BigInteger copiedTopLevelAsbiepId;
    private List<BigInteger> bizCtxIds;
    private BigInteger userId;

}
