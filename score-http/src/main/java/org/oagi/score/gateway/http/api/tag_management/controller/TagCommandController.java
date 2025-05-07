package org.oagi.score.gateway.http.api.tag_management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.tag_management.controller.payload.CreateTagRequest;
import org.oagi.score.gateway.http.api.tag_management.controller.payload.UpdateTagRequest;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;
import org.oagi.score.gateway.http.api.tag_management.model.TagNotFoundException;
import org.oagi.score.gateway.http.api.tag_management.service.TagCommandService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling tag-related commands such as creation, updating, and deletion.
 */
@RestController
@Tag(name = "Tag - Commands", description = "API for creating, updating, and deleting tags")
@RequestMapping("/tags")
public class TagCommandController {

    @Autowired
    private TagCommandService tagCommandService;

    @Autowired
    private SessionService sessionService;

    /**
     * Creates a new tag.
     *
     * @param user    The authenticated user.
     * @param request The request payload containing tag details.
     * @return A response containing the newly created tag ID.
     */
    @Operation(summary = "Create a Tag", description = "Creates a new tag with the provided details.")
    @PostMapping()
    public ResponseEntity<TagId> createTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The request payload containing tag details.")
            @RequestBody CreateTagRequest request) {

        TagId tagId = tagCommandService.create(sessionService.asScoreUser(user), request);
        return ResponseEntity.ok(tagId);
    }

    /**
     * Updates an existing tag.
     *
     * @param user    The authenticated user.
     * @param tagId   The ID of the tag to update.
     * @param request The request payload containing updated tag details.
     */
    @Operation(summary = "Update a Tag", description = "Updates an existing tag with new details.")
    @PutMapping(value = "/{tagId:[\\d]+}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag to update.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The request payload containing updated tag details.")
            @RequestBody UpdateTagRequest request) {

        try {
            boolean updated = tagCommandService.update(sessionService.asScoreUser(user), request.withTagId(tagId));
            return (updated) ? ResponseEntity.noContent().build() : ResponseEntity.accepted().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes an existing tag.
     *
     * @param user  The authenticated user.
     * @param tagId The ID of the tag to delete.
     */
    @Operation(summary = "Delete a Tag", description = "Deletes an existing tag.")
    @DeleteMapping(value = "/{tagId:[\\d]+}")
    public ResponseEntity<Void> discard(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag to delete.")
            @PathVariable("tagId") TagId tagId) {

        try {
            boolean deleted = tagCommandService.discard(sessionService.asScoreUser(user), tagId);
            return (deleted) ? ResponseEntity.noContent().build() : ResponseEntity.accepted().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Appends a tag to an ACC.
     *
     * @param user          The authenticated user.
     * @param tagId         The ID of the tag to append.
     * @param accManifestId The ACC manifest ID to which the tag is appended.
     */
    @Operation(summary = "Append Tag to ACC", description = "Associates a tag with an ACC.")
    @PostMapping(value = "/{tagId:[\\d]+}/acc/{accManifestId:[\\d]+}")
    public ResponseEntity<Void> appendTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The ACC manifest ID.")
            @PathVariable("accManifestId") AccManifestId accManifestId) {

        try {
            tagCommandService.append(sessionService.asScoreUser(user), tagId, accManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Removes a tag from an ACC.
     *
     * @param user          The authenticated user.
     * @param tagId         The ID of the tag to remove.
     * @param accManifestId The ACC manifest ID from which the tag is removed.
     */
    @Operation(summary = "Remove Tag from ACC", description = "Removes an associated tag from an ACC.")
    @DeleteMapping(value = "/{tagId:[\\d]+}/acc/{accManifestId:[\\d]+}")
    public ResponseEntity<Void> removeTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The ACC manifest ID.")
            @PathVariable("accManifestId") AccManifestId accManifestId) {

        try {
            tagCommandService.remove(sessionService.asScoreUser(user), tagId, accManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Appends a tag to an ASCCP.
     *
     * @param user            The authenticated user.
     * @param tagId           The ID of the tag to append.
     * @param asccpManifestId The ASCCP manifest ID to which the tag is appended.
     */
    @Operation(summary = "Append Tag to ASCCP", description = "Associates a tag with an ASCCP.")
    @PostMapping(value = "/{tagId:[\\d]+}/asccp/{asccpManifestId:[\\d]+}")
    public ResponseEntity<Void> appendTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The ASCCP manifest ID.")
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId) {

        try {
            tagCommandService.append(sessionService.asScoreUser(user), tagId, asccpManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Removes a tag from an ASCCP.
     *
     * @param user            The authenticated user.
     * @param tagId           The ID of the tag to remove.
     * @param asccpManifestId The ASCCP manifest ID from which the tag is removed.
     */
    @Operation(summary = "Remove Tag from ASCCP", description = "Removes an associated tag from an ASCCP.")
    @DeleteMapping(value = "/{tagId:[\\d]+}/asccp/{asccpManifestId:[\\d]+}")
    public ResponseEntity<Void> removeTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The ASCCP manifest ID.")
            @PathVariable("asccpManifestId") AsccpManifestId asccpManifestId) {

        try {
            tagCommandService.remove(sessionService.asScoreUser(user), tagId, asccpManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Appends a tag to a BCCP.
     *
     * @param user           The authenticated user.
     * @param tagId          The ID of the tag to append.
     * @param bccpManifestId The BCCP manifest ID to which the tag is appended.
     */
    @Operation(summary = "Append Tag to BCCP", description = "Associates a tag with an BCCP.")
    @PostMapping(value = "/{tagId:[\\d]+}/bccp/{bccpManifestId:[\\d]+}")
    public ResponseEntity<Void> appendTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The BCCP manifest ID.")
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId) {

        try {
            tagCommandService.append(sessionService.asScoreUser(user), tagId, bccpManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Removes a tag from a BCCP.
     *
     * @param user           The authenticated user.
     * @param tagId          The ID of the tag to remove.
     * @param bccpManifestId The BCCP manifest ID from which the tag is removed.
     */
    @Operation(summary = "Remove Tag from BCCP", description = "Removes an associated tag from an BCCP.")
    @DeleteMapping(value = "/{tagId:[\\d]+}/bccp/{bccpManifestId:[\\d]+}")
    public ResponseEntity<Void> removeTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The BCCP manifest ID.")
            @PathVariable("bccpManifestId") BccpManifestId bccpManifestId) {

        try {
            tagCommandService.remove(sessionService.asScoreUser(user), tagId, bccpManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Appends a tag to a DT.
     *
     * @param user         The authenticated user.
     * @param tagId        The ID of the tag to append.
     * @param dtManifestId The DT manifest ID to which the tag is appended.
     */
    @Operation(summary = "Append Tag to DT", description = "Associates a tag with an DT.")
    @PostMapping(value = "/{tagId:[\\d]+}/dt/{dtManifestId:[\\d]+}")
    public ResponseEntity<Void> appendTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The DT manifest ID.")
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        try {
            tagCommandService.append(sessionService.asScoreUser(user), tagId, dtManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Removes a tag from a DT.
     *
     * @param user         The authenticated user.
     * @param tagId        The ID of the tag to remove.
     * @param dtManifestId The DT manifest ID from which the tag is removed.
     */
    @Operation(summary = "Remove Tag from DT", description = "Removes an associated tag from an DT.")
    @DeleteMapping(value = "/{tagId:[\\d]+}/dt/{dtManifestId:[\\d]+}")
    public ResponseEntity<Void> removeTag(
            @AuthenticationPrincipal AuthenticatedPrincipal user,

            @Parameter(description = "The ID of the tag.")
            @PathVariable("tagId") TagId tagId,

            @Parameter(description = "The DT manifest ID.")
            @PathVariable("dtManifestId") DtManifestId dtManifestId) {

        try {
            tagCommandService.remove(sessionService.asScoreUser(user), tagId, dtManifestId);
            return ResponseEntity.noContent().build();
        } catch (TagNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
