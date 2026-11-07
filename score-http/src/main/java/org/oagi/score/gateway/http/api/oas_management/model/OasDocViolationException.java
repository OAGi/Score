package org.oagi.score.gateway.http.api.oas_management.model;

import java.util.List;

/**
 * A change set breaks a rule. It is an {@link IllegalArgumentException} so that whatever handles one (HTTP 400) handles it too,
 * and it carries where the rule is broken.
 */
public class OasDocViolationException extends IllegalArgumentException {

    private final transient List<OasDocViolation> violations;

    public OasDocViolationException(List<OasDocViolation> violations) {
        super(violations.isEmpty() ? "The change is not valid." : violations.get(0).message());
        this.violations = List.copyOf(violations);
    }

    public OasDocViolationException(OasDocViolation violation) {
        this(List.of(violation));
    }

    /** What was made of the names of the operations before the rule was broken. */
    private transient List<OasDocChangeResult.DerivedOperation> derived = List.of();

    public List<OasDocViolation> violations() {
        return violations;
    }

    public List<OasDocChangeResult.DerivedOperation> derived() {
        return derived;
    }

    public OasDocViolationException withDerived(List<OasDocChangeResult.DerivedOperation> derived) {
        this.derived = derived;
        return this;
    }
}
