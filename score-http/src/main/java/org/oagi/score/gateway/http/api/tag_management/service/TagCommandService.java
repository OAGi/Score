package org.oagi.score.gateway.http.api.tag_management.service;

import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.tag_management.controller.payload.CreateTagRequest;
import org.oagi.score.gateway.http.api.tag_management.controller.payload.UpdateTagRequest;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;
import org.oagi.score.gateway.http.api.tag_management.model.TagNotFoundException;
import org.oagi.score.gateway.http.api.tag_management.repository.TagCommandRepository;
import org.oagi.score.gateway.http.api.tag_management.repository.TagQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for executing tag-related commands.
 * <p>
 * This service provides methods to create, update, delete, and manage associations
 * between tags and various manifest types.
 */
@Service
@Transactional
public class TagCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private TagCommandRepository command(ScoreUser requester) {
        return repositoryFactory.tagCommandRepository(requester);
    }

    private TagQueryRepository query(ScoreUser requester) {
        return repositoryFactory.tagQueryRepository(requester);
    }

    /**
     * Creates a new tag.
     *
     * @param requester The user requesting the operation.
     * @param request   The tag creation request.
     * @return The ID of the created tag.
     * @throws IllegalArgumentException if the {@code requester} or {@code request} is null.
     */
    public TagId create(ScoreUser requester, CreateTagRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("`request` parameter must not be null.");
        }

        request.assertValid();

        return command(requester).create(
                request.name(),
                request.textColor(), request.backgroundColor(),
                request.description());
    }

    /**
     * Updates an existing tag.
     *
     * @param requester The user requesting the operation.
     * @param request The tag update request.
     * @return true if updated successfully, false otherwise.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester} or {@code request} is null.
     */
    public boolean update(ScoreUser requester, UpdateTagRequest request) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("`request` parameter must not be null.");
        }

        request.assertValid();

        var query = query(requester);
        if (!query.exists(request.tagId())) {
            throw new TagNotFoundException(request.tagId());
        }

        return command(requester).update(request.tagId(),
                request.name(),
                request.textColor(), request.backgroundColor(),
                request.description());
    }

    /**
     * Discards a tag.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag to delete.
     * @return true if deleted successfully, false otherwise.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester} or {@code tagId} is null.
     */
    public boolean discard(ScoreUser requester, TagId tagId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        return command(requester).delete(tagId);
    }

    /**
     * Associates a tag with an ACC manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param accManifestId The ACC manifest ID to associate the tag with.
     * @return true if the tag was successfully associated, false if already linked.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code accManifestId} is null.
     */
    public boolean append(ScoreUser requester, TagId tagId, AccManifestId accManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("`accManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (query.hasTag(tagId, accManifestId)) {
            return false;
        }

        return command(requester).addTag(tagId, accManifestId);
    }

    /**
     * Removes a tag from an ACC manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param accManifestId The ACC manifest ID to remove the tag from.
     * @return true if the tag was successfully removed, false otherwise.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code accManifestId} is null.
     */
    public boolean remove(ScoreUser requester, TagId tagId, AccManifestId accManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("`accManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (!query.hasTag(tagId, accManifestId)) {
            return false;
        }

        return command(requester).removeTag(tagId, accManifestId);
    }

    /**
     * Associates a tag with an ASCCP manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param asccpManifestId The ASCCP manifest ID to associate the tag with.
     * @return true if the tag was successfully associated, false if already linked.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code asccpManifestId} is null.
     */
    public boolean append(ScoreUser requester, TagId tagId, AsccpManifestId asccpManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("`asccpManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (query.hasTag(tagId, asccpManifestId)) {
            return false;
        }

        return command(requester).addTag(tagId, asccpManifestId);
    }

    /**
     * Removes a tag from an ASCCP manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param asccpManifestId The ASCCP manifest ID to remove the tag from.
     * @return true if the tag was successfully removed, false otherwise.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code asccpManifestId} is null.
     */
    public boolean remove(ScoreUser requester, TagId tagId, AsccpManifestId asccpManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("`asccpManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (!query.hasTag(tagId, asccpManifestId)) {
            return false;
        }

        return command(requester).removeTag(tagId, asccpManifestId);
    }

    /**
     * Associates a tag with a BCCP manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param bccpManifestId The BCCP manifest ID to associate the tag with.
     * @return true if the tag was successfully associated, false if already linked.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code bccpManifestId} is null.
     */
    public boolean append(ScoreUser requester, TagId tagId, BccpManifestId bccpManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("`bccpManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (query.hasTag(tagId, bccpManifestId)) {
            return false;
        }

        return command(requester).addTag(tagId, bccpManifestId);
    }

    /**
     * Removes a tag from a BCCP manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param bccpManifestId The BCCP manifest ID to remove the tag from.
     * @return true if the tag was successfully removed, false otherwise.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code bccpManifestId} is null.
     */
    public boolean remove(ScoreUser requester, TagId tagId, BccpManifestId bccpManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("`bccpManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (!query.hasTag(tagId, bccpManifestId)) {
            return false;
        }

        return command(requester).removeTag(tagId, bccpManifestId);
    }

    /**
     * Associates a tag with a DT manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param dtManifestId The DT manifest ID to associate the tag with.
     * @return true if the tag was successfully associated, false if already linked.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code dtManifestId} is null.
     */
    public boolean append(ScoreUser requester, TagId tagId, DtManifestId dtManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (query.hasTag(tagId, dtManifestId)) {
            return false;
        }

        return command(requester).addTag(tagId, dtManifestId);
    }

    /**
     * Removes a tag from a DT manifest.
     *
     * @param requester The user requesting the operation.
     * @param tagId The ID of the tag.
     * @param dtManifestId The DT manifest ID to remove the tag from.
     * @return true if the tag was successfully removed, false otherwise.
     * @throws TagNotFoundException if the tag does not exist.
     * @throws IllegalArgumentException if the {@code requester}, {@code tagId}, or {@code dtManifestId} is null.
     */
    public boolean remove(ScoreUser requester, TagId tagId, DtManifestId dtManifestId) throws TagNotFoundException {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` parameter must not be null.");
        }

        if (tagId == null) {
            throw new IllegalArgumentException("`tagId` parameter must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("`dtManifestId` parameter must not be null.");
        }

        var query = query(requester);
        if (!query.exists(tagId)) {
            throw new TagNotFoundException(tagId);
        }

        if (!query.hasTag(tagId, dtManifestId)) {
            return false;
        }

        return command(requester).removeTag(tagId, dtManifestId);
    }

}
