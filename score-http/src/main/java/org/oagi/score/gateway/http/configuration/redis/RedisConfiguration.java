package org.oagi.score.gateway.http.configuration.redis;

import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.PlaceholderResolutionException;
import org.springframework.util.StringUtils;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.jsontype.impl.DefaultTypeResolverBuilder;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
public class RedisConfiguration {

    @Autowired
    private Environment environment;

    private String getProperty(String key, String defaultValue) {
        try {
            return environment.getProperty(key, defaultValue);
        } catch (PlaceholderResolutionException e) {
            return defaultValue;
        }
    }

    public final String redisHost() {
        return getProperty("spring.data.redis.host", "localhost");
    }

    public final String redisPort() {
        return getProperty("spring.data.redis.port", "6379");
    }

    public final String redisSentinelMaster() {
        return getProperty("spring.data.redis.sentinel.master", "");
    }

    public final String redisSentinelNodes() {
        return getProperty("spring.data.redis.sentinel.nodes", "");
    }

    public final String redisClusterNodes() {
        return getProperty("spring.data.redis.cluster.nodes", "");
    }

    private Set<String> getRedisSentinelNodes() {
        if (StringUtils.hasLength(redisSentinelNodes())) {
            return Arrays.asList(redisSentinelNodes().split(",")).stream()
                    .map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private List<String> getRedisClusterNodes() {
        if (StringUtils.hasLength(redisClusterNodes())) {
            return Arrays.asList(redisClusterNodes().split(",")).stream()
                    .map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        org.springframework.data.redis.connection.RedisConfiguration redisConfiguration;
        List<String> redisClusterNodes = getRedisClusterNodes();
        if (!redisClusterNodes.isEmpty()) {
            // Without these a failed node keeps being asked, and every command to it waits for the (one minute) time-out,
            // because the client learns of the new master only when told to look at the cluster again.
            ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                    .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                            .enablePeriodicRefresh(Duration.ofSeconds(30))
                            .enableAllAdaptiveRefreshTriggers()
                            .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(5))
                            .build())
                    .build();
            LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                    .clientOptions(clusterClientOptions)
                    .commandTimeout(Duration.ofSeconds(5))
                    .build();
            return new LettuceConnectionFactory(new RedisClusterConfiguration(redisClusterNodes), clientConfiguration);
        } else if (StringUtils.hasLength(redisSentinelMaster())) {
            Set<String> redisSentinelNodes = getRedisSentinelNodes();
            redisConfiguration = new RedisSentinelConfiguration(redisSentinelMaster(), redisSentinelNodes);
        } else {
            redisConfiguration = new RedisStandaloneConfiguration(redisHost(), Integer.parseInt(redisPort()));
        }
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfiguration);
        return connectionFactory;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setTransportMode(TransportMode.NIO);
        List<String> redisClusterNodes = getRedisClusterNodes();
        if (!redisClusterNodes.isEmpty()) {
            config.useClusterServers()
                    .setNodeAddresses(redisClusterNodes.stream().map(e -> {
                        if (e.startsWith("redis://") || e.startsWith("rediss://")) {
                            return e;
                        }
                        return "redis://" + e;
                    }).collect(Collectors.toList()));
        } else if (StringUtils.hasLength(redisSentinelMaster())) {
            Set<String> redisSentinelNodes = getRedisSentinelNodes();
            config.useSentinelServers()
                    .setMasterName(redisSentinelMaster())
                    .setSentinelAddresses(redisSentinelNodes.stream().map(e -> {
                        if (e.startsWith("redis://") || e.startsWith("rediss://")) {
                            return e;
                        }
                        return "redis://" + e;
                    }).collect(Collectors.toList()));
        } else {
            config.useSingleServer()
                    .setAddress("redis://" + redisHost() + ":" + redisPort());
        }
        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate redisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();

        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setEnableTransactionSupport(false);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(eventValueSerializer());

        return redisTemplate;
    }

    /**
     * Events carry id records that serialize as a bare {@code BigInteger} scalar ({@code @JsonValue}) and
     * deserialize through a delegating {@code @JsonCreator}. Default typing must therefore skip
     * {@code BigInteger}, or the reader demands a type wrapper the writer never emits.
     */
    static GenericJacksonJsonRedisSerializer eventValueSerializer() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        JsonMapper mapper = JsonMapper.builder()
                .setDefaultTyping(new DefaultTypeResolverBuilder(typeValidator, DefaultTyping.NON_FINAL, "@class") {
                    @Override
                    public boolean useForType(JavaType t) {
                        return t.getRawClass() != BigInteger.class && super.useForType(t);
                    }
                })
                .build();
        return new GenericJacksonJsonRedisSerializer(mapper);
    }
}
