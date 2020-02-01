package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAsbiepNodeDetail extends BieEditAsbiepNode implements BieEditNodeDetail {

    private Integer ccCardinalityMin;
    private Integer ccCardinalityMax;
    private Integer bieCardinalityMin;
    private Integer bieCardinalityMax;

    private Boolean ccNillable;
    private Boolean bieNillable;
    private String bizTerm;
    private String remark;

    private String contextDefinition;
    private String associationDefinition;
    private String componentDefinition;
    private String typeDefinition;

    public BieEditAsbiepNodeDetail append(BieEditAsbiepNode asbiepNode) {

        this.setTopLevelAbieId(asbiepNode.getTopLevelAbieId());
        this.setReleaseId(asbiepNode.getReleaseId());
        this.setType(asbiepNode.getType());
        this.setGuid(asbiepNode.getGuid());
        this.setName(asbiepNode.getName());
        this.setHasChild(asbiepNode.isHasChild());

        this.setAsbieId(asbiepNode.getAsbieId());
        this.setAsccId(asbiepNode.getAsccId());
        this.setAsbiepId(asbiepNode.getAsbiepId());
        this.setAsccpId(asbiepNode.getAsccpId());
        this.setAbieId(asbiepNode.getAbieId());
        this.setAccId(asbiepNode.getAccId());

        return this;
    }
}
