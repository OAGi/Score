package org.oagi.score.gateway.http.api.release_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

import java.util.HashMap;
import java.util.Map;

@Data
public class AssignComponents {

    private Map<AccManifestId, AssignableNode> assignableAccManifestMap = new HashMap();
    private Map<AsccpManifestId, AssignableNode> assignableAsccpManifestMap = new HashMap();
    private Map<BccpManifestId, AssignableNode> assignableBccpManifestMap = new HashMap();
    private Map<CodeListManifestId, AssignableNode> assignableCodeListManifestMap = new HashMap();
    private Map<AgencyIdListManifestId, AssignableNode> assignableAgencyIdListManifestMap = new HashMap();
    private Map<DtManifestId, AssignableNode> assignableDtManifestMap = new HashMap();

    private Map<AccManifestId, AssignableNode> unassignableAccManifestMap = new HashMap();
    private Map<AsccpManifestId, AssignableNode> unassignableAsccpManifestMap = new HashMap();
    private Map<BccpManifestId, AssignableNode> unassignableBccpManifestMap = new HashMap();
    private Map<CodeListManifestId, AssignableNode> unassignableCodeListManifestMap = new HashMap();
    private Map<AgencyIdListManifestId, AssignableNode> unassignableAgencyIdListManifestMap = new HashMap();
    private Map<DtManifestId, AssignableNode> unassignableDtManifestMap = new HashMap();

    public void addAssignableAccManifest(AccManifestId accManifestId, AssignableNode node) {
        assignableAccManifestMap.put(accManifestId, node);
    }

    public void addAssignableAsccpManifest(AsccpManifestId asccpManifestId, AssignableNode node) {
        assignableAsccpManifestMap.put(asccpManifestId, node);
    }

    public void addAssignableBccpManifest(BccpManifestId bccpManifestId, AssignableNode node) {
        assignableBccpManifestMap.put(bccpManifestId, node);
    }

    public void addAssignableCodeListManifest(CodeListManifestId codeListManifestId, AssignableNode node) {
        assignableCodeListManifestMap.put(codeListManifestId, node);
    }

    public void addAssignableAgencyIdListManifest(AgencyIdListManifestId agencyIdListManifestId, AssignableNode node) {
        assignableAgencyIdListManifestMap.put(agencyIdListManifestId, node);
    }

    public void addAssignableDtManifest(DtManifestId dtManifestId, AssignableNode node) {
        assignableDtManifestMap.put(dtManifestId, node);
    }

    public void addUnassignableAccManifest(AccManifestId accManifestId, AssignableNode node) {
        unassignableAccManifestMap.put(accManifestId, node);
    }

    public void addUnassignableAsccpManifest(AsccpManifestId asccpManifestId, AssignableNode node) {
        unassignableAsccpManifestMap.put(asccpManifestId, node);
    }

    public void addUnassignableBccpManifest(BccpManifestId bccpManifestId, AssignableNode node) {
        unassignableBccpManifestMap.put(bccpManifestId, node);
    }

    public void addUnassignableCodeListManifest(CodeListManifestId codeListManifestId, AssignableNode node) {
        unassignableCodeListManifestMap.put(codeListManifestId, node);
    }

    public void addUnassignableAgencyIdListManifest(AgencyIdListManifestId agencyIdListManifestId, AssignableNode node) {
        unassignableAgencyIdListManifestMap.put(agencyIdListManifestId, node);
    }

    public void addUnassignableDtManifest(DtManifestId dtManifestId, AssignableNode node) {
        unassignableDtManifestMap.put(dtManifestId, node);
    }
}
