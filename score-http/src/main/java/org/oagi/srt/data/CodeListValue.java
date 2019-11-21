package org.oagi.srt.data;

import lombok.Data;

@Data
public class CodeListValue {
    private long codeListValueId;
    private long codeListId;
    private String value;
    private String name;
    private String definition;
    private String definitionSource;
    private boolean usedIndicator;
    private boolean lockedIndicator;
    private boolean extensionIndicator;
}
