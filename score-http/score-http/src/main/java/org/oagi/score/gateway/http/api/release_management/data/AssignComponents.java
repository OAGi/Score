package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Data
public class AssignComponents {

    private Map<BigInteger, AssignableNode> assignableAccManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableAsccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableBccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> assignableCodeListManifestMap = new HashMap();

    private Map<BigInteger, AssignableNode> unassignableAccManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> unassignableAsccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> unassignableBccpManifestMap = new HashMap();
    private Map<BigInteger, AssignableNode> unassignableCodeListManifestMap = new HashMap();

    public void addAssignableAccManifest(BigInteger accManifestId, AssignableNode node) {
        assignableAccManifestMap.put(accManifestId, node);
    }

    public void addAssignableAsccpManifest(BigInteger asccpManifestId, AssignableNode node) {
        assignableAsccpManifestMap.put(asccpManifestId, node);
    }

    public void addAssignableBccpManifest(BigInteger bccpManifestId, AssignableNode node) {
        assignableBccpManifestMap.put(bccpManifestId, node);
    }

    public void addAssignableCodeListManifest(BigInteger codeListManifestId, AssignableNode node) {
        assignableBccpManifestMap.put(codeListManifestId, node);
    }

    public void addUnassignableAccManifest(BigInteger accManifestId, AssignableNode node) {
        unassignableAccManifestMap.put(accManifestId, node);
    }

    public void addUnassignableAsccpManifest(BigInteger asccpManifestId, AssignableNode node) {
        unassignableAsccpManifestMap.put(asccpManifestId, node);
    }

    public void addUnassignableBccpManifest(BigInteger bccpManifestId, AssignableNode node) {
        unassignableBccpManifestMap.put(bccpManifestId, node);
    }

    public void addUnassignableCodeListManifest(BigInteger codeListManifestId, AssignableNode node) {
        unassignableCodeListManifestMap.put(codeListManifestId, node);
    }
}
