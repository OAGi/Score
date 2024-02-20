package org.oagi.score.redis.configuration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
public class RedisConfiguration {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.sentinel.master}")
    private String redisSentinelMaster;

    @Value("${spring.data.redis.sentinel.nodes}")
    private String redisSentinelNodes;

    @Value("${spring.data.redis.cluster.nodes}")
    private String redisClusterNodes;

    private Set<String> getRedisSentinelNodes() {
        if (StringUtils.hasLength(redisSentinelNodes)) {
            return Arrays.asList(redisSentinelNodes.split(",")).stream()
                    .map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private List<String> getRedisClusterNodes() {
        if (StringUtils.hasLength(redisClusterNodes)) {
            return Arrays.asList(redisClusterNodes.split(",")).stream()
                    .map(e -> e.trim()).filter(e -> StringUtils.hasLength(e)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        org.springframework.data.redis.connection.RedisConfiguration redisConfiguration;
        List<String> redisClusterNodes = getRedisClusterNodes();
        if (!redisClusterNodes.isEmpty()) {
            redisConfiguration = new RedisClusterConfiguration(redisClusterNodes);
        } else if (StringUtils.hasLength(redisSentinelMaster)) {
            Set<String> redisSentinelNodes = getRedisSentinelNodes();
            redisConfiguration = new RedisSentinelConfiguration(redisSentinelMaster, redisSentinelNodes);
        } else {
            redisConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
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
        } else if (StringUtils.hasLength(redisSentinelMaster)) {
            Set<String> redisSentinelNodes = getRedisSentinelNodes();
            config.useSentinelServers()
                    .setMasterName(redisSentinelMaster)
                    .setSentinelAddresses(redisSentinelNodes.stream().map(e -> {
                        if (e.startsWith("redis://") || e.startsWith("rediss://")) {
                            return e;
                        }
                        return "redis://" + e;
                    }).collect(Collectors.toList()));
        } else {
            config.useSingleServer()
                    .setAddress("redis://" + redisHost + ":" + redisPort);
        }
        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate redisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();

        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setEnableTransactionSupport(false);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }
}
