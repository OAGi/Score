package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.oagi.score.gateway.http.api.tag_management.data.ShortTag;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class CcChangesResponse {

    private List<CcChange> ccChangeList = new ArrayList<>();

    public void addCcChangeList(Collection<CcChange> changeList) {
        ccChangeList.addAll(changeList);
    }

    public enum CcChangeType {
        NEW_COMPONENT,
        REVISED;
    }

    @Data
    @AllArgsConstructor
    public static class CcChange {

        private String type;
        private BigInteger manifestId;
        private String den;
        private CcChangeType changeType;
        private List<ShortTag> tagList;

        public void addTag(ShortTag tag) {
            if (this.tagList == null) {
                this.tagList = new ArrayList<>();
            }
            this.tagList.add(tag);
        }
    }

}
