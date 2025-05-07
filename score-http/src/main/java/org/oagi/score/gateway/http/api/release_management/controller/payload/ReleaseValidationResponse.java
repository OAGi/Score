package org.oagi.score.gateway.http.api.release_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;

import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Error;
import static org.oagi.score.gateway.http.api.release_management.controller.payload.ReleaseValidationResponse.ValidationMessageLevel.Warning;

@Data
public class ReleaseValidationResponse {

    public enum ValidationMessageLevel {
        Warning,
        Error
    }

    public enum ValidationMessageCode {
        ACC_BasedACC,
        ACC_Association,
        ASCCP_RoleOfAcc,
        BCCP_BDT,
        NAMESPACE
    }

    private class ValidationMessage {
        private ValidationMessageLevel level;
        private String message;
        private ValidationMessageCode code;

        public ValidationMessage(ValidationMessageLevel level, String message, ValidationMessageCode code) {
            this.level = level;
            this.message = message;
            this.code = code;
        }

        public ValidationMessageLevel getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public ValidationMessageCode getCode() {
            return code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValidationMessage that = (ValidationMessage) o;
            return Objects.equals(level, that.level) &&
                    Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(level, message);
        }
    }

    private Map<AccManifestId, Set<ValidationMessage>> statusMapForAcc = new HashMap();
    private Map<AsccpManifestId, Set<ValidationMessage>> statusMapForAsccp = new HashMap();
    private Map<BccpManifestId, Set<ValidationMessage>> statusMapForBccp = new HashMap();
    private Map<DtManifestId, Set<ValidationMessage>> statusMapForDt = new HashMap();
    private Map<CodeListManifestId, Set<ValidationMessage>> statusMapForCodeList = new HashMap();
    private Map<AgencyIdListManifestId, Set<ValidationMessage>> statusMapForAgencyIdList = new HashMap();

    public boolean isSucceed() {
        return (statusMapForAcc.isEmpty() || statusMapForAcc.values().stream().flatMap(e -> e.stream()).filter(e -> e.getLevel() == Error).count() == 0) &&
                (statusMapForAsccp.isEmpty() || statusMapForAsccp.values().stream().flatMap(e -> e.stream()).filter(e -> e.getLevel() == Error).count() == 0) &&
                (statusMapForBccp.isEmpty() || statusMapForBccp.values().stream().flatMap(e -> e.stream()).filter(e -> e.getLevel() == Error).count() == 0) &&
                (statusMapForDt.isEmpty() || statusMapForDt.values().stream().flatMap(e -> e.stream()).filter(e -> e.getLevel() == Error).count() == 0) &&
                (statusMapForCodeList.isEmpty() || statusMapForCodeList.values().stream().flatMap(e -> e.stream()).filter(e -> e.getLevel() == Error).count() == 0) &&
                (statusMapForAgencyIdList.isEmpty() || statusMapForAgencyIdList.values().stream().flatMap(e -> e.stream()).filter(e -> e.getLevel() == Error).count() == 0);
    }

    public void clearWarnings() {
        statusMapForAcc.entrySet().forEach(e -> {
            e.setValue(e.getValue().stream().filter(x -> x.getLevel() != Warning).collect(Collectors.toSet()));
        });
        statusMapForAsccp.entrySet().forEach(e -> {
            e.setValue(e.getValue().stream().filter(x -> x.getLevel() != Warning).collect(Collectors.toSet()));
        });
        statusMapForBccp.entrySet().forEach(e -> {
            e.setValue(e.getValue().stream().filter(x -> x.getLevel() != Warning).collect(Collectors.toSet()));
        });
        statusMapForDt.entrySet().forEach(e -> {
            e.setValue(e.getValue().stream().filter(x -> x.getLevel() != Warning).collect(Collectors.toSet()));
        });
        statusMapForCodeList.entrySet().forEach(e -> {
            e.setValue(e.getValue().stream().filter(x -> x.getLevel() != Warning).collect(Collectors.toSet()));
        });
        statusMapForAgencyIdList.entrySet().forEach(e -> {
            e.setValue(e.getValue().stream().filter(x -> x.getLevel() != Warning).collect(Collectors.toSet()));
        });
    }

    public void addMessageForAcc(AccManifestId manifestId, ValidationMessageLevel level, String message, ValidationMessageCode code) {
        addMessage(statusMapForAcc, manifestId, level, message, code);
    }

    public void addMessageForAsccp(AsccpManifestId manifestId, ValidationMessageLevel level, String message, ValidationMessageCode code) {
        addMessage(statusMapForAsccp, manifestId, level, message, code);
    }

    public void addMessageForBccp(BccpManifestId manifestId, ValidationMessageLevel level, String message, ValidationMessageCode code) {
        addMessage(statusMapForBccp, manifestId, level, message, code);
    }

    public void addMessageForDt(DtManifestId manifestId, ValidationMessageLevel level, String message, ValidationMessageCode code) {
        addMessage(statusMapForDt, manifestId, level, message, code);
    }

    public void addMessageForCodeList(CodeListManifestId manifestId, ValidationMessageLevel level, String message, ValidationMessageCode code) {
        addMessage(statusMapForCodeList, manifestId, level, message, code);
    }

    public void addMessageForAgencyIdList(AgencyIdListManifestId manifestId, ValidationMessageLevel level, String message, ValidationMessageCode code) {
        addMessage(statusMapForAgencyIdList, manifestId, level, message, code);
    }

    private <T extends ManifestId> void addMessage(Map<T, Set<ValidationMessage>> statusMap,
                                                   T manifestId, ValidationMessageLevel level, String message,
                                                   ValidationMessageCode code) {
        Set<ValidationMessage> messages;
        if (!statusMap.containsKey(manifestId)) {
            messages = new HashSet();
            statusMap.put(manifestId, messages);
        } else {
            messages = statusMap.get(manifestId);
        }

        messages.add(new ValidationMessage(level, message, code));
    }
}
