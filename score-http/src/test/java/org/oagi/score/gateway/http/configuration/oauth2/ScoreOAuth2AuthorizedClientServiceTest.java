package org.oagi.score.gateway.http.configuration.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.mockito.Mockito.*;

class ScoreOAuth2AuthorizedClientServiceTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void removeAuthorizedClientDeletesEachStoredHashField() {
        RedisOperations<Object, Object> redis = mock(RedisOperations.class);
        HashOperations hashOperations = mock(HashOperations.class);
        when(redis.opsForHash()).thenReturn(hashOperations);
        var service = new ScoreOAuth2AuthorizedClientService(redis, mock(ClientRegistrationRepository.class));

        service.removeAuthorizedClient("registration", "principal");

        verify(hashOperations).delete("score:oauth2:authorized_client:registration:principal",
                "client_registration_id", "principal_name",
                "access_token_type", "access_token_value",
                "access_token_issued_at", "access_token_expires_at", "access_token_scopes",
                "refresh_token_value", "refresh_token_issued_at", "created_at");
    }
}
