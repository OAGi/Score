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
    private String asbiepBizTerm;
    private String asbiepRemark;

    private String asccDen;
    private String asccpDen;
    private String accDen;

    private String asbiepDefinition;
    private String asccDefinition;
    private String asccpDefinition;
    private String accDefinition;

    public BieEditAsbiepNodeDetail append(BieEditAsbiepNode asbiepNode) {

        this.setTopLevelAsbiepId(asbiepNode.getTopLevelAsbiepId());
        this.setReleaseId(asbiepNode.getReleaseId());
        this.setType(asbiepNode.getType());
        this.setGuid(asbiepNode.getGuid());
        this.setName(asbiepNode.getName());
        this.setHasChild(asbiepNode.isHasChild());

        this.setRequired(asbiepNode.isRequired());
        this.setDerived(asbiepNode.isDerived());
        this.setLocked(asbiepNode.isLocked());

        this.setAsbieId(asbiepNode.getAsbieId());
        this.setAsccManifestId(asbiepNode.getAsccManifestId());
        this.setAsbiepId(asbiepNode.getAsbiepId());
        this.setAsccpManifestId(asbiepNode.getAsccpManifestId());
        this.setAbieId(asbiepNode.getAbieId());
        this.setAccManifestId(asbiepNode.getAccManifestId());

        return this;
    }
}
