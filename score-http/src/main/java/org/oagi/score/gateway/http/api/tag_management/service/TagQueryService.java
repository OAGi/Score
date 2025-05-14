package org.oagi.score.gateway.http.api.tag_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tag_management.model.TagDetailsRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.repository.TagQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service class for querying tag-related data, including tag summaries and details,
 * along with mappings for various manifest types (ACC, ASCCP, BCCP, DT).
 */
@Service
@Transactional(readOnly = true)
public class TagQueryService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    private TagQueryRepository query(ScoreUser requester) {
        return repositoryFactory.tagQueryRepository(requester);
    }

    /**
     * Retrieves a list of tag summaries.
     *
     * @param requester the user requesting the tag summaries
     * @return a list of TagSummaryRecord
     */
    public List<TagSummaryRecord> getTagSummaryList(ScoreUser requester) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        return query(requester).getTagSummaryList();
    }

    /**
     * Retrieves a list of tag details.
     *
     * @param requester the user requesting the tag details
     * @return a list of TagDetailsRecord
     */
    public List<TagDetailsRecord> getTagDetailsList(ScoreUser requester) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        return query(requester).getTagDetailsList();
    }

    /**
     * Retrieves a map of tag summaries indexed by ACC manifest IDs.
     *
     * @param requester the user requesting the tag summaries
     * @param releaseId the release ID to filter the tags
     * @return a map of AccManifestId to a list of TagSummaryRecord
     */
    public Map<AccManifestId, List<TagSummaryRecord>> getTagAccManifestMap(
            ScoreUser requester, ReleaseId releaseId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null.");
        }

        return query(requester).getTagAccManifestMap(releaseId);
    }

    /**
     * Retrieves a map of tag summaries indexed by ASCCP manifest IDs.
     *
     * @param requester the user requesting the tag summaries
     * @param releaseId the release ID to filter the tags
     * @return a map of AsccpManifestId to a list of TagSummaryRecord
     */
    public Map<AsccpManifestId, List<TagSummaryRecord>> getTagAsccpManifestMap(
            ScoreUser requester, ReleaseId releaseId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null.");
        }

        return query(requester).getTagAsccpManifestMap(releaseId);
    }

    /**
     * Retrieves a map of tag summaries indexed by BCCP manifest IDs.
     *
     * @param requester the user requesting the tag summaries
     * @param releaseId the release ID to filter the tags
     * @return a map of BccpManifestId to a list of TagSummaryRecord
     */
    public Map<BccpManifestId, List<TagSummaryRecord>> getTagBccpManifestMap(
            ScoreUser requester, ReleaseId releaseId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null.");
        }

        return query(requester).getTagBccpManifestMap(releaseId);
    }

    /**
     * Retrieves a map of tag summaries indexed by DT manifest IDs.
     *
     * @param requester the user requesting the tag summaries
     * @param releaseId the release ID to filter the tags
     * @return a map of DtManifestId to a list of TagSummaryRecord
     */
    public Map<DtManifestId, List<TagSummaryRecord>> getTagDtManifestMap(
            ScoreUser requester, ReleaseId releaseId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        if (releaseId == null) {
            throw new IllegalArgumentException("`releaseId` must not be null.");
        }

        return query(requester).getTagDtManifestMap(releaseId);
    }

    /**
     * Retrieves a map of tag summaries indexed by pairs of CcType and ManifestId.
     *
     * @param requester                the user requesting the tag summaries
     * @param pairsOfTypeAndManifestId a list of pairs of CcType and ManifestId
     * @return a map of Pair<CcType, ManifestId> to a list of TagSummaryRecord
     */
    public Map<Pair<CcType, ManifestId>, List<TagSummaryRecord>> getTagSummariesByPairsOfTypeAndManifestId(
            ScoreUser requester, List<Pair<CcType, ManifestId>> pairsOfTypeAndManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        if (pairsOfTypeAndManifestId == null) {
            throw new IllegalArgumentException("`pairsOfTypeAndManifestId` must not be null.");
        }

        return query(requester).getTagSummariesByPairsOfTypeAndManifestId(pairsOfTypeAndManifestId);
    }

}
