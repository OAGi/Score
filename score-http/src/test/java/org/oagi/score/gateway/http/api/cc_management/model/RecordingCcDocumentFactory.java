package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * A {@link CcDocumentFactory} whose documents write down what is asked of them: which methods, how often, and, for a
 * {@link CacheFirstCcDocument}, which call made it go over to the loaded releases. For classifying what a consumer does with its
 * document; it is not for timing (a proxy costs).
 */
public class RecordingCcDocumentFactory extends CcDocumentFactory {

    /** What happened to one document. */
    public static final class Doc {
        public final Map<String, LongAdder> calls = new ConcurrentHashMap<>();
        public volatile String switchedBy;
        public volatile boolean cacheFirst;
    }

    private final List<Doc> docs = Collections.synchronizedList(new ArrayList<>());

    public RecordingCcDocumentFactory(RepositoryFactory repositoryFactory) {
        super(repositoryFactory);
    }

    public List<Doc> docs() {
        synchronized (docs) {
            return new ArrayList<>(docs);
        }
    }

    public void clear() {
        docs.clear();
    }

    @Override
    public CcDocument create(ScoreUser requester, Collection<ReleaseId> releaseIds) {
        CcDocument target = super.create(requester, releaseIds);
        Doc doc = new Doc();
        doc.cacheFirst = target instanceof CacheFirstCcDocument;
        docs.add(doc);
        return (CcDocument) Proxy.newProxyInstance(CcDocument.class.getClassLoader(), new Class<?>[]{CcDocument.class}, (proxy, method, args) -> {
            doc.calls.computeIfAbsent(method.getName(), k -> new LongAdder()).increment();
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            } finally {
                if (doc.switchedBy == null && target instanceof CacheFirstCcDocument d && d.isLoaded()) {
                    doc.switchedBy = method.getName();
                }
            }
        });
    }
}
