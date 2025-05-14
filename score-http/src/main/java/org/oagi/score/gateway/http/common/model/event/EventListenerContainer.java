package org.oagi.score.gateway.http.common.model.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class EventListenerContainer {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisMessageListenerContainer messageListenerContainer;

    public void addMessageListener(Object delegate, String defaultListenerMethod, Topic topic) {
        MessageListenerAdapter listenerAdapter =
                new MessageListenerAdapter(delegate, defaultListenerMethod);
        listenerAdapter.setSerializer(redisTemplate.getValueSerializer());
        listenerAdapter.afterPropertiesSet();

        messageListenerContainer.addMessageListener(listenerAdapter, topic);
    }

}
