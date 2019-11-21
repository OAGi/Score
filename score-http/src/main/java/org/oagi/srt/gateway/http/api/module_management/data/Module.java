package org.oagi.srt.gateway.http.api.module_management.data;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Module {

    private long moduleId;
    private String module;
    private long namespaceId;

    private List<ModuleDependency> moduleDependencies = Collections.emptyList();

}
