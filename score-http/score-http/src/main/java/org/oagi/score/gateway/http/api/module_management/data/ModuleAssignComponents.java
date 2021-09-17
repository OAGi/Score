package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;
import org.oagi.score.repo.api.module.model.AssignableNode;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Data
public class ModuleAssignComponents {

    private Map<BigInteger, AssignableNode> assignableAccManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableAsccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableBccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableDtManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableCodeListManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableAgencyIdListManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableXbtManifestMap = new HashMap();

    private Map<BigInteger, AssignableNode> assignedAccManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignedAsccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignedBccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignedDtManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignedCodeListManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignedAgencyIdListManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignedXbtManifestMap = new HashMap();

    public Map<BigInteger, AssignableNode> getAssignableAccManifestMap() {
        return assignableAccManifestMap;
    }

    public void setAssignableAccManifestMap(Map<BigInteger, AssignableNode> assignableAccManifestMap) {
        this.assignableAccManifestMap = assignableAccManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignableAsccpManifestMap() {
        return assignableAsccpManifestMap;
    }

    public void setAssignableAsccpManifestMap(Map<BigInteger, AssignableNode> assignableAsccpManifestMap) {
        this.assignableAsccpManifestMap = assignableAsccpManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignableBccpManifestMap() {
        return assignableBccpManifestMap;
    }

    public void setAssignableBccpManifestMap(Map<BigInteger, AssignableNode> assignableBccpManifestMap) {
        this.assignableBccpManifestMap = assignableBccpManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignableDtManifestMap() {
        return assignableDtManifestMap;
    }

    public void setAssignableDtManifestMap(Map<BigInteger, AssignableNode> assignableDtManifestMap) {
        this.assignableDtManifestMap = assignableDtManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignableCodeListManifestMap() {
        return assignableCodeListManifestMap;
    }

    public void setAssignableCodeListManifestMap(Map<BigInteger, AssignableNode> assignableCodeListManifestMap) {
        this.assignableCodeListManifestMap = assignableCodeListManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignableAgencyIdListManifestMap() {
        return assignableAgencyIdListManifestMap;
    }

    public void setAssignableAgencyIdListManifestMap(Map<BigInteger, AssignableNode> assignableAgencyIdListManifestMap) {
        this.assignableAgencyIdListManifestMap = assignableAgencyIdListManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignableXbtManifestMap() {
        return assignableXbtManifestMap;
    }

    public void setAssignableXbtManifestMap(Map<BigInteger, AssignableNode> assignableXbtManifestMap) {
        this.assignableXbtManifestMap = assignableXbtManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignedAccManifestMap() {
        return assignedAccManifestMap;
    }

    public void setAssignedAccManifestMap(Map<BigInteger, AssignableNode> assignedAccManifestMap) {
        this.assignedAccManifestMap = assignedAccManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignedAsccpManifestMap() {
        return assignedAsccpManifestMap;
    }

    public void setAssignedAsccpManifestMap(Map<BigInteger, AssignableNode> assignedAsccpManifestMap) {
        this.assignedAsccpManifestMap = assignedAsccpManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignedBccpManifestMap() {
        return assignedBccpManifestMap;
    }

    public void setAssignedBccpManifestMap(Map<BigInteger, AssignableNode> assignedBccpManifestMap) {
        this.assignedBccpManifestMap = assignedBccpManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignedDtManifestMap() {
        return assignedDtManifestMap;
    }

    public void setAssignedDtManifestMap(Map<BigInteger, AssignableNode> assignedDtManifestMap) {
        this.assignedDtManifestMap = assignedDtManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignedCodeListManifestMap() {
        return assignedCodeListManifestMap;
    }

    public void setAssignedCodeListManifestMap(Map<BigInteger, AssignableNode> assignedCodeListManifestMap) {
        this.assignedCodeListManifestMap = assignedCodeListManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignedAgencyIdListManifestMap() {
        return assignedAgencyIdListManifestMap;
    }

    public void setAssignedAgencyIdListManifestMap(Map<BigInteger, AssignableNode> assignedAgencyIdListManifestMap) {
        this.assignedAgencyIdListManifestMap = assignedAgencyIdListManifestMap;
    }

    public Map<BigInteger, AssignableNode> getAssignedXbtManifestMap() {
        return assignedXbtManifestMap;
    }

    public void setAssignedXbtManifestMap(Map<BigInteger, AssignableNode> assignedXbtManifestMap) {
        this.assignedXbtManifestMap = assignedXbtManifestMap;
    }
}
