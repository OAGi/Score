package org.oagi.score.gateway.http.api.tag_management.repository;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.tag_management.model.TagId;

/**
 * Repository interface for managing Tag commands.
 * <p>
 * This interface defines methods for creating, updating, deleting, and managing tags across multiple related
 * manifest tables such as ACC, ASCCP, BCCP, and DT manifests. It provides operations for adding and removing tags
 * to/from these tables based on tag IDs and manifest IDs.
 */
public interface TagCommandRepository {

    /**
     * Creates a new tag with the specified attributes.
     * <p>
     * This method ensures that the tag name, text color, and background color are non-null and valid.
     * The text and background colors must be in RGB hex format (e.g., '#FFFFFF').
     *
     * @param name            The name of the tag. Must not be {@code null} or empty.
     * @param textColor       The text color of the tag, in RGB hex format (e.g., '#FFFFFF'). Must not be {@code null} or empty.
     * @param backgroundColor The background color of the tag, in RGB hex format (e.g., '#D1446B'). Must not be {@code null} or empty.
     * @param description     The description of the tag. Can be {@code null}.
     * @return The unique identifier of the newly created tag.
     * @throws IllegalArgumentException if {@code name}, {@code textColor}, or {@code backgroundColor} is {@code null}, empty,
     *                                  or if the colors are not in the valid RGB hex format.
     */
    TagId create(String name,
                 String textColor,
                 String backgroundColor,
                 @Nullable String description);

    /**
     * Updates the tag details for the given {@code tagId}.
     * <p>
     * This method allows updating the tag's name, text color, background color, and description.
     * If the {@code textColor} or {@code backgroundColor} are provided, they must be in RGB hex format (e.g., '#FFFFFF').
     * If the description is not provided, it will be set to {@code null}.
     *
     * @param tagId           The unique identifier of the tag to be updated. Must not be {@code null}.
     * @param name            The new name of the tag. Must not be {@code null} or empty.
     * @param textColor       The new text color of the tag, in RGB hex format (e.g., '#FFFFFF'). Can be {@code null} to leave unchanged.
     * @param backgroundColor The new background color of the tag, in RGB hex format (e.g., '#FFFFFF'). Can be {@code null} to leave unchanged.
     * @param description     The new description of the tag. Can be {@code null} to remove the existing value.
     * @return {@code true} if the tag was successfully updated, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} is {@code null}, {@code name} is {@code null} or empty, or
     *                                  if {@code textColor} or {@code backgroundColor} are not in a valid RGB hex format.
     */
    boolean update(TagId tagId,
                   String name,
                   String textColor, String backgroundColor,
                   String description);

    /**
     * Deletes the tag with the given {@code tagId} from various related tables.
     * <p>
     * This method removes the tag record from the main {@code TAG} table and also deletes any associated
     * records in the {@code ACC_MANIFEST_TAG}, {@code ASCCP_MANIFEST_TAG}, {@code BCCP_MANIFEST_TAG},
     * and {@code DT_MANIFEST_TAG} tables where the {@code tagId} is referenced.
     *
     * @param tagId The unique identifier of the tag to be deleted. Must not be {@code null}.
     * @return {@code true} if the tag record was deleted from the {@code TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} is {@code null}.
     */
    boolean delete(TagId tagId);

    /**
     * Adds a tag to the given {@code accManifestId}.
     * <p>
     * This method inserts a record into the {@code ACC_MANIFEST_TAG} table, associating the specified
     * {@code tagId} with the given {@code accManifestId}. The user ID of the requester is recorded as
     * the creator, and the current timestamp is saved as the creation time.
     *
     * @param tagId         The unique identifier of the tag to be added. Must not be {@code null}.
     * @param accManifestId The unique identifier of the ACC manifest to which the tag is being added. Must not be {@code null}.
     * @return {@code true} if exactly one record was inserted into the {@code ACC_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code accManifestId} is {@code null}.
     */
    boolean addTag(TagId tagId, AccManifestId accManifestId);

    /**
     * Removes a tag from the given {@code accManifestId}.
     * <p>
     * This method deletes the association between the specified {@code tagId} and {@code accManifestId}
     * in the {@code ACC_MANIFEST_TAG} table.
     *
     * @param tagId         The unique identifier of the tag to be removed. Must not be {@code null}.
     * @param accManifestId The unique identifier of the ACC manifest from which the tag is being removed. Must not be {@code null}.
     * @return {@code true} if exactly one record was deleted from the {@code ACC_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code accManifestId} is {@code null}.
     */
    boolean removeTag(TagId tagId, AccManifestId accManifestId);

    /**
     * Adds a tag to the given {@code asccpManifestId}.
     * <p>
     * This method inserts a record into the {@code ASCCP_MANIFEST_TAG} table, associating the specified
     * {@code tagId} with the given {@code asccpManifestId}. The user ID of the requester is recorded as
     * the creator, and the current timestamp is saved as the creation time.
     *
     * @param tagId           The unique identifier of the tag to be added. Must not be {@code null}.
     * @param asccpManifestId The unique identifier of the ASCCP manifest to which the tag is being added. Must not be {@code null}.
     * @return {@code true} if exactly one record was inserted into the {@code ASCCP_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code asccpManifestId} is {@code null}.
     */
    boolean addTag(TagId tagId, AsccpManifestId asccpManifestId);

    /**
     * Removes a tag from the given {@code asccpManifestId}.
     * <p>
     * This method deletes the association between the specified {@code tagId} and {@code asccpManifestId}
     * in the {@code ASCCP_MANIFEST_TAG} table.
     *
     * @param tagId           The unique identifier of the tag to be removed. Must not be {@code null}.
     * @param asccpManifestId The unique identifier of the ASCCP manifest from which the tag is being removed. Must not be {@code null}.
     * @return {@code true} if exactly one record was deleted from the {@code ASCCP_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code asccpManifestId} is {@code null}.
     */
    boolean removeTag(TagId tagId, AsccpManifestId asccpManifestId);

    /**
     * Adds a tag to the given {@code bccpManifestId}.
     * <p>
     * This method inserts a record into the {@code BCCP_MANIFEST_TAG} table, associating the specified
     * {@code tagId} with the given {@code bccpManifestId}. The user ID of the requester is recorded as
     * the creator, and the current timestamp is saved as the creation time.
     *
     * @param tagId          The unique identifier of the tag to be added. Must not be {@code null}.
     * @param bccpManifestId The unique identifier of the BCCP manifest to which the tag is being added. Must not be {@code null}.
     * @return {@code true} if exactly one record was inserted into the {@code BCCP_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code bccpManifestId} is {@code null}.
     */
    boolean addTag(TagId tagId, BccpManifestId bccpManifestId);

    /**
     * Removes a tag from the given {@code bccpManifestId}.
     * <p>
     * This method deletes the association between the specified {@code tagId} and {@code bccpManifestId}
     * in the {@code BCCP_MANIFEST_TAG} table.
     *
     * @param tagId          The unique identifier of the tag to be removed. Must not be {@code null}.
     * @param bccpManifestId The unique identifier of the BCCP manifest from which the tag is being removed. Must not be {@code null}.
     * @return {@code true} if exactly one record was deleted from the {@code BCCP_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code bccpManifestId} is {@code null}.
     */
    boolean removeTag(TagId tagId, BccpManifestId bccpManifestId);

    /**
     * Adds a tag to the given {@code dtManifestId}.
     * <p>
     * This method inserts a record into the {@code DT_MANIFEST_TAG} table, associating the specified
     * {@code tagId} with the given {@code dtManifestId}. The user ID of the requester is recorded as
     * the creator, and the current timestamp is saved as the creation time.
     *
     * @param tagId        The unique identifier of the tag to be added. Must not be {@code null}.
     * @param dtManifestId The unique identifier of the DT manifest to which the tag is being added. Must not be {@code null}.
     * @return {@code true} if exactly one record was inserted into the {@code DT_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code dtManifestId} is {@code null}.
     */
    boolean addTag(TagId tagId, DtManifestId dtManifestId);

    /**
     * Removes a tag from the given {@code dtManifestId}.
     * <p>
     * This method deletes the association between the specified {@code tagId} and {@code dtManifestId}
     * in the {@code DT_MANIFEST_TAG} table.
     *
     * @param tagId        The unique identifier of the tag to be removed. Must not be {@code null}.
     * @param dtManifestId The unique identifier of the DT manifest from which the tag is being removed. Must not be {@code null}.
     * @return {@code true} if exactly one record was deleted from the {@code DT_MANIFEST_TAG} table, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code tagId} or {@code dtManifestId} is {@code null}.
     */
    boolean removeTag(TagId tagId, DtManifestId dtManifestId);
}
