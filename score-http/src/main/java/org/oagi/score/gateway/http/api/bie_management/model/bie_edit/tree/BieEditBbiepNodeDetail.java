package org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBbiepNodeDetail extends BieEditBbiepNode implements BieEditNodeDetail {

    private Integer ccCardinalityMin;
    private Integer ccCardinalityMax;
    private Integer bieCardinalityMin;
    private Integer bieCardinalityMax;

    private Boolean ccNillable;
    private Boolean bieNillable;
    private String ccFixedValue;
    private String bieFixedValue;
    private String ccDefaultValue;
    private String bieDefaultValue;
    private String bizTerm;
    private String remark;

    private String bccDen;
    private String bccpDen;

    private BigInteger bdtId;
    private String bdtDen;

    private XbtManifestId xbtManifestId;
    private CodeListManifestId codeListManifestId;
    private AgencyIdListManifestId agencyIdListManifestId;

    private List<XbtSummaryRecord> xbtList = Collections.emptyList();
    private List<CodeListSummaryRecord> codeLists = Collections.emptyList();
    private List<AgencyIdListSummaryRecord> agencyIdLists = Collections.emptyList();

    private String contextDefinition;
    private String associationDefinition;
    private String componentDefinition;

    private String example;

    public BieEditBbiepNodeDetail append(BieEditBbiepNode bbiepNode) {

        this.setTopLevelAsbiepId(bbiepNode.getTopLevelAsbiepId());
        this.setReleaseId(bbiepNode.getReleaseId());
        this.setType(bbiepNode.getType());
        this.setGuid(bbiepNode.getGuid());
        this.setName(bbiepNode.getName());
        this.setHasChild(bbiepNode.isHasChild());

        this.setRequired(bbiepNode.isRequired());
        this.setDerived(bbiepNode.isDerived());
        this.setLocked(bbiepNode.isLocked());

        this.setBbieId(bbiepNode.getBbieId());
        this.setBccManifestId(bbiepNode.getBccManifestId());
        this.setBbiepId(bbiepNode.getBbiepId());
        this.setBccpManifestId(bbiepNode.getBccpManifestId());
        this.setAttribute(bbiepNode.isAttribute());

        return this;
    }

    public void setBdtDen(String bdtDen) {
        if (StringUtils.hasLength(bdtDen)) {
            this.bdtDen = bdtDen.replaceAll("_ ", " ");
        }

    }
}
