package org.oagi.score.gateway.http.common.async;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class RedisAsyncJobTransportTest {

    private final RedisTemplate template = mock(RedisTemplate.class);
    private final RedisMessageListenerContainer container = mock(RedisMessageListenerContainer.class);
    private final RedissonClient redisson = mock(RedissonClient.class);
    private final RLock lock = mock(RLock.class);
    private final ObjectProvider<RedissonClient> provider = mock(ObjectProvider.class);

    RedisAsyncJobTransportTest() {
        when(provider.getObject()).thenReturn(redisson);
        when(redisson.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
    }

    private MessageListener subscribed(RedisAsyncJobTransport transport, List<String> received) {
        transport.subscribe("cleanup", received::add);
        ArgumentCaptor<MessageListener> listener = ArgumentCaptor.forClass(MessageListener.class);
        ArgumentCaptor<ChannelTopic> topic = ArgumentCaptor.forClass(ChannelTopic.class);
        verify(container).addMessageListener(listener.capture(), topic.capture());
        assertEquals("cleanup", topic.getValue().getTopic());
        return listener.getValue();
    }

    private static DefaultMessage message(String payload) {
        return new DefaultMessage("cleanup".getBytes(StandardCharsets.UTF_8), payload.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void publishesTheTextAsItIsWithoutTheSerializerOfTheTemplate() {
        RedisAsyncJobTransport transport = new RedisAsyncJobTransport(template, container, provider, true);

        transport.send(new AsyncJobMessage("cleanup", "12", "{\"a\":1}"));

        ArgumentCaptor<RedisCallback> callback = ArgumentCaptor.forClass(RedisCallback.class);
        verify(template).execute(callback.capture());
        RedisConnection connection = mock(RedisConnection.class);
        callback.getValue().doInRedis(connection);
        verify(connection).publish("cleanup".getBytes(StandardCharsets.UTF_8), "{\"a\":1}".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void handsTheTextOfWhatIsReceivedToTheHandlerUnderALockOfTheMessage() {
        List<String> received = new ArrayList<>();
        MessageListener listener = subscribed(new RedisAsyncJobTransport(template, container, provider, true), received);

        listener.onMessage(message("{\"a\":1}"), null);

        assertEquals(List.of("{\"a\":1}"), received);
        verify(lock).unlock();
    }

    @Test
    void letsGoOfAMessageWhenAnotherInstanceHoldsItsLock() {
        when(lock.tryLock()).thenReturn(false);
        List<String> received = new ArrayList<>();
        MessageListener listener = subscribed(new RedisAsyncJobTransport(template, container, provider, true), received);

        listener.onMessage(message("{\"a\":1}"), null);

        assertTrue(received.isEmpty());
        verify(lock, never()).unlock();
    }

    @Test
    void lockTheSameMessageTheSameWayOnEveryInstanceAndDifferentOnesDifferently() {
        MessageListener listener = subscribed(new RedisAsyncJobTransport(template, container, provider, true), new ArrayList<>());

        listener.onMessage(message("one"), null);
        listener.onMessage(message("one"), null);
        listener.onMessage(message("two"), null);

        ArgumentCaptor<String> names = ArgumentCaptor.forClass(String.class);
        verify(redisson, times(3)).getLock(names.capture());
        assertEquals(names.getAllValues().get(0), names.getAllValues().get(1));
        assertTrue(!names.getAllValues().get(0).equals(names.getAllValues().get(2)));
    }

    @Test
    void releasesTheLockWhenTheHandlerFails() {
        RedisAsyncJobTransport transport = new RedisAsyncJobTransport(template, container, provider, true);
        transport.subscribe("cleanup", p -> {
            throw new IllegalStateException("x");
        });
        ArgumentCaptor<MessageListener> listener = ArgumentCaptor.forClass(MessageListener.class);
        verify(container).addMessageListener(listener.capture(), any(ChannelTopic.class));

        try {
            listener.getValue().onMessage(message("one"), null);
        } catch (IllegalStateException expected) {
        }

        verify(lock).unlock();
    }

    @Test
    void takesNoLockWhenTheSettingSaysTheMessageIsNotHandedToEveryInstance() {
        List<String> received = new ArrayList<>();
        MessageListener listener = subscribed(new RedisAsyncJobTransport(template, container, provider, false), received);

        listener.onMessage(message("one"), null);

        assertEquals(List.of("one"), received);
        verifyNoInteractions(redisson);
    }
}
