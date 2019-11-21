package org.oagi.srt.data;

import lombok.Data;

@Data
public class AgencyIdListValue {
    private long agencyIdListValueId;
    private String value;
    private String name;
    private String definition;
    private long ownerListId;
}
