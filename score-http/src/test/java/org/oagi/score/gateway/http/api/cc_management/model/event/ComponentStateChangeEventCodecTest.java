package org.oagi.score.gateway.http.api.cc_management.model.event;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.service.ComponentStateChangeEventPublisher;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Proves the event survives the Redis queue round-trip with the shared JSON codec
 * ({@link ComponentStateChangeEventPublisher#COMPONENT_STATE_CHANGE_EVENT_CODEC}, issue #1533):
 * the {@code ManifestId} field is interface-typed and its concrete value is a Java record, both
 * of which the codec must carry across instances intact. JSON replaced Kryo so the payload is
 * schema-tolerant — a payload from a producer without the {@code comment} field decodes with
 * {@code comment == null}, and a payload with a field this consumer does not know yet still decodes.
 */
class ComponentStateChangeEventCodecTest {

    private static final Codec CODEC = ComponentStateChangeEventPublisher.COMPONENT_STATE_CHANGE_EVENT_CODEC;

    private static String encodeToJson(ComponentStateChangeEvent event) throws Exception {
        ByteBuf buf = CODEC.getValueEncoder().encode(event);
        try {
            return buf.toString(StandardCharsets.UTF_8);
        } finally {
            buf.release();
        }
    }

    private static ComponentStateChangeEvent decode(String json) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer(json, StandardCharsets.UTF_8);
        try {
            return (ComponentStateChangeEvent) CODEC.getValueDecoder().decode(buf, new State());
        } finally {
            buf.release();
        }
    }

    private static ComponentStateChangeEvent roundTrip(ComponentStateChangeEvent event) throws Exception {
        return decode(encodeToJson(event));
    }

    @Test
    void roundTripsWithATypedManifestId() throws Exception {
        ComponentStateChangeEvent event = new ComponentStateChangeEvent(
                CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, new UserId(BigInteger.ONE), "event-1",
                "Resolves the **mismatch** reported here.");

        ComponentStateChangeEvent decoded = roundTrip(event);

        assertThat(decoded).isEqualTo(event);
        assertThat(decoded.getManifestId()).isInstanceOf(AccManifestId.class);
    }

    @Test
    void roundTripsEveryOtherFieldShape() throws Exception {
        ComponentStateChangeEvent event = new ComponentStateChangeEvent(
                CcType.CODE_LIST, new CodeListManifestId(new BigInteger("12345678901234567890")),
                CcState.Candidate, CcState.WIP, new UserId(BigInteger.TWO), null, null);

        ComponentStateChangeEvent decoded = roundTrip(event);

        assertThat(decoded).isEqualTo(event);
        assertThat(decoded.getManifestId().value()).isEqualTo(new BigInteger("12345678901234567890"));
    }

    @Test
    void aPayloadWithoutTheCommentFieldDecodesWithANullComment() throws Exception {
        // An old producer's payload is exactly the serialization of the event minus the comment
        // property: encode an event carrying a comment, strip the property out, and decode.
        ComponentStateChangeEvent event = new ComponentStateChangeEvent(
                CcType.DT, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, new UserId(BigInteger.ONE), "event-1", "a comment");
        String json = encodeToJson(event);
        String oldProducerJson = json.replace("\"comment\":\"a comment\",", "").replace(",\"comment\":\"a comment\"", "");
        assertThat(oldProducerJson).isNotEqualTo(json).doesNotContain("comment");

        ComponentStateChangeEvent decoded = decode(oldProducerJson);

        assertThat(decoded.getComment()).isNull();
        event.setComment(null);
        assertThat(decoded).isEqualTo(event);
    }

    @Test
    void aNullCommentIsOmittedFromThePayload() throws Exception {
        // NON_NULL inclusion: the wire shape of a comment-less event is identical to the old
        // producer's, so mixed-version consumers never see an explicit null to trip over.
        ComponentStateChangeEvent event = new ComponentStateChangeEvent(
                CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, new UserId(BigInteger.ONE), "event-1", null);

        assertThat(encodeToJson(event)).doesNotContain("comment");
    }

    @Test
    void aPayloadWithAnUnknownExtraFieldStillDecodes() throws Exception {
        ComponentStateChangeEvent event = new ComponentStateChangeEvent(
                CcType.ACC, new AccManifestId(BigInteger.TEN),
                CcState.Draft, CcState.Candidate, new UserId(BigInteger.ONE), "event-1", "a comment");
        String json = encodeToJson(event);
        String futureProducerJson = json.substring(0, json.length() - 1) + ",\"futureField\":\"x\"}";

        assertThatCode(() -> {
            ComponentStateChangeEvent decoded = decode(futureProducerJson);
            assertThat(decoded).isEqualTo(event);
        }).doesNotThrowAnyException();
    }

}
