package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Internal per-generation reference metadata keyed by top-level ASBIEP ID.
 * <p>
 * This model is intentionally kept outside request/response option classes because it is
 * generation-runtime state, not user input. It allows expression implementations to determine:
 * <ul>
 *   <li>whether the current schema file is generated as a referenced/reused file,</li>
 *   <li>which top-level schemas directly refer to the current one, and</li>
 *   <li>which top-level schemas are directly referenced by the current one.</li>
 * </ul>
 * The latter two sets are maintained for forward compatibility (for example, diagnostics
 * and documentation/reporting use cases).
 */
public class SchemaReferenceInfo {

    private static final SchemaReferenceInfo EMPTY = new SchemaReferenceInfo(
            null, false, Collections.emptySet(), Collections.emptySet());

    private final TopLevelAsbiepId currentTopLevelAsbiepId;
    private final boolean referencedSchemaFile;
    private final Set<TopLevelAsbiepId> referredByTopLevelAsbiepIds;
    private final Set<TopLevelAsbiepId> refersToTopLevelAsbiepIds;

    public SchemaReferenceInfo(TopLevelAsbiepId currentTopLevelAsbiepId,
                               boolean referencedSchemaFile,
                               Set<TopLevelAsbiepId> referredByTopLevelAsbiepIds,
                               Set<TopLevelAsbiepId> refersToTopLevelAsbiepIds) {
        this.currentTopLevelAsbiepId = currentTopLevelAsbiepId;
        this.referencedSchemaFile = referencedSchemaFile;
        this.referredByTopLevelAsbiepIds = Collections.unmodifiableSet(
                new LinkedHashSet<>((referredByTopLevelAsbiepIds != null) ? referredByTopLevelAsbiepIds : Collections.emptySet()));
        this.refersToTopLevelAsbiepIds = Collections.unmodifiableSet(
                new LinkedHashSet<>((refersToTopLevelAsbiepIds != null) ? refersToTopLevelAsbiepIds : Collections.emptySet()));
    }

    public static SchemaReferenceInfo empty() {
        return EMPTY;
    }

    /**
     * @return top-level ASBIEP ID represented by this metadata entry
     */
    public TopLevelAsbiepId getCurrentTopLevelAsbiepId() {
        return currentTopLevelAsbiepId;
    }

    /**
     * @return true when this schema is generated through referenced-schema flow
     */
    public boolean isReferencedSchemaFile() {
        return referencedSchemaFile;
    }

    /**
     * @return immutable set of top-level ASBIEP IDs that refer to this schema
     */
    public Set<TopLevelAsbiepId> getReferredByTopLevelAsbiepIds() {
        return referredByTopLevelAsbiepIds;
    }

    /**
     * @return immutable set of top-level ASBIEP IDs that this schema refers to
     */
    public Set<TopLevelAsbiepId> getRefersToTopLevelAsbiepIds() {
        return refersToTopLevelAsbiepIds;
    }
}
