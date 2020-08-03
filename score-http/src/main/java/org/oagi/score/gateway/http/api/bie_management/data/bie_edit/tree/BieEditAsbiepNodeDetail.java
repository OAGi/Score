package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

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
    private String remark; // asbiep remark
    private String bizTerm; // asbiep bizterm

    private String contextDefinition; // asbie definition
    private String associationDefinition;
    private String componentDefinition;
    private String typeDefinition;

    private String asccDen;
    private String asccDefinition;
    private String asccpDen;
    private String asccpDefinition;
    private String accDen;
    private String accDefinition;

    public BieEditAsbiepNodeDetail append(BieEditAsbiepNode asbiepNode) {

        this.setTopLevelAsbiepId(asbiepNode.getTopLevelAsbiepId());
        this.setReleaseId(asbiepNode.getReleaseId());
        this.setType(asbiepNode.getType());
        this.setGuid(asbiepNode.getGuid());
        this.setName(asbiepNode.getName());
        this.setRequired(asbiepNode.isRequired());
        this.setDerived(asbiepNode.isDerived());
        this.setLocked(asbiepNode.isLocked());
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
