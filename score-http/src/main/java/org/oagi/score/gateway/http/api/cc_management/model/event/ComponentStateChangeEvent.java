package org.oagi.score.gateway.http.api.cc_management.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.common.model.event.Event;

/**
 * Put on the {@code componentStateChangeEventQueueV2} Redis queue after an <em>explicit</em>
 * component state change has been committed (issue #1533) — every successful {@code updateState}
 * of an ACC/ASCCP/BCCP/DT/code list/agency ID list except the implicit bulk moves of the release
 * lifecycle, which the publisher filters out. The queue is point-to-point: exactly one backend
 * instance consumes each event, and the consumer decides which transitions it cares about.
 *
 * <p>Carries the typed {@link ManifestId} (the concrete class travels with the codec) plus the
 * {@link CcType} discriminator, and the requesting user as {@link UserId} so the consuming
 * instance can re-resolve the {@code ScoreUser}. The {@code eventId} uniquely identifies this
 * occurrence — two identical transitions in a row are still two distinct events — for logging and
 * tracing.</p>
 *
 * <p>The {@code comment} is the GitHub status comment accompanying the state change (issue #1533,
 * sub-task 5): the frontend pre-fills it with the rendered change summary, the acting user edits
 * it freely, and consumers post it <em>verbatim</em> — no further composition happens on the
 * backend. {@code null} means the user cleared it (opt-out: nothing is posted). It is normalized
 * (trimmed, blank-to-null, length-capped) by the publisher before the event is built — events
 * serialized before the field existed decode with {@code comment == null}.</p>
 *
 * <p>The {@code projectFieldOptionOverride} is an optional user-chosen Projects v2 board fieldOption (issue #1533,
 * Feature 2) for this transition: the state-change dialog lets the acting user override the
 * config-derived destination fieldOption via a dropdown of the board field's options. {@code null} (the
 * usual case) means "use the configured fieldOption for the destination state". When present, the fieldOption
 * sync moves the linked issue to this fieldOption instead — see
 * {@code GitHubStatusPostEventSubscriber.syncProjectFieldOption}. Normalized (trimmed, blank-to-null,
 * length-capped) by the publisher; events serialized before the field existed decode with it
 * {@code null}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentStateChangeEvent implements Event {

    private CcType ccType;

    private ManifestId manifestId;

    private CcState prevState;

    private CcState nextState;

    private UserId userId;

    private String eventId;

    private String comment;

    private String projectFieldOptionOverride;

}
