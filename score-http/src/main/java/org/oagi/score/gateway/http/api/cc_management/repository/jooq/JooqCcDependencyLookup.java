package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.oagi.score.gateway.http.cache.RecordCacheProperties;


import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.propagation.CcDependencyLookup;
import org.oagi.score.gateway.http.api.cc_management.propagation.Dependent;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.Id;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

/**
 * Answers {@link CcDependencyLookup} with one query per batch of ids; the columns it searches are indexed. A manifest never refers to
 * another release, so no lookup needs the release.
 */
@Component
public class JooqCcDependencyLookup implements CcDependencyLookup {

    /** The parent column of a kind that has none, typed so that the driver does not report a column of the type NULL. */
    private static final Field<ULong> NO_PARENT = DSL.inline((ULong) null, SQLDataType.BIGINTUNSIGNED);

    private final DSLContext dslContext;
    private final int batchSize;

    /** The same connection and settings as everywhere, without the log of every statement; the batch size is that of the cache. */
    @Autowired
    public JooqCcDependencyLookup(DSLContext dslContext, RecordCacheProperties properties) {
        this(dslContext.configuration().derive(dslContext.settings().withExecuteLogging(false)).dsl(), properties.getBatchSize());
    }

    /** @param dslContext one that does not log every statement: a change that reaches many records asks many times */
    public JooqCcDependencyLookup(DSLContext dslContext, int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be at least 1: " + batchSize);
        }
        this.dslContext = dslContext;
        this.batchSize = batchSize;
    }

    @Override
    public List<Dependent<AccManifestId, AsccpManifestId>> asccpsRoleOf(Collection<AccManifestId> accIds) {
        return find(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID, AsccpManifestId::new, null,
                ASCCP_MANIFEST.RELEASE_ID, accIds);
    }

    @Override
    public List<Dependent<AsccpManifestId, AsccManifestId>> asccsToAsccp(Collection<AsccpManifestId> asccpIds) {
        return find(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID, ASCC_MANIFEST.ASCC_MANIFEST_ID, AsccManifestId::new,
                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID, ASCC_MANIFEST.RELEASE_ID, asccpIds);
    }

    @Override
    public List<Dependent<AccManifestId, AsccManifestId>> asccsFromAcc(Collection<AccManifestId> accIds) {
        return find(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID, ASCC_MANIFEST.ASCC_MANIFEST_ID, AsccManifestId::new,
                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID, ASCC_MANIFEST.RELEASE_ID, accIds);
    }

    @Override
    public List<Dependent<BccpManifestId, BccManifestId>> bccsToBccp(Collection<BccpManifestId> bccpIds) {
        return find(BCC_MANIFEST.TO_BCCP_MANIFEST_ID, BCC_MANIFEST.BCC_MANIFEST_ID, BccManifestId::new,
                BCC_MANIFEST.FROM_ACC_MANIFEST_ID, BCC_MANIFEST.RELEASE_ID, bccpIds);
    }

    @Override
    public List<Dependent<AccManifestId, BccManifestId>> bccsFromAcc(Collection<AccManifestId> accIds) {
        return find(BCC_MANIFEST.FROM_ACC_MANIFEST_ID, BCC_MANIFEST.BCC_MANIFEST_ID, BccManifestId::new,
                BCC_MANIFEST.FROM_ACC_MANIFEST_ID, BCC_MANIFEST.RELEASE_ID, accIds);
    }

    @Override
    public List<Dependent<DtManifestId, BccpManifestId>> bccpsOfDt(Collection<DtManifestId> dtIds) {
        return find(BCCP_MANIFEST.BDT_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID, BccpManifestId::new, null,
                BCCP_MANIFEST.RELEASE_ID, dtIds);
    }

    /**
     * @param sourceColumn the column that holds the id of the record the dependent derives from
     * @param idColumn     the id of the dependent
     * @param idOf         makes the id of a dependent from the number the database gave
     * @param parentColumn the column with the id of the ACC that owns the dependent, or null when it has none
     */
    private <S extends Id, D extends Id> List<Dependent<S, D>> find(
            TableField<?, ULong> sourceColumn, TableField<?, ULong> idColumn, Function<BigInteger, D> idOf,
            TableField<?, ULong> parentColumn, TableField<?, ULong> releaseColumn, Collection<S> sources) {
        List<Dependent<S, D>> dependents = new ArrayList<>();
        List<S> all = new ArrayList<>(sources);
        for (int from = 0; from < all.size(); from += batchSize) {
            List<S> batch = all.subList(from, Math.min(from + batchSize, all.size()));
            Map<BigInteger, S> byValue = new HashMap<>();
            batch.forEach(id -> byValue.put(id.value(), id));
            dslContext.select(sourceColumn, idColumn, (parentColumn != null) ? parentColumn : NO_PARENT, releaseColumn)
                    .from(sourceColumn.getTable())
                    .where(sourceColumn.in(batch.stream().map(id -> ULong.valueOf(id.value())).toList()))
                    .fetch(r -> dependents.add(new Dependent<>(
                            byValue.get(r.get(0, ULong.class).toBigInteger()),
                            idOf.apply(r.get(1, ULong.class).toBigInteger()),
                            (r.get(2, ULong.class) != null) ? new AccManifestId(r.get(2, ULong.class).toBigInteger()) : null,
                            (r.get(3, ULong.class) != null) ? new ReleaseId(r.get(3, ULong.class).toBigInteger()) : null)));
        }
        return dependents;
    }
}
