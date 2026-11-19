package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Everything the load of a release package can be told, under {@code score.release-package-load}. The values written here are the
 * defaults; {@code application.yml} says them again with what each one does.
 */
@Component
@ConfigurationProperties("score.release-package-load")
public class ReleasePackageLoadProperties {

    /** The number from which the records of the users are numbered (what the generator of a package is given). */
    private long delta = 100_000_000L;

    /** How long a statement of the swap waits for a lock that another connection holds, before the swap is given up. */
    private Duration lockWait = Duration.ofSeconds(30);

    /** How long a load waits, to stop writing, for the writers that are at work (a load that cannot stop writing in this time is given up before it changes anything). */
    private Duration drainWait = Duration.ofSeconds(60);

    /** How long a writer waits, while writing is stopped, before it is refused. */
    private Duration writeWait = Duration.ofSeconds(60);

    /** How long a load that goes on may stay as it is before it is taken to have been abandoned. */
    private Duration staleAfter = Duration.ofHours(2);

    /** How long what a load that succeeded or was undone keeps (the schema that an undo needs) is kept, from the last time the load changed, before it is discarded. */
    private Duration retention = Duration.ofDays(7);

    /** The most bytes a package may have. */
    private long maxPackageSize = 1024L * 1024 * 1024;

    /** Where the packages that are on their way to a load are kept (empty: a directory of the temporary files). */
    private String uploadDirectory = "";

    /** How long a package that was not loaded is kept. */
    private Duration uploadTtl = Duration.ofHours(6);

    /** How often the packages that are kept for longer than that are thrown away, and the time to the first sweep. */
    private Duration uploadSweepInterval = Duration.ofMinutes(15);

    /** How often the loads that are kept for longer than the retention are discarded. */
    private Duration purgeInterval = Duration.ofHours(1);

    /** The time from the start of the application to the first purge. */
    private Duration purgeInitialDelay = Duration.ofMinutes(5);

    /** How often the loads that were abandoned are taken back. */
    private Duration recoveryInterval = Duration.ofMinutes(10);

    /** The time from the start of the application to the first recovery. */
    private Duration recoveryInitialDelay = Duration.ofMinutes(2);

    public long getDelta() {
        return delta;
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }

    public Duration getLockWait() {
        return lockWait;
    }

    public void setLockWait(Duration lockWait) {
        this.lockWait = positive("lock-wait", lockWait);
    }

    public Duration getDrainWait() {
        return drainWait;
    }

    public void setDrainWait(Duration drainWait) {
        this.drainWait = positive("drain-wait", drainWait);
    }

    public Duration getWriteWait() {
        return writeWait;
    }

    public void setWriteWait(Duration writeWait) {
        this.writeWait = positive("write-wait", writeWait);
    }

    public Duration getStaleAfter() {
        return staleAfter;
    }

    public void setStaleAfter(Duration staleAfter) {
        this.staleAfter = positive("stale-after", staleAfter);
    }

    public Duration getRetention() {
        return retention;
    }

    public void setRetention(Duration retention) {
        this.retention = positive("retention", retention);
    }

    public long getMaxPackageSize() {
        return maxPackageSize;
    }

    public void setMaxPackageSize(long maxPackageSize) {
        if (maxPackageSize < 1) {
            throw new IllegalArgumentException("score.release-package-load.max-package-size must be at least 1: " + maxPackageSize);
        }
        this.maxPackageSize = maxPackageSize;
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory == null ? "" : uploadDirectory;
    }

    public Duration getUploadTtl() {
        return uploadTtl;
    }

    public void setUploadTtl(Duration uploadTtl) {
        this.uploadTtl = positive("upload-ttl", uploadTtl);
    }

    public Duration getUploadSweepInterval() {
        return uploadSweepInterval;
    }

    public void setUploadSweepInterval(Duration uploadSweepInterval) {
        this.uploadSweepInterval = positive("upload-sweep-interval", uploadSweepInterval);
    }

    public Duration getPurgeInterval() {
        return purgeInterval;
    }

    public void setPurgeInterval(Duration purgeInterval) {
        this.purgeInterval = positive("purge-interval", purgeInterval);
    }

    public Duration getPurgeInitialDelay() {
        return purgeInitialDelay;
    }

    public void setPurgeInitialDelay(Duration purgeInitialDelay) {
        this.purgeInitialDelay = notNegative("purge-initial-delay", purgeInitialDelay);
    }

    public Duration getRecoveryInterval() {
        return recoveryInterval;
    }

    public void setRecoveryInterval(Duration recoveryInterval) {
        this.recoveryInterval = positive("recovery-interval", recoveryInterval);
    }

    public Duration getRecoveryInitialDelay() {
        return recoveryInitialDelay;
    }

    public void setRecoveryInitialDelay(Duration recoveryInitialDelay) {
        this.recoveryInitialDelay = notNegative("recovery-initial-delay", recoveryInitialDelay);
    }

    private static Duration positive(String name, Duration value) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("score.release-package-load." + name + " must be longer than zero: " + value);
        }
        return value;
    }

    private static Duration notNegative(String name, Duration value) {
        if (value == null || value.isNegative()) {
            throw new IllegalArgumentException("score.release-package-load." + name + " must not be negative: " + value);
        }
        return value;
    }
}
