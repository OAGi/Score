package org.oagi.srt.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContextCategory implements Serializable {
    private long ctxCategoryId;
    private String guid;
    private String name;
    private String description;
}
