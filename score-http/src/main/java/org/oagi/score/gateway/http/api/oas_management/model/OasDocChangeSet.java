package org.oagi.score.gateway.http.api.oas_management.model;

import java.math.BigInteger;
import java.util.List;

/**
 * What the user has changed in an OpenAPI Document and not saved yet. The client only says what was changed: it holds
 * no rule about the document, so it asks the server to {@code check} the change set and, when that is fine, to
 * {@code save} it. Both are the same operation on the server; the check is not kept.
 *
 * @param document   the document's own fields, its Security Schemes and Security Requirements, as the user has them;
 *                   {@code null} when none of them was changed (its {@code httpHeaders} are not read: see {@code headers})
 * @param operations the operations (rows of the table) that were edited, of every page
 * @param headers    the request parameters and response headers that were added, changed or deleted
 * @param added      the operations that were added in the page and have no id yet
 * @param removed    the operations (rows of the table) that were deleted
 */
public record OasDocChangeSet(OasDoc document, List<BieForOasDoc> operations, HeaderChanges headers,
                              List<OasOperationAddition> added, List<BieForOasDoc> removed) {

    public OasDocChangeSet(OasDoc document, List<BieForOasDoc> operations, HeaderChanges headers) {
        this(document, operations, headers, null, null);
    }

    public List<OasOperationAddition> addedOrEmpty() {
        return added == null ? List.of() : added;
    }

    public List<BieForOasDoc> removedOrEmpty() {
        return removed == null ? List.of() : removed;
    }

    /**
     * @param upserts the entries that were added or changed (an entry whose name or location was changed is a removal of the
     *                old one and an upsert of the new one)
     * @param removed the entries that were deleted, including a registered one that the user did not want
     */
    public record HeaderChanges(List<OasDocHttpHeader> upserts, List<HeaderKey> removed) {
        public static final HeaderChanges NONE = new HeaderChanges(List.of(), List.of());

        public List<OasDocHttpHeader> upsertsOrEmpty() {
            return upserts == null ? List.of() : upserts;
        }

        public List<HeaderKey> removedOrEmpty() {
            return removed == null ? List.of() : removed;
        }
    }

    /** What tells one entry from another: the operation it is for ({@code null}: the whole document), its direction, location and name. */
    public record HeaderKey(BigInteger oasOperationId, OasDocHttpHeader.Direction direction, String in, String name) {
        public static HeaderKey of(OasDocHttpHeader header) {
            return new HeaderKey(header.getOasOperationId(), header.getDirection(), locationOf(header), header.getName());
        }

        /** A request parameter without a location is one in a header; a response header always is. */
        private static String locationOf(OasDocHttpHeader header) {
            return (header.getDirection() == OasDocHttpHeader.Direction.REQUEST && header.getIn() != null)
                    ? header.getIn() : "header";
        }
    }

    public List<BieForOasDoc> operationsOrEmpty() {
        return operations == null ? List.of() : operations;
    }

    public HeaderChanges headersOrNone() {
        return headers == null ? HeaderChanges.NONE : headers;
    }
}
