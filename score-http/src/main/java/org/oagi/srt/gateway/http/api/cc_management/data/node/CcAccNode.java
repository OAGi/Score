package org.oagi.srt.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcAccNode extends CcNode {

    private String type = "acc";
    private long accId;
    private String den;
    private String guid;
    private String objectClassTerm;
    private Long basedAccId;
    private Long currentAccId;
    private int oagisComponentType;
    private String definition;
    private boolean group;
    private boolean isDeprecated;
    private boolean isAbstract;

    @Override
    public long getId() {
        return accId;
    }

    @Override
    public Long getCurrentId() {
        return currentAccId;
    }
}
