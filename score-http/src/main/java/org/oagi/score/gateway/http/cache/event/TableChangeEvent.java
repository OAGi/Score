package org.oagi.score.gateway.http.cache.event;

import lombok.Data;
import org.oagi.score.gateway.http.common.model.event.Event;

@Data
public class TableChangeEvent implements Event {

    private String tableName;
    private long primaryKey;
    private String eventName;

}
