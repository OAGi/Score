package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.data.OagisComponentType;

import java.util.Date;

@Data
public class CcList {

    private String type;
    private long id;
    private String guid;
    private String den;
    private String definition;
    private String module;
    private String definitionSource;
    private OagisComponentType oagisComponentType;
    private String owner;
    private CcState state;
    private String revision;
    private boolean deprecated;
    private Long currentId;
    private String lastUpdateUser;
    private Date lastUpdateTimestamp;

}
