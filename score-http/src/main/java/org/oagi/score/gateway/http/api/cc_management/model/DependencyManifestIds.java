package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;

import java.util.Set;

/**
 * The components that one component depends on: through the base of an ACC, the ASCCs and BCCs of an ACC to
 * their ASCCPs and BCCPs, the ACC an ASCCP is of, and the DT of a BCCP.
 * <p>
 * It is also what {@link CachedCcDocument} keeps in the cache for the dependencies it has read, so that the next reader reads them
 * back in a few bulk reads: besides the four kinds above, the ACCs and DTs that stand in for the ones without associations
 * ({@code prevAccs}, {@code prevDts}), and, for the data types, their supplementary components, the DTs they are based on, the
 * namespaces and the code lists and agency identification lists of their allowed primitives.
 * <p>
 * Each kind of id has its own type, so that an id of one kind cannot be put where another kind is meant.
 */
public record DependencyManifestIds(Set<AccManifestId> accs,
                               Set<AsccpManifestId> asccps,
                               Set<BccpManifestId> bccps,
                               Set<DtManifestId> dts,
                               Set<AccManifestId> prevAccs,
                               Set<DtManifestId> prevDts,
                               Set<DtScManifestId> dtScs,
                               Set<DtManifestId> basedDts,
                               Set<NamespaceId> namespaces,
                               Set<CodeListManifestId> codeLists,
                               Set<AgencyIdListManifestId> agencyIdLists) {

    /** What the dependency query finds: the four kinds of components. */
    public DependencyManifestIds(Set<AccManifestId> accs, Set<AsccpManifestId> asccps, Set<BccpManifestId> bccps, Set<DtManifestId> dts) {
        this(accs, asccps, bccps, dts, Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
    }

    /** Whether it has what the data types need besides themselves (the graph shown on a screen does not). */
    public boolean hasDtRelated() {
        return !namespaces.isEmpty() || !dtScs.isEmpty() || !codeLists.isEmpty();
    }
}
