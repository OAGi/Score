package org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbieScNodeDetail extends BieEditBbieScNode implements BieEditNodeDetail {

    private Integer ccCardinalityMin;
    private Integer ccCardinalityMax;
    private Integer bieCardinalityMin;
    private Integer bieCardinalityMax;

    private String ccFixedValue;
    private String bieFixedValue;
    private String ccDefaultValue;
    private String bieDefaultValue;
    private String bizTerm;
    private String remark;

    private XbtManifestId xbtManifestId;
    private CodeListManifestId codeListManifestId;
    private AgencyIdListManifestId agencyIdListManifestId;

    private List<XbtSummaryRecord> xbtList = Collections.emptyList();
    private List<CodeListSummaryRecord> codeLists = Collections.emptyList();
    private List<AgencyIdListSummaryRecord> agencyIdLists = Collections.emptyList();

    private String contextDefinition;
    private String componentDefinition;

    private String example;

    public BieEditBbieScNodeDetail append(BieEditBbieScNode bbieScNode) {

        this.setTopLevelAsbiepId(bbieScNode.getTopLevelAsbiepId());
        this.setReleaseId(bbieScNode.getReleaseId());
        this.setType(bbieScNode.getType());
        this.setGuid(bbieScNode.getGuid());
        this.setName(bbieScNode.getName());
        this.setHasChild(bbieScNode.isHasChild());

        this.setRequired(bbieScNode.isRequired());
        this.setDerived(bbieScNode.isDerived());
        this.setLocked(bbieScNode.isLocked());

        this.setBbieScId(bbieScNode.getBbieScId());
        this.setDtScManifestId(bbieScNode.getDtScManifestId());

        return this;
    }
}
