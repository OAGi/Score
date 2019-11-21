package org.oagi.srt.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ACC implements CoreComponent {

    private long accId;
    private String guid;
    private String objectClassTerm;
    private String den;
    private String definition;
    private String definitionSource;
    private Long basedAccId;
    private String objectClassQualifier;
    private Integer oagisComponentType;
    private Long moduleId;
    private String module;
    private Long namespaceId;
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
    private Long currentAccId;
    private boolean deprecated;
    private boolean abstracted;

    public long getId() {
        return getAccId();
    }

    @Override
    public Long getCurrentId() {
        return getCurrentAccId();
    }

}
