package org.oagi.score.gateway.http.api.release_management.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.event.Event;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseCreateRequestEvent implements Event {

    private UserId userId;
    private ReleaseId releaseId;

    private List<AccManifestId> accManifestIds;
    private List<AsccpManifestId> asccpManifestIds;
    private List<BccpManifestId> bccpManifestIds;
    private List<DtManifestId> dtManifestIds;
    private List<CodeListManifestId> codeListManifestIds;
    private List<AgencyIdListManifestId> agencyIdListManifestIds;

    public List<AccManifestId> getAccManifestIds() {
        if (accManifestIds == null) {
            return Collections.emptyList();
        }
        return accManifestIds;
    }

    public List<AsccpManifestId> getAsccpManifestIds() {
        if (asccpManifestIds == null) {
            return Collections.emptyList();
        }
        return asccpManifestIds;
    }

    public List<BccpManifestId> getBccpManifestIds() {
        if (bccpManifestIds == null) {
            return Collections.emptyList();
        }
        return bccpManifestIds;
    }

    public List<DtManifestId> getDtManifestIds() {
        if (dtManifestIds == null) {
            return Collections.emptyList();
        }
        return dtManifestIds;
    }

    public List<CodeListManifestId> getCodeListManifestIds() {
        if (codeListManifestIds == null) {
            return Collections.emptyList();
        }
        return codeListManifestIds;
    }

    public List<AgencyIdListManifestId> getAgencyIdListManifestIds() {
        if (agencyIdListManifestIds == null) {
            return Collections.emptyList();
        }
        return agencyIdListManifestIds;
    }
}
