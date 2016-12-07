package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NamespaceRepository extends JpaRepository<Namespace, Long> {

    @Query("select n from Namespace n where n.uri = ?1")
    public Namespace findByUri(String uri);

    @Query("select n.namespaceId from Namespace n where n.uri = ?1")
    public int findNamespaceIdByUri(String uri);

}
