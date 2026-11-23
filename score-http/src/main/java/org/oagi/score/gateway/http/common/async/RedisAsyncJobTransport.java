package org.oagi.score.gateway.http.common.async;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Redis pub/sub: the transport in use unless {@code score.async.transport} says another. A message is handed to every
 * instance that is running, and one that is sent while none is listening is lost (what takes back the work that was
 * waiting for it is {@link StaleJobSweeper}); the key is of no use to it.
 *
 * <p>Since every instance gets the message, only one of them is to handle it: with {@code score.async.redis.exclusive}
 * (the default) the instance that handles it holds a lock of the message for as long as it does, and an instance that does
 * not get the lock lets it go. The text goes as it is, not through the serializer of the template, whose typing is for
 * the values it keeps.</p>
 */
@Component
@ConditionalOnProperty(name = "score.async.transport", havingValue = "redis", matchIfMissing = true)
public class RedisAsyncJobTransport implements AsyncJobTransport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RedisTemplate<?, ?> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectProvider<RedissonClient> redissonClient;
    private final boolean exclusive;

    public RedisAsyncJobTransport(@Qualifier("redisTemplate") RedisTemplate<?, ?> redisTemplate,
                                  @Qualifier("messageListenerContainer") RedisMessageListenerContainer listenerContainer,
                                  ObjectProvider<RedissonClient> redissonClient,
                                  @Value("${score.async.redis.exclusive:true}") boolean exclusive) {
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        this.redissonClient = redissonClient;
        this.exclusive = exclusive;
    }

    @Override
    public void send(AsyncJobMessage message) {
        byte[] channel = message.channel().getBytes(StandardCharsets.UTF_8);
        byte[] payload = message.payload().getBytes(StandardCharsets.UTF_8);
        redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Long>) (RedisConnection connection) ->
                connection.publish(channel, payload));
    }

    @Override
    public void subscribe(String channel, Consumer<String> handler) {
        listenerContainer.addMessageListener((message, pattern) ->
                        handle(channel, new String(message.getBody(), StandardCharsets.UTF_8), handler),
                new ChannelTopic(channel));
    }

    private void handle(String channel, String payload, Consumer<String> handler) {
        if (!exclusive) {
            handler.accept(payload);
            return;
        }
        RLock lock = redissonClient.getObject().getLock("AsyncJob:" + channel + ":"
                + UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8)));
        if (!lock.tryLock()) {
            logger.debug("Another instance handles the event on " + channel);
            return;
        }
        try {
            handler.accept(payload);
        } finally {
            lock.unlock();
        }
    }
}
