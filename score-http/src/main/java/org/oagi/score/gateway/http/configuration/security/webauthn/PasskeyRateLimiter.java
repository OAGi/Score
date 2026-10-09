package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Limits the unauthenticated passkey endpoints ({@code /info/passkey/lookup} and
 * {@code /webauthn/authenticate/options}), which anyone can call: the first answers whether a login ID has a
 * passkey and the second creates a session in Redis for every call.
 * <p>
 * Fixed one-minute windows kept in memory, so the limits apply per backend instance. The lookup has the tightest
 * limits, because a person types a handful of names a minute: {@code lookup-per-ip-per-minute} per client address and
 * {@code per-username-per-minute} per login ID. The sign-in options, which the login page also requests for the
 * autofill and the passkey button, have their own, higher limit per client address ({@code per-ip-per-minute}).
 */
@Component
public class PasskeyRateLimiter {

    static final long WINDOW_MILLIS = 60_000L;

    private record Window(long start, int count) {
    }

    private final int lookupPerIp;
    private final int optionsPerIp;
    private final int perUsername;
    private final LongSupplier clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private volatile long lastSweep;

    @Autowired
    public PasskeyRateLimiter(
            @Value("${score.passkey.rate-limit.lookup-per-ip-per-minute:10}") int lookupPerIp,
            @Value("${score.passkey.rate-limit.per-ip-per-minute:30}") int optionsPerIp,
            @Value("${score.passkey.rate-limit.per-username-per-minute:5}") int perUsername) {
        this(lookupPerIp, optionsPerIp, perUsername, System::currentTimeMillis);
    }

    PasskeyRateLimiter(int lookupPerIp, int optionsPerIp, int perUsername, LongSupplier clock) {
        this.lookupPerIp = lookupPerIp;
        this.optionsPerIp = optionsPerIp;
        this.perUsername = perUsername;
        this.clock = clock;
        this.lastSweep = clock.getAsLong();
    }

    /**
     * @return whether the client at this address may look up another login ID now
     */
    public boolean tryAcquireLookupForIp(String ip) {
        return tryAcquire("lookup-ip:" + ip, lookupPerIp);
    }

    /**
     * @return whether the client at this address may ask for another set of sign-in options now
     */
    public boolean tryAcquireOptionsForIp(String ip) {
        return tryAcquire("options-ip:" + ip, optionsPerIp);
    }

    /**
     * @return whether another lookup of this login ID is allowed now; the same limit holds however many addresses
     * the requests come from
     */
    public boolean tryAcquireForUsername(String loginId) {
        return tryAcquire("user:" + loginId.trim().toLowerCase(), perUsername);
    }

    private boolean tryAcquire(String key, int limit) {
        long now = clock.getAsLong();
        sweep(now);
        Window window = windows.compute(key, (k, w) ->
                (w == null || now - w.start() >= WINDOW_MILLIS) ? new Window(now, 1) : new Window(w.start(), w.count() + 1));
        return window.count() <= limit;
    }

    /** Drops expired windows, so a flood of distinct keys cannot grow the map without bound. */
    private void sweep(long now) {
        if (now - lastSweep < WINDOW_MILLIS) {
            return;
        }
        synchronized (this) {
            if (now - lastSweep >= WINDOW_MILLIS) {
                windows.values().removeIf(w -> now - w.start() >= WINDOW_MILLIS);
                lastSweep = now;
            }
        }
    }

}
