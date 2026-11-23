package org.oagi.score.gateway.http.common.async;

import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * The text of an event: plain JSON, with no name of a class in it. What it is is told by the one who handles it, who
 * subscribes to a channel for one type of event, so what arrives is never taken for a type it does not name. The mapper is
 * its own, not the one of the application, since what is on the wire must not change when the settings of the other do.
 * A property it does not know is let go, so that a newer instance can send to an older one while they are both running.
 *
 * <p>An id is a bare number, as the ids are in every other JSON of the application.</p>
 */
@Component
public class AsyncJobCodec {

    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public String encode(Object event) {
        return mapper.writeValueAsString(event);
    }

    public <E> E decode(String payload, Class<E> type) {
        return mapper.readValue(payload, type);
    }
}
