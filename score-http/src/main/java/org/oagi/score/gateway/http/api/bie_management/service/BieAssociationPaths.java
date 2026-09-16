package org.oagi.score.gateway.http.api.bie_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/** Canonical association paths through inherited and group ACCs, shared by source traversal and target matching. */
public final class BieAssociationPaths {
    private static final Logger logger = LoggerFactory.getLogger(BieAssociationPaths.class);

    private BieAssociationPaths() {}

    public static final class Association {

        private final String parentPath;
        private final CcAssociation ccAssociation;

        public Association(String parentPath, CcAssociation ccAssociation) {
            this.parentPath = parentPath;
            this.ccAssociation = ccAssociation;
        }

        public String getPath() {
            return parentPath + ">" + ((this.ccAssociation.isAscc()) ?
                    "ASCC-" + ((AsccSummaryRecord) this.ccAssociation).asccManifestId() :
                    "BCC-" + ((BccSummaryRecord) this.ccAssociation).bccManifestId());
        }

        public CcAssociation getCcAssociation() {
            return ccAssociation;
        }
    }

    public static List<Association> getAssociationsRegardingBases(String path, CcDocument ccDocument, AccSummaryRecord acc) {
        Stack<AccSummaryRecord> accStack = new Stack<>();
        while (acc != null) {
            accStack.push(acc);
            acc = ccDocument.getAcc(acc.basedAccManifestId());
        }

        List<Association> associations = new ArrayList<>();
        while (!accStack.isEmpty()) {
            String parentPath = path + ">" + String.join(">", accStack.stream()
                    .map(e -> "ACC-" + e.accManifestId()).collect(Collectors.toList()));
            acc = accStack.pop();
            associations.addAll(getAssociationsRegardingGroup(parentPath, ccDocument, acc));
        }

        return associations;
    }

    private static List<Association> getAssociationsRegardingGroup(String parentPath, CcDocument ccDocument, AccSummaryRecord acc) {
        Collection<CcAssociation> ccAssociations = ccDocument.getAssociations(acc);
        List<Association> associations = new ArrayList<>();
        for (CcAssociation ccAssociation : ccAssociations) {
            if (ccAssociation.isAscc()) {
                AsccSummaryRecord ascc = (AsccSummaryRecord) ccAssociation;
                AsccpSummaryRecord asccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
                AccSummaryRecord roleOfAcc = asccp == null ? null : ccDocument.getAcc(asccp.roleOfAccManifestId());
                // Stale extension associations can remain after their target components are deleted.
                if (roleOfAcc == null) {
                    logger.warn("Skipping unresolved association ASCC-{} at {}: ASCCP-{}, role-of ACC-{}",
                            ascc.asccManifestId(), parentPath, ascc.toAsccpManifestId(),
                            asccp == null ? null : asccp.roleOfAccManifestId());
                    continue;
                }
                if (roleOfAcc.isGroup()) {
                    associations.addAll(
                            getAssociationsRegardingGroup(
                                    String.join(">",
                                            Arrays.asList(parentPath,
                                                    "ASCC-" + ascc.asccManifestId(),
                                                    "ASCCP-" + asccp.asccpManifestId(),
                                                    "ACC-" + roleOfAcc.accManifestId())
                                    ),
                                    ccDocument, roleOfAcc)
                    );
                    continue;
                }
            }

            associations.add(new Association(parentPath, ccAssociation));
        }

        return associations;
    }

}
