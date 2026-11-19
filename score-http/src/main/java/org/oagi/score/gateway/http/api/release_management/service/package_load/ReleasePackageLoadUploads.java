package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.NotFoundException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;

/**
 * The release packages that are on the server and not loaded yet, which an administrator sent. A package is kept (in a file of the
 * server) until it is loaded, thrown away, or is older than it may be. It is told of a package by what is in its information file, so that a file that is not a package is
 * refused at once.
 *
 * <p>The packages are of the server that has them: a load is done by the server that was given the package.</p>
 */
@Component
public class ReleasePackageLoadUploads {

    public enum State {RECEIVING, READY}

    /**
     * A package on the server.
     *
     * @param bytes      how many bytes have come
     * @param totalBytes how many there are, or -1 when it is not known
     */
    public record Upload(String id, UserId owner, String fileName, State state, long bytes, long totalBytes, String message,
                         String releaseNum, String version, Instant created, Path file) {
        Upload with(State state, long bytes, String message, String releaseNum, String version) {
            return new Upload(id, owner, fileName, state, bytes, totalBytes, message, releaseNum, version, created, file);
        }
    }

    private static final Pattern FILE_NAME = Pattern.compile("[A-Za-z0-9_.-]+\\.zip");
    private static final long PROGRESS_STEP = 1 << 20;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReleasePackageLoadTransferSettings settings;
    private final ReleasePackageLoadCommandService commands;
    private final Map<String, Upload> uploads = new ConcurrentHashMap<>();
    private Path directory;

    public ReleasePackageLoadUploads(ReleasePackageLoadTransferSettings settings, ReleasePackageLoadCommandService commands) {
        this.settings = settings;
        this.commands = commands;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // What comes
    // ---------------------------------------------------------------------------------------------------------------

    /** Takes a package that is sent, and returns when all of it has come. */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public Upload receive(ScoreUser requester, String fileName, InputStream stream, long declaredLength) {
        requireName(fileName);
        if (declaredLength > settings.maxPackageSize()) {
            throw tooLarge();
        }
        Upload upload = begin(requester, fileName, declaredLength);
        try {
            copy(stream, upload);
            return ready(upload);
        } catch (RuntimeException | IOException e) {
            drop(upload);
            if (e instanceof ReleasePackageLoadRefusedException refused) {
                throw refused;
            }
            logger.warn("A package could not be received: " + e.getMessage(), e);
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_RECEIVED, e);
        }
    }

    private Upload begin(ScoreUser requester, String fileName, long totalBytes) {
        String id = UUID.randomUUID().toString();
        try {
            Upload upload = new Upload(id, requester.userId(), fileName, State.RECEIVING, 0, totalBytes, null, null, null, Instant.now(),
                    directory().resolve(id + ".zip"));
            uploads.put(id, upload);
            return upload;
        } catch (IOException e) {
            logger.error("There is no place to keep a package: " + e.getMessage(), e);
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NO_ROOM, e);
        }
    }

    private void copy(InputStream stream, Upload upload) throws IOException {
        long max = settings.maxPackageSize();
        long bytes = 0;
        long reported = 0;
        byte[] buffer = new byte[64 * 1024];
        try (OutputStream out = Files.newOutputStream(upload.file())) {
            for (int n; (n = stream.read(buffer)) >= 0; ) {
                if (bytes == 0 && !startsLikeZip(buffer, n)) {
                    // Not a zip archive: it is refused at its first bytes, and nothing of it is kept.
                    throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_A_PACKAGE);
                }
                bytes += n;
                if (bytes > max) {
                    throw tooLarge();
                }
                out.write(buffer, 0, n);
                if (bytes - reported >= PROGRESS_STEP) {
                    reported = bytes;
                    long now = bytes;
                    uploads.computeIfPresent(upload.id(), (k, u) -> u.with(State.RECEIVING, now, null, null, null));
                }
            }
        }
        long total = bytes;
        uploads.computeIfPresent(upload.id(), (k, u) -> u.with(State.RECEIVING, total, null, null, null));
    }

    private static boolean startsLikeZip(byte[] buffer, int n) {
        byte[] signature = {'P', 'K', 3, 4};
        for (int i = 0; i < Math.min(n, signature.length); i++) {
            if (buffer[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /** Looks into the package: a file that is not one is refused, and thrown away. */
    private Upload ready(Upload upload) {
        try {
            Info info = commands.read(upload.file());
            long size = Files.size(upload.file());
            return uploads.computeIfPresent(upload.id(), (k, u) -> u.with(State.READY, size, null,
                    info.getMigration().getRelease().getReleaseNum(), info.getVersion()));
        } catch (IOException | ReleasePackageLoadRefusedException e) {
            deleteQuietly(upload.file());
            uploads.remove(upload.id());
            if (e instanceof ReleasePackageLoadRefusedException refused) {
                throw refused;
            }
            logger.warn("A package could not be read: " + e.getMessage(), e);
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_READ, e);
        }
    }

    private void drop(Upload upload) {
        deleteQuietly(upload.file());
        uploads.remove(upload.id());
    }

    // ---------------------------------------------------------------------------------------------------------------
    // What is there
    // ---------------------------------------------------------------------------------------------------------------

    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public Upload get(ScoreUser requester, String id) {
        Upload upload = uploads.get(id);
        if (upload == null) {
            throw new NotFoundException(ReleasePackageLoadMessages.GONE);
        }
        return upload;
    }

    /** Throws a package away. */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public void discard(ScoreUser requester, String id) {
        Upload upload = get(requester, id);
        drop(upload);
    }

    /** Gives a package that is ready to a load, which is its own from now on: it is no more here. */
    @AccessControl(requiredAnyRole = {ADMINISTRATOR}, errorMessage = ReleasePackageLoadMessages.ONLY_ADMINISTRATOR)
    public Upload take(ScoreUser requester, String id) {
        Upload upload = get(requester, id);
        if (upload.state() != State.READY) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_READY);
        }
        if (!uploads.remove(id, upload)) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.BEING_LOADED);
        }
        return upload;
    }

    /** Gives a package that was taken back, when the load did not start. */
    void putBack(Upload upload) {
        uploads.put(upload.id(), upload);
    }

    /** A package that is kept for longer than it may be is thrown away, with the files that no package is of. */
    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT15M")
    void sweep() {
        Instant oldest = Instant.now().minus(settings.uploadTtl());
        uploads.values().stream().filter(u -> u.created().isBefore(oldest)).forEach(this::drop);
        try (var files = Files.list(directory())) {
            files.filter(file -> file.getFileName().toString().endsWith(".zip"))
                    .filter(file -> !uploads.containsKey(file.getFileName().toString().replace(".zip", "")))
                    .filter(file -> Files.isRegularFile(file) && lastModified(file).isBefore(oldest))
                    .forEach(ReleasePackageLoadUploads::deleteQuietly);
        } catch (IOException e) {
            logger.warn("The packages that are kept could not be looked through: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private synchronized Path directory() throws IOException {
        if (directory == null) {
            Path base = settings.uploadDirectory().isBlank()
                    ? Path.of(System.getProperty("java.io.tmpdir"), "score-release-package-loads") : Path.of(settings.uploadDirectory());
            directory = Files.createDirectories(base);
        }
        return directory;
    }

    private static Instant lastModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException e) {
            return Instant.now();
        }
    }

    private ReleasePackageLoadRefusedException tooLarge() {
        long max = settings.maxPackageSize();
        String size = max >= 1024 * 1024 ? max / (1024 * 1024) + " MB" : max / 1024 + " KB";
        return new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.tooLarge(size));
    }

    private static void requireName(String fileName) {
        if (fileName == null || !FILE_NAME.matcher(fileName).matches()) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.BAD_FILE_NAME);
        }
    }


    private static void closeQuietly(InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // Nothing is read from it.
        }
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // The sweep tries again.
        }
    }
}
