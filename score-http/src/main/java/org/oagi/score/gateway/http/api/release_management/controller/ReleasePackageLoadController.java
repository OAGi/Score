package org.oagi.score.gateway.http.api.release_management.controller;

import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJob;
import org.oagi.score.gateway.http.api.release_management.model.ReleasePackageLoadJobId;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadCommandService;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadPreflightReport;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadRefusedException;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadTransferSettings;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadUploads;
import org.oagi.score.gateway.http.api.release_management.service.package_load.ReleasePackageLoadUploads.Upload;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.AccessControlException;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import static org.oagi.score.gateway.http.common.model.ScoreRole.ADMINISTRATOR;

/**
 * Loading a release package, for administrators: the package is sent to the server,
 * it is looked at without anything being changed, and then it is loaded in the background, which is followed by asking how it goes.
 */
@RestController
@Tag(name = "Release - Package loads", description = "API for loading a release package into the database, and undoing it")
@RequestMapping("/release-package-loads")
public class ReleasePackageLoadController {

    @Autowired
    private ReleasePackageLoadCommandService service;

    @Autowired
    private ReleasePackageLoadUploads uploads;

    @Autowired
    private ReleasePackageLoadTransferSettings transferSettings;

    public record UploadResponse(String uploadId, String fileName, String state, long bytes, long totalBytes, String message,
                                 String releaseNum, String version) {
        static UploadResponse of(Upload u) {
            return new UploadResponse(u.id(), u.fileName(), u.state().name(), u.bytes(), u.totalBytes(), u.message(), u.releaseNum(), u.version());
        }
    }

    public record PreflightResponse(String releaseNum, String version, String format, boolean ok, List<String> problems) {
        static PreflightResponse of(ReleasePackageLoadPreflightReport r) {
            return new PreflightResponse(r.releaseNum(), r.version(), r.format(), r.ok(), r.problems());
        }
    }

    public record JobResponse(BigInteger id, String state, boolean active, String message, String packageFileName, String releaseNum,
                              String backupSchema, BigInteger ownerUserId, LocalDateTime creationTimestamp, LocalDateTime lastUpdateTimestamp) {
        static JobResponse of(ReleasePackageLoadJob j) {
            return new JobResponse(j.id().value(), j.state().name(), j.state().isActive(), j.message(), j.packageFileName(), j.releaseNum(),
                    j.backupSchema(), j.ownerUserId().value(), j.creationTimestamp(), j.lastUpdateTimestamp());
        }
    }

    public record StartRequest(String uploadId) {
    }

    public record SettingsResponse(long maxPackageSize, boolean available, String problem) {
    }

    /** What is asked by one who is not an administrator is refused before the method is called, whichever route it is. */
    @ExceptionHandler(AccessControlException.class)
    public ResponseEntity<String> forbidden(AccessControlException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Error-Message", "Only an administrator loads release packages.");
        return new ResponseEntity<>("Only an administrator loads release packages.", headers, HttpStatus.FORBIDDEN);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The package comes
    // ---------------------------------------------------------------------------------------------------------------

    @Operation(summary = "Send a release package (the bytes of the .zip file are the body of the request)")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @PutMapping(value = "/uploads")
    public UploadResponse upload(ScoreUser requester, @RequestParam("fileName") String fileName,
                                 HttpServletRequest request) throws IOException {
        service.requireAvailable(requester);
        return UploadResponse.of(uploads.receive(requester, fileName, request.getInputStream(), request.getContentLengthLong()));
    }

    @Operation(summary = "Throw a package away")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @DeleteMapping(value = "/uploads/{uploadId}")
    public void discardUpload(ScoreUser requester, @PathVariable("uploadId") String uploadId) {
        uploads.discard(requester, uploadId);
    }

    @Operation(summary = "Look at what is in the way of loading a package; nothing is changed")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @GetMapping(value = "/uploads/{uploadId}/preflight")
    public PreflightResponse preflight(ScoreUser requester, @PathVariable("uploadId") String uploadId) {
        Upload upload = uploads.get(requester, uploadId);
        if (upload.state() != ReleasePackageLoadUploads.State.READY) {
            throw new ReleasePackageLoadRefusedException(ReleasePackageLoadMessages.NOT_READY);
        }
        return PreflightResponse.of(service.preflight(requester, upload.file()));
    }

    @Operation(summary = "How large a package may be")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @GetMapping(value = "/settings")
    public SettingsResponse settings(ScoreUser requester) {
        List<String> problems = service.unavailable(requester);
        return new SettingsResponse(transferSettings.maxPackageSize(), problems.isEmpty(),
                problems.isEmpty() ? null : String.join("; ", problems));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // The load
    // ---------------------------------------------------------------------------------------------------------------

    @Operation(summary = "Load a package that has come, in the background")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @PostMapping
    public JobResponse start(ScoreUser requester, @RequestBody StartRequest request) {
        Upload upload = uploads.take(requester, request.uploadId());
        // When the load is refused the service has thrown the file away.
        return JobResponse.of(service.start(requester, upload.file(), upload.fileName()));
    }

    @Operation(summary = "The loads that were started last")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @GetMapping
    public List<JobResponse> recent(ScoreUser requester,
                                    @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return service.recent(requester, limit).stream().map(JobResponse::of).toList();
    }

    @Operation(summary = "How a load goes")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @GetMapping(value = "/{id:[\\d]+}")
    public JobResponse get(ScoreUser requester, @PathVariable("id") BigInteger id) {
        return JobResponse.of(service.get(requester, new ReleasePackageLoadJobId(id)));
    }

    @Operation(summary = "Undo the swap of the latest load that succeeded, in the background")
    @AccessControl(requiredAnyRole = {ADMINISTRATOR})
    @PostMapping(value = "/{id:[\\d]+}/rollback")
    public JobResponse rollback(ScoreUser requester, @PathVariable("id") BigInteger id) {
        return JobResponse.of(service.rollback(requester, new ReleasePackageLoadJobId(id)));
    }
}
