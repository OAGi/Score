package org.oagi.score.gateway.http.api.cc_management.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.event.ComponentStateChangeEvent;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Publishes {@link ComponentStateChangeEvent}s to the {@code componentStateChangeEventQueueV2}
 * Redis queue (issue #1533). A queue — not a pub/sub topic — so each event is consumed by exactly
 * one backend instance no matter how many subscribe (competing consumers), and events published
 * while no consumer is up wait in the queue instead of being lost.
 *
 * <p>Implicit moves ({@link CcState#isImplicitMove}) are not published: the release lifecycle
 * moves every component of a release in bulk (Candidate → ReleaseDraft → Published, and back to
 * Candidate when a draft is cancelled), which would flood the queue with thousands of messages
 * no consumer acts on. Only explicit, user-driven transitions go out.</p>
 *
 * <p>The event is sent only <em>after the surrounding transaction commits</em> — i.e. after every
 * operation of the state change (the state update, the log entry, and anything else the
 * transaction does) is complete and durable. Consumers read the database, so publishing inside
 * the transaction would let them observe the pre-change state — or react to a transition that
 * ends up rolled back; when {@code updateState} runs inside a larger transaction, the event waits
 * for the <em>outermost</em> commit. Without an active transaction there is nothing pending and
 * the event is sent immediately. Spring does <em>not</em> catch exceptions thrown from
 * {@code afterCommit} (they would surface as an error for an already-committed state change and
 * skip the remaining synchronizations), so a Redis failure is logged and swallowed here.</p>
 */
@Component
public class ComponentStateChangeEventPublisher {

    /**
     * The {@code V2} suffix marks the JSON-payload generation of the queue: the original
     * {@code componentStateChangeEventQueue} carried Kryo-serialized events, and a JSON decoder
     * must never meet a leftover Kryo payload (or vice versa during a rolling upgrade).
     */
    public static final String COMPONENT_STATE_CHANGE_EVENT_QUEUE = "componentStateChangeEventQueueV2";

    /**
     * The JSON codec shared by this publisher and {@code GitHubStatusPostEventSubscriber} — both
     * sides must address the queue with the same codec, or the payloads would not decode. JSON
     * (instead of Redisson's default Kryo) keeps the queue payload schema-tolerant: a consumer can
     * decode events produced before a field existed (the field stays {@code null}) and events
     * carrying fields it does not know yet (ignored), so publisher and consumer can be upgraded
     * independently. {@link JsonJacksonCodec}'s mapper provides most of what the event needs:
     * class-typed default typing (the interface-typed {@link ManifestId} field is restored to its
     * concrete record, e.g. {@code ["...AccManifestId",10]}), {@code FAIL_ON_UNKNOWN_PROPERTIES}
     * disabled, and {@code NON_NULL} inclusion (an absent comment is simply omitted).
     *
     * <p>One stock rule must be overridden: {@code java.math.BigInteger} is a <em>non-final</em>
     * class, so Redisson's {@code NON_FINAL} default typing wants type info for it — but the id
     * records serialize through {@code @JsonValue BigInteger value()} (emitting a bare scalar,
     * no wrapper) and deserialize through their delegating {@code from(BigInteger)} creators
     * (which, typed, would demand a {@code ["java.math.BigInteger",…]} wrapper the encoder never
     * writes). Excluding {@code BigInteger} from default typing — exactly like the stock codec
     * already excludes {@code XMLGregorianCalendar} — keeps both directions symmetric: the scalar
     * id value round-trips plainly while the polymorphic types keep their {@code @class} info.</p>
     */
    public static final Codec COMPONENT_STATE_CHANGE_EVENT_CODEC = new JsonJacksonCodec() {
        @Override
        protected void initTypeInclusion(ObjectMapper mapObjectMapper) {
            TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(
                    ObjectMapper.DefaultTyping.NON_FINAL, mapObjectMapper.getPolymorphicTypeValidator()) {
                @Override
                public boolean useForType(JavaType t) {
                    while (t.isArrayType()) {
                        t = t.getContentType();
                    }
                    // Same Long fix as the stock codec (a bare JSON int would otherwise come back as Integer).
                    if (t.getRawClass() == Long.class) {
                        return true;
                    }
                    // The @JsonValue/@JsonCreator scalar of the id records: see the constant's javadoc.
                    if (t.getRawClass() == BigInteger.class) {
                        return false;
                    }
                    return !t.isFinal(); // includes Object.class
                }
            };
            typer.init(JsonTypeInfo.Id.CLASS, null);
            typer.inclusion(JsonTypeInfo.As.PROPERTY);
            mapObjectMapper.setDefaultTyping(typer);
        }
    };

    /**
     * Comments longer than this are truncated before publishing (issue #1533, sub-task 5). The
     * comment is the whole rendered GitHub post (pre-filled by the frontend, edited by the user),
     * so the cap sits just under GitHub's hard 65,536-character comment limit.
     */
    public static final int MAX_COMMENT_LENGTH = 60_000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedissonClient redissonClient;

    public void publish(CcType ccType, ManifestId manifestId,
                        CcState prevState, CcState nextState, UserId userId) {
        publish(ccType, manifestId, prevState, nextState, userId, null);
    }

    /**
     * Publishes the state change with the user-approved GitHub status comment ({@code null} when
     * the user cleared it — nothing will be posted). The comment is normalized in this single
     * place — trimmed, blank-to-null, truncated to {@value #MAX_COMMENT_LENGTH} characters — so
     * every consumer sees the same canonical value.
     */
    public void publish(CcType ccType, ManifestId manifestId,
                        CcState prevState, CcState nextState, UserId userId, String comment) {
        if (prevState != null && prevState.isImplicitMove(nextState)) {
            return;
        }

        ComponentStateChangeEvent event = new ComponentStateChangeEvent(
                ccType, manifestId, prevState, nextState, userId, UUID.randomUUID().toString(),
                normalizeComment(comment));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private static String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() <= MAX_COMMENT_LENGTH) {
            return trimmed;
        }
        // Never cut through a surrogate pair: an unpaired surrogate is not encodable as UTF-8
        // and would make GitHub reject the whole status comment.
        int cut = MAX_COMMENT_LENGTH;
        if (Character.isHighSurrogate(trimmed.charAt(cut - 1))) {
            cut--;
        }
        return trimmed.substring(0, cut);
    }

    private void send(ComponentStateChangeEvent event) {
        try {
            redissonClient.getBlockingQueue(COMPONENT_STATE_CHANGE_EVENT_QUEUE, COMPONENT_STATE_CHANGE_EVENT_CODEC)
                    .offer(event);
        } catch (Exception e) {
            logger.warn("Failed to publish the component state change event for {} {} ({} -> {}).",
                    event.getCcType(), event.getManifestId(), event.getPrevState(), event.getNextState(), e);
        }
    }

}
