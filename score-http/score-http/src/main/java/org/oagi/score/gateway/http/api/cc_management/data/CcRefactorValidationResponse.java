package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Data
public class CcRefactorValidationResponse {

    private String type;

    private BigInteger manifestId;

    private List<IssuedCc> issueList;

    @Data
    public static class IssuedCc {
        private BigInteger manifestId;
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
        private BigInteger id;

        private List<String> reasons;
    }
}
