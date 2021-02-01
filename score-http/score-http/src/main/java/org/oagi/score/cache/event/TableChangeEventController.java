package org.oagi.score.cache.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TableChangeEventController {

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/event/tableChange/{eventName}/{tableName}/{id}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity putTableChangeEvent(
            @PathVariable("eventName") String eventName,
            @PathVariable("tableName") String tableName,
            @PathVariable("id") long id
    ) {
        TableChangeEvent tableChangeEvent = new TableChangeEvent();
        tableChangeEvent.setTableName(tableName);
        tableChangeEvent.setPrimaryKey(id);
        tableChangeEvent.setEventName(eventName);

        redisTemplate.convertAndSend("tableChangeEvent", tableChangeEvent);

        return ResponseEntity.accepted().build();
    }
}
