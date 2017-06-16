package org.oagi.srt.service;

import org.oagi.srt.repository.JpaRepositoryDefinitionHelper;
import org.oagi.srt.repository.NamespaceRepository;
import org.oagi.srt.repository.entity.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NamespaceService {

    @Autowired
    private NamespaceRepository namespaceRepository;

    @Autowired
    private JpaRepositoryDefinitionHelper jpaRepositoryDefinitionHelper;

    public List<Namespace> findAll(Sort.Direction direction, String property) {
        return namespaceRepository.findAll(new Sort(direction, property));
    }

    public List<Namespace> findAll() {
        return namespaceRepository.findAll();
    }

    public Namespace findById(long namespaceId) {
        return namespaceRepository.findOne(namespaceId);
    }

    @Transactional
    public void update(Namespace namespace) {
        jpaRepositoryDefinitionHelper.saveAndFlush(namespace);
    }

    public boolean isExistsUri(String uri, long namespaceId) {
        return namespaceRepository.existsByUriExceptNamespaceId((uri != null) ? uri.trim() : null, namespaceId);
    }

    public boolean isExistsPrefix(String prefix, long namespaceId) {
        return namespaceRepository.existsByPrefixExceptNamespaceId((prefix != null) ? prefix.trim() : null, namespaceId);
    }
}
