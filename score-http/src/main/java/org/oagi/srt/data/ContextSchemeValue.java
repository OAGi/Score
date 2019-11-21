package org.oagi.srt.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContextSchemeValue implements Serializable {
    private long ctxSchemeValueId;
    private String guid;
    private String value;
    private String meaning;
    private long ownerCtxSchemeId;
}
