package org.oagi.score.gateway.http.api.validation;

import java.util.List;

/**
 * A node a violation is about. The path is the address the tree of the screen uses, root first, so that the screen can
 * walk to the node.
 */
public record Location(String label, List<PathSegment> path, String role) {
}
