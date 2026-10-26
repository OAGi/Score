package org.oagi.score.gateway.http.configuration.redis;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.event.BieCopyRequestEvent;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedisEventValueSerializerTest {

    @Test
    void roundTripsAPubSubEventWithTypedIds() {
        var serializer = RedisConfiguration.eventValueSerializer();
        var event = new BieCopyRequestEvent(
                new UserId(BigInteger.valueOf(7)),
                new TopLevelAsbiepId(BigInteger.valueOf(11)),
                new TopLevelAsbiepId(new BigInteger("12345678901234567890")),
                List.of(new BusinessContextId(BigInteger.valueOf(3))));

        Object decoded = serializer.deserialize(serializer.serialize(event));

        assertThat(decoded).isInstanceOf(BieCopyRequestEvent.class);
        var copy = (BieCopyRequestEvent) decoded;
        assertThat(copy.getUserId()).isEqualTo(event.getUserId());
        assertThat(copy.getSourceTopLevelAsbiepId()).isEqualTo(event.getSourceTopLevelAsbiepId());
        assertThat(copy.getCopiedTopLevelAsbiepId()).isEqualTo(event.getCopiedTopLevelAsbiepId());
        assertThat(copy.getBizCtxIdList()).isEqualTo(event.getBizCtxIdList());
    }
}
