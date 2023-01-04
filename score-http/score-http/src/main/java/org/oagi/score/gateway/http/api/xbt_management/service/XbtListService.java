package org.oagi.score.gateway.http.api.xbt_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.Xbt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.XBT;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.XBT_MANIFEST;

@Service
@Transactional(readOnly = true)
public class XbtListService {

    @Autowired
    private DSLContext dslContext;

    public List<Xbt> getXbtSimpleList(AuthenticatedPrincipal user, BigInteger releaseId) {

        return dslContext.select(XBT.XBT_ID,
                XBT_MANIFEST.XBT_MANIFEST_ID.as("manifestId"),
                XBT.NAME,
                XBT.BUILTIN_TYPE,
                XBT.JBT_DRAFT05_MAP,
                XBT.OPENAPI30_MAP,
                XBT.SUBTYPE_OF_XBT_ID,
                XBT.SCHEMA_DEFINITION,
                XBT_MANIFEST.RELEASE_ID,
                XBT.CREATED_BY,
                XBT.OWNER_USER_ID,
                XBT.LAST_UPDATED_BY,
                XBT.CREATION_TIMESTAMP,
                XBT.LAST_UPDATE_TIMESTAMP,
                XBT.IS_DEPRECATED
            ).from(XBT_MANIFEST)
                .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                .where(XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(Xbt.class);
    }
}
