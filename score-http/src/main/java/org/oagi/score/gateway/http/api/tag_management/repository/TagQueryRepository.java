package org.oagi.score.gateway.http.api.tag_management.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tag_management.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Repository interface for querying tags and their associations.
 * <p>
 * This interface provides methods for checking tag existence, retrieving tag summaries and details,
 * and fetching tag associations with various manifest types.
 */
public interface TagQueryRepository {

    /**
     * Checks if a tag with the given {@code tagId} exists in the database.
     *
     * @param tagId The unique identifier of the tag to check.
     * @return {@code true} if the tag exists, {@code false} otherwise.
     */
    boolean exists(TagId tagId);

    /**
     * Retrieves a list of tag summaries.
     *
     * @return A list of {@link TagSummaryRecord} containing tag summaries.
     */
    List<TagSummaryRecord> getTagSummaryList();

    List<TagSummaryRecord> getTagSummaryList(AccManifestId accManifestId);

    List<TagSummaryRecord> getTagSummaryList(AsccpManifestId asccpManifestId);

    List<TagSummaryRecord> getTagSummaryList(BccpManifestId bccpManifestId);

    List<TagSummaryRecord> getTagSummaryList(DtManifestId dtManifestId);

    /**
     * Retrieves a tag summary for the given {@code tagId}.
     *
     * @param tagId The unique identifier of the tag.
     * @return The {@link TagSummaryRecord} for the specified tag, or {@code null} if not found.
     */
    TagSummaryRecord getTagSummary(TagId tagId);

    /**
     * Retrieves a tag summary for the given tag name.
     *
     * @param tagName The name of the tag.
     * @return The {@link TagSummaryRecord} for the specified tag, or {@code null} if not found.
     */
    TagSummaryRecord getTagSummaryByName(String tagName);

    /**
     * Retrieves a list of tag details.
     *
     * @return A list of {@link TagDetailsRecord} containing tag details.
     */
    List<TagDetailsRecord> getTagDetailsList();

    /**
     * Checks if a tag is associated with a given {@code accManifestId}.
     *
     * @param tagId         The unique identifier of the tag.
     * @param accManifestId The unique identifier of the ACC manifest.
     * @return {@code true} if the tag is associated with the ACC manifest, {@code false} otherwise.
     */
    boolean hasTag(TagId tagId, AccManifestId accManifestId);

    /**
     * Checks if a tag is associated with a given {@code asccpManifestId}.
     *
     * @param tagId           The unique identifier of the tag.
     * @param asccpManifestId The unique identifier of the ASCCP manifest.
     * @return {@code true} if the tag is associated with the ASCCP manifest, {@code false} otherwise.
     */
    boolean hasTag(TagId tagId, AsccpManifestId asccpManifestId);

    /**
     * Checks if a tag is associated with a given {@code bccpManifestId}.
     *
     * @param tagId          The unique identifier of the tag.
     * @param bccpManifestId The unique identifier of the BCCP manifest.
     * @return {@code true} if the tag is associated with the BCCP manifest, {@code false} otherwise.
     */
    boolean hasTag(TagId tagId, BccpManifestId bccpManifestId);

    /**
     * Checks if a tag is associated with a given {@code dtManifestId}.
     *
     * @param tagId        The unique identifier of the tag.
     * @param dtManifestId The unique identifier of the DT manifest.
     * @return {@code true} if the tag is associated with the DT manifest, {@code false} otherwise.
     */
    boolean hasTag(TagId tagId, DtManifestId dtManifestId);

    List<AccManifestTagSummaryRecord> getAccManifestTagList(Collection<ReleaseId> releaseIdList);

    List<AsccpManifestTagSummaryRecord> getAsccpManifestTagList(Collection<ReleaseId> releaseIdList);

    List<BccpManifestTagSummaryRecord> getBccpManifestTagList(Collection<ReleaseId> releaseIdList);

    List<DtManifestTagSummaryRecord> getDtManifestTagList(Collection<ReleaseId> releaseIdList);

    /**
     * Retrieves a map of ACC manifest IDs to associated tag summaries for a given release.
     *
     * @param releaseId The release ID.
     * @return A map of ACC manifest IDs to tag summaries.
     */
    Map<AccManifestId, List<TagSummaryRecord>> getTagAccManifestMap(ReleaseId releaseId);

    /**
     * Retrieves a map of ASCCP manifest IDs to associated tag summaries for a given release.
     *
     * @param releaseId The release ID.
     * @return A map of ASCCP manifest IDs to tag summaries.
     */
    Map<AsccpManifestId, List<TagSummaryRecord>> getTagAsccpManifestMap(ReleaseId releaseId);

    /**
     * Retrieves a map of BCCP manifest IDs to associated tag summaries for a given release.
     *
     * @param releaseId The release ID.
     * @return A map of BCCP manifest IDs to tag summaries.
     */
    Map<BccpManifestId, List<TagSummaryRecord>> getTagBccpManifestMap(ReleaseId releaseId);

    /**
     * Retrieves a map of DT manifest IDs to associated tag summaries for a given release.
     *
     * @param releaseId The release ID.
     * @return A map of DT manifest IDs to tag summaries.
     */
    Map<DtManifestId, List<TagSummaryRecord>> getTagDtManifestMap(ReleaseId releaseId);

    /**
     * Retrieves a map of tag summaries associated with different manifest types and their corresponding manifest IDs.
     * <p>
     * This method allows retrieving tag summaries based on multiple combinations of {@link CcType} and {@link ManifestId}.
     *
     * @param pairsOfTypeAndManifestId A list of pairs, where each pair contains a {@link CcType} representing the type
     *                                 of the manifest and a {@link ManifestId} representing the manifest identifier.
     * @return A map where each key is a pair of {@link CcType} and {@link ManifestId}, and the value is a list of
     * {@link TagSummaryRecord} associated with that manifest type and identifier.
     */
    Map<Pair<CcType, ManifestId>, List<TagSummaryRecord>> getTagSummariesByPairsOfTypeAndManifestId(
            List<Pair<CcType, ManifestId>> pairsOfTypeAndManifestId);

}
