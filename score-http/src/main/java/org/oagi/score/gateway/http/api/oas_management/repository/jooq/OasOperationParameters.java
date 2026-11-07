package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasOperationParameterState;
import org.oagi.score.gateway.http.api.oas_management.model.OasResourcePaths;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/**
 * Issue #1791: the request parameters that the system registers for an operation, as entries of the operation in the
 * request parameters of the document (see {@link OasDocHttpHeaderStore}), so that they are the user's to edit:
 * <ul>
 *   <li>{@code sinceLastDateTime}: when the operation becomes a GET that returns an array; it is taken away when the
 *   operation stops being one.</li>
 *   <li>a path parameter for each {@code {name}} of the Resource Path: one is added for a name that is new, and the
 *   one of a name that was changed is renamed (the first removed name to the first added one, and so on); the one
 *   of a name that is gone is removed.</li>
 * </ul>
 * Only a change of the operation does it: an entry that the user deleted does not come back until the operation
 * changes into that case again, and one that was there before (the generator still makes those itself when there is no
 * entry) is not touched. The comparison is by location and name; nothing tells an entry the system made from one the
 * user made.
 */
final class OasOperationParameters {

    static final String SINCE_LAST_DATE_TIME = "sinceLastDateTime";
    private static final String QUERY = "query";
    private static final String PATH = "path";

    private OasOperationParameters() {
    }

    /** What the operation is now. */
    static OasOperationParameterState capture(DSLContext dsl, ULong oasOperationId) {
        var row = dsl.select(OAS_OPERATION.VERB, OAS_RESOURCE.PATH, OAS_DOC.VERSION)
                .from(OAS_OPERATION)
                .join(OAS_RESOURCE).on(OAS_RESOURCE.OAS_RESOURCE_ID.eq(OAS_OPERATION.OAS_RESOURCE_ID))
                .join(OAS_DOC).on(OAS_DOC.OAS_DOC_ID.eq(OAS_RESOURCE.OAS_DOC_ID))
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId))
                .fetchOne();
        if (row == null) {
            return OasOperationParameterState.NONE;
        }
        return capture(dsl, oasOperationId, row.get(OAS_OPERATION.VERB), row.get(OAS_RESOURCE.PATH), row.get(OAS_DOC.VERSION));
    }

    /** What the operation is, were the document's version the given one. */
    static OasOperationParameterState capture(DSLContext dsl, ULong oasOperationId, String verb, String path, String documentVersion) {
        boolean returnsArray = dsl.fetchExists(dsl.selectOne().from(OAS_REQUEST)
                .where(OAS_REQUEST.OAS_OPERATION_ID.eq(oasOperationId), OAS_REQUEST.MAKE_ARRAY_INDICATOR.eq((byte) 1)))
                || dsl.fetchExists(dsl.selectOne().from(OAS_RESPONSE)
                .where(OAS_RESPONSE.OAS_OPERATION_ID.eq(oasOperationId), OAS_RESPONSE.MAKE_ARRAY_INDICATOR.eq((byte) 1)));
        return OasOperationParameterState.of(verb, returnsArray, OasResourcePaths.resolve(path, documentVersion));
    }

    /** The operations of the resources, and the given ones, as they are now. */
    static java.util.Map<ULong, OasOperationParameterState> captureAll(DSLContext dsl, java.util.Collection<ULong> oasOperationIds,
                                                                      java.util.Collection<ULong> oasResourceIds) {
        java.util.Set<ULong> all = new java.util.LinkedHashSet<>(oasOperationIds);
        if (!oasResourceIds.isEmpty()) {
            all.addAll(dsl.select(OAS_OPERATION.OAS_OPERATION_ID).from(OAS_OPERATION)
                    .where(OAS_OPERATION.OAS_RESOURCE_ID.in(oasResourceIds)).fetch(OAS_OPERATION.OAS_OPERATION_ID));
        }
        java.util.Map<ULong, OasOperationParameterState> states = new java.util.LinkedHashMap<>();
        all.forEach(id -> states.put(id, capture(dsl, id)));
        return states;
    }

    /** Make the entries of the operation follow its change from {@code before} to what it is now. */
    static void reconcile(DSLContext dsl, ULong oasOperationId, OasOperationParameterState before,
                          ULong requesterId, LocalDateTime timestamp) {
        var resource = dsl.select(OAS_RESOURCE.OAS_DOC_ID).from(OAS_OPERATION)
                .join(OAS_RESOURCE).on(OAS_RESOURCE.OAS_RESOURCE_ID.eq(OAS_OPERATION.OAS_RESOURCE_ID))
                .where(OAS_OPERATION.OAS_OPERATION_ID.eq(oasOperationId)).fetchOne();
        if (resource == null) {
            return; // the operation is gone, and its entries with it
        }
        apply(dsl, resource.get(OAS_RESOURCE.OAS_DOC_ID), oasOperationId, before, capture(dsl, oasOperationId), requesterId, timestamp);
    }

    static void apply(DSLContext dsl, ULong oasDocId, ULong oasOperationId, OasOperationParameterState before,
                      OasOperationParameterState after, ULong requesterId, LocalDateTime timestamp) {
        if (after.sinceLastDateTime() && !before.sinceLastDateTime()) {
            if (!exists(dsl, oasDocId, oasOperationId, QUERY, SINCE_LAST_DATE_TIME)) {
                OasDocHttpHeaderStore.insert(dsl, oasDocId, sinceLastDateTime(oasOperationId), requesterId, timestamp);
            }
        } else if (before.sinceLastDateTime() && !after.sinceLastDateTime()) {
            OasDocHttpHeaderStore.delete(dsl, ofOperation(oasOperationId, QUERY, SINCE_LAST_DATE_TIME));
        }

        List<String> removed = new ArrayList<>(before.pathParameters());
        removed.removeAll(after.pathParameters());
        List<String> added = new ArrayList<>(after.pathParameters());
        added.removeAll(before.pathParameters());
        for (int i = 0; i < Math.max(removed.size(), added.size()); i++) {
            String from = i < removed.size() ? removed.get(i) : null;
            String to = i < added.size() ? added.get(i) : null;
            if (from != null && to != null) {
                // the name was changed: the entry follows it, whatever the user made of it
                if (exists(dsl, oasDocId, oasOperationId, PATH, to) || !hasOwn(dsl, oasOperationId, PATH, from)) {
                    OasDocHttpHeaderStore.delete(dsl, ofOperation(oasOperationId, PATH, from));
                } else {
                    rename(dsl, oasOperationId, from, to, requesterId, timestamp);
                }
            } else if (from != null) {
                // a path parameter that is not in the path would make the document refer to nothing
                OasDocHttpHeaderStore.delete(dsl, ofOperation(oasOperationId, PATH, from));
            } else if (!exists(dsl, oasDocId, oasOperationId, PATH, to)) {
                OasDocHttpHeaderStore.insert(dsl, oasDocId, pathParameter(oasOperationId, to), requesterId, timestamp);
            }
        }
    }

    /** The entries of a request parameter of the operation: those of the operation itself. */
    private static Condition ofOperation(ULong oasOperationId, String in, String name) {
        return OAS_DOC_HTTP_HEADER.OAS_OPERATION_ID.eq(oasOperationId).and(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID.in(
                org.jooq.impl.DSL.select(OAS_PARAMETER.OAS_PARAMETER_ID).from(OAS_PARAMETER)
                        .where(OAS_PARAMETER.IN.eq(in), OAS_PARAMETER.NAME.eq(name))));
    }

    private static boolean hasOwn(DSLContext dsl, ULong oasOperationId, String in, String name) {
        return dsl.fetchExists(dsl.selectOne().from(OAS_DOC_HTTP_HEADER).where(ofOperation(oasOperationId, in, name)));
    }

    /** Whether the operation has that request parameter, of its own or of the whole document. */
    private static boolean exists(DSLContext dsl, ULong oasDocId, ULong oasOperationId, String in, String name) {
        return dsl.fetchExists(dsl.selectOne().from(OAS_DOC_HTTP_HEADER)
                .where(OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId))
                .and(OAS_DOC_HTTP_HEADER.OAS_OPERATION_ID.isNull().or(OAS_DOC_HTTP_HEADER.OAS_OPERATION_ID.eq(oasOperationId)))
                .and(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID.in(org.jooq.impl.DSL.select(OAS_PARAMETER.OAS_PARAMETER_ID).from(OAS_PARAMETER)
                        .where(OAS_PARAMETER.IN.eq(in), OAS_PARAMETER.NAME.eq(name)))));
    }

    private static void rename(DSLContext dsl, ULong oasOperationId, String from, String to,
                               ULong requesterId, LocalDateTime timestamp) {
        dsl.update(OAS_PARAMETER)
                .set(OAS_PARAMETER.NAME, to)
                .set(OAS_PARAMETER.LAST_UPDATED_BY, requesterId)
                .set(OAS_PARAMETER.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(OAS_PARAMETER.IN.eq(PATH), OAS_PARAMETER.NAME.eq(from))
                .and(OAS_PARAMETER.OAS_PARAMETER_ID.in(org.jooq.impl.DSL.select(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID)
                        .from(OAS_DOC_HTTP_HEADER).where(OAS_DOC_HTTP_HEADER.OAS_OPERATION_ID.eq(oasOperationId))))
                .execute();
    }

    private static OasDocHttpHeader sinceLastDateTime(ULong oasOperationId) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setOasOperationId(oasOperationId.toBigInteger());
        header.setDirection(OasDocHttpHeader.Direction.REQUEST);
        header.setIn(QUERY);
        header.setName(SINCE_LAST_DATE_TIME);
        header.setDescription("Returns resources that have been updated since the last time the endpoint has been called");
        header.setRequired(false);
        header.setSchema(OasSchema.fromJson("{\"type\":\"string\",\"format\":\"date-time\"}"));
        return header;
    }

    private static OasDocHttpHeader pathParameter(ULong oasOperationId, String name) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setOasOperationId(oasOperationId.toBigInteger());
        header.setDirection(OasDocHttpHeader.Direction.REQUEST);
        header.setIn(PATH);
        header.setName(name);
        header.setDescription("");
        header.setRequired(true);
        header.setSchema(OasSchema.fromJson("{\"type\":\"string\"}"));
        return header;
    }
}
