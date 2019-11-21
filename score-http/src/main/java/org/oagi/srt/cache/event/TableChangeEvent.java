package org.oagi.srt.cache.event;

import lombok.Data;
import org.oagi.srt.redis.event.Event;

@Data
public class TableChangeEvent implements Event {

    private String tableName;
    private long primaryKey;
    private String eventName;

}
