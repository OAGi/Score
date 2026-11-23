package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocChangeSet;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;
import org.oagi.score.gateway.http.api.oas_management.model.OasJson;
import org.oagi.score.gateway.http.api.oas_management.model.OasSchema;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasDocHttpHeaderRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreGuidUtils.randomGuid;

/**
 * Issue #1791: persistence of the HTTP headers of an OpenAPI document. A header definition is kept in
 * {@code oas_http_header} (one of its own for each use) and, for a request parameter, in one
 * {@code oas_parameter} per use; {@code oas_doc_http_header} binds them to the document or to one operation.
 */
final class OasDocHttpHeaderStore {

    private static final String HEADER_LOCATION = "header";

    private OasDocHttpHeaderStore() {
    }

    /** Replace the headers of the document. {@code null} keeps them as they are. */
    static void save(DSLContext dslContext, ULong oasDocId, List<OasDocHttpHeader> headers,
                     ULong requesterId, LocalDateTime timestamp) {
        if (headers == null) {
            return;
        }
        delete(dslContext, OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId));

        for (OasDocHttpHeader header : headers) {
            if (header == null || header.getDirection() == null) {
                continue;
            }
            insert(dslContext, oasDocId, header, requesterId, timestamp);
        }
    }

    /**
     * Apply what was changed in the headers of the document (see {@link #merge}). Returns the headers the document has then.
     */
    static List<OasDocHttpHeader> applyChanges(DSLContext dslContext, ULong oasDocId,
                                               List<OasDocHttpHeader> upserts, List<OasDocChangeSet.HeaderKey> removed,
                                               ULong requesterId, LocalDateTime timestamp) {
        List<OasDocHttpHeader> current = load(dslContext, oasDocId);
        if (upserts.isEmpty() && removed.isEmpty()) {
            return current;
        }
        save(dslContext, oasDocId, merge(current, upserts, removed), requesterId, timestamp);
        return load(dslContext, oasDocId);
    }

    /**
     * The headers that are left when the removed ones are taken out and the upserted ones put in: an upserted header takes
     * the place of the one with its key, or is added at the end.
     */
    static List<OasDocHttpHeader> merge(List<OasDocHttpHeader> current, List<OasDocHttpHeader> upserts,
                                        List<OasDocChangeSet.HeaderKey> removed) {
        Set<OasDocChangeSet.HeaderKey> removedKeys = new LinkedHashSet<>(removed);
        List<OasDocHttpHeader> result = new ArrayList<>();
        for (OasDocHttpHeader header : current) {
            if (!removedKeys.contains(OasDocChangeSet.HeaderKey.of(header))) {
                result.add(header);
            }
        }
        for (OasDocHttpHeader upsert : upserts) {
            if (upsert == null || upsert.getDirection() == null) {
                continue;
            }
            OasDocChangeSet.HeaderKey key = OasDocChangeSet.HeaderKey.of(upsert);
            int at = -1;
            for (int i = 0; i < result.size() && at < 0; i++) {
                if (OasDocChangeSet.HeaderKey.of(result.get(i)).equals(key)) {
                    at = i;
                }
            }
            if (at >= 0) {
                result.set(at, upsert);
            } else {
                result.add(upsert);
            }
        }
        return result;
    }

    /** Add one header to the document, or to its operation when the header has one. */
    static void insert(DSLContext dslContext, ULong oasDocId, OasDocHttpHeader header,
                       ULong requesterId, LocalDateTime timestamp) {
        ULong oasOperationId = (header.getOasOperationId() != null)
                ? ULong.valueOf(header.getOasOperationId()) : null;
        if (header.getDirection() == OasDocHttpHeader.Direction.REQUEST) {
            String in = header.getIn() == null ? HEADER_LOCATION : header.getIn();
            // only a parameter in a header has a header definition
            ULong oasHttpHeaderId = HEADER_LOCATION.equals(in)
                    ? createHttpHeader(dslContext, header, requesterId, timestamp) : null;
            ULong oasParameterId = dslContext.insertInto(OAS_PARAMETER)
                    .set(OAS_PARAMETER.GUID, randomGuid())
                    .set(OAS_PARAMETER.NAME, header.getName())
                    .set(OAS_PARAMETER.IN, in)
                    .set(OAS_PARAMETER.REQUIRED, flag(header.isRequired()))
                    .set(OAS_PARAMETER.DESCRIPTION, header.getDescription())
                    .set(OAS_PARAMETER.SCHEMA_TYPE_REFERENCE, header.getSchema().toJson())
                    .set(OAS_PARAMETER.EXAMPLE, OasJson.toJson(header.getExample()))
                    .set(OAS_PARAMETER.STYLE, header.getStyle())
                    .set(OAS_PARAMETER.EXPLODE, flag(header.getExplode()))
                    .set(OAS_PARAMETER.ALLOW_RESERVED, flag(header.isAllowReserved()))
                    .set(OAS_PARAMETER.CONTENT_MEDIA_TYPE, header.getContentMediaType())
                    .set(OAS_PARAMETER.OAS_HTTP_HEADER_ID, oasHttpHeaderId)
                    .set(OAS_PARAMETER.CREATED_BY, requesterId)
                    .set(OAS_PARAMETER.LAST_UPDATED_BY, requesterId)
                    .set(OAS_PARAMETER.CREATION_TIMESTAMP, timestamp)
                    .set(OAS_PARAMETER.LAST_UPDATE_TIMESTAMP, timestamp)
                    .returningResult(OAS_PARAMETER.OAS_PARAMETER_ID)
                    .fetchOne().value1();
            insertBinding(dslContext, oasDocId, oasOperationId, oasParameterId, null, requesterId, timestamp);
        } else {
            insertBinding(dslContext, oasDocId, oasOperationId, null,
                    createHttpHeader(dslContext, header, requesterId, timestamp), requesterId, timestamp);
        }
    }

    /**
     * Delete the bindings that match the condition, the request parameters they refer to, and the header
     * definitions that nothing else refers to.
     */
    static void delete(DSLContext dslContext, Condition condition) {
        List<ULong> oasParameterIds = dslContext.select(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID)
                .from(OAS_DOC_HTTP_HEADER)
                .where(condition, OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID.isNotNull())
                .fetch(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID);
        // the header definitions that the entries use: a response header's own, and that of a request parameter in a header
        Set<ULong> oasHttpHeaderIds = new LinkedHashSet<>(dslContext.select(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID)
                .from(OAS_DOC_HTTP_HEADER)
                .where(condition, OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID.isNotNull())
                .fetch(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID));
        if (!oasParameterIds.isEmpty()) {
            oasHttpHeaderIds.addAll(dslContext.select(OAS_PARAMETER.OAS_HTTP_HEADER_ID).from(OAS_PARAMETER)
                    .where(OAS_PARAMETER.OAS_PARAMETER_ID.in(oasParameterIds), OAS_PARAMETER.OAS_HTTP_HEADER_ID.isNotNull())
                    .fetch(OAS_PARAMETER.OAS_HTTP_HEADER_ID));
        }
        dslContext.deleteFrom(OAS_DOC_HTTP_HEADER).where(condition).execute();
        if (!oasParameterIds.isEmpty()) {
            dslContext.deleteFrom(OAS_PARAMETER).where(OAS_PARAMETER.OAS_PARAMETER_ID.in(oasParameterIds)).execute();
        }
        // A definition goes with its entry. One that something else still refers to (a definition that several entries
        // shared before each had its own, or one of the response headers of the operations) stays.
        if (!oasHttpHeaderIds.isEmpty()) {
            dslContext.deleteFrom(OAS_HTTP_HEADER)
                    .where(OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID.in(oasHttpHeaderIds))
                    .andNotExists(dslContext.selectOne().from(OAS_DOC_HTTP_HEADER)
                            .where(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID.eq(OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID)))
                    .andNotExists(dslContext.selectOne().from(OAS_PARAMETER)
                            .where(OAS_PARAMETER.OAS_HTTP_HEADER_ID.eq(OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID)))
                    .andNotExists(dslContext.selectOne().from(OAS_RESPONSE_HEADERS)
                            .where(OAS_RESPONSE_HEADERS.OAS_HTTP_HEADER_ID.eq(OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID)))
                    .execute();
        }
    }

    /** The headers of the document, in the order they were saved. */
    static List<OasDocHttpHeader> load(DSLContext dslContext, ULong oasDocId) {
        List<OasDocHttpHeader> headers = new ArrayList<>();
        for (OasDocHttpHeaderRecord binding : dslContext.selectFrom(OAS_DOC_HTTP_HEADER)
                .where(OAS_DOC_HTTP_HEADER.OAS_DOC_ID.eq(oasDocId))
                .orderBy(OAS_DOC_HTTP_HEADER.OAS_DOC_HTTP_HEADER_ID.asc())
                .fetch()) {
            OasDocHttpHeader header = new OasDocHttpHeader();
            header.setOasOperationId(binding.getOasOperationId() != null
                    ? binding.getOasOperationId().toBigInteger() : null);
            if (binding.getOasParameterId() != null) {
                var parameter = dslContext.selectFrom(OAS_PARAMETER)
                        .where(OAS_PARAMETER.OAS_PARAMETER_ID.eq(binding.getOasParameterId())).fetchOne();
                header.setDirection(OasDocHttpHeader.Direction.REQUEST);
                header.setName(parameter.getName());
                header.setIn(parameter.getIn());
                header.setDescription(parameter.getDescription());
                header.setSchema(OasSchema.fromJson(parameter.getSchemaTypeReference()));
                header.setExample(OasJson.fromJson(parameter.getExample()));
                header.setRequired(parameter.getRequired() != null && parameter.getRequired() == 1);
                header.setStyle(parameter.getStyle());
                header.setExplode(bool(parameter.getExplode()));
                header.setAllowReserved(parameter.getAllowReserved() != null && parameter.getAllowReserved() == 1);
                header.setContentMediaType(parameter.getContentMediaType());
            } else {
                var definition = dslContext.selectFrom(OAS_HTTP_HEADER)
                        .where(OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID.eq(binding.getOasHttpHeaderId())).fetchOne();
                header.setDirection(OasDocHttpHeader.Direction.RESPONSE);
                header.setName(definition.getHeader());
                header.setDescription(definition.getDescription());
                header.setSchema(OasSchema.fromJson(definition.getSchemaTypeReference()));
                header.setExample(OasJson.fromJson(definition.getExample()));
                header.setRequired(definition.getRequired() != null && definition.getRequired() == 1);
                header.setStyle(definition.getStyle());
                header.setExplode(bool(definition.getExplode()));
                header.setContentMediaType(definition.getContentMediaType());
            }
            headers.add(header);
        }
        return headers;
    }

    /** A header definition of its own for each use, so that deleting the entry deletes it. */
    private static ULong createHttpHeader(DSLContext dslContext, OasDocHttpHeader header,
                                          ULong requesterId, LocalDateTime timestamp) {
        return Objects.requireNonNull(dslContext.insertInto(OAS_HTTP_HEADER)
                .set(OAS_HTTP_HEADER.GUID, randomGuid())
                .set(OAS_HTTP_HEADER.HEADER, header.getName())
                .set(OAS_HTTP_HEADER.DESCRIPTION, header.getDescription())
                .set(OAS_HTTP_HEADER.SCHEMA_TYPE_REFERENCE, header.getSchema().toJson())
                .set(OAS_HTTP_HEADER.EXAMPLE, OasJson.toJson(header.getExample()))
                .set(OAS_HTTP_HEADER.REQUIRED, flag(header.isRequired()))
                .set(OAS_HTTP_HEADER.STYLE, header.getStyle())
                .set(OAS_HTTP_HEADER.EXPLODE, flag(header.getExplode()))
                .set(OAS_HTTP_HEADER.CONTENT_MEDIA_TYPE, header.getContentMediaType())
                .set(OAS_HTTP_HEADER.OWNER_USER_ID, requesterId)
                .set(OAS_HTTP_HEADER.CREATED_BY, requesterId)
                .set(OAS_HTTP_HEADER.LAST_UPDATED_BY, requesterId)
                .set(OAS_HTTP_HEADER.CREATION_TIMESTAMP, timestamp)
                .set(OAS_HTTP_HEADER.LAST_UPDATE_TIMESTAMP, timestamp)
                .returningResult(OAS_HTTP_HEADER.OAS_HTTP_HEADER_ID)
                .fetchOne()).value1();
    }

    private static void insertBinding(DSLContext dslContext, ULong oasDocId, ULong oasOperationId,
                                      ULong oasParameterId, ULong oasHttpHeaderId,
                                      ULong requesterId, LocalDateTime timestamp) {
        dslContext.insertInto(OAS_DOC_HTTP_HEADER)
                .set(OAS_DOC_HTTP_HEADER.OAS_DOC_ID, oasDocId)
                .set(OAS_DOC_HTTP_HEADER.OAS_OPERATION_ID, oasOperationId)
                .set(OAS_DOC_HTTP_HEADER.OAS_PARAMETER_ID, oasParameterId)
                .set(OAS_DOC_HTTP_HEADER.OAS_HTTP_HEADER_ID, oasHttpHeaderId)
                .set(OAS_DOC_HTTP_HEADER.CREATED_BY, requesterId)
                .set(OAS_DOC_HTTP_HEADER.LAST_UPDATED_BY, requesterId)
                .set(OAS_DOC_HTTP_HEADER.CREATION_TIMESTAMP, timestamp)
                .set(OAS_DOC_HTTP_HEADER.LAST_UPDATE_TIMESTAMP, timestamp)
                .execute();
    }

    private static Byte flag(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    /** The flag of a value that may be unset: null stays null, the default of the specification. */
    private static Byte flag(Boolean value) {
        return value == null ? null : flag(value.booleanValue());
    }

    private static Boolean bool(Byte value) {
        return value == null ? null : value == 1;
    }
}
