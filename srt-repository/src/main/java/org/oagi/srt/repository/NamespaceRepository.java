package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NamespaceRepository extends JpaRepository<Namespace, Long> {

    @Query("select n from Namespace n where n.uri = ?1")
    public Namespace findByUri(String uri);

    @Query("select n.namespaceId from Namespace n where n.uri = ?1")
    public int findNamespaceIdByUri(String uri);

    @Query("select case when count(n) > 0 then true else false end from Namespace n where n.uri = ?1 and n.namespaceId <> ?2")
    public boolean existsByUriExceptNamespaceId(String uri, long namespaceId);

    @Query("select case when count(n) > 0 then true else false end from Namespace n where n.prefix = ?1 and n.namespaceId <> ?2")
    public boolean existsByPrefixExceptNamespaceId(String prefix, long namespaceId);
}
