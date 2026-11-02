package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;

import java.util.List;
import java.util.Map;

/**
 * The core components as they would be after a change of some ACCs: those ACCs have the associations and the ACC record
 * (the base ACC is in it) given, everything else is as it is. Nothing is written anywhere.
 */
public class AccGraphOverlay implements AccGraphSource {

    private final AccGraphSource base;
    private final Map<AccManifestId, List<CcAssociation>> associations;
    private final Map<AccManifestId, AccSummaryRecord> accs;

    public AccGraphOverlay(AccGraphSource base, Map<AccManifestId, List<CcAssociation>> associations,
                           Map<AccManifestId, AccSummaryRecord> accs) {
        this.base = base;
        this.associations = associations;
        this.accs = accs;
    }

    @Override
    public AccSummaryRecord getAcc(AccManifestId accManifestId) {
        AccSummaryRecord changed = accs.get(accManifestId);
        return changed != null ? changed : base.getAcc(accManifestId);
    }

    @Override
    public List<CcAssociation> getAssociations(AccSummaryRecord acc) {
        List<CcAssociation> changed = associations.get(acc.accManifestId());
        return changed != null ? changed : base.getAssociations(acc);
    }

    @Override
    public AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId) {
        return base.getAsccp(asccpManifestId);
    }

    @Override
    public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
        return base.getBccp(bccpManifestId);
    }

    @Override
    public NamespaceSummaryRecord getNamespace(NamespaceId namespaceId) {
        return base.getNamespace(namespaceId);
    }

    @Override
    public List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId) {
        return base.getDtScList(dtManifestId);
    }
}
