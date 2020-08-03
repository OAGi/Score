package org.oagi.score.gateway.http.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.redis.event.Event;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieMakeReusableBieRequestEvent implements Event {

    private long sourceTopLevelAsbiepId;
    private long targetTopLevelAsbiepId;
    private long asbiepId;
    private List<Long> bizCtxIds = Collections.emptyList();
    private long userId;

}
