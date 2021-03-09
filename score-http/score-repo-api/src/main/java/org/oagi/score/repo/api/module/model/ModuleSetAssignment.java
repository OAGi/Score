package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;

public class ModuleSetAssignment extends Auditable implements Comparable<ModuleSetAssignment>, Serializable {

    private BigInteger moduleSetAssignmentId;

    private BigInteger moduleSetId;

    private BigInteger moduleId;

    public BigInteger getModuleSetAssignmentId() {
        return moduleSetAssignmentId;
    }

    public void setModuleSetAssignmentId(BigInteger moduleSetAssignmentId) {
        this.moduleSetAssignmentId = moduleSetAssignmentId;
    }

    public BigInteger getModuleSetId() {
        return moduleSetId;
    }

    public void setModuleSetId(BigInteger moduleSetId) {
        this.moduleSetId = moduleSetId;
    }

    public BigInteger getModuleId() {
        return moduleId;
    }

    public void setModuleId(BigInteger moduleId) {
        this.moduleId = moduleId;
    }

    @Override
    public int compareTo(ModuleSetAssignment o) {
        return 1;
    }
}
