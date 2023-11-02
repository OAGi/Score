package org.oagi.score.gateway.http.configuration.intializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisFlushAllInitializer implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Environment environment;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!environment.matchesProfiles("dev")) {
            logger.info("Remove all keys from redis databases.");
            try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
                connection.serverCommands().flushAll();
            }
        }
    }

}
