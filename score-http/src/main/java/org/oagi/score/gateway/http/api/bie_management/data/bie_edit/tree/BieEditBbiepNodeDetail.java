package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditAgencyIdList;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditCodeList;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditXbt;
import org.springframework.util.StringUtils;

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

    private long bdtId;
    private String bdtDen;

    private Long bdtPriRestriId;
    private Long codeListId;
    private Long agencyIdListId;

    private List<BieEditXbt> xbtList = Collections.emptyList();
    private List<BieEditCodeList> codeLists = Collections.emptyList();
    private List<BieEditAgencyIdList> agencyIdLists = Collections.emptyList();

    private String contextDefinition;
    private String associationDefinition;
    private String componentDefinition;

    private String bccDen;
    private String bccpDen;

    private String example;

    public BieEditBbiepNodeDetail append(BieEditBbiepNode bbiepNode) {

        this.setTopLevelAsbiepId(bbiepNode.getTopLevelAsbiepId());
        this.setReleaseId(bbiepNode.getReleaseId());
        this.setType(bbiepNode.getType());
        this.setGuid(bbiepNode.getGuid());
        this.setName(bbiepNode.getName());
        this.setRequired(bbiepNode.isRequired());
        this.setDerived(bbiepNode.isDerived());
        this.setLocked(bbiepNode.isLocked());
        this.setHasChild(bbiepNode.isHasChild());

        this.setBbieId(bbiepNode.getBbieId());
        this.setBccId(bbiepNode.getBccId());
        this.setBbiepId(bbiepNode.getBbiepId());
        this.setBccpId(bbiepNode.getBccpId());
        this.setAttribute(bbiepNode.isAttribute());

        return this;
    }

    public void setBdtDen(String bdtDen) {
        if (!StringUtils.isEmpty(bdtDen)) {
            this.bdtDen = bdtDen.replaceAll("_ ", " ");
        }

    }
}
