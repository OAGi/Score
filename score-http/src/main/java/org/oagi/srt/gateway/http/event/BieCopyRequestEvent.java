package org.oagi.srt.gateway.http.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.srt.redis.event.Event;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BieCopyRequestEvent implements Event {

    private long sourceTopLevelAbieId;
    private long copiedTopLevelAbieId;
    private List<Long> bizCtxIds;
    private long userId;

}
