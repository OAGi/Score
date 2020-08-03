package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BdtScPriRestri implements Serializable {

    private long bdtScPriRestriId;
    private long bdtScId;
    private Long cdtScAwdPriXpsTypeMapId;
    private Long codeListId;
    private Long agencyIdListId;
    private boolean defaulted;

    public boolean isDefault() {
        return isDefaulted();
    }

}
