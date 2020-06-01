package org.oagi.srt.gateway.http.configuration.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import static org.springframework.session.data.redis.RedisIndexedSessionRepository.DEFAULT_NAMESPACE;

public class SafeDeserializationRepository<S extends Session> implements SessionRepository<S> {
    private final SessionRepository<S> delegate;
    private final RedisOperations<Object, Object> sessionRedisOperations;
    private final Log logger = LogFactory.getLog(getClass());

    private String namespace = DEFAULT_NAMESPACE + ":";

    public SafeDeserializationRepository(SessionRepository<S> delegate,
                                         RedisOperations<Object, Object> sessionRedisOperations) {
        this.delegate = delegate;
        this.sessionRedisOperations = sessionRedisOperations;
    }

    @Override
    public S createSession() {
        return delegate.createSession();
    }

    @Override
    public void save(S session) {
        delegate.save(session);
    }

    @Override
    public S findById(String id) {
        try {
            return delegate.findById(id);
        } catch (SerializationException e) {
            logger.warn("Deleting non-deserializable session with key " + id, e);

            String sessionKey = getSessionKey(id);
            this.sessionRedisOperations.delete(sessionKey);
            return null;
        }
    }

    String getSessionKey(String sessionId) {
        return this.namespace + "sessions:" + sessionId;
    }

    @Override
    public void deleteById(String id) {
        delegate.deleteById(id);
    }
}
