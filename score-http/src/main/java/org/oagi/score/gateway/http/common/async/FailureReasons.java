package org.oagi.score.gateway.http.common.async;

import org.springframework.dao.ConcurrencyFailureException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

/**
 * What a user is told of why background work failed, and what an administrator keeps of it. The user is told one of a
 * few kinds of reason; the rest is in the record that is kept for the administrator.
 */
public final class FailureReasons {

    /** How far down the chain of causes it looks, since a cause may point back to the one it came from. */
    static final int MAX_CAUSES = 20;

    static final String CONCURRENT = "Another change was being made at the same time. Please try again.";
    static final String UNEXPECTED = "An unexpected error occurred. Please try again, and tell an administrator if it happens again.";

    private FailureReasons() {
    }

    /** What is told of a cause that only the work itself can explain: empty when it does not know it. */
    @FunctionalInterface
    public interface Explainer extends Function<Throwable, Optional<String>> {
    }

    /**
     * The reason to tell. The causes are looked at from the outermost one down, and for each of them the explainers are
     * asked in the order they are given, then the kinds that are the same for any work (a conflict of locks).
     */
    public static String of(Throwable cause, Explainer... explainers) {
        int depth = 0;
        for (Throwable t = cause; t != null && depth < MAX_CAUSES; t = t.getCause(), depth++) {
            for (Explainer explainer : explainers) {
                Optional<String> reason = explainer.apply(t);
                if (reason.isPresent()) {
                    return reason.get();
                }
            }
            if (t instanceof ConcurrencyFailureException || isLockConflict(t)) {
                return CONCURRENT;
            }
        }
        return UNEXPECTED;
    }

    static boolean isLockConflict(Throwable t) {
        return t instanceof SQLException e
                && ("40001".equals(e.getSQLState()) || e.getErrorCode() == 1205 || e.getErrorCode() == 1213);
    }

    /** The cause at the bottom of the chain, as a line. */
    public static String rootMessage(Throwable cause) {
        Throwable root = cause;
        int depth = 0;
        while (root != null && root.getCause() != null && root.getCause() != root && depth++ < MAX_CAUSES) {
            root = root.getCause();
        }
        return root == null ? "unknown" : root.getClass().getName() + ": " + root.getMessage();
    }

    /** All there is to know, for the record to keep: what went wrong, whole, and the stack trace that goes with it. */
    public static String withStackTrace(String technical, Throwable cause) {
        if (cause == null) {
            return technical;
        }
        java.io.StringWriter writer = new java.io.StringWriter();
        writer.write(technical);
        writer.write("\n\n");
        cause.printStackTrace(new java.io.PrintWriter(writer));
        return writer.toString();
    }
}
