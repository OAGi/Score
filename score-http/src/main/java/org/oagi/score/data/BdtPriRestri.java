package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BdtPriRestri implements Serializable {

    private long bdtPriRestriId;
    private long bdtId;
    private Long cdtAwdPriXpsTypeMapId;
    private Long codeListId;
    private Long agencyIdListId;
    private boolean defaulted;

    public boolean isDefault() {
        return isDefaulted();
    }

}
