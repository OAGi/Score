package org.oagi.score.gateway.http.api.tag_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.api.tag_management.data.Tag;
import org.oagi.score.gateway.http.api.tag_management.data.ShortTag;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.component.tag.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SessionService sessionService;

    public List<Tag> getTags() {
        return tagRepository.getTags();
    }

    public Tag getTagByName(String name) {
        return tagRepository.getTagByName(name);
    }

    public List<Tag> getTagsByTypeAndManifestId(CcType type, BigInteger manifestId) {
        if (type == null) {
            throw new IllegalArgumentException("'type' argument must be not null.");
        }

        switch (type) {
            case ACC:
                return tagRepository.getTagsByAccManifestId(manifestId);
            case ASCCP:
                return tagRepository.getTagsByAsccpManifestId(manifestId);
            case BCCP:
                return tagRepository.getTagsByBccpManifestId(manifestId);
            case DT:
                return tagRepository.getTagsByDtManifestId(manifestId);
            default:
                return Collections.emptyList();
        }
    }

    public List<ShortTag> getShortTagsByTypeAndManifestId(CcType type, BigInteger manifestId) {
        if (type == null) {
            throw new IllegalArgumentException("'type' argument must be not null.");
        }

        switch (type) {
            case ACC:
                return tagRepository.getShortTagsByAccManifestId(manifestId);
            case ASCCP:
                return tagRepository.getShortTagsByAsccpManifestId(manifestId);
            case BCCP:
                return tagRepository.getShortTagsByBccpManifestId(manifestId);
            case DT:
                return tagRepository.getShortTagsByDtManifestId(manifestId);
            default:
                return Collections.emptyList();
        }
    }

    public Map<Pair<CcType, BigInteger>, List<ShortTag>> getShortTagsByPairsOfTypeAndManifestId(
            List<Pair<CcType, BigInteger>> pairsOfTypeAndManifestId) {
        return tagRepository.getShortTagsByPairsOfTypeAndManifestId(pairsOfTypeAndManifestId);
    }

    public void toggleTag(AuthenticatedPrincipal user,
                            String type, BigInteger manifestId, String name) {

        ScoreUser requester = sessionService.asScoreUser(user);
        Tag tag = tagRepository.getTagByName(name);
        if (tag == null) {
            throw new IllegalArgumentException("Unknown tag name: " + name);
        }

        switch (CcType.valueOf(type)) {
            case ACC:
                tagRepository.toggleTagByAccManifestId(requester, manifestId, tag);
                break;
            case ASCCP:
                tagRepository.toggleTagByAsccpManifestId(requester, manifestId, tag);
                break;
            case BCCP:
                tagRepository.toggleTagByBccpManifestId(requester, manifestId, tag);
                break;
            case DT:
                tagRepository.toggleTagByDtManifestId(requester, manifestId, tag);
                break;
            default:
                throw new IllegalArgumentException("Unsupported 'type' argument: " + type);
        }
    }

    public void add(AuthenticatedPrincipal user, Tag tag) {
        ScoreUser requester = sessionService.asScoreUser(user);
        tagRepository.add(requester, tag);
    }

    public void update(AuthenticatedPrincipal user, Tag tag) {
        ScoreUser requester = sessionService.asScoreUser(user);
        tagRepository.update(requester, tag);
    }

    public void discard(AuthenticatedPrincipal user, BigInteger tagId) {

        ScoreUser requester = sessionService.asScoreUser(user);
        tagRepository.discard(requester, tagId);
    }

}
