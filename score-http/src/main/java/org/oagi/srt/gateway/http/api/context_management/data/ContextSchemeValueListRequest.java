package org.oagi.srt.gateway.http.api.context_management.data;

import lombok.Data;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;

import java.util.Date;
import java.util.List;

@Data
public class ContextSchemeValueListRequest {

    private String value;

    private PageRequest pageRequest;

}
