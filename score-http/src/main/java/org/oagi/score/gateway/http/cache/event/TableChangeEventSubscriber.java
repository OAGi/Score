package org.oagi.score.gateway.http.cache.event;

import org.oagi.score.gateway.http.common.model.event.EventListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
public class TableChangeEventSubscriber implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private EventListenerContainer eventListenerContainer;

    @Override
    public void afterPropertiesSet() throws Exception {
        eventListenerContainer.addMessageListener(this,
                "onReceivedTableChangeEvent",
                new ChannelTopic("tableChangeEvent"));
    }

    public void onReceivedTableChangeEvent(TableChangeEvent tableChangeEvent) {
        logger.info("Received " + tableChangeEvent);
    }
}
