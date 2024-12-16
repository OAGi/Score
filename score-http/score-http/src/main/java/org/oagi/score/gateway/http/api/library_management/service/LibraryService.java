package org.oagi.score.gateway.http.api.library_management.service;

import org.oagi.score.gateway.http.api.library_management.data.Library;
import org.oagi.score.gateway.http.api.library_management.data.LibraryList;
import org.oagi.score.gateway.http.api.library_management.data.LibraryListRequest;
import org.oagi.score.gateway.http.api.library_management.repository.LibraryRepository;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.service.common.data.PageResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LibraryService implements InitializingBean {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LibraryRepository libraryRepository;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public List<Library> getLibraries() {
        return libraryRepository.getLibraries();
    }

    public Library getLibraryById(AuthenticatedPrincipal user, BigInteger libraryId) {
        return libraryRepository.getLibraryById(libraryId);
    }

    @Transactional
    public BigInteger create(AuthenticatedPrincipal user, Library library) {
        return libraryRepository.create(sessionService.asScoreUser(user), library);
    }

    @Transactional
    public void update(AuthenticatedPrincipal user, Library library) {
        libraryRepository.update(sessionService.asScoreUser(user), library);
    }

    @Transactional
    public void discard(AuthenticatedPrincipal user, BigInteger libraryId) {
        libraryRepository.discard(sessionService.asScoreUser(user), libraryId);
    }

    public PageResponse<LibraryList> getLibraryList(AuthenticatedPrincipal user, LibraryListRequest request) {
        return libraryRepository.fetch(sessionService.asScoreUser(user), request);
    }

}
