package org.oagi.score.gateway.http.api.bie_management.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.BieViewOrderUpdateRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieViewOrderEntry;
import org.oagi.score.gateway.http.api.bie_management.service.BieViewOrderService;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for the sibling view order (Issue #1638). Keyed by the view-parent ACC: the frontend
 * GETs one parent's order lazily as each ACC node is expanded (group/choice flattening — hence the
 * view parent — is resolved on the client), and PUTs the affected child weights under that parent.
 */
@RestController
@Tag(name = "BIE View Order", description = "Instance-level sibling sort order for model browsing and BIE editing")
@RequestMapping("/bie-view-order")
public class BieViewOrderController {

    private final BieViewOrderService service;
    private final SessionService sessionService;

    public BieViewOrderController(BieViewOrderService service, SessionService sessionService) {
        this.service = service;
        this.sessionService = sessionService;
    }

    /**
     * The sibling view-order weights stored under the view-parent {@code accManifestId}. Read by any
     * viewer; an empty list means that parent keeps its current seq_key order.
     */
    @GetMapping("/acc/{accManifestId:[\\d]+}")
    public List<BieViewOrderEntry> getViewOrder(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("accManifestId") AccManifestId accManifestId) {
        return service.getViewOrder(sessionService.asScoreUser(user), accManifestId);
    }

    /**
     * Upsert the affected child weights under the view parent {@code accManifestId}. Developer-only.
     */
    @PutMapping("/acc/{accManifestId:[\\d]+}")
    public ResponseEntity<Void> updateViewOrder(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("accManifestId") AccManifestId accManifestId,
                                                @RequestBody BieViewOrderUpdateRequest request) {
        service.updateViewOrder(sessionService.asScoreUser(user), accManifestId, request.toEntries(accManifestId));
        return ResponseEntity.ok().build();
    }

    /**
     * Remove every child weight stored directly under the view parent {@code accManifestId} — the
     * model browser's root-node "Reset Order Weights" (Issue #1638). Resets only this ACC's own
     * children (rows whose {@code from_acc_manifest_id} is this ACC); children reordered under deeper
     * nested ACCs are untouched. Developer-only; idempotent (a no-op when nothing is stored).
     */
    @DeleteMapping("/acc/{accManifestId:[\\d]+}")
    public ResponseEntity<Void> resetViewOrder(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @PathVariable("accManifestId") AccManifestId accManifestId) {
        service.resetViewOrder(sessionService.asScoreUser(user), accManifestId);
        return ResponseEntity.ok().build();
    }
}
