package org.oagi.score.gateway.http.change;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A {@link ChangeSignal} over a Redis channel. The message is the id of the instance that sent it; the others have the change log
 * read at once (without waiting for it), and the sender ignores its own, which has read the log already.
 * <p>
 * Without Redis, or when Redis does not answer, nothing is sent and nothing fails: the change log is read at the next poll.
 */
@Component
public class RedisChangeSignal implements ChangeSignal, MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisChangeSignal.class);

    static final String CHANNEL = "score:change:wake";

    private final ObjectProvider<RedisConnectionFactory> connectionFactory;
    private final ObjectProvider<RedisMessageListenerContainer> listenerContainer;
    private final ChangeLogReader reader;
    private final String instanceId = UUID.randomUUID().toString();

    private StringRedisTemplate template;
    private ThreadPoolExecutor sender;
    private boolean failureLogged;

    /**
     * The container is the application's own ({@code PublisherSubscriberConfiguration}); Spring Session and Boot make two more.
     * Lazy: the reader reads the change log with the DSLContext whose listener is what ends up calling {@link #announce()}. */
    public RedisChangeSignal(ObjectProvider<RedisConnectionFactory> connectionFactory,
                             @Qualifier("messageListenerContainer") ObjectProvider<RedisMessageListenerContainer> listenerContainer,
                             @Lazy ChangeLogReader reader) {
        this.connectionFactory = connectionFactory;
        this.listenerContainer = listenerContainer;
        this.reader = reader;
    }

    @PostConstruct
    void start() {
        RedisConnectionFactory factory = connectionFactory.getIfAvailable();
        RedisMessageListenerContainer container = listenerContainer.getIfAvailable();
        if (factory == null || container == null) {
            return;
        }
        template = new StringRedisTemplate(factory);
        // One send waiting is enough: the message says nothing but "read the log", so those that come meanwhile share it.
        sender = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1),
                runnable -> {
                    Thread thread = new Thread(runnable, "record-change-signal");
                    thread.setDaemon(true);
                    return thread;
                }, new ThreadPoolExecutor.DiscardPolicy());
        container.addMessageListener(this, new ChannelTopic(CHANNEL));
    }

    @PreDestroy
    void stop() {
        if (sender != null) {
            sender.shutdownNow();
        }
    }

    @Override
    public void announce() {
        ThreadPoolExecutor sender = this.sender;
        if (sender != null) {
            sender.execute(this::send);
        }
    }

    private void send() {
        try {
            template.convertAndSend(CHANNEL, instanceId);
            failureLogged = false;
        } catch (RuntimeException e) {
            if (!failureLogged) {
                failureLogged = true;
                logger.warn("The other instances could not be told of a change; they read the change log at their next poll.", e);
            }
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        if (!instanceId.equals(new String(message.getBody(), StandardCharsets.UTF_8))) {
            reader.catchUp(0);
        }
    }

}
