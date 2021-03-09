package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.Auditable;

import java.io.Serializable;
import java.math.BigInteger;

public class ModuleDep extends Auditable implements Comparable<ModuleDep>, Serializable {

    private BigInteger moduleDepId;

    private BigInteger dependencyType;

    private BigInteger dependingModuleSetAssignmentId;

    private BigInteger dependedModuleSetAssignmentId;

    public BigInteger getModuleDepId() {
        return moduleDepId;
    }

    public void setModuleDepId(BigInteger moduleDepId) {
        this.moduleDepId = moduleDepId;
    }

    public BigInteger getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(BigInteger dependencyType) {
        this.dependencyType = dependencyType;
    }

    public BigInteger getDependingModuleSetAssignmentId() {
        return dependingModuleSetAssignmentId;
    }

    public void setDependingModuleSetAssignmentId(BigInteger dependingModuleSetAssignmentId) {
        this.dependingModuleSetAssignmentId = dependingModuleSetAssignmentId;
    }

    public BigInteger getDependedModuleSetAssignmentId() {
        return dependedModuleSetAssignmentId;
    }

    public void setDependedModuleSetAssignmentId(BigInteger dependedModuleSetAssignmentId) {
        this.dependedModuleSetAssignmentId = dependedModuleSetAssignmentId;
    }

    @Override
    public int compareTo(ModuleDep o) {
        return this.moduleDepId.compareTo(o.getModuleDepId());
    }
}
