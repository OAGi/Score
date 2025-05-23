package org.oagi.score.gateway.http.api.account_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.common.model.PageRequest;

import java.util.Date;

@Data
public class PendingListRequest {

    private String preferredUsername;
    private String email;
    private String providerName;

    private Date createStartDate;
    private Date createEndDate;
    private PageRequest pageRequest;
}