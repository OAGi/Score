package org.oagi.score.cache.event;

import lombok.Data;
import org.oagi.score.redis.event.Event;

@Data
public class TableChangeEvent implements Event {

    private String tableName;
    private long primaryKey;
    private String eventName;

}
