package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link CcDocument} that delegates to a {@link CachedCcDocument} and, when that one cannot answer or is not the
 * right one for the question, to a document that has the releases loaded ({@link CcDocumentImpl}).
 * <p>
 * It switches to the loaded document in two cases, and stays with it from then on, so what one document answers
 * always comes from one place afterwards:
 * <ul>
 *   <li>a lookup of the cached document fails (the database, the cache provider, anything unexpected): the same
 *   lookup is answered by the loaded document, which throws what it throws if it cannot answer either;</li>
 *   <li>a lookup that scans the release or looks for the users of a component is called: those are what the loaded
 *   document is made for, and it answers everything after them from memory instead of one query at a time.</li>
 * </ul>
 * The prefetch hints only speed the cached document up, so a hint that fails is ignored.
 * It may be shared by the threads of one request.
 */
public class DelegatingCcDocument implements CcDocument {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingCcDocument.class);

    private final CcDocument cached;
    private final LoadOnce<CcDocument> loaded;

    private volatile CcDocument current;

    /**
     * @param cached the document that answers until it cannot
     * @param loader loads the releases, the way they are always loaded, the first time the loaded document is needed
     */
    public DelegatingCcDocument(CcDocument cached, Supplier<CcDocument> loader) {
        this.cached = cached;
        this.loaded = new LoadOnce<>(loader);
        this.current = cached;
    }

    private CcDocument switchToLoaded() {
        CcDocument document = loaded.get();
        current = document;
        return document;
    }

    /** A lookup the cached document is made for. */
    private <T> T lookup(Function<CcDocument, T> call) {
        CcDocument document = current;
        if (document != cached) {
            return call.apply(document);
        }
        try {
            return call.apply(cached);
        } catch (RuntimeException e) {
            logger.warn("The cached CC document failed; the loaded releases answer instead.", e);
            return call.apply(switchToLoaded());
        }
    }

    /** A lookup that is what the loaded document is for. */
    private <T> T scan(Function<CcDocument, T> call) {
        return call.apply(current != cached ? current : switchToLoaded());
    }

    // ---- lookups by manifest id and under a parent ----------------------------------------------------------------

    @Override
    public AccSummaryRecord getAcc(AccManifestId accManifestId) {
        return lookup(d -> d.getAcc(accManifestId));
    }

    @Override
    public List<CcAssociation> getAssociations(AccSummaryRecord accManifest) {
        return lookup(d -> d.getAssociations(accManifest));
    }

    @Override
    public AsccSummaryRecord getAscc(AsccManifestId asccManifestId) {
        return lookup(d -> d.getAscc(asccManifestId));
    }

    @Override
    public BccSummaryRecord getBcc(BccManifestId bccManifestId) {
        return lookup(d -> d.getBcc(bccManifestId));
    }

    @Override
    public AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId) {
        return lookup(d -> d.getAsccp(asccpManifestId));
    }

    @Override
    public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
        return lookup(d -> d.getBccp(bccpManifestId));
    }

    @Override
    public DtSummaryRecord getDt(DtManifestId dtManifestId) {
        return lookup(d -> d.getDt(dtManifestId));
    }

    @Override
    public DtScSummaryRecord getDtSc(DtScManifestId dtScManifestId) {
        return lookup(d -> d.getDtSc(dtScManifestId));
    }

    @Override
    public List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId) {
        return lookup(d -> d.getDtScList(dtManifestId));
    }

    @Override
    public List<DtScSummaryRecord> getDtScListByDtManifestId(DtManifestId dtManifestId) {
        return lookup(d -> d.getDtScListByDtManifestId(dtManifestId));
    }

    @Override
    public List<AsccSummaryRecord> getAsccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return lookup(d -> d.getAsccListByFromAccManifestId(fromAccManifestId));
    }

    @Override
    public List<BccSummaryRecord> getBccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return lookup(d -> d.getBccListByFromAccManifestId(fromAccManifestId));
    }

    @Override
    public List<SeqKeySupportable> getAssociationListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return lookup(d -> d.getAssociationListByFromAccManifestId(fromAccManifestId));
    }

    @Override
    public List<TagSummaryRecord> getTagListByAccManifestId(AccManifestId accManifestId) {
        return lookup(d -> d.getTagListByAccManifestId(accManifestId));
    }

    @Override
    public List<TagSummaryRecord> getTagListByAsccpManifestId(AsccpManifestId asccpManifestId) {
        return lookup(d -> d.getTagListByAsccpManifestId(asccpManifestId));
    }

    @Override
    public List<TagSummaryRecord> getTagListByBccpManifestId(BccpManifestId bccpManifestId) {
        return lookup(d -> d.getTagListByBccpManifestId(bccpManifestId));
    }

    @Override
    public List<TagSummaryRecord> getTagListByDtManifestId(DtManifestId dtManifestId) {
        return lookup(d -> d.getTagListByDtManifestId(dtManifestId));
    }

    // ---- scans of the release, and the lookups of the users of a component -----------------------------------------

    @Override
    public Collection<AsccpSummaryRecord> getAsccpList() {
        return scan(CcDocument::getAsccpList);
    }

    @Override
    public List<DtSummaryRecord> getDtList() {
        return scan(CcDocument::getDtList);
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriList(DtManifestId dtManifestId) {
        return scan(d -> d.getDtAwdPriList(dtManifestId));
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriList(DtScManifestId dtScManifestId) {
        return scan(d -> d.getDtScAwdPriList(dtScManifestId));
    }

    @Override
    public XbtSummaryRecord getXbt(XbtManifestId xbtManifestId) {
        return scan(d -> d.getXbt(xbtManifestId));
    }

    @Override
    public List<XbtSummaryRecord> getXbtListByName(String name) {
        return scan(d -> d.getXbtListByName(name));
    }

    @Override
    public List<AccSummaryRecord> getAccListByBasedAccManifestId(AccManifestId basedAccManifestId) {
        return scan(d -> d.getAccListByBasedAccManifestId(basedAccManifestId));
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpListByRoleOfAccManifestId(AccManifestId roleOfAccManifestId) {
        return scan(d -> d.getAsccpListByRoleOfAccManifestId(roleOfAccManifestId));
    }

    @Override
    public List<AccSummaryRecord> getAccListByToAsccpManifestId(AsccpManifestId toAsccpManifestId) {
        return scan(d -> d.getAccListByToAsccpManifestId(toAsccpManifestId));
    }

    @Override
    public List<AccSummaryRecord> getAccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return scan(d -> d.getAccListByToBccpManifestId(toBccpManifestId));
    }

    @Override
    public List<BccpSummaryRecord> getBccpListByDtManifestId(DtManifestId dtManifestId) {
        return scan(d -> d.getBccpListByDtManifestId(dtManifestId));
    }

    @Override
    public List<BccSummaryRecord> getBccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return scan(d -> d.getBccListByToBccpManifestId(toBccpManifestId));
    }

    @Override
    public NamespaceSummaryRecord getNamespace(NamespaceId namespaceId) {
        return scan(d -> d.getNamespace(namespaceId));
    }

    @Override
    public CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId) {
        return scan(d -> d.getCodeList(codeListManifestId));
    }

    @Override
    public AgencyIdListSummaryRecord getAgencyIdList(AgencyIdListManifestId agencyIdListManifestId) {
        return scan(d -> d.getAgencyIdList(agencyIdListManifestId));
    }

    @Override
    public BlobContentSummaryRecord getBlobContent(BlobContentManifestId blobContentManifestId) {
        return scan(d -> d.getBlobContent(blobContentManifestId));
    }

    // ---- hints ---------------------------------------------------------------------------------------------------

    @Override
    public void prefetchChildren(CcType type, Collection<? extends ManifestId> manifestIds) {
        if (current == cached) {
            try {
                cached.prefetchChildren(type, manifestIds);
            } catch (RuntimeException e) {
                logger.debug("A prefetch of the cached CC document failed and is ignored.", e);
            }
        }
    }

    @Override
    public void prefetchGraph(CcType type, ManifestId root) {
        if (current == cached) {
            try {
                cached.prefetchGraph(type, root);
            } catch (RuntimeException e) {
                logger.debug("A prefetch of the cached CC document failed and is ignored.", e);
            }
        }
    }

}
