package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.common.model.PageRequest;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class OasDocListRequest {

    private BigInteger oasDocId;
    private String openAPIVersion;
    private String title;
    private String description;
    private String version;
    private List<String> updaterLoginIdList = Collections.emptyList();
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest;

}
