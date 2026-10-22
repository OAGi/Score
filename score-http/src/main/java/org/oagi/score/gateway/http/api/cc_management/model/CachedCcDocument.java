package org.oagi.score.gateway.http.api.cc_management.model;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCacheProperties;
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
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.cc_management.service.SeqKeyHandler;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A {@link CcDocument} that answers from a cache shared by every request and reads the database only for what is
 * missing, instead of loading every component of the release the way {@link CcDocumentImpl} does.
 * <p>
 * Its lookups are safe to call from several threads.
 * <p>
 * It answers like {@link CcDocumentImpl}: a component outside the releases the document was made for (and the
 * releases they depend on) is not found. What it caches is the lookups by manifest id and the lists under a parent
 * (the associations of an ACC and their order, the supplementary components of a DT, the tags) and the records that point at
 * a component, which it asks the database for. It does not answer the two lookups that need the whole release
 * ({@link #getAsccpList()} and {@link #getBlobContent}); {@link CacheFirstCcDocument}, which is what the factory hands out,
 * answers them from the loaded releases.
 */
public class CachedCcDocument implements CcDocument {

    private final ScoreUser requester;
    private final RepositoryFactory repositoryFactory;
    private final CcCache cache;
    private final List<ReleaseId> releaseIdList;

    /** The epoch of the cache when this document was made: it stores nothing once entries have been forgotten since. */
    private final long epoch;

    /**
     * What this document has already got from the cache or the database. Reading the dependencies reads the same entries thousands of
     * times, and when the cache is not in the JVM each of those reads would be a round trip. It lives as long as the
     * document, which is as long as one operation.
     */
    private final ConcurrentMap<String, Object> memo = new ConcurrentHashMap<>();

    private volatile Set<ReleaseId> scope;

    /** The keys of the components in the dependencies this document has prefetched: asking for one of them needs no prefetch of its own. */
    private final Set<String> prefetchedKeys = ConcurrentHashMap.newKeySet();
    private final AtomicInteger prefetchedDependencies = new AtomicInteger();
    /** The most ids sent to the database in one {@code IN} list; see {@link CcCacheProperties#getBatchSize()}. */
    private final int batchSize;
    /** The most times the dependencies of a component are read ahead; see {@link CcCacheProperties#getMaxPrefetchedDependencies()}. */
    private final int maxPrefetchedDependencies;

    public CachedCcDocument(ScoreUser requester, RepositoryFactory repositoryFactory, ReleaseId releaseId) {
        this(requester, repositoryFactory, Arrays.asList(releaseId));
    }

    public CachedCcDocument(ScoreUser requester, RepositoryFactory repositoryFactory, Collection<ReleaseId> releaseIdList) {
        this.requester = requester;
        this.repositoryFactory = repositoryFactory;
        this.cache = repositoryFactory.ccCache();
        this.epoch = cache.epoch();
        this.releaseIdList = new ArrayList<>(releaseIdList);
        this.batchSize = repositoryFactory.ccCacheProperties().getBatchSize();
        this.maxPrefetchedDependencies = repositoryFactory.ccCacheProperties().getMaxPrefetchedDependencies();
    }

    // ---- the cache -----------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private <T> T cached(String key, Supplier<T> loader) {
        boolean bypassed = cache.isBypassed();
        if (!bypassed) {
            Object hit = recall(key);
            if (hit != null) {
                return (T) hit;
            }
        }
        T value = loader.get();
        if (value != null && !bypassed) {
            remember(key, value);
        }
        return value;
    }

    private Object recall(String key) {
        Object value = memo.get(key);
        if (value == null) {
            value = cache.get(key);
            if (value != null) {
                memo.put(key, value);
            }
        }
        return value;
    }

    /** The values of the keys that have one, the cache asked once for all the ones the document does not have yet. */
    private Map<String, Object> recallAll(Collection<String> keys) {
        Map<String, Object> found = new HashMap<>();
        List<String> missing = new ArrayList<>();
        for (String key : keys) {
            Object value = memo.get(key);
            if (value != null) {
                found.put(key, value);
            } else {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            Map<String, Object> fromCache = cache.getAll(missing);
            memo.putAll(fromCache);
            found.putAll(fromCache);
        }
        return found;
    }

    private void remember(String key, Object value) {
        cache.put(epoch, key, value);
        memo.put(key, value);
    }

    private void rememberAll(Map<String, Object> values) {
        if (!values.isEmpty()) {
            cache.putAll(epoch, values);
            memo.putAll(values);
        }
    }

    /**
     * Prefetches the dependencies of a component the first time the document asks for it, unless that component is among dependencies that were read
     * already. The consumers of this document (a schema export, a BIE, the graph screen) all go on to ask for most of
     * what the component reaches, and asking the cache for each of those one by one costs a round trip each when the cache is in
     * Redis, and a query each when the cache is empty; the dependencies are read in a few bulk reads, or loaded in a few queries.
     */
    private void prefetchFor(CcType type, ManifestId manifestId, String key) {
        if (memo.containsKey(key) || prefetchedKeys.contains(key) || cache.isBypassed()) {
            return;
        }
        if (prefetchedDependencies.incrementAndGet() > maxPrefetchedDependencies) {
            return;
        }
        try {
            prefetch(type, manifestId, true);
        } catch (RuntimeException e) {
            // Only a speed-up: what could not be read now is read, or fails, where it is looked up.
        }
    }

    /** The releases this document answers for: the given ones and everything they depend on. */
    private Set<ReleaseId> scope() {
        Set<ReleaseId> scope = this.scope;
        if (scope == null) {
            var releaseQuery = repositoryFactory.releaseQueryRepository(requester);
            Set<ReleaseId> releaseIds = new HashSet<>();
            for (ReleaseId releaseId : releaseIdList) {
                List<ReleaseId> included = cached("scope:" + releaseId.value(), () ->
                        releaseQuery.getIncludedReleaseSummaryList(releaseId).stream()
                                .map(ReleaseSummaryRecord::releaseId)
                                .collect(Collectors.toList()));
                releaseIds.addAll(included);
            }
            scope = releaseIds;
            this.scope = scope;
        }
        return scope;
    }

    private <T> T inScope(T record, Function<T, ReleaseSummaryRecord> releaseOf) {
        return (record != null && scope().contains(releaseOf.apply(record).releaseId())) ? record : null;
    }

    private <T> List<T> inScope(List<T> records, Function<T, ReleaseSummaryRecord> releaseOf) {
        return records.stream().filter(e -> scope().contains(releaseOf.apply(e).releaseId())).collect(Collectors.toList());
    }

    // ---- lookups by manifest id ----------------------------------------------------------------------------------

    @Override
    public AccSummaryRecord getAcc(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        prefetchFor(CcType.ACC, accManifestId, "acc:" + accManifestId.value());
        return inScope(cached("acc:" + accManifestId.value(),
                () -> repositoryFactory.accQueryRepository(requester).getAccSummary(accManifestId)),
                AccSummaryRecord::release);
    }

    @Override
    public AsccSummaryRecord getAscc(AsccManifestId asccManifestId) {
        if (asccManifestId == null) {
            return null;
        }
        return inScope(cached("ascc:" + asccManifestId.value(),
                () -> repositoryFactory.accQueryRepository(requester).getAsccSummary(asccManifestId)),
                AsccSummaryRecord::release);
    }

    @Override
    public BccSummaryRecord getBcc(BccManifestId bccManifestId) {
        if (bccManifestId == null) {
            return null;
        }
        return inScope(cached("bcc:" + bccManifestId.value(),
                () -> repositoryFactory.accQueryRepository(requester).getBccSummary(bccManifestId)),
                BccSummaryRecord::release);
    }

    @Override
    public AsccpSummaryRecord getAsccp(AsccpManifestId asccpManifestId) {
        if (asccpManifestId == null) {
            return null;
        }
        prefetchFor(CcType.ASCCP, asccpManifestId, "asccp:" + asccpManifestId.value());
        return inScope(cached("asccp:" + asccpManifestId.value(),
                () -> repositoryFactory.asccpQueryRepository(requester).getAsccpSummary(asccpManifestId)),
                AsccpSummaryRecord::release);
    }

    @Override
    public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }
        prefetchFor(CcType.BCCP, bccpManifestId, "bccp:" + bccpManifestId.value());
        return inScope(cached("bccp:" + bccpManifestId.value(),
                () -> repositoryFactory.bccpQueryRepository(requester).getBccpSummary(bccpManifestId)),
                BccpSummaryRecord::release);
    }

    @Override
    public DtSummaryRecord getDt(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return null;
        }
        prefetchFor(CcType.DT, dtManifestId, "dt:" + dtManifestId.value());
        return inScope(cached("dt:" + dtManifestId.value(),
                () -> repositoryFactory.dtQueryRepository(requester).getDtSummary(dtManifestId)),
                DtSummaryRecord::release);
    }

    @Override
    public DtScSummaryRecord getDtSc(DtScManifestId dtScManifestId) {
        if (dtScManifestId == null) {
            return null;
        }
        return inScope(cached("dt_sc:" + dtScManifestId.value(),
                () -> repositoryFactory.dtQueryRepository(requester).getDtScSummary(dtScManifestId)),
                DtScSummaryRecord::release);
    }

    // ---- lists under a parent ------------------------------------------------------------------------------------

    @Override
    public List<AsccSummaryRecord> getAsccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        if (fromAccManifestId == null) {
            return Collections.emptyList();
        }
        return inScope(cached("ascc-from-acc:" + fromAccManifestId.value(),
                () -> List.copyOf(repositoryFactory.accQueryRepository(requester).getAsccSummaryList(fromAccManifestId))),
                AsccSummaryRecord::release);
    }

    @Override
    public List<BccSummaryRecord> getBccListByFromAccManifestId(AccManifestId fromAccManifestId) {
        if (fromAccManifestId == null) {
            return Collections.emptyList();
        }
        return inScope(cached("bcc-from-acc:" + fromAccManifestId.value(),
                () -> List.copyOf(repositoryFactory.accQueryRepository(requester).getBccSummaryList(fromAccManifestId))),
                BccSummaryRecord::release);
    }

    @Override
    public List<DtScSummaryRecord> getDtScList(DtManifestId dtManifestId) {
        return getDtScListByDtManifestId(dtManifestId);
    }

    @Override
    public List<DtScSummaryRecord> getDtScListByDtManifestId(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return Collections.emptyList();
        }
        return inScope(cached("dt_sc-of-dt:" + dtManifestId.value(),
                () -> List.copyOf(repositoryFactory.dtQueryRepository(requester).getDtScSummaryList(dtManifestId))),
                DtScSummaryRecord::release);
    }

    @Override
    public List<CcAssociation> getAssociations(AccSummaryRecord accManifest) {
        if (accManifest == null || getAcc(accManifest.accManifestId()) == null) {
            return Collections.emptyList();
        }
        AccManifestId accManifestId = accManifest.accManifestId();
        List<SeqKeySummaryRecord> seqKeys = cached("seq-key-from-acc:" + accManifestId.value(),
                () -> List.copyOf(repositoryFactory.seqKeyQueryRepository(requester).getSeqKeySummaryList(accManifestId)));

        // The ASCCs and BCCs of the ACC are in the cache as lists (and are read together with the dependencies).
        Map<AsccManifestId, AsccSummaryRecord> asccs = new HashMap<>();
        getAsccListByFromAccManifestId(accManifestId).forEach(e -> asccs.put(e.asccManifestId(), e));
        Map<BccManifestId, BccSummaryRecord> bccs = new HashMap<>();
        getBccListByFromAccManifestId(accManifestId).forEach(e -> bccs.put(e.bccManifestId(), e));

        List<CcAssociation> associations = new ArrayList<>();
        SeqKeyHandler.sort(seqKeys).forEach(seqKey -> {
            if (seqKey.asccManifestId() != null) {
                AsccSummaryRecord ascc = asccs.get(seqKey.asccManifestId());
                associations.add((ascc != null) ? ascc : getAscc(seqKey.asccManifestId()));
            } else if (seqKey.bccManifestId() != null) {
                BccSummaryRecord bcc = bccs.get(seqKey.bccManifestId());
                associations.add((bcc != null) ? bcc : getBcc(seqKey.bccManifestId()));
            }
        });
        return associations;
    }

    @Override
    public List<SeqKeySupportable> getAssociationListByFromAccManifestId(AccManifestId fromAccManifestId) {
        List<SeqKeySupportable> associations = new ArrayList<>();
        associations.addAll(getAsccListByFromAccManifestId(fromAccManifestId));
        associations.addAll(getBccListByFromAccManifestId(fromAccManifestId));
        return SeqKeyHandler.sort(associations);
    }

    @Override
    public List<TagSummaryRecord> getTagListByAccManifestId(AccManifestId accManifestId) {
        // The tags of a component the document does not know (one of another release) are not its to give.
        return (getAcc(accManifestId) == null) ? Collections.emptyList() : cached("tags:acc:" + accManifestId.value(),
                () -> inTagOrder(repositoryFactory.tagQueryRepository(requester).getTagSummaryList(accManifestId)));
    }

    @Override
    public List<TagSummaryRecord> getTagListByAsccpManifestId(AsccpManifestId asccpManifestId) {
        // The tags of a component the document does not know (one of another release) are not its to give.
        return (getAsccp(asccpManifestId) == null) ? Collections.emptyList() : cached("tags:asccp:" + asccpManifestId.value(),
                () -> inTagOrder(repositoryFactory.tagQueryRepository(requester).getTagSummaryList(asccpManifestId)));
    }

    @Override
    public List<TagSummaryRecord> getTagListByBccpManifestId(BccpManifestId bccpManifestId) {
        // The tags of a component the document does not know (one of another release) are not its to give.
        return (getBccp(bccpManifestId) == null) ? Collections.emptyList() : cached("tags:bccp:" + bccpManifestId.value(),
                () -> inTagOrder(repositoryFactory.tagQueryRepository(requester).getTagSummaryList(bccpManifestId)));
    }

    @Override
    public List<TagSummaryRecord> getTagListByDtManifestId(DtManifestId dtManifestId) {
        // The tags of a component the document does not know (one of another release) are not its to give.
        return (getDt(dtManifestId) == null) ? Collections.emptyList() : cached("tags:dt:" + dtManifestId.value(),
                () -> inTagOrder(repositoryFactory.tagQueryRepository(requester).getTagSummaryList(dtManifestId)));
    }

    // ---- prefetching ---------------------------------------------------------------------------------------------

    @Override
    public void prefetchChildren(CcType type, Collection<? extends ManifestId> manifestIds) {
        if (cache.isBypassed() || manifestIds == null || manifestIds.isEmpty()) {
            return;
        }
        switch (type) {
            case ACC -> prefetchAccChildren(cast(manifestIds, AccManifestId.class));
            case ASCCP -> prefetchAsccpChildren(cast(manifestIds, AsccpManifestId.class));
            case BCCP -> prefetchBccpChildren(cast(manifestIds, BccpManifestId.class));
            case DT -> prefetchDtChildren(cast(manifestIds, DtManifestId.class));
            default -> {
            }
        }
    }

    private static <I extends ManifestId> List<I> cast(Collection<? extends ManifestId> ids, Class<I> type) {
        return ids.stream().filter(type::isInstance).map(type::cast).distinct().collect(Collectors.toList());
    }

    /** The components that a component depends on, not what their data types need besides (the screen showing the graph needs only the components). */
    @Override
    public void prefetchDependencies(CcType type, ManifestId root) {
        prefetch(type, root, false);
    }

    /** @param withDtRelated also what the data types need: their supplementary components, allowed primitives, namespaces, code lists... */
    private void prefetch(CcType type, ManifestId root, boolean withDtRelated) {
        if (cache.isBypassed() || root == null) {
            return;
        }
        String marker = (withDtRelated ? "dependencies-dt:" : "dependencies:") + type.name().toLowerCase(Locale.ROOT) + ":" + root.value();
        if (!readDependencies(marker)) {
            cache.singleFlight(marker, () -> {
                if (!readDependencies(marker)) {
                    loadDependencies(type, root, marker, withDtRelated);
                }
            });
            readDependencies(marker);
        }
    }

    /** Reads all the dependencies that the marker stands for, in a few bulk reads. @return whether there was one */
    private boolean readDependencies(String marker) {
        Object found = recall(marker);
        if (!(found instanceof DependencyManifestIds dependencies)) {
            return false;
        }
        List<String> keys = new ArrayList<>();
        for (AccManifestId id : dependencies.accs()) {
            keys.add("acc:" + id.value());
            keys.add("ascc-from-acc:" + id.value());
            keys.add("bcc-from-acc:" + id.value());
            keys.add("seq-key-from-acc:" + id.value());
            keys.add("tags:acc:" + id.value());
            prefetchedKeys.add("acc:" + id.value());
        }
        for (AsccpManifestId id : dependencies.asccps()) {
            keys.add("asccp:" + id.value());
            keys.add("tags:asccp:" + id.value());
            prefetchedKeys.add("asccp:" + id.value());
        }
        for (BccpManifestId id : dependencies.bccps()) {
            keys.add("bccp:" + id.value());
            keys.add("tags:bccp:" + id.value());
            prefetchedKeys.add("bccp:" + id.value());
        }
        for (DtManifestId id : dependencies.dts()) {
            keys.add("dt:" + id.value());
            keys.add("dt_sc-of-dt:" + id.value());
            keys.add("tags:dt:" + id.value());
            if (dependencies.hasDtRelated()) {
                keys.add("dt-awd-pri:" + id.value());
            }
            prefetchedKeys.add("dt:" + id.value());
        }
        for (AccManifestId id : dependencies.prevAccs()) {
            keys.add("ascc-from-acc:" + id.value());
            keys.add("bcc-from-acc:" + id.value());
        }
        for (DtManifestId id : dependencies.prevDts()) {
            keys.add("dt:" + id.value());
            keys.add("dt_sc-of-dt:" + id.value());
            keys.add("dt-awd-pri:" + id.value());
            prefetchedKeys.add("dt:" + id.value());
        }
        for (DtManifestId id : dependencies.basedDts()) {
            keys.add("dt:" + id.value());
            keys.add("dt_sc-of-dt:" + id.value());
            keys.add("dt-awd-pri:" + id.value());
            prefetchedKeys.add("dt:" + id.value());
        }
        for (DtScManifestId id : dependencies.dtScs()) {
            keys.add("dt_sc:" + id.value());
            keys.add("dt_sc-awd-pri:" + id.value());
        }
        for (NamespaceId id : dependencies.namespaces()) {
            keys.add("namespace:" + id.value());
        }
        for (CodeListManifestId id : dependencies.codeLists()) {
            keys.add("code-list:" + id.value());
            keys.add("code-list-release:" + id.value());
        }
        for (AgencyIdListManifestId id : dependencies.agencyIdLists()) {
            keys.add("agency-id-list:" + id.value());
            keys.add("agency-id-list-release:" + id.value());
        }
        if (dependencies.hasDtRelated()) {
            for (ReleaseId releaseId : scope()) {
                keys.add("xbts:" + releaseId.value());
                keys.add("dts:" + releaseId.value());
            }
        }
        recallAll(keys);
        return true;
    }

    private void loadDependencies(CcType type, ManifestId root, String marker, boolean withDtRelated) {
        DependencyManifestIds reached = repositoryFactory.ccQueryRepository(requester).getDependencyManifestIds(type, root);
        Set<AccManifestId> prevAccs = prefetchAccChildren(List.copyOf(reached.accs()));
        prefetchAsccpChildren(List.copyOf(reached.asccps()));
        prefetchBccpChildren(List.copyOf(reached.bccps()));
        Set<DtManifestId> prevDts = prefetchDtChildren(List.copyOf(reached.dts()));
        resolveTags(List.of(Pair.of(type, root)));
        DependencyManifestIds dtRelated = withDtRelated ? prefetchDtRelated(reached.dts(), prevDts) : reached;
        if (withDtRelated) {
            // what the document will ask for besides the components: the XBTs and the data types of its releases
            xbtIndex();
            dtIndex();
        }
        remember(marker, new DependencyManifestIds(
                Set.copyOf(reached.accs()), Set.copyOf(reached.asccps()), Set.copyOf(reached.bccps()), Set.copyOf(reached.dts()),
                Set.copyOf(prevAccs), Set.copyOf(prevDts),
                Set.copyOf(dtRelated.dtScs()), Set.copyOf(dtRelated.basedDts()), Set.copyOf(dtRelated.namespaces()),
                Set.copyOf(dtRelated.codeLists()), Set.copyOf(dtRelated.agencyIdLists())));
    }

    /**
     * Reads, in bulk, what the data types among the dependencies lead to: the supplementary components, the data types they are based on, the
     * allowed primitives of both (and so the code lists and agency identification lists those refer to, with their releases) and
     * the namespaces. These tables are small, and a schema export asks for all of it, one record at a time.
     */
    private DependencyManifestIds prefetchDtRelated(Collection<DtManifestId> reachedDts, Set<DtManifestId> prevDts) {
        var dtQuery = repositoryFactory.dtQueryRepository(requester);

        // the data types: the ones reached, the ones they stand in for, and the ones they are based on (and so on)
        Set<DtManifestId> all = new LinkedHashSet<>(reachedDts);
        all.addAll(prevDts);
        Map<DtManifestId, DtSummaryRecord> dts = new LinkedHashMap<>(resolve("dt:", all,
                dtQuery::getDtSummaryListByManifestIds, DtSummaryRecord::dtManifestId));
        Set<DtManifestId> basedDts = new LinkedHashSet<>();
        Map<DtManifestId, DtSummaryRecord> pending = dts;
        for (int depth = 0; depth < 8; depth++) {
            Set<DtManifestId> next = pending.values().stream().map(DtSummaryRecord::basedDtManifestId)
                    .filter(Objects::nonNull).filter(e -> !dts.containsKey(e)).collect(Collectors.toCollection(LinkedHashSet::new));
            if (next.isEmpty()) {
                break;
            }
            Map<DtManifestId, DtSummaryRecord> found = resolve("dt:", next,
                    dtQuery::getDtSummaryListByManifestIds, DtSummaryRecord::dtManifestId);
            resolveLists("dt_sc-of-dt:", next,
                    dtQuery::getDtScSummaryListByOwnerDtManifestIds, DtScSummaryRecord::ownerDtManifestId);
            basedDts.addAll(found.keySet());
            dts.putAll(found);
            pending = found;
        }

        // the supplementary components, each also under its own key
        Map<DtManifestId, List<DtScSummaryRecord>> dtScLists = resolveLists("dt_sc-of-dt:", dts.keySet(),
                dtQuery::getDtScSummaryListByOwnerDtManifestIds, DtScSummaryRecord::ownerDtManifestId);
        Map<DtScManifestId, DtScSummaryRecord> dtScs = new LinkedHashMap<>();
        dtScLists.values().forEach(list -> list.forEach(e -> dtScs.put(e.dtScManifestId(), e)));
        Map<String, Object> dtScEntries = new HashMap<>();
        dtScs.forEach((id, record) -> {
            if (!memo.containsKey("dt_sc:" + id.value())) {
                dtScEntries.put("dt_sc:" + id.value(), record);
            }
        });
        rememberAll(dtScEntries);

        // the allowed primitives
        List<DtAwdPriSummaryRecord> awdPris = new ArrayList<>();
        awdPriOf(dts, awdPris);
        List<DtScAwdPriSummaryRecord> dtScAwdPris = new ArrayList<>();
        dtScAwdPriOf(dtScs, dtScAwdPris);

        // the code lists and agency identification lists they refer to, and the release of each
        Set<CodeListManifestId> codeLists = new LinkedHashSet<>();
        Set<AgencyIdListManifestId> agencyIdLists = new LinkedHashSet<>();
        awdPris.forEach(e -> {
            if (e.codeListManifestId() != null) {
                codeLists.add(e.codeListManifestId());
            }
            if (e.agencyIdListManifestId() != null) {
                agencyIdLists.add(e.agencyIdListManifestId());
            }
        });
        dtScAwdPris.forEach(e -> {
            if (e.codeListManifestId() != null) {
                codeLists.add(e.codeListManifestId());
            }
            if (e.agencyIdListManifestId() != null) {
                agencyIdLists.add(e.agencyIdListManifestId());
            }
        });
        var codeListQuery = repositoryFactory.codeListQueryRepository(requester);
        resolve("code-list:", codeLists, codeListQuery::getCodeListSummaryListByManifestIds, CodeListSummaryRecord::codeListManifestId);
        resolveReleases("code-list-release:", codeLists, codeListQuery::getCodeListReleaseIds);
        var agencyQuery = repositoryFactory.agencyIdListQueryRepository(requester);
        resolve("agency-id-list:", agencyIdLists, agencyQuery::getAgencyIdListSummaryListByManifestIds, AgencyIdListSummaryRecord::agencyIdListManifestId);
        resolveReleases("agency-id-list-release:", agencyIdLists, agencyQuery::getAgencyIdListReleaseIds);

        // the namespaces: a few, all of them
        List<NamespaceSummaryRecord> namespaces = repositoryFactory.namespaceQueryRepository(requester).getNamespaceSummaryList();
        Map<String, Object> namespaceEntries = new HashMap<>();
        namespaces.forEach(e -> namespaceEntries.put("namespace:" + e.namespaceId().value(), e));
        rememberAll(namespaceEntries);

        return new DependencyManifestIds(Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(),
                Set.copyOf(dtScs.keySet()),
                Set.copyOf(basedDts),
                namespaces.stream().map(NamespaceSummaryRecord::namespaceId).collect(Collectors.toUnmodifiableSet()),
                Set.copyOf(codeLists),
                Set.copyOf(agencyIdLists));
    }

    /** The allowed primitives of each data type, read like {@link #resolveLists}; they are added to {@code collected}. */
    @SuppressWarnings("unchecked")
    private void awdPriOf(Map<DtManifestId, DtSummaryRecord> dts, List<DtAwdPriSummaryRecord> collected) {
        Map<String, Object> hits = recallAll(dts.keySet().stream().map(e -> "dt-awd-pri:" + e.value()).collect(Collectors.toList()));
        List<DtSummaryRecord> missing = new ArrayList<>();
        dts.forEach((id, dt) -> {
            Object hit = hits.get("dt-awd-pri:" + id.value());
            if (hit != null) {
                collected.addAll((List<DtAwdPriSummaryRecord>) hit);
            } else {
                missing.add(dt);
            }
        });
        if (missing.isEmpty()) {
            return;
        }
        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        Map<Pair<ReleaseId, org.oagi.score.gateway.http.api.cc_management.model.dt.DtId>, List<DtAwdPriSummaryRecord>> byDt = new HashMap<>();
        for (List<org.oagi.score.gateway.http.api.cc_management.model.dt.DtId> batch :
                partition(missing.stream().map(DtSummaryRecord::dtId).distinct().collect(Collectors.toList()))) {
            for (DtAwdPriSummaryRecord record : dtQuery.getDtAwdPriSummaryListByDtIds(batch)) {
                byDt.computeIfAbsent(Pair.of(record.releaseId(), record.dtId()), k -> new ArrayList<>()).add(record);
            }
        }
        Map<String, Object> remembered = new HashMap<>();
        for (DtSummaryRecord dt : missing) {
            // the loaded releases list them by id
            List<DtAwdPriSummaryRecord> list = byDt.getOrDefault(Pair.of(dt.release().releaseId(), dt.dtId()), Collections.emptyList()).stream()
                    .sorted(Comparator.comparing(e -> e.dtAwdPriId().value())).collect(Collectors.toUnmodifiableList());
            remembered.put("dt-awd-pri:" + dt.dtManifestId().value(), list);
            collected.addAll(list);
        }
        rememberAll(remembered);
    }

    @SuppressWarnings("unchecked")
    private void dtScAwdPriOf(Map<DtScManifestId, DtScSummaryRecord> dtScs, List<DtScAwdPriSummaryRecord> collected) {
        Map<String, Object> hits = recallAll(dtScs.keySet().stream().map(e -> "dt_sc-awd-pri:" + e.value()).collect(Collectors.toList()));
        List<DtScSummaryRecord> missing = new ArrayList<>();
        dtScs.forEach((id, dtSc) -> {
            Object hit = hits.get("dt_sc-awd-pri:" + id.value());
            if (hit != null) {
                collected.addAll((List<DtScAwdPriSummaryRecord>) hit);
            } else {
                missing.add(dtSc);
            }
        });
        if (missing.isEmpty()) {
            return;
        }
        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        Map<Pair<ReleaseId, org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId>, List<DtScAwdPriSummaryRecord>> byDtSc = new HashMap<>();
        for (List<org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId> batch :
                partition(missing.stream().map(DtScSummaryRecord::dtScId).distinct().collect(Collectors.toList()))) {
            for (DtScAwdPriSummaryRecord record : dtQuery.getDtScAwdPriSummaryListByDtScIds(batch)) {
                byDtSc.computeIfAbsent(Pair.of(record.releaseId(), record.dtScId()), k -> new ArrayList<>()).add(record);
            }
        }
        Map<String, Object> remembered = new HashMap<>();
        for (DtScSummaryRecord dtSc : missing) {
            List<DtScAwdPriSummaryRecord> list = byDtSc.getOrDefault(Pair.of(dtSc.release().releaseId(), dtSc.dtScId()), Collections.emptyList()).stream()
                    .sorted(Comparator.comparing(e -> e.dtScAwdPriId().value())).collect(Collectors.toUnmodifiableList());
            remembered.put("dt_sc-awd-pri:" + dtSc.dtScManifestId().value(), list);
            collected.addAll(list);
        }
        rememberAll(remembered);
    }

    /** The release of each manifest: the cached ones read together, the missing ones read in batches (and cached). */
    private <I extends ManifestId> void resolveReleases(String prefix, Collection<I> ids,
                                                       Function<List<I>, Map<I, ReleaseId>> loader) {
        Map<String, Object> hits = recallAll(ids.stream().map(e -> prefix + e.value()).collect(Collectors.toList()));
        List<I> missing = ids.stream().filter(e -> !hits.containsKey(prefix + e.value())).collect(Collectors.toList());
        for (List<I> batch : partition(missing)) {
            Map<String, Object> remembered = new HashMap<>();
            loader.apply(batch).forEach((id, releaseId) -> remembered.put(prefix + id.value(), releaseId));
            rememberAll(remembered);
        }
    }

    /** @return the components the ACCs without associations stand in for */
    private Set<AccManifestId> prefetchAccChildren(List<AccManifestId> accManifestIds) {
        var accQuery = repositoryFactory.accQueryRepository(requester);

        Map<AccManifestId, AccSummaryRecord> accs = resolve("acc:", accManifestIds,
                accQuery::getAccSummaryListByManifestIds, AccSummaryRecord::accManifestId);
        Set<AccManifestId> basedAccManifestIds = accs.values().stream()
                .map(AccSummaryRecord::basedAccManifestId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<AccManifestId, AccSummaryRecord> basedAccs = resolve("acc:", basedAccManifestIds,
                accQuery::getAccSummaryListByManifestIds, AccSummaryRecord::accManifestId);

        Map<AccManifestId, List<AsccSummaryRecord>> asccs = resolveLists("ascc-from-acc:", accManifestIds,
                accQuery::getAsccSummaryListByFromAccManifestIds, AsccSummaryRecord::fromAccManifestId);
        Map<AccManifestId, List<BccSummaryRecord>> bccs = resolveLists("bcc-from-acc:", accManifestIds,
                accQuery::getBccSummaryListByFromAccManifestIds, BccSummaryRecord::fromAccManifestId);
        resolveLists("seq-key-from-acc:", accManifestIds,
                repositoryFactory.seqKeyQueryRepository(requester)::getSeqKeySummaryListByFromAccManifestIds,
                SeqKeySummaryRecord::fromAccManifestId);

        // A component without associations of its own is looked up under the component it was made from.
        Set<AccManifestId> previous = new LinkedHashSet<>(previousOf(accs, asccs));
        previous.addAll(previousOf(accs, bccs));
        resolveLists("ascc-from-acc:", previousOf(accs, asccs),
                accQuery::getAsccSummaryListByFromAccManifestIds, AsccSummaryRecord::fromAccManifestId);
        resolveLists("bcc-from-acc:", previousOf(accs, bccs),
                accQuery::getBccSummaryListByFromAccManifestIds, BccSummaryRecord::fromAccManifestId);

        Set<AsccpManifestId> asccpManifestIds = asccs.values().stream().flatMap(List::stream)
                .map(AsccSummaryRecord::toAsccpManifestId).collect(Collectors.toSet());
        Set<BccpManifestId> bccpManifestIds = bccs.values().stream().flatMap(List::stream)
                .map(BccSummaryRecord::toBccpManifestId).collect(Collectors.toSet());
        resolve("asccp:", asccpManifestIds,
                repositoryFactory.asccpQueryRepository(requester)::getAsccpSummaryListByManifestIds,
                AsccpSummaryRecord::asccpManifestId);
        resolve("bccp:", bccpManifestIds,
                repositoryFactory.bccpQueryRepository(requester)::getBccpSummaryListByManifestIds,
                BccpSummaryRecord::bccpManifestId);

        List<Pair<CcType, ManifestId>> tagged = new ArrayList<>();
        basedAccs.keySet().forEach(e -> tagged.add(Pair.of(CcType.ACC, e)));
        asccpManifestIds.forEach(e -> tagged.add(Pair.of(CcType.ASCCP, e)));
        bccpManifestIds.forEach(e -> tagged.add(Pair.of(CcType.BCCP, e)));
        resolveTags(tagged);
        return previous;
    }

    /** The components that were made from the ACCs that have no associations in {@code associationsByAcc}. */
    private static <T> Set<AccManifestId> previousOf(Map<AccManifestId, AccSummaryRecord> accs,
                                                     Map<AccManifestId, List<T>> associationsByAcc) {
        return accs.values().stream()
                .filter(acc -> associationsByAcc.getOrDefault(acc.accManifestId(), Collections.emptyList()).isEmpty())
                .map(AccSummaryRecord::prevAccManifestId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** @return the data types the ones without supplementary components stand in for */
    private Set<DtManifestId> prefetchDtChildren(List<DtManifestId> dtManifestIds) {
        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        Map<DtManifestId, List<DtScSummaryRecord>> dtScs = resolveLists("dt_sc-of-dt:", dtManifestIds,
                dtQuery::getDtScSummaryListByOwnerDtManifestIds, DtScSummaryRecord::ownerDtManifestId);

        // A data type without supplementary components of its own is looked up under the one it was made from.
        List<DtManifestId> withoutDtScs = dtScs.entrySet().stream()
                .filter(e -> e.getValue().isEmpty()).map(Map.Entry::getKey).collect(Collectors.toList());
        Set<DtManifestId> previous = resolve("dt:", withoutDtScs,
                dtQuery::getDtSummaryListByManifestIds, DtSummaryRecord::dtManifestId).values().stream()
                .map(DtSummaryRecord::prevDtManifestId).filter(Objects::nonNull).collect(Collectors.toSet());
        resolveLists("dt_sc-of-dt:", previous,
                dtQuery::getDtScSummaryListByOwnerDtManifestIds, DtScSummaryRecord::ownerDtManifestId);
        return previous;
    }

    private void prefetchAsccpChildren(List<AsccpManifestId> asccpManifestIds) {
        Map<AsccpManifestId, AsccpSummaryRecord> asccps = resolve("asccp:", asccpManifestIds,
                repositoryFactory.asccpQueryRepository(requester)::getAsccpSummaryListByManifestIds,
                AsccpSummaryRecord::asccpManifestId);
        Map<AccManifestId, AccSummaryRecord> accs = resolve("acc:",
                asccps.values().stream().map(AsccpSummaryRecord::roleOfAccManifestId).collect(Collectors.toSet()),
                repositoryFactory.accQueryRepository(requester)::getAccSummaryListByManifestIds,
                AccSummaryRecord::accManifestId);
        resolveTags(accs.keySet().stream()
                .map(e -> Pair.<CcType, ManifestId>of(CcType.ACC, e)).collect(Collectors.toList()));
    }

    private void prefetchBccpChildren(List<BccpManifestId> bccpManifestIds) {
        Map<BccpManifestId, BccpSummaryRecord> bccps = resolve("bccp:", bccpManifestIds,
                repositoryFactory.bccpQueryRepository(requester)::getBccpSummaryListByManifestIds,
                BccpSummaryRecord::bccpManifestId);
        Map<DtManifestId, DtSummaryRecord> dts = resolve("dt:",
                bccps.values().stream().map(BccpSummaryRecord::dtManifestId).collect(Collectors.toSet()),
                repositoryFactory.dtQueryRepository(requester)::getDtSummaryListByManifestIds,
                DtSummaryRecord::dtManifestId);
        resolveTags(dts.keySet().stream()
                .map(e -> Pair.<CcType, ManifestId>of(CcType.DT, e)).collect(Collectors.toList()));
    }

    /** The records for the ids: the cached ones, read together, and the missing ones read in batches (and cached). */
    @SuppressWarnings("unchecked")
    private <I extends ManifestId, R> Map<I, R> resolve(String prefix, Collection<I> ids,
                                                       Function<List<I>, List<R>> batchLoader, Function<R, I> idOf) {
        Map<I, R> resolved = new LinkedHashMap<>();
        Map<String, Object> hits = recallAll(ids.stream().map(e -> prefix + e.value()).collect(Collectors.toList()));
        List<I> missing = new ArrayList<>();
        for (I id : ids) {
            Object hit = hits.get(prefix + id.value());
            if (hit != null) {
                resolved.put(id, (R) hit);
            } else {
                missing.add(id);
            }
        }
        for (List<I> batch : partition(missing)) {
            Map<String, Object> loaded = new HashMap<>();
            for (R record : batchLoader.apply(batch)) {
                I id = idOf.apply(record);
                resolved.put(id, record);
                loaded.put(prefix + id.value(), record);
            }
            rememberAll(loaded);
        }
        return resolved;
    }

    /** The records under each parent id, an empty list for a parent without any; read like {@link #resolve}. */
    @SuppressWarnings("unchecked")
    private <I extends ManifestId, R> Map<I, List<R>> resolveLists(String prefix, Collection<I> parentIds,
                                                                  Function<List<I>, List<R>> batchLoader, Function<R, I> parentOf) {
        Map<I, List<R>> resolved = new LinkedHashMap<>();
        Map<String, Object> hits = recallAll(parentIds.stream().map(e -> prefix + e.value()).collect(Collectors.toList()));
        List<I> missing = new ArrayList<>();
        for (I parentId : parentIds) {
            Object hit = hits.get(prefix + parentId.value());
            if (hit != null) {
                resolved.put(parentId, (List<R>) hit);
            } else {
                missing.add(parentId);
            }
        }
        for (List<I> batch : partition(missing)) {
            Map<I, List<R>> loaded = new HashMap<>();
            for (R record : batchLoader.apply(batch)) {
                loaded.computeIfAbsent(parentOf.apply(record), k -> new ArrayList<>()).add(record);
            }
            Map<String, Object> remembered = new HashMap<>();
            for (I parentId : batch) {
                List<R> list = List.copyOf(loaded.getOrDefault(parentId, Collections.emptyList()));
                resolved.put(parentId, list);
                remembered.put(prefix + parentId.value(), list);
            }
            rememberAll(remembered);
        }
        return resolved;
    }

    private void resolveTags(List<Pair<CcType, ManifestId>> pairs) {
        Map<String, Object> hits = recallAll(pairs.stream().map(CachedCcDocument::tagKey).collect(Collectors.toList()));
        List<Pair<CcType, ManifestId>> missing = pairs.stream()
                .filter(e -> !hits.containsKey(tagKey(e))).collect(Collectors.toList());
        var tagQuery = repositoryFactory.tagQueryRepository(requester);
        for (List<Pair<CcType, ManifestId>> batch : partition(missing)) {
            Map<Pair<CcType, ManifestId>, List<TagSummaryRecord>> loaded =
                    tagQuery.getTagSummariesByPairsOfTypeAndManifestId(batch);
            Map<String, Object> remembered = new HashMap<>();
            for (Pair<CcType, ManifestId> pair : batch) {
                remembered.put(tagKey(pair), inTagOrder(loaded.getOrDefault(pair, Collections.emptyList())));
            }
            rememberAll(remembered);
        }
    }

    /** The tags in the order {@link CcDocumentImpl} lists them: by tag id, whatever order the database answered in. */
    static List<TagSummaryRecord> inTagOrder(List<TagSummaryRecord> tags) {
        return tags.stream().sorted(Comparator.comparing(e -> e.tagId().value())).collect(Collectors.toUnmodifiableList());
    }

    private static String tagKey(Pair<CcType, ManifestId> pair) {
        return "tags:" + pair.getLeft().name().toLowerCase(Locale.ROOT) + ":" + pair.getRight().value();
    }

    private <T> List<List<T>> partition(List<T> list) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    // ---- everything else: answered by the document that loads the releases ---------------------------------------

    // ---- the XBTs of the releases in scope: a list per release, which is what they are cached as -----------------------

    private volatile Map<XbtManifestId, XbtSummaryRecord> xbtIndex;
    private volatile Map<String, List<XbtSummaryRecord>> xbtIndexByName;

    private void indexXbts() {
        Map<XbtManifestId, XbtSummaryRecord> byId = new HashMap<>();
        Map<String, List<XbtSummaryRecord>> byName = new HashMap<>();
        var xbtQuery = repositoryFactory.xbtQueryRepository(requester);
        for (ReleaseId releaseId : scope().stream().sorted(Comparator.comparing(e -> e.value())).collect(Collectors.toList())) {
            List<XbtSummaryRecord> xbts = cached("xbts:" + releaseId.value(), () -> List.copyOf(xbtQuery.getXbtSummaryList(releaseId)));
            for (XbtSummaryRecord xbt : xbts) {
                byId.put(xbt.xbtManifestId(), xbt);
                byName.computeIfAbsent(xbt.name(), k -> new ArrayList<>()).add(xbt);
            }
        }
        xbtIndexByName = byName;
        xbtIndex = byId;
    }

    private Map<XbtManifestId, XbtSummaryRecord> xbtIndex() {
        if (xbtIndex == null) {
            indexXbts();
        }
        return xbtIndex;
    }

    private volatile Map<DtManifestId, DtSummaryRecord> dtIndex;

    /** The data types of the releases in scope: a list per release, which is what they are cached as. */
    private Map<DtManifestId, DtSummaryRecord> dtIndex() {
        Map<DtManifestId, DtSummaryRecord> index = dtIndex;
        if (index == null) {
            index = new HashMap<>();
            var dtQuery = repositoryFactory.dtQueryRepository(requester);
            for (ReleaseId releaseId : scope().stream().sorted(Comparator.comparing(e -> e.value())).collect(Collectors.toList())) {
                for (DtSummaryRecord dt : cached("dts:" + releaseId.value(), () -> List.copyOf(dtQuery.getDtSummaryList(releaseId)))) {
                    index.put(dt.dtManifestId(), dt);
                }
            }
            dtIndex = index;
        }
        return index;
    }

    private Map<String, List<XbtSummaryRecord>> xbtIndexByName() {
        xbtIndex();
        return xbtIndexByName;
    }

    @Override
    public Collection<AsccpSummaryRecord> getAsccpList() {
        throw new UnsupportedOperationException("The list of every ASCCP of the releases is answered from the loaded releases, by CacheFirstCcDocument.");
    }

    @Override
    public List<DtSummaryRecord> getDtList() {
        return new ArrayList<>(dtIndex().values());
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriList(DtManifestId dtManifestId) {
        if (dtManifestId == null || getDt(dtManifestId) == null) {
            return Collections.emptyList();
        }
        return cached("dt-awd-pri:" + dtManifestId.value(),
                () -> repositoryFactory.dtQueryRepository(requester).getDtAwdPriSummaryList(dtManifestId).stream()
                        // the query has no order of its own; the loaded releases list them by id
                        .sorted(Comparator.comparing(e -> e.dtAwdPriId().value())).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriList(DtScManifestId dtScManifestId) {
        if (dtScManifestId == null || getDtSc(dtScManifestId) == null) {
            return Collections.emptyList();
        }
        return cached("dt_sc-awd-pri:" + dtScManifestId.value(),
                () -> repositoryFactory.dtQueryRepository(requester).getDtScAwdPriSummaryList(dtScManifestId).stream()
                        .sorted(Comparator.comparing(e -> e.dtScAwdPriId().value())).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public XbtSummaryRecord getXbt(XbtManifestId xbtManifestId) {
        return xbtIndex().get(xbtManifestId);
    }

    @Override
    public List<XbtSummaryRecord> getXbtListByName(String name) {
        return xbtIndexByName().getOrDefault(name, Collections.emptyList());
    }

    @Override
    public List<AccSummaryRecord> getAccListByBasedAccManifestId(AccManifestId basedAccManifestId) {
        return eager().getAccListByBasedAccManifestId(basedAccManifestId);
    }

    @Override
    public List<AsccpSummaryRecord> getAsccpListByRoleOfAccManifestId(AccManifestId roleOfAccManifestId) {
        return eager().getAsccpListByRoleOfAccManifestId(roleOfAccManifestId);
    }

    @Override
    public List<AccSummaryRecord> getAccListByToAsccpManifestId(AsccpManifestId toAsccpManifestId) {
        return eager().getAccListByToAsccpManifestId(toAsccpManifestId);
    }

    @Override
    public List<AccSummaryRecord> getAccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return eager().getAccListByToBccpManifestId(toBccpManifestId);
    }

    @Override
    public List<BccpSummaryRecord> getBccpListByDtManifestId(DtManifestId dtManifestId) {
        return eager().getBccpListByDtManifestId(dtManifestId);
    }

    @Override
    public List<BccSummaryRecord> getBccListByToBccpManifestId(BccpManifestId toBccpManifestId) {
        return eager().getBccListByToBccpManifestId(toBccpManifestId);
    }

    @Override
    public NamespaceSummaryRecord getNamespace(NamespaceId namespaceId) {
        if (namespaceId == null) {
            return null;
        }
        return cached("namespace:" + namespaceId.value(),
                () -> repositoryFactory.namespaceQueryRepository(requester).getNamespaceSummary(namespaceId));
    }

    @Override
    public CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId) {
        if (codeListManifestId == null) {
            return null;
        }
        var query = repositoryFactory.codeListQueryRepository(requester);
        // the record carries no release; the release of a manifest never changes
        ReleaseId releaseId = cached("code-list-release:" + codeListManifestId.value(), () -> query.getCodeListReleaseId(codeListManifestId));
        if (releaseId == null || !scope().contains(releaseId)) {
            return null;
        }
        return cached("code-list:" + codeListManifestId.value(), () -> query.getCodeListSummary(codeListManifestId));
    }

    @Override
    public AgencyIdListSummaryRecord getAgencyIdList(AgencyIdListManifestId agencyIdListManifestId) {
        if (agencyIdListManifestId == null) {
            return null;
        }
        var query = repositoryFactory.agencyIdListQueryRepository(requester);
        ReleaseId releaseId = cached("agency-id-list-release:" + agencyIdListManifestId.value(), () -> query.getAgencyIdListReleaseId(agencyIdListManifestId));
        if (releaseId == null || !scope().contains(releaseId)) {
            return null;
        }
        return cached("agency-id-list:" + agencyIdListManifestId.value(), () -> query.getAgencyIdListSummary(agencyIdListManifestId));
    }

    @Override
    public BlobContentSummaryRecord getBlobContent(BlobContentManifestId blobContentManifestId) {
        throw new UnsupportedOperationException("A blob is answered from the loaded releases, by CacheFirstCcDocument.");
    }

}
