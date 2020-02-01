package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAbieNodeDetail extends BieEditAbieNode implements BieEditNodeDetail {

    private String version;
    private String status;
    private String remark;
    private String bizTerm;
    private String definition;

    public BieEditAbieNodeDetail append(BieEditAbieNode abieNode) {

        this.setTopLevelAbieId(abieNode.getTopLevelAbieId());
        this.setReleaseId(abieNode.getReleaseId());
        this.setType(abieNode.getType());
        this.setGuid(abieNode.getGuid());
        this.setName(abieNode.getName());
        this.setHasChild(abieNode.isHasChild());

        this.setAsbiepId(abieNode.getAsbiepId());
        this.setAbieId(abieNode.getAbieId());
        this.setAsccpId(abieNode.getAsccpId());
        this.setAccId(abieNode.getAccId());
        this.setTopLevelAbieState(abieNode.getTopLevelAbieState());

        return this;
    }
}
