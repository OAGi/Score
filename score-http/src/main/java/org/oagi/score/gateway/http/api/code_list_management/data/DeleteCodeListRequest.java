package org.oagi.score.gateway.http.api.code_list_management.data;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class DeleteCodeListRequest {

    private List<Long> codeListIds = Collections.emptyList();
}
