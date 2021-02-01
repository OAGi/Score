package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAbieNodeDetail extends BieEditAbieNode implements BieEditNodeDetail {

    private String accDen;
    private String accDefinition;

    private String asccpDen;
    private String asccpDefinition;

    private String topLevelAsbiepVersion;
    private String topLevelAsbiepStatus;
    private String asbiepRemark;
    private String asbiepBizTerm;

    public BieEditAbieNodeDetail append(BieEditAbieNode abieNode) {

        this.setTopLevelAsbiepId(abieNode.getTopLevelAsbiepId());
        this.setReleaseId(abieNode.getReleaseId());
        this.setType(abieNode.getType());
        this.setGuid(abieNode.getGuid());
        this.setName(abieNode.getName());
        this.setHasChild(abieNode.isHasChild());

        this.setAsbiepId(abieNode.getAsbiepId());
        this.setAbieId(abieNode.getAbieId());
        this.setAsccpManifestId(abieNode.getAsccpManifestId());
        this.setAccManifestId(abieNode.getAccManifestId());

        return this;
    }
}
