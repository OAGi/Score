package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.Date;

@Data
public class CcList {

    private CcType type;
    private BigInteger manifestId;
    private String guid;
    private String den;
    private String definition;
    private String module;
    private String name;

    public String getModule() {
        return !StringUtils.hasLength(module) ? "" : module;
    }

    private String definitionSource;
    private OagisComponentType oagisComponentType;
    private String dtType;
    private String owner;
    private CcState state;
    private String revision;
    private boolean deprecated;
    private String lastUpdateUser;
    private Date lastUpdateTimestamp;
    private String releaseNum;
    private BigInteger id;

    private boolean ownedByDeveloper;
}
