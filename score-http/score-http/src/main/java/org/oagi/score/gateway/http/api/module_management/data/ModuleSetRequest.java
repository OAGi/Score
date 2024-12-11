package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ModuleSetRequest {
    public BigInteger libraryId;
    public String name;
    public String description;
    public boolean createModuleSetRelease;
    public BigInteger targetReleaseId;
    public BigInteger targetModuleSetReleaseId;
}
