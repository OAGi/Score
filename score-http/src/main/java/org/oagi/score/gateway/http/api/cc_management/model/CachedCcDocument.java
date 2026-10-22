package org.oagi.score.gateway.http.api.cc_management.model;

import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.cache.CcCache;
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
 * (the associations of an ACC and their order, the supplementary components of a DT, the tags); the other lookups, the ones that scan
 * the release or look for the users of a component, are answered by a {@link CcDocumentImpl} that is built the first
 * time one of them is called.
 */
public class CachedCcDocument implements CcDocument {

    /** The most ids sent to the database in one {@code IN} list. */
    private static final int BATCH_SIZE = 500;

    /**
     * How many components this document has had to read one by one before it reads the graph of the first of them at
     * once: a document that walks a tree finds nothing cached the first time, and a query per component would take
     * seconds where the graph takes a few hundred milliseconds, while one that looks up a few components has no use
     * for a graph.
     */
    private static final int MISSES_BEFORE_READING_THE_GRAPH = 16;

    private final ScoreUser requester;
    private final RepositoryFactory repositoryFactory;
    private final CcCache cache;
    private final List<ReleaseId> releaseIdList;

    /** The generation of the cache this document reads and writes in, pinned when it was made. */
    private final long generation;

    /**
     * What this document has already got from the cache or the database. A graph reads the same entries thousands of
     * times, and when the cache is not in the JVM each of those reads would be a round trip. It lives as long as the
     * document, which is as long as one operation.
     */
    private final ConcurrentMap<String, Object> memo = new ConcurrentHashMap<>();

    private volatile Set<ReleaseId> scope;
    private final LoadOnce<CcDocument> eagerDocument;

    private final AtomicInteger misses = new AtomicInteger();
    private final AtomicReference<Pair<CcType, ManifestId>> firstMiss = new AtomicReference<>();

    public CachedCcDocument(ScoreUser requester, RepositoryFactory repositoryFactory, ReleaseId releaseId) {
        this(requester, repositoryFactory, Arrays.asList(releaseId));
    }

    public CachedCcDocument(ScoreUser requester, RepositoryFactory repositoryFactory, Collection<ReleaseId> releaseIdList) {
        this.requester = requester;
        this.repositoryFactory = repositoryFactory;
        this.cache = repositoryFactory.ccCache();
        this.generation = cache.generation();
        this.releaseIdList = new ArrayList<>(releaseIdList);
        this.eagerDocument = new LoadOnce<>(() -> new CcDocumentImpl(requester, repositoryFactory, this.releaseIdList));
    }

    // ---- the cache -----------------------------------------------------------------------------------------------

    private <T> T cached(String key, Supplier<T> loader) {
        return cached(key, loader, null);
    }

    /** @param onMiss what to do when the cache has nothing under the key, before the value is read */
    @SuppressWarnings("unchecked")
    private <T> T cached(String key, Supplier<T> loader, Runnable onMiss) {
        boolean bypassed = cache.isBypassed();
        if (!bypassed) {
            Object hit = recall(key);
            if (hit != null) {
                return (T) hit;
            }
            if (onMiss != null) {
                onMiss.run();
                hit = recall(key);
                if (hit != null) {
                    return (T) hit;
                }
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
            value = cache.get(generation, key);
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
            Map<String, Object> fromCache = cache.getAll(generation, missing);
            memo.putAll(fromCache);
            found.putAll(fromCache);
        }
        return found;
    }

    private void remember(String key, Object value) {
        cache.put(generation, key, value);
        memo.put(key, value);
    }

    private void rememberAll(Map<String, Object> values) {
        if (!values.isEmpty()) {
            cache.putAll(generation, values);
            memo.putAll(values);
        }
    }

    private void noteMiss(CcType type, ManifestId manifestId) {
        firstMiss.compareAndSet(null, Pair.of(type, manifestId));
        if (misses.incrementAndGet() == MISSES_BEFORE_READING_THE_GRAPH) {
            Pair<CcType, ManifestId> first = firstMiss.get();
            try {
                prefetchGraph(first.getLeft(), first.getRight());
            } catch (RuntimeException e) {
                // Only a speed-up: what could not be read now is read, or fails, where it is looked up.
            }
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

    private CcDocument eager() {
        return eagerDocument.get();
    }

    // ---- lookups by manifest id ----------------------------------------------------------------------------------

    @Override
    public AccSummaryRecord getAcc(AccManifestId accManifestId) {
        if (accManifestId == null) {
            return null;
        }
        return inScope(cached("acc:" + accManifestId.value(),
                () -> repositoryFactory.accQueryRepository(requester).getAccSummary(accManifestId),
                () -> noteMiss(CcType.ACC, accManifestId)),
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
        return inScope(cached("asccp:" + asccpManifestId.value(),
                () -> repositoryFactory.asccpQueryRepository(requester).getAsccpSummary(asccpManifestId),
                () -> noteMiss(CcType.ASCCP, asccpManifestId)),
                AsccpSummaryRecord::release);
    }

    @Override
    public BccpSummaryRecord getBccp(BccpManifestId bccpManifestId) {
        if (bccpManifestId == null) {
            return null;
        }
        return inScope(cached("bccp:" + bccpManifestId.value(),
                () -> repositoryFactory.bccpQueryRepository(requester).getBccpSummary(bccpManifestId),
                () -> noteMiss(CcType.BCCP, bccpManifestId)),
                BccpSummaryRecord::release);
    }

    @Override
    public DtSummaryRecord getDt(DtManifestId dtManifestId) {
        if (dtManifestId == null) {
            return null;
        }
        return inScope(cached("dt:" + dtManifestId.value(),
                () -> repositoryFactory.dtQueryRepository(requester).getDtSummary(dtManifestId),
                () -> noteMiss(CcType.DT, dtManifestId)),
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

        // The ASCCs and BCCs of the ACC are in the cache as lists (and are read together with the graph).
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

    @Override
    public void prefetchGraph(CcType type, ManifestId root) {
        if (cache.isBypassed() || root == null) {
            return;
        }
        String marker = "graph:" + type.name().toLowerCase(Locale.ROOT) + ":" + root.value();
        if (!readGraph(marker)) {
            cache.singleFlight(marker, () -> {
                if (!readGraph(marker)) {
                    loadGraph(type, root, marker);
                }
            });
            readGraph(marker);
        }
    }

    /** Reads the whole graph that the marker stands for, in a few bulk reads. @return whether there was one */
    private boolean readGraph(String marker) {
        Object found = recall(marker);
        if (!(found instanceof GraphClosure closure)) {
            return false;
        }
        List<String> keys = new ArrayList<>();
        for (long id : closure.accs()) {
            keys.add("acc:" + id);
            keys.add("ascc-from-acc:" + id);
            keys.add("bcc-from-acc:" + id);
            keys.add("seq-key-from-acc:" + id);
            keys.add("tags:acc:" + id);
        }
        for (long id : closure.asccps()) {
            keys.add("asccp:" + id);
            keys.add("tags:asccp:" + id);
        }
        for (long id : closure.bccps()) {
            keys.add("bccp:" + id);
            keys.add("tags:bccp:" + id);
        }
        for (long id : closure.dts()) {
            keys.add("dt:" + id);
            keys.add("dt_sc-of-dt:" + id);
            keys.add("tags:dt:" + id);
        }
        for (long id : closure.previousAccs()) {
            keys.add("ascc-from-acc:" + id);
            keys.add("bcc-from-acc:" + id);
        }
        for (long id : closure.previousDts()) {
            keys.add("dt_sc-of-dt:" + id);
        }
        recallAll(keys);
        return true;
    }

    private void loadGraph(CcType type, ManifestId root, String marker) {
        GraphManifestIds reached = repositoryFactory.ccQueryRepository(requester).getGraphManifestIds(type, root);
        Set<AccManifestId> previousAccs = prefetchAccChildren(List.copyOf(reached.accs()));
        prefetchAsccpChildren(List.copyOf(reached.asccps()));
        prefetchBccpChildren(List.copyOf(reached.bccps()));
        Set<DtManifestId> previousDts = prefetchDtChildren(List.copyOf(reached.dts()));
        resolveTags(List.of(Pair.of(type, root)));
        try {
            remember(marker, new GraphClosure(
                    ids(reached.accs()), ids(reached.asccps()), ids(reached.bccps()), ids(reached.dts()),
                    ids(previousAccs), ids(previousDts)));
        } catch (ArithmeticException e) {
            // An id that does not fit a long: the graph is cached all the same, only not as a whole.
        }
    }

    private static long[] ids(Collection<? extends ManifestId> manifestIds) {
        return manifestIds.stream().mapToLong(e -> e.value().longValueExact()).toArray();
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

    private static <T> List<List<T>> partition(List<T> list) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += BATCH_SIZE) {
            batches.add(list.subList(i, Math.min(i + BATCH_SIZE, list.size())));
        }
        return batches;
    }

    // ---- everything else: answered by the document that loads the releases ---------------------------------------

    @Override
    public Collection<AsccpSummaryRecord> getAsccpList() {
        return eager().getAsccpList();
    }

    @Override
    public List<DtSummaryRecord> getDtList() {
        return eager().getDtList();
    }

    @Override
    public List<DtAwdPriSummaryRecord> getDtAwdPriList(DtManifestId dtManifestId) {
        return eager().getDtAwdPriList(dtManifestId);
    }

    @Override
    public List<DtScAwdPriSummaryRecord> getDtScAwdPriList(DtScManifestId dtScManifestId) {
        return eager().getDtScAwdPriList(dtScManifestId);
    }

    @Override
    public XbtSummaryRecord getXbt(XbtManifestId xbtManifestId) {
        return eager().getXbt(xbtManifestId);
    }

    @Override
    public List<XbtSummaryRecord> getXbtListByName(String name) {
        return eager().getXbtListByName(name);
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
        return eager().getNamespace(namespaceId);
    }

    @Override
    public CodeListSummaryRecord getCodeList(CodeListManifestId codeListManifestId) {
        return eager().getCodeList(codeListManifestId);
    }

    @Override
    public AgencyIdListSummaryRecord getAgencyIdList(AgencyIdListManifestId agencyIdListManifestId) {
        return eager().getAgencyIdList(agencyIdListManifestId);
    }

    @Override
    public BlobContentSummaryRecord getBlobContent(BlobContentManifestId blobContentManifestId) {
        return eager().getBlobContent(blobContentManifestId);
    }

}
