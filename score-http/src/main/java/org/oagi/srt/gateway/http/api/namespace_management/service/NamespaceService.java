package org.oagi.srt.gateway.http.api.namespace_management.service;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.srt.entity.jooq.Tables;
import org.oagi.srt.gateway.http.api.namespace_management.data.Namespace;
import org.oagi.srt.gateway.http.api.namespace_management.data.NamespaceList;
import org.oagi.srt.gateway.http.api.namespace_management.data.SimpleNamespace;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NamespaceService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    public List<SimpleNamespace> getSimpleNamespaces() {
        return dslContext.select(Tables.NAMESPACE.NAMESPACE_ID, Tables.NAMESPACE.URI).from(Tables.NAMESPACE)
                .fetchInto(SimpleNamespace.class);
    }

    public List<NamespaceList> getNamespaceList(User user) {
        long userId = sessionService.userId(user);

        List<NamespaceList> namespaceLists = dslContext.select(Tables.NAMESPACE.fields())
                .select(Tables.APP_USER.LOGIN_ID.as("owner"))
                .from(Tables.NAMESPACE)
                .join(Tables.APP_USER)
                .on(Tables.NAMESPACE.OWNER_USER_ID.eq(Tables.APP_USER.APP_USER_ID))
                .fetchInto(NamespaceList.class);
        namespaceLists.stream().forEach(namespaceList -> {
            namespaceList.setCanEdit(namespaceList.getOwnerUserId() == userId);
        });

        return namespaceLists;
    }

    public Namespace getNamespace(User user, long namespaceId) {
        long userId = sessionService.userId(user);

        Namespace namespace =
                dslContext.select(Tables.NAMESPACE.fields()).from(Tables.NAMESPACE)
                        .where(Tables.NAMESPACE.NAMESPACE_ID.eq(ULong.valueOf(namespaceId)))
                        .fetchOneInto(Namespace.class);
        if (namespace.getOwnerUserId() != userId) {
            throw new AccessDeniedException("Access is denied");
        }
        return namespace;
    }

    public long getNamespaceIdByUri(String uri) {
        return dslContext.select(Tables.NAMESPACE.NAMESPACE_ID).from(Tables.NAMESPACE)
                .where(Tables.NAMESPACE.URI.eq(uri))
                .fetchOneInto(long.class);
    }

    @Transactional
    public void create(User user, Namespace namespace) {
        long userId = sessionService.userId(user);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        dslContext.insertInto(Tables.NAMESPACE,
                Tables.NAMESPACE.URI,
                Tables.NAMESPACE.CREATED_BY,
                Tables.NAMESPACE.LAST_UPDATED_BY,
                Tables.NAMESPACE.OWNER_USER_ID,
                Tables.NAMESPACE.CREATION_TIMESTAMP,
                Tables.NAMESPACE.DESCRIPTION,
                Tables.NAMESPACE.IS_STD_NMSP,
                Tables.NAMESPACE.LAST_UPDATE_TIMESTAMP,
                Tables.NAMESPACE.PREFIX).values(namespace.getUri(), ULong.valueOf(userId), ULong.valueOf(userId),
                ULong.valueOf(userId), timestamp, namespace.getDescription(), (byte) 0, timestamp,
                namespace.getPrefix()).execute();
    }

    @Transactional
    public void update(User user, Namespace namespace) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        int res = dslContext.update(Tables.NAMESPACE)
                .set(Tables.NAMESPACE.URI, namespace.getUri())
                .set(Tables.NAMESPACE.PREFIX, namespace.getPrefix())
                .set(Tables.NAMESPACE.DESCRIPTION, namespace.getDescription())
                .set(Tables.NAMESPACE.LAST_UPDATED_BY, userId)
                .set(Tables.NAMESPACE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(Tables.NAMESPACE.OWNER_USER_ID.eq(userId),
                        Tables.NAMESPACE.NAMESPACE_ID.eq(ULong.valueOf(namespace.getNamespaceId()))).execute();

        if (res != 1) {
            throw new AccessDeniedException("Access is denied");
        }
    }
}
