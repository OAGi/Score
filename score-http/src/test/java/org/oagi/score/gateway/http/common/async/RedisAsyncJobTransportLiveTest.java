package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.event.ReleaseCleanupEvent;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The transport against a real Redis, on a channel of its own that only this test uses (nothing is flushed or kept):
 * an event published as the application publishes it arrives as the same event, and two instances that are both listening
 * handle it once between them. Run with {@code -Dscore.redis=true}, and {@code -Dscore.redis.port=...} when it is not 6379.
 */
@EnabledIfSystemProperty(named = "score.redis", matches = "true")
class RedisAsyncJobTransportLiveTest {

    static LettuceConnectionFactory factory;
    static RedisMessageListenerContainer container;
    static RedissonClient redisson;
    static RedisTemplate<String, Object> template;

    @BeforeAll
    static void open() {
        int port = Integer.getInteger("score.redis.port", 6379);
        factory = new LettuceConnectionFactory("localhost", port);
        factory.afterPropertiesSet();
        template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.afterPropertiesSet();
        container.start();
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:" + port);
        redisson = Redisson.create(config);
    }

    @AfterAll
    static void close() {
        redisson.shutdown();
        container.stop();
        factory.destroy();
    }

    @SuppressWarnings("unchecked")
    private RedisAsyncJobTransport instance(boolean exclusive) {
        ObjectProvider<RedissonClient> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(redisson);
        return new RedisAsyncJobTransport(template, container, provider, exclusive);
    }

    private static void awaitQuietly(List<?> received, int count) throws InterruptedException {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (received.size() < count && System.nanoTime() < end) {
            Thread.sleep(20);
        }
        Thread.sleep(300); // a second delivery, if there is one, has arrived by now
    }

    @Test
    void anEventPublishedAsTheApplicationPublishesItArrivesAsTheSameEventAndIsHandledOnceByTwoInstances() throws Exception {
        String channel = "asyncJobLiveTest:" + UUID.randomUUID();
        AsyncJobCodec codec = new AsyncJobCodec();
        List<ReleaseCleanupEvent> handled = new CopyOnWriteArrayList<>();
        RedisAsyncJobTransport first = instance(true);
        RedisAsyncJobTransport second = instance(true);
        new AsyncJobEvents(first, codec).subscribe(channel, ReleaseCleanupEvent.class, handled::add);
        new AsyncJobEvents(second, codec).subscribe(channel, ReleaseCleanupEvent.class, e -> {
            try {
                Thread.sleep(200); // still handling when the other's copy arrives
            } catch (InterruptedException ignored) {
            }
            handled.add(e);
        });
        Thread.sleep(300); // the subscriptions are in place

        ReleaseCleanupEvent event = new ReleaseCleanupEvent(new UserId(BigInteger.valueOf(7)), new ReleaseId(BigInteger.valueOf(12)));
        new AfterCommitAsyncJobPublisher(codec, first).publish(channel, "12", event);
        awaitQuietly(handled, 1);

        assertEquals(List.of(event), handled);
    }
}
