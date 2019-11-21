package org.oagi.srt.gateway.http.api.code_list_management.data;

import lombok.Data;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class CodeListForListRequest {

    private String name;
    private List<String> states = Collections.emptyList();
    private Boolean extensible;

    private List<String> updaterLoginIds;
    private Date updateStartDate;
    private Date updateEndDate;
    private PageRequest pageRequest;

}
