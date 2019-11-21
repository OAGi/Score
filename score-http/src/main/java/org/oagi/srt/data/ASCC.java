package org.oagi.srt.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ASCC implements CoreComponent {

    private long asccId;
    private String guid;
    private int cardinalityMin;
    private int cardinalityMax;
    private int seqKey;
    private long fromAccId;
    private long toAsccpId;
    private String den;
    private String definition;
    private String definitionSource;
    private long createdBy;
    private long ownerUserId;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private int revisionNum;
    private int revisionTrackingNum;
    private Integer revisionAction;
    private Long releaseId;
    private int state;
    private Long currentAsccId;
    private boolean deprecated;

    public long getId() {
        return getAsccId();
    }

    @Override
    public Long getCurrentId() {
        return getCurrentAsccId();
    }


}
