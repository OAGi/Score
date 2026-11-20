package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What the sweep of the packages that are kept does with the file of a package that is taken for a load. */
class ReleasePackageLoadUploadsSweepTest {

    private static final ScoreUser ADMIN = new ScoreUser(new UserId(BigInteger.ONE), "admin", "Admin", "admin@example.com", true,
            List.of(ScoreRole.ADMINISTRATOR));

    @TempDir
    Path directory;

    @Test
    void theFileOfAPackageThatWasTakenNearTheEndOfItsTimeIsNotThrownAwayWhileItIsLoaded() throws Exception {
        ReleasePackageLoadProperties settings = new ReleasePackageLoadProperties();
        settings.setUploadDirectory(directory.toString());
        ReleasePackageLoadUploads uploads = new ReleasePackageLoadUploads(settings, null);
        String id = "11111111-1111-1111-1111-111111111111";
        Path file = Files.writeString(directory.resolve(id + ".zip"), "PK");
        Instant old = Instant.now().minus(settings.getUploadTtl()).plus(10, ChronoUnit.MINUTES);
        Files.setLastModifiedTime(file, FileTime.from(old));
        uploads.putBack(new ReleasePackageLoadUploads.Upload(id, ADMIN.userId(), "10_0.zip", ReleasePackageLoadUploads.State.READY,
                2, 2, null, "10.0", "1.2", old, file));

        uploads.take(ADMIN, id);
        // The load goes on, so that the sweep comes when the time of the file would have been past the time a package is kept.
        settings.setUploadTtl(Duration.ofMinutes(5));
        uploads.sweep();

        assertTrue(Files.exists(file), "a package that a load reads is not thrown away");
    }

    @Test
    void theFileThatNoPackageIsOfAndIsOlderThanItMayBeIsThrownAway() throws Exception {
        ReleasePackageLoadProperties settings = new ReleasePackageLoadProperties();
        settings.setUploadDirectory(directory.toString());
        ReleasePackageLoadUploads uploads = new ReleasePackageLoadUploads(settings, null);
        Path file = Files.writeString(directory.resolve("22222222-2222-2222-2222-222222222222.zip"), "PK");
        Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(settings.getUploadTtl()).minus(1, ChronoUnit.HOURS)));

        uploads.sweep();

        assertFalse(Files.exists(file));
    }
}
