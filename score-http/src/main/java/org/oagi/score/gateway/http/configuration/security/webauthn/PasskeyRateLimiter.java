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
 * Fixed one-minute windows kept in memory, so the limits apply per backend instance. The limits are set by
 * {@code score.passkey.rate-limit.per-ip-per-minute} and {@code score.passkey.rate-limit.per-username-per-minute}.
 */
@Component
public class PasskeyRateLimiter {

    static final long WINDOW_MILLIS = 60_000L;

    private record Window(long start, int count) {
    }

    private final int perIp;
    private final int perUsername;
    private final LongSupplier clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private volatile long lastSweep;

    @Autowired
    public PasskeyRateLimiter(
            @Value("${score.passkey.rate-limit.per-ip-per-minute:60}") int perIp,
            @Value("${score.passkey.rate-limit.per-username-per-minute:10}") int perUsername) {
        this(perIp, perUsername, System::currentTimeMillis);
    }

    PasskeyRateLimiter(int perIp, int perUsername, LongSupplier clock) {
        this.perIp = perIp;
        this.perUsername = perUsername;
        this.clock = clock;
        this.lastSweep = clock.getAsLong();
    }

    /**
     * @return whether the client at this address may make another request now
     */
    public boolean tryAcquireForIp(String ip) {
        return tryAcquire("ip:" + ip, perIp);
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
