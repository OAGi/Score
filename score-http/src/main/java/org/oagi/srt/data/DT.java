package org.oagi.srt.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DT implements CoreComponent {

    private long dtId;
    private String guid;
    private int type;
    private String versionNum;
    private Long previousVersionDtId;
    private String dataTypeTerm;
    private String qualifier;
    private Long basedDtId;
    private String den;
    private String contentComponentDen;
    private String definition;
    private String definitionSource;
    private String contentComponentDefinition;
    private String revisionDoc;
    private int state;
    private Long moduleId;
    private String module;
    private long createdBy;
    private long ownerUserId;
    private long lastUpdatedBy;
    private Date creationTimestamp;
    private Date lastUpdateTimestamp;
    private int revisionNum;
    private int revisionTrackingNum;
    private Integer revisionAction;
    private Long releaseId;
    private Long currentBdtId;
    private boolean deprecated;

    @Override
    public long getId() {
        return getDtId();
    }

    @Override
    public Long getCurrentId() {
        return currentBdtId;
    }

}
