package org.oagi.score.gateway.http.common.model;

import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AccessControlException extends ScoreDataAccessException {

    private final List<ScoreRole> requiredAnyRole;

    public AccessControlException(String msg) {
        super(msg);
        this.requiredAnyRole = List.of();
    }

    public AccessControlException(ScoreUser requester) {
        super(String.format("'%s' account does not have a right permission to access the function.", requester.username()));
        this.requiredAnyRole = List.of();
    }

    /**
     * A user who has none of the roles that a method requires. It says what is lacking, which is one of the roles, unless the method
     * says in its own words what the user is told ({@link AccessControl#errorMessage()}).
     */
    public AccessControlException(ScoreUser requester, ScoreRole[] requiredAnyRole, String errorMessage) {
        super(errorMessage != null && !errorMessage.isBlank() ? errorMessage : lacking(requiredAnyRole));
        this.requiredAnyRole = List.copyOf(Arrays.asList(requiredAnyRole));
    }

    /** The roles of which the user needs one. */
    public List<ScoreRole> requiredAnyRole() {
        return requiredAnyRole;
    }

    static String lacking(ScoreRole[] requiredAnyRole) {
        List<String> who = Arrays.stream(requiredAnyRole).map(AccessControlException::whoHas).distinct().collect(Collectors.toList());
        String list = who.size() <= 1 ? String.join("", who)
                : String.join(", ", who.subList(0, who.size() - 1)) + " or " + who.get(who.size() - 1);
        return "Only " + list + " can access this.";
    }

    private static String whoHas(ScoreRole role) {
        return switch (role) {
            case ADMINISTRATOR -> "an administrator";
            case DEVELOPER -> "a developer";
            case END_USER -> "an end user";
            case ANONYMOUS -> "an anonymous user";
        };
    }

}
