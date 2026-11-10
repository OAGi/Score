package org.oagi.score.gateway.http.common.async;

/** What is done now: whatever fails is given back at once. */
public class GiveBackRetryPolicy implements RetryPolicy {

    @Override
    public Decision decide(Failure failure) {
        return Decision.GIVE_BACK;
    }
}
