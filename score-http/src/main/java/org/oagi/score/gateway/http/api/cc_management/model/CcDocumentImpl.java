package org.oagi.score.gateway.http.api.cc_management.model;

import org.apache.commons.lang3.tuple.Pair;
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
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.cc_management.service.SeqKeyHandler;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.tag_management.model.*;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.oagi.score.gateway.http.api.cc_management.service.SeqKeyHandler.sort;

public class CcDocumentImpl implements CcDocument {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<AccManifestId, AccSummaryRecord> accMap;
    private Map<AccManifestId, List<AccSummaryRecord>> accMapByBasedAccManifestId;
    private Map<AsccManifestId, AsccSummaryRecord> asccMap;
    private Map<AccManifestId, List<AsccSummaryRecord>> asccMapByFromAccManifestId;
    private Map<AsccpManifestId, List<AsccSummaryRecord>> asccMapByToAsccpManifestId;
    private Map<BccManifestId, BccSummaryRecord> bccMap;
    private Map<AccManifestId, List<BccSummaryRecord>> bccMapByFromAccManifestId;
    private Map<BccpManifestId, List<BccSummaryRecord>> bccMapByToBccpManifestId;
    private Map<AccManifestId, List<SeqKeySummaryRecord>> seqKeyMapByFromAccManifestId;
    private Map<AsccpManifestId, AsccpSummaryRecord> asccpMap;
    private Map<AccManifestId, List<AsccpSummaryRecord>> asccpMapByRoleOfAccManifestId;
    private Map<BccpManifestId, BccpSummaryRecord> bccpMap;
    private Map<DtManifestId, List<BccpSummaryRecord>> bccpMapByDtManifestId;
    private Map<DtManifestId, DtSummaryRecord> dtMap;
    private Map<DtScManifestId, DtScSummaryRecord> dtScMap;
    private Map<DtManifestId, List<DtScSummaryRecord>> dtScMapByDtManifestId;
    private Map<Pair<ReleaseId, DtId>, List<DtAwdPriSummaryRecord>> dtAwdPriMap;
    private Map<Pair<ReleaseId, DtScId>, List<DtScAwdPriSummaryRecord>> dtScAwdPriMap;
    private Map<XbtManifestId, XbtSummaryRecord> xbtMap;
    private Map<String, List<XbtSummaryRecord>> xbtMapByName;
    private Map<TagId, TagSummaryRecord> tagMap;
    private Map<AccManifestId, List<AccManifestTagSummaryRecord>> tagMapByAccManifestId;
    private Map<AsccpManifestId, List<AsccpManifestTagSummaryRecord>> tagMapByAsccpManifestId;
    private Map<BccpManifestId, List<BccpManifestTagSummaryRecord>> tagMapByBccpManifestId;
    private Map<DtManifestId, List<DtManifestTagSummaryRecord>> tagMapByDtManifestId;
    private Map<NamespaceId, NamespaceSummaryRecord> namespaceMapByNamespaceId;
    private Map<CodeListManifestId, CodeListSummaryRecord> codeListMapByCodeListManifestId;
    private Map<AgencyIdListManifestId, AgencyIdListSummaryRecord> agencyMapByAgencyIdListManifestId;
    private Map<BlobContentManifestId, BlobContentSummaryRecord> blobContentMapByBlobContentManifestId;

    public CcDocumentImpl(ScoreUser requester, RepositoryFactory repositoryFactory, ReleaseId releaseId) {
        this(requester, repositoryFactory, Arrays.asList(releaseId));
    }

    public CcDocumentImpl(ScoreUser requester, RepositoryFactory repositoryFactory, Collection<ReleaseId> releaseIdList) {

        var releaseQuery = repositoryFactory.releaseQueryRepository(requester);
        Set<ReleaseId> releaseIdListIncludingDependents = new LinkedHashSet<>();
        for (ReleaseId releaseId : releaseIdList) {
            releaseIdListIncludingDependents.addAll(
                    releaseQuery.getIncludedReleaseSummaryList(releaseId)
                            .stream().map(e -> e.releaseId()).collect(Collectors.toSet()));
        }

        ExecutorService executor = ForkJoinPool.commonPool();

        var accQuery = repositoryFactory.accQueryRepository(requester);
        var seqKeyQuery = repositoryFactory.seqKeyQueryRepository(requester);
        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);
        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        var xbtQuery = repositoryFactory.xbtQueryRepository(requester);
        var tagQuery = repositoryFactory.tagQueryRepository(requester);
        var namespaceQuery = repositoryFactory.namespaceQueryRepository(requester);
        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        var agencyIdListQuery = repositoryFactory.agencyIdListQueryRepository(requester);
        var blobContentQuery = repositoryFactory.blobContentQueryRepository(requester);

        try {
            CompletableFuture<List<AccSummaryRecord>> accListFuture =
                    CompletableFuture.supplyAsync(() -> accQuery.getAccSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<AsccSummaryRecord>> asccListFuture =
                    CompletableFuture.supplyAsync(() -> accQuery.getAsccSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<BccSummaryRecord>> bccListFuture =
                    CompletableFuture.supplyAsync(() -> accQuery.getBccSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<SeqKeySummaryRecord>> seqKeyListFuture =
                    CompletableFuture.supplyAsync(() -> seqKeyQuery.getSeqKeySummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<AsccpSummaryRecord>> asccpListFuture =
                    CompletableFuture.supplyAsync(() -> asccpQuery.getAsccpSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<BccpSummaryRecord>> bccpListFuture =
                    CompletableFuture.supplyAsync(() -> bccpQuery.getBccpSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<DtSummaryRecord>> dtListFuture =
                    CompletableFuture.supplyAsync(() -> dtQuery.getDtSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<DtScSummaryRecord>> dtScListFuture =
                    CompletableFuture.supplyAsync(() -> dtQuery.getDtScSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<DtAwdPriSummaryRecord>> dtAwdPriListFuture =
                    CompletableFuture.supplyAsync(() -> dtQuery.getDtAwdPriSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<DtScAwdPriSummaryRecord>> dtScAwdPriListFuture =
                    CompletableFuture.supplyAsync(() -> dtQuery.getDtScAwdPriSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<XbtSummaryRecord>> xbtListFuture =
                    CompletableFuture.supplyAsync(() -> xbtQuery.getXbtSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<TagSummaryRecord>> tagListFuture =
                    CompletableFuture.supplyAsync(tagQuery::getTagSummaryList, executor);
            CompletableFuture<List<AccManifestTagSummaryRecord>> accManifestTagListFuture =
                    CompletableFuture.supplyAsync(() -> tagQuery.getAccManifestTagList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<AsccpManifestTagSummaryRecord>> asccpManifestTagListFuture =
                    CompletableFuture.supplyAsync(() -> tagQuery.getAsccpManifestTagList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<BccpManifestTagSummaryRecord>> bccpManifestTagListFuture =
                    CompletableFuture.supplyAsync(() -> tagQuery.getBccpManifestTagList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<DtManifestTagSummaryRecord>> dtManifestTagListFuture =
                    CompletableFuture.supplyAsync(() -> tagQuery.getDtManifestTagList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<NamespaceSummaryRecord>> namespaceListFuture =
                    CompletableFuture.supplyAsync(() -> namespaceQuery.getNamespaceSummaryList(), executor);
            CompletableFuture<List<CodeListSummaryRecord>> codeListListFuture =
                    CompletableFuture.supplyAsync(() -> codeListQuery.getCodeListSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<AgencyIdListSummaryRecord>> agencyIdListListFuture =
                    CompletableFuture.supplyAsync(() -> agencyIdListQuery.getAgencyIdListSummaryList(releaseIdListIncludingDependents), executor);
            CompletableFuture<List<BlobContentSummaryRecord>> blobContentListFuture =
                    CompletableFuture.supplyAsync(() -> blobContentQuery.getBlobContentSummaryList(releaseIdListIncludingDependents), executor);

            // Collect results
            List<AccSummaryRecord> accList = accListFuture.get();
            accMap = accList.stream().collect(Collectors.toMap(AccSummaryRecord::accManifestId, Function.identity()));
            accMapByBasedAccManifestId = accList.stream().filter(e -> e.basedAccManifestId() != null)
                    .collect(groupingBy(AccSummaryRecord::basedAccManifestId));

            List<AsccSummaryRecord> asccList = asccListFuture.get();
            asccMap = asccList.stream().collect(Collectors.toMap(AsccSummaryRecord::asccManifestId, Function.identity()));
            asccMapByFromAccManifestId = asccList.stream().collect(groupingBy(AsccSummaryRecord::fromAccManifestId));
            asccMapByToAsccpManifestId = asccList.stream().collect(groupingBy(AsccSummaryRecord::toAsccpManifestId));

            List<BccSummaryRecord> bccList = bccListFuture.get();
            bccMap = bccList.stream().collect(Collectors.toMap(BccSummaryRecord::bccManifestId, Function.identity()));
            bccMapByFromAccManifestId = bccList.stream().collect(groupingBy(BccSummaryRecord::fromAccManifestId));
            bccMapByToBccpManifestId = bccList.stream().collect(groupingBy(BccSummaryRecord::toBccpManifestId));

            List<SeqKeySummaryRecord> seqKeyList = seqKeyListFuture.get();
            seqKeyMapByFromAccManifestId = seqKeyList.stream().collect(groupingBy(SeqKeySummaryRecord::fromAccManifestId));

            List<AsccpSummaryRecord> asccpList = asccpListFuture.get();
            asccpMap = asccpList.stream().collect(Collectors.toMap(AsccpSummaryRecord::asccpManifestId, Function.identity()));
            asccpMapByRoleOfAccManifestId = asccpList.stream().collect(groupingBy(AsccpSummaryRecord::roleOfAccManifestId));

            List<BccpSummaryRecord> bccpList = bccpListFuture.get();
            bccpMap = bccpList.stream().collect(Collectors.toMap(BccpSummaryRecord::bccpManifestId, Function.identity()));
            bccpMapByDtManifestId = bccpList.stream().collect(groupingBy(BccpSummaryRecord::dtManifestId));

            List<DtSummaryRecord> dtList = dtListFuture.get();
            dtMap = dtList.stream().collect(Collectors.toMap(DtSummaryRecord::dtManifestId, Function.identity()));

            List<DtScSummaryRecord> dtScList = dtScListFuture.get();
            dtScMap = dtScList.stream().collect(Collectors.toMap(DtScSummaryRecord::dtScManifestId, Function.identity()));
            dtScMapByDtManifestId = dtScList.stream().collect(groupingBy(DtScSummaryRecord::ownerDtManifestId));

            List<DtAwdPriSummaryRecord> dtAwdPriList = dtAwdPriListFuture.get();
            dtAwdPriMap = dtAwdPriList.stream().collect(groupingBy(e -> Pair.of(e.releaseId(), e.dtId())));

            List<DtScAwdPriSummaryRecord> dtScAwdPriList = dtScAwdPriListFuture.get();
            dtScAwdPriMap = dtScAwdPriList.stream().collect(groupingBy(e -> Pair.of(e.releaseId(), e.dtScId())));

            List<XbtSummaryRecord> xbtList = xbtListFuture.get();
            xbtMap = xbtList.stream().collect(Collectors.toMap(XbtSummaryRecord::xbtManifestId, Function.identity()));
            xbtMapByName = xbtList.stream().collect(groupingBy(XbtSummaryRecord::name));

            List<TagSummaryRecord> tagList = tagListFuture.get();
            tagMap = tagList.stream().collect(Collectors.toMap(TagSummaryRecord::tagId, Function.identity()));

            List<AccManifestTagSummaryRecord> accManifestTagList = accManifestTagListFuture.get();
            tagMapByAccManifestId = accManifestTagList.stream().collect(groupingBy(AccManifestTagSummaryRecord::accManifestId));

            List<AsccpManifestTagSummaryRecord> asccpManifestTagList = asccpManifestTagListFuture.get();
            tagMapByAsccpManifestId = asccpManifestTagList.stream().collect(groupingBy(AsccpManifestTagSummaryRecord::asccpManifestId));

            List<BccpManifestTagSummaryRecord> bccpManifestTagList = bccpManifestTagListFuture.get();
            tagMapByBccpManifestId = bccpManifestTagList.stream().collect(groupingBy(BccpManifestTagSummaryRecord::bccpManifestId));

            List<DtManifestTagSummaryRecord> dtManifestTagList = dtManifestTagListFuture.get();
            tagMapByDtManifestId = dtManifestTagList.stream().collect(groupingBy(DtManifestTagSummaryRecord::dtManifestId));

            List<NamespaceSummaryRecord> namespaceList = namespaceListFuture.get();
            namespaceMapByNamespaceId = namespaceList.stream().collect(Collectors.toMap(NamespaceSummaryRecord::namespaceId, Function.identity()));

            List<CodeListSummaryRecord> codeListList = codeListListFuture.get();
            codeListMapByCodeListManifestId = codeListList.stream().collect(Collectors.toMap(CodeListSummaryRecord::codeListManifestId, Function.identity()));

            List<AgencyIdListSummaryRecord> agencyIdListList = agencyIdListListFuture.get();
            agencyMapByAgencyIdListManifestId = agencyIdListList.stream().collect(Collectors.toMap(AgencyIdListSummaryRecord::agencyIdListManifestId, Function.identity()));

            List<BlobContentSummaryRecord> blobContentList = blobContentListFuture.get();
            blobContentMapByBlobContentManifestId = blobContentList.stream().collect(Collectors.toMap(BlobContentSummaryRecord::blobContentManifestId, Function.identity()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching data", e);
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public AccSummaryRecord getAcc(AccManifestId accManifestId) {
        return accMap.get(accManifestId);
    }

    @Override
    public List<CcAssociation> getAssociations(AccSummaryRecord accManifest) {
        if (accManifest == null) {
            return Collections.emptyList();
        }

        List<CcAssociation> associations = new ArrayList();
        sort(this.seqKeyMapByFromAccManifestId.getOrDefault(
                accManifest.accManifestId(), Collections.emptyList())).forEach(seqKey -> {
            if (seqKey.asccManifestId() != null) {
                associations.add(getAscc(seqKey.asccManifestId()));
            } else if (seqKey.bccManifestId() != null) {
                associations.add(getBcc(seqKey.bccManifestId()));
            }
        });

        return associations;
    }

    @Override
    public AsccSummaryRecord getAscc(AsccManifestId asccManifestId) {
        return asccMap.get(asccManifestId);
    }

    @Override
    public BccSummaryRecord getBcc(BccManifestId bccManifestId) {
        return bccMap.get(bccManifestId);
    }

    @Override
    public AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId) {
        return asccpMap.get(asccpManifestId);
    }

    @Override
    public Collection<AsccpSummaryRecord> getAsccpList() {
        return asccpMap.values();
    }

    @Override
    public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
        return bccpMap.get(bccpManifestId);
    }

    @Override
    public DtSummaryRecord getDt(DtManifestId dtManifestId) {
        return dtMap.get(dtManifestId);
    }

    @Override
    public List<DtSummaryRecord> getDtList() {
        return new ArrayList<>(dtMap.values());
    }

    @Override
    public DtScSummaryRecord getDtSc(DtScManifestId dtScManifestId) {
        return dtScMap.get(dtScManifestId);
    }

    @Override
    public List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId) {
        return dtScMapByDtManifestId.getOrDefault(dtManifestId, Collections.emptyList());
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriList(DtManifestId dtManifestId) {
        DtSummaryRecord dt = getDt(dtManifestId);
        if (dt == null) {
            return Collections.emptyList();
        }
        return dtAwdPriMap.getOrDefault(Pair.of(dt.release().releaseId(), dt.dtId()), Collections.emptyList());
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriList(DtScManifestId dtScManifestId) {
        DtScSummaryRecord dtSc = getDtSc(dtScManifestId);
        if (dtSc == null) {
            return Collections.emptyList();
        }
        return dtScAwdPriMap.getOrDefault(Pair.of(dtSc.release().releaseId(), dtSc.dtScId()), Collections.emptyList());
    }

    @Override
    public XbtSummaryRecord getXbt(XbtManifestId xbtManifestId) {
        return xbtMap.get(xbtManifestId);
    }

    @Override
    public List<XbtSummaryRecord> getXbtListByName(String name) {
        return xbtMapByName.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public List<AccSummaryRecord> getAccListByBasedAccManifestId(AccManifestId basedAccManifestId) {
        return accMapByBasedAccManifestId.getOrDefault(basedAccManifestId, Collections.emptyList());
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpListByRoleOfAccManifestId(AccManifestId roleOfAccManifestId) {
        return asccpMapByRoleOfAccManifestId.getOrDefault(roleOfAccManifestId, Collections.emptyList());
    }

    @Override
    public List<AccSummaryRecord> getAccListByToAsccpManifestId(AsccpManifestId toAsccpManifestId) {
        return asccMapByToAsccpManifestId.getOrDefault(toAsccpManifestId, Collections.emptyList())
                .stream().map(e -> getAcc(e.fromAccManifestId())).collect(Collectors.toList());
    }

    @Override
    public List<AccSummaryRecord> getAccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return bccMapByToBccpManifestId.getOrDefault(toBccpManifestId, Collections.emptyList())
                .stream().map(e -> getAcc(e.fromAccManifestId())).collect(Collectors.toList());
    }

    @Override
    public List<BccpSummaryRecord> getBccpListByDtManifestId(DtManifestId dtManifestId) {
        return bccpMapByDtManifestId.getOrDefault(dtManifestId, Collections.emptyList());
    }

    @Override
    public List<AsccSummaryRecord> getAsccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return asccMapByFromAccManifestId.getOrDefault(fromAccManifestId, Collections.emptyList());
    }

    @Override
    public List<BccSummaryRecord> getBccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        return bccMapByFromAccManifestId.getOrDefault(fromAccManifestId, Collections.emptyList());
    }

    @Override
    public List<BccSummaryRecord> getBccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return bccMapByToBccpManifestId.getOrDefault(toBccpManifestId, Collections.emptyList());
    }

    @Override
    public List<SeqKeySupportable> getAssociationListByFromAccManifestId(AccManifestId fromAccManifestId) {
        List<SeqKeySupportable> associations = new ArrayList();

        associations.addAll(getAsccListByFromAccManifestId(fromAccManifestId));
        associations.addAll(getBccListByFromAccManifestId(fromAccManifestId));

        return SeqKeyHandler.sort(associations);
    }

    @Override
    public List<DtScSummaryRecord> getDtScListByDtManifestId(DtManifestId dtManifestId) {
        return dtScMapByDtManifestId.getOrDefault(dtManifestId, Collections.emptyList());
    }

    @Override
    public List<TagSummaryRecord> getTagListByAccManifestId(AccManifestId accManifestId) {
        List<AccManifestTagSummaryRecord> accManifestTagList = tagMapByAccManifestId.getOrDefault(accManifestId, Collections.emptyList());
        return accManifestTagList.stream().map(e -> tagMap.get(e.tagId()))
                .filter(e -> e != null).collect(Collectors.toList());
    }

    @Override
    public List<TagSummaryRecord> getTagListByAsccpManifestId(AsccpManifestId asccpManifestId) {
        List<AsccpManifestTagSummaryRecord> asccpManifestTagList = tagMapByAsccpManifestId.getOrDefault(asccpManifestId, Collections.emptyList());
        return asccpManifestTagList.stream().map(e -> tagMap.get(e.tagId()))
                .filter(e -> e != null).collect(Collectors.toList());
    }

    @Override
    public List<TagSummaryRecord> getTagListByBccpManifestId(BccpManifestId bccpManifestId) {
        List<BccpManifestTagSummaryRecord> bccpManifestTagList = tagMapByBccpManifestId.getOrDefault(bccpManifestId, Collections.emptyList());
        return bccpManifestTagList.stream().map(e -> tagMap.get(e.tagId()))
                .filter(e -> e != null).collect(Collectors.toList());
    }

    @Override
    public List<TagSummaryRecord> getTagListByDtManifestId(DtManifestId dtManifestId) {
        List<DtManifestTagSummaryRecord> dtManifestTagList = tagMapByDtManifestId.getOrDefault(dtManifestId, Collections.emptyList());
        return dtManifestTagList.stream().map(e -> tagMap.get(e.tagId()))
                .filter(e -> e != null).collect(Collectors.toList());
    }

    @Override
    public NamespaceSummaryRecord getNamespace(NamespaceId namespaceId) {
        return namespaceMapByNamespaceId.get(namespaceId);
    }

    @Override
    public CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId) {
        return codeListMapByCodeListManifestId.get(codeListManifestId);
    }

    @Override
    public AgencyIdListSummaryRecord getAgencyIdList(AgencyIdListManifestId agencyIdListManifestId) {
        return agencyMapByAgencyIdListManifestId.get(agencyIdListManifestId);
    }

    @Override
    public BlobContentSummaryRecord getBlobContent(BlobContentManifestId blobContentManifestId) {
        return blobContentMapByBlobContentManifestId.get(blobContentManifestId);
    }

}
