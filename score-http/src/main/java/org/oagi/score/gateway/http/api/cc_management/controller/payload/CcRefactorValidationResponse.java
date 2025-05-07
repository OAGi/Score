package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.common.model.Id;

import java.util.Date;
import java.util.List;

@Data
public class CcRefactorValidationResponse {

    private CcType type;

    private ManifestId manifestId;

    private List<IssuedCc> issueList;

    @Data
    public static class IssuedCc {
        private ManifestId manifestId;
        private String guid;
        private String den;
        private String name;

        private OagisComponentType oagisComponentType;
        private String owner;
        private CcState state;
        private String revision;
        private boolean deprecated;
        private String lastUpdateUser;
        private Date lastUpdateTimestamp;
        private String releaseNum;
        private Id id;

        private List<String> reasons;
    }
}
