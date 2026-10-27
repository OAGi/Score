package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.validation.PathSegment;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.util.ArrayList;
import java.util.List;

/**
 * A data type (DT) with what its rules need of what it is made of.
 *
 * @param previous               the previous revision, or null
 * @param namespace              the namespace of the DT, or null when it has none
 * @param valueDomains           the value domains of the content component of the DT
 * @param supplementaryComponents the supplementary components (SCs) of the DT, the ones that are not deleted
 */
public record DtValidationInput(DtSummaryRecord root,
                                DtSummaryRecord previous,
                                NamespaceSummaryRecord namespace,
                                List<ValueDomain> valueDomains,
                                List<SupplementaryComponent> supplementaryComponents) {

    /**
     * One value domain: a primitive, or a code list or an agency ID list on a primitive. The name of the primitive is
     * the one of the core data type primitive (Token, String, ...); for a code list or an agency ID list it is the one
     * the list is on.
     */
    public record ValueDomain(String primitive,
                              XbtManifestId xbtManifestId,
                              CodeListManifestId codeListManifestId,
                              AgencyIdListManifestId agencyIdListManifestId,
                              boolean isDefault) {

        public static ValueDomain of(DtAwdPriSummaryRecord e) {
            return new ValueDomain(e.cdtPriName(), e.xbtManifestId(), e.codeListManifestId(), e.agencyIdListManifestId(), e.isDefault());
        }

        public static ValueDomain of(DtScAwdPriSummaryRecord e) {
            return new ValueDomain(e.cdtPriName(), e.xbtManifestId(), e.codeListManifestId(), e.agencyIdListManifestId(), e.isDefault());
        }

        public boolean isList() {
            return codeListManifestId != null || agencyIdListManifestId != null;
        }
    }

    /** A supplementary component with its value domains and the address of its node in the tree of the screen. */
    public record SupplementaryComponent(DtScSummaryRecord sc, List<ValueDomain> valueDomains, List<PathSegment> path) {
    }

    /** What has value domains: the content component of the DT and each of its supplementary components. */
    public record ValueDomainHolder(String kind, String label, List<PathSegment> path, List<ValueDomain> valueDomains) {
    }

    public List<PathSegment> rootPath() {
        return List.of(new PathSegment("DT", String.valueOf(root.dtManifestId())));
    }

    public List<ValueDomainHolder> holders() {
        List<ValueDomainHolder> holders = new ArrayList<>();
        holders.add(new ValueDomainHolder("data type", root.den(), rootPath(), valueDomains));
        for (SupplementaryComponent e : supplementaryComponents) {
            holders.add(new ValueDomainHolder("supplementary component", e.sc().den(), e.path(), e.valueDomains()));
        }
        return holders;
    }
}
