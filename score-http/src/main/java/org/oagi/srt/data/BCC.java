package org.oagi.srt.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BCC implements CoreComponent {

    private long bccId;
    private String guid;
    private int cardinalityMin;
    private int cardinalityMax;
    private int seqKey;
    private int entityType;
    private long fromAccId;
    private long toBccpId;
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
    private Long currentBccId;
    private boolean deprecated;
    private boolean nillable;

    public long getId() {
        return getBccId();
    }

    @Override
    public Long getCurrentId() {
        return getCurrentBccId();
    }

}
