package org.oagi.score.gateway.http.configuration.redis;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Lettuce and Redisson must connect to the same Redis whichever topology the environment selects.
 */
class RedisConfigurationTopologyTest {

    private static RedisConfiguration configuration(MockEnvironment environment) {
        var configuration = new RedisConfiguration();
        ReflectionTestUtils.setField(configuration, "environment", environment);
        return configuration;
    }

    private static String redissonYaml(RedisConfiguration configuration) throws Exception {
        return RedisConfiguration.redissonConfig(configuration.redisTopology()).toYAML();
    }

    @Test
    void standaloneIsTheDefault() throws Exception {
        var configuration = configuration(new MockEnvironment()
                .withProperty("spring.data.redis.host", "redis-a").withProperty("spring.data.redis.port", "6380"));

        var lettuce = (LettuceConnectionFactory) configuration.redisConnectionFactory();

        assertThat(lettuce.getHostName()).isEqualTo("redis-a");
        assertThat(lettuce.getPort()).isEqualTo(6380);
        assertThat(redissonYaml(configuration)).contains("singleServerConfig").contains("redis://redis-a:6380");
    }

    @Test
    void sentinelUsesTheSameMasterAndNodes() throws Exception {
        var configuration = configuration(new MockEnvironment()
                .withProperty("spring.data.redis.sentinel.master", "mymaster")
                .withProperty("spring.data.redis.sentinel.nodes", "s1:26379, s2:26380"));

        var lettuce = (LettuceConnectionFactory) configuration.redisConnectionFactory();

        assertThat(lettuce.getSentinelConfiguration().getMaster().getName()).isEqualTo("mymaster");
        assertThat(lettuce.getSentinelConfiguration().getSentinels()).extracting("host", "port")
                .containsExactlyInAnyOrder(tuple("s1", 26379), tuple("s2", 26380));
        assertThat(redissonYaml(configuration)).contains("sentinelServersConfig").contains("mymaster")
                .contains("redis://s1:26379").contains("redis://s2:26380");
    }

    @Test
    void clusterUsesTheSameNodesAndWinsOverSentinel() throws Exception {
        var configuration = configuration(new MockEnvironment()
                .withProperty("spring.data.redis.cluster.nodes", "c1:7000, c2:7001")
                .withProperty("spring.data.redis.sentinel.master", "ignored"));

        var lettuce = (LettuceConnectionFactory) configuration.redisConnectionFactory();

        assertThat(lettuce.getClusterConfiguration()).isNotNull();
        assertThat(lettuce.getClusterConfiguration().getClusterNodes()).extracting("host", "port")
                .containsExactlyInAnyOrder(tuple("c1", 7000), tuple("c2", 7001));
        assertThat(redissonYaml(configuration)).contains("clusterServersConfig")
                .contains("redis://c1:7000").contains("redis://c2:7001");
    }
}
