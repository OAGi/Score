package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAbieNodeDetail extends BieEditAbieNode implements BieEditNodeDetail {
    private String accDen;
    private String typeDefinition;
    private String asccpDen;
    private String componentDefinition;

    private String bizTerm;
    private String remark;
    private String contextDefinition;
    private String version;
    private String status;

    public BieEditAbieNodeDetail append(BieEditAbieNode abieNode) {

        this.setTopLevelAsbiepId(abieNode.getTopLevelAsbiepId());
        this.setReleaseId(abieNode.getReleaseId());
        this.setType(abieNode.getType());
        this.setGuid(abieNode.getGuid());
        this.setName(abieNode.getName());
        this.setHasChild(abieNode.isHasChild());

        this.setAsbiepId(abieNode.getAsbiepId());
        this.setAbieId(abieNode.getAbieId());
        this.setAsccpId(abieNode.getAsccpId());
        this.setAccId(abieNode.getAccId());
        this.setTopLevelAsbiepState(abieNode.getTopLevelAsbiepState());
        this.setOwnerLoginId(abieNode.getOwnerLoginId());

        return this;
    }
}
