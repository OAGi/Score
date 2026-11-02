package org.oagi.score.gateway.http.api.cc_management.validator;

import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
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

/**
 * What it takes to lay an ACC out: the core components of a release, as they are or as they would be after a change
 * (see {@link AccGraphOverlay}).
 */
public interface AccGraphSource {

    AccSummaryRecord getAcc(AccManifestId accManifestId);

    /** The associations of the ACC itself, in the order of its content. */
    List<CcAssociation> getAssociations(AccSummaryRecord acc);

    AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId);

    BccpSummaryRecord getBccp(BccpManifestId bccpManifestId);

    NamespaceSummaryRecord getNamespace(NamespaceId namespaceId);

    List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId);

    /** The core components of the release as they are. */
    static AccGraphSource of(CcDocument ccDocument) {
        return new AccGraphSource() {
            @Override
            public AccSummaryRecord getAcc(AccManifestId accManifestId) {
                return ccDocument.getAcc(accManifestId);
            }

            @Override
            public List<CcAssociation> getAssociations(AccSummaryRecord acc) {
                return ccDocument.getAssociations(acc);
            }

            @Override
            public AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId) {
                return ccDocument.getAsccp(asccpManifestId);
            }

            @Override
            public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
                return ccDocument.getBccp(bccpManifestId);
            }

            @Override
            public NamespaceSummaryRecord getNamespace(NamespaceId namespaceId) {
                return ccDocument.getNamespace(namespaceId);
            }

            @Override
            public List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId) {
                return ccDocument.getDtScList(dtManifestId);
            }
        };
    }
}
