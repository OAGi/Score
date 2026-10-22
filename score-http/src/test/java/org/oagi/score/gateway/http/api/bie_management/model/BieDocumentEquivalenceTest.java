package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.common.TestDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.oagi.score.gateway.http.api.bie_management.model.abie.Abie;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.Asbie;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.Asbiep;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.Bbie;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieSc;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.Bbiep;
import org.oagi.score.gateway.http.api.bie_management.service.BieDocument;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitContext;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitResult;
import org.oagi.score.gateway.http.api.bie_management.service.BieVisitor;
import org.oagi.score.gateway.http.api.cc_management.cache.RedisCcCache;
import org.oagi.score.gateway.http.api.cc_management.cache.SpringCcCache;
import org.oagi.score.gateway.http.api.cc_management.model.CachedCcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocumentImpl;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;


import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Builds every BIE of the database twice over the same rows, once with the release read into a {@code CcDocumentImpl}
 * and once with a {@code CachedCcDocument}, and checks that the BIE document answers every question the same way and
 * that a traversal of it visits the same things along the same paths. It only runs when asked to:
 * {@code mvn test -Dtest=BieDocumentEquivalenceTest -Dscore.equivalence=true}.
 */
@EnabledIfSystemProperty(named = "score.equivalence", matches = "true")
class BieDocumentEquivalenceTest {

    /** Records every callback of a traversal with the ids and the paths it was given. */
    private static final class Trace implements BieVisitor {
        final List<String> events = new ArrayList<>();

        private static String where(BieVisitContext context) {
            return context.getOccurrencePath() + " | " + context.getParentOccurrencePath();
        }

        @Override
        public void visitStart(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {
            events.add("start " + topLevelAsbiep.topLevelAsbiepId() + " " + where(context));
        }

        @Override
        public void visitEnd(TopLevelAsbiepSummaryRecord topLevelAsbiep, BieVisitContext context) {
            events.add("end " + topLevelAsbiep.topLevelAsbiepId() + " " + where(context));
        }

        @Override
        public BieVisitResult visitAbie(Abie abie, BieVisitContext context) {
            events.add("abie " + abie.getAbieId() + " " + where(context));
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitAsbie(Asbie asbie, BieVisitContext context) {
            events.add("asbie " + asbie.getAsbieId() + " " + where(context));
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbie(Bbie bbie, BieVisitContext context) {
            events.add("bbie " + bbie.getBbieId() + " " + where(context));
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitAsbiep(Asbiep asbiep, BieVisitContext context) {
            events.add("asbiep " + asbiep.getAsbiepId() + " " + where(context));
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbiep(Bbiep bbiep, BieVisitContext context) {
            events.add("bbiep " + bbiep.getBbiepId() + " " + where(context));
            return BieVisitResult.CONTINUE;
        }

        @Override
        public BieVisitResult visitBbieSc(BbieSc bbieSc, BieVisitContext context) {
            events.add("bbieSc " + bbieSc.getBbieScId() + " " + where(context));
            return BieVisitResult.CONTINUE;
        }
    }

    private int compared;

    private void same(String what, Object expected, Object actual) {
        compared++;
        assertEquals(expected, actual, what);
    }

    private static List<Object> ids(Collection<?> associations) {
        List<Object> ids = new ArrayList<>();
        for (Object e : associations) {
            ids.add(e instanceof Asbie a ? "ASBIE-" + a.getAsbieId() : e instanceof Bbie b ? "BBIE-" + b.getBbieId() : e);
        }
        return ids;
    }

    private void compare(String name, BieSet bieSet, BieDocumentImpl expectedDocument, BieDocumentImpl actualDocument) {
        BieDocument expected = expectedDocument, actual = actualDocument;
        same(name + " root", expected.getRootAsbiep(), actual.getRootAsbiep());

        for (Abie abie : bieSet.getAbieList()) {
            same(name + " associations of ABIE " + abie.getAbieId(),
                    ids(expected.getAssociations(abie)), ids(actual.getAssociations(abie)));
        }
        for (Asbiep asbiep : bieSet.getAsbiepList()) {
            same(name + " ABIE of ASBIEP " + asbiep.getAsbiepId(), expected.getAbie(asbiep), actual.getAbie(asbiep));
        }
        for (Asbie asbie : bieSet.getAsbieList()) {
            same(name + " ASBIEP of ASBIE " + asbie.getAsbieId(), expected.getAsbiep(asbie), actual.getAsbiep(asbie));
        }
        for (Bbie bbie : bieSet.getBbieList()) {
            same(name + " BBIE " + bbie.getBbieId(), expected.getBbie(bbie.getBbieId()), actual.getBbie(bbie.getBbieId()));
            same(name + " BBIEP of BBIE " + bbie.getBbieId(), expected.getBbiep(bbie), actual.getBbiep(bbie));
            same(name + " BBIE_SCs of BBIE " + bbie.getBbieId(),
                    expected.getBbieScList(bbie).stream().map(BbieSc::getBbieScId).toList(),
                    actual.getBbieScList(bbie).stream().map(BbieSc::getBbieScId).toList());
        }
        for (BbieSc bbieSc : bieSet.getBbieScList()) {
            same(name + " BBIE_SC " + bbieSc.getBbieScId(),
                    expected.getBbieSc(bbieSc.getBbieScId()), actual.getBbieSc(bbieSc.getBbieScId()));
        }
        same(name + " reused ASBIEs", expected.getRefAsbieMap(), actual.getRefAsbieMap());

        Trace expectedTrace = new Trace(), actualTrace = new Trace();
        expected.accept(expectedTrace);
        actual.accept(actualTrace);
        same(name + " traversal", expectedTrace.events, actualTrace.events);
        assertTrue(expectedTrace.events.size() >= 2, name + " was not traversed");
    }

    @Test
    void everyBieIsReadTheSameOverTheCachedReleaseAsOverTheLoadedOne() {
        try (TestDataSource dataSource = TestDataSource.open(4)) {
            DSLContext dsl = DSL.using(dataSource, SQLDialect.MARIADB);

            RepositoryFactory repositoryFactory = new RepositoryFactory(dsl);
            LettuceConnectionFactory redis = null;
            String prefix = "zz-cc-equivalence-bie:" + java.util.UUID.randomUUID() + ":";
            if ("redis".equals(System.getProperty("score.equivalence.cache"))) {
                redis = new LettuceConnectionFactory(new RedisStandaloneConfiguration(
                        System.getProperty("score.redis.host", "localhost"), Integer.getInteger("score.redis.port", 6379)));
                redis.afterPropertiesSet();
                ReflectionTestUtils.setField(repositoryFactory, "ccCache",
                        new RedisCcCache(redis, prefix, org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady(), java.time.Duration.ofMinutes(30)));
            } else {
                CaffeineCacheManager manager = new CaffeineCacheManager();
                manager.setCacheSpecification("maximumSize=200000,expireAfterWrite=600s");
                ReflectionTestUtils.setField(repositoryFactory, "ccCache", new SpringCcCache(manager, org.oagi.score.gateway.http.api.cc_management.cache.CcCacheStatus.alwaysReady()));
            }

            int bies = 0;
            for (var row : dsl.fetch("select top_level_asbiep_id, owner_user_id, release_id from top_level_asbiep order by 1")) {
                TopLevelAsbiepId id = new TopLevelAsbiepId(row.get(0, ULong.class).toBigInteger());
                UserId owner = new UserId(row.get(1, ULong.class).toBigInteger());
                ReleaseId release = new ReleaseId(row.get(2, ULong.class).toBigInteger());
                ScoreUser requester = new ScoreUser(owner, "owner", "owner", null, false, null);

                for (boolean used : new boolean[]{true, false}) {
                    BieSet bieSet = repositoryFactory.bieQueryRepository(requester).getBieSet(id, used);

                    BieDocumentImpl loaded = new BieDocumentImpl(bieSet);
                    loaded.with(new CcDocumentImpl(requester, repositoryFactory, release));
                    BieDocumentImpl cached = new BieDocumentImpl(bieSet);
                    cached.with(new CachedCcDocument(requester, repositoryFactory, release));

                    compare("BIE " + id + (used ? " (used)" : " (all)"), bieSet, loaded, cached);
                }
                bies++;
            }
            if (redis != null) {
                try (var connection = redis.getConnection()) {
                    var keys = connection.keyCommands().keys((prefix + "*").getBytes());
                    if (keys != null && !keys.isEmpty()) {
                        connection.keyCommands().del(keys.toArray(new byte[0][]));
                    }
                }
                redis.destroy();
            }
            System.out.println("compared " + compared + " answers over " + bies + " BIEs, used only and all");
            assertTrue(bies > 0 && compared > 0);
        }
    }

}
