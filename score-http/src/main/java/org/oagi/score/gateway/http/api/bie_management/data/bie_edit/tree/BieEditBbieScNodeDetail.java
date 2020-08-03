package org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditAgencyIdList;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditCodeList;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.BieEditXbt;

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

    private Long dtScPriRestriId;
    private Long codeListId;
    private Long agencyIdListId;

    private List<BieEditXbt> xbtList = Collections.emptyList();
    private List<BieEditCodeList> codeLists = Collections.emptyList();
    private List<BieEditAgencyIdList> agencyIdLists = Collections.emptyList();

    private String contextDefinition;
    private String componentDefinition;

    private String example;

    public BieEditBbieScNodeDetail append(BieEditBbieScNode bbieScNode) {

        this.setTopLevelAsbiepId(bbieScNode.getTopLevelAsbiepId());
        this.setReleaseId(bbieScNode.getReleaseId());
        this.setType(bbieScNode.getType());
        this.setGuid(bbieScNode.getGuid());
        this.setName(bbieScNode.getName());
        this.setRequired(bbieScNode.isRequired());
        this.setDerived(bbieScNode.isDerived());
        this.setLocked(bbieScNode.isLocked());
        this.setHasChild(bbieScNode.isHasChild());

        this.setBbieScId(bbieScNode.getBbieScId());
        this.setDtScId(bbieScNode.getDtScId());

        return this;
    }
}
