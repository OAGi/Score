package org.oagi.score.gateway.http.api.oas_management.service.generate_openapi_expression;

import org.oagi.score.gateway.http.api.oas_management.model.OasOperationId;
import org.oagi.score.gateway.http.api.oas_management.model.OpenAPITemplateForVerbOption;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Guards the OAS Doc generator against an illegal path-item collapse.
 *
 * <p>An OpenAPI {@code paths} object can hold only ONE operation per (path, verb). The generator keys
 * {@code root.paths} by the resolved resource path then the verb and, on a key collision, REUSES the
 * existing operation map — so two DISTINCT {@code oas_operation}s that resolve to the same (path, verb)
 * would silently merge/clobber each other's body, responses and identity (and collapse in the #1347
 * error-response post-step) with no error surfaced to the user. Nothing at the DB, service, or UI layer
 * forbids this, so it is rejected here, up front, before any generation work.
 *
 * <p>The generator collapses every template of a (path, verb) into a SINGLE operation map: a Request
 * template fills {@code requestBody} and a Response template fills {@code responses}. That merge is lossless
 * as long as the templates are <em>complementary</em>: at most one distinct operation supplies the request
 * body and at most one supplies the responses, so neither slot is silently clobbered. A single operation may
 * legitimately contribute many templates (multiple media types, extra/confirm responses) — those all share
 * one {@code oasOperationId} and never trip the guard. This admits the legacy/imported shape where a
 * Request-only operation and a Response-only operation share the same (path, verb) (pre-#1492 docs stored the
 * Request and Response as two separate {@code oas_operation} rows): they merge into one correct path item.
 * The two operations may even carry different {@code operationId}s — each is auto-derived from its own message
 * BIE (issue #1732) and request and response BIEs commonly differ — and the merged path item simply takes
 * one; that is not a collision. A genuine collision is two distinct operations fighting over the same body
 * slot (two request bodies, or two responses, on one endpoint). The resolved path mirrors the generator's own
 * {@link OpenAPI30GenerateExpression#resolveResourceName(String, String)} (the {@code {version}}
 * substitution), so two paths that differ only by the version placeholder are caught too.
 */
public final class OasOperationCollisionValidator {

    private OasOperationCollisionValidator() {
    }

    /** One offending endpoint: a resolved (path, verb) reached by more than one distinct operation. */
    public static final class Collision {
        public final String pathKey;
        public final String verbKey;
        public final List<BigInteger> operationIds; // distinct, in first-seen order

        public Collision(String pathKey, String verbKey, List<BigInteger> operationIds) {
            this.pathKey = pathKey;
            this.verbKey = verbKey;
            this.operationIds = operationIds;
        }
    }

    /**
     * Detect every (path, verb) whose operations cannot be safely merged into one path item: more than one
     * distinct operation supplying the request body, or more than one supplying the responses. Templates with
     * no verb or no {@code oasOperationId} are skipped (they cannot be attributed to an operation). Returns an
     * empty list when the document is well-formed.
     */
    public static List<Collision> detectCollisions(Collection<OpenAPITemplateForVerbOption> templates,
                                                   String documentVersion) {
        List<Collision> collisions = new ArrayList<>();
        if (templates == null || templates.isEmpty()) {
            return collisions;
        }
        // group key "<resolvedPath> <verb>" -> accumulator of how its templates would merge
        Map<String, GroupAccumulator> groups = new LinkedHashMap<>();
        for (OpenAPITemplateForVerbOption template : templates) {
            if (template.getVerbOption() == null || template.getOasOperationId() == null) {
                continue;
            }
            String pathKey = OpenAPI30GenerateExpression.resolveResourceName(template.getResourceName(), documentVersion);
            String verbKey = template.getVerbOption().name().toLowerCase();
            groups.computeIfAbsent(pathKey + " " + verbKey, k -> new GroupAccumulator(pathKey, verbKey))
                    .add(template);
        }
        for (GroupAccumulator group : groups.values()) {
            if (group.isCollision()) {
                collisions.add(new Collision(group.pathKey, group.verbKey, new ArrayList<>(group.allOpIds)));
            }
        }
        return collisions;
    }

    /**
     * Accumulates, per (path, verb), which distinct operations write the request-body slot and which write
     * the responses slot — the two ways the generator's single-path-item merge can silently clobber a body.
     */
    private static final class GroupAccumulator {
        private final String pathKey;
        private final String verbKey;
        private final LinkedHashSet<BigInteger> allOpIds = new LinkedHashSet<>();      // for the report, first-seen order
        private final LinkedHashSet<BigInteger> requestOpIds = new LinkedHashSet<>();  // distinct ops filling requestBody
        private final LinkedHashSet<BigInteger> responseOpIds = new LinkedHashSet<>(); // distinct ops filling responses

        GroupAccumulator(String pathKey, String verbKey) {
            this.pathKey = pathKey;
            this.verbKey = verbKey;
        }

        void add(OpenAPITemplateForVerbOption template) {
            BigInteger opId = template.getOasOperationId().value();
            allOpIds.add(opId);
            String messageBody = template.getMessageBodyType();
            if ("Request".equalsIgnoreCase(messageBody)) {
                requestOpIds.add(opId);
            } else if ("Response".equalsIgnoreCase(messageBody)) {
                responseOpIds.add(opId);
            }
        }

        boolean isCollision() {
            // Two distinct operations fighting over the same slot would clobber one of the bodies. A single
            // operation contributing many templates (media types, extra responses) shares one op id, so it
            // never trips this; complementary Request-only + Response-only operations merge cleanly.
            return requestOpIds.size() > 1 || responseOpIds.size() > 1;
        }
    }

    /**
     * Throw {@link IllegalArgumentException} (mapped to HTTP 400 with the message) if any (path, verb) is
     * mapped by more than one operation; otherwise return quietly. This is a client-side / invalid-document
     * condition, so it surfaces as a Bad Request rather than a generation (server) failure.
     */
    public static void assertNoCollision(Collection<OpenAPITemplateForVerbOption> templates,
                                         String documentVersion) {
        List<Collision> collisions = detectCollisions(templates, documentVersion);
        if (!collisions.isEmpty()) {
            throw new IllegalArgumentException(buildMessage(collisions));
        }
    }

    private static String buildMessage(List<Collision> collisions) {
        String detail = collisions.stream()
                .map(c -> c.verbKey.toUpperCase() + " " + c.pathKey + " (operation ids "
                        + c.operationIds.stream().map(BigInteger::toString).collect(Collectors.joining(", ")) + ")")
                .collect(Collectors.joining("; "));
        return "Each (Resource Name, Verb) can be defined by only one operation, but the following endpoint(s) are "
                + "mapped by multiple operations: " + detail + ". Change the Resource Name or Verb so "
                + "that each endpoint belongs to a single operation.";
    }
}
