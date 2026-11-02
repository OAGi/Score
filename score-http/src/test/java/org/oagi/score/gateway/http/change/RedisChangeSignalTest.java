package org.oagi.score.gateway.http.change;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.DefaultMessage;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class RedisChangeSignalTest {

    @SuppressWarnings("unchecked")
    private static RedisChangeSignal signal(ChangeLogReader reader) {
        return new RedisChangeSignal(mock(ObjectProvider.class), mock(ObjectProvider.class), reader);
    }

    private static DefaultMessage message(String body) {
        return new DefaultMessage(RedisChangeSignal.CHANNEL.getBytes(StandardCharsets.UTF_8), body.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void withoutRedisAnnouncingDoesNothing() {
        RedisChangeSignal signal = signal(mock(ChangeLogReader.class));
        signal.start();
        assertDoesNotThrow(signal::announce);
    }

    @Test
    void aSignalOfAnotherInstanceHasTheLogReadWithoutWaiting() {
        ChangeLogReader reader = mock(ChangeLogReader.class);
        signal(reader).onMessage(message("another-instance"), null);
        verify(reader).catchUp(0);
    }

    @Test
    void theSignalOfThisInstanceIsIgnored() throws Exception {
        ChangeLogReader reader = mock(ChangeLogReader.class);
        RedisChangeSignal signal = signal(reader);
        var field = RedisChangeSignal.class.getDeclaredField("instanceId");
        field.setAccessible(true);
        signal.onMessage(message((String) field.get(signal)), null);
        verifyNoInteractions(reader);
    }
}
