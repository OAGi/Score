package org.oagi.score.gateway.http.api.library_management.service;

import org.jooq.DSLContext;
import org.oagi.score.gateway.http.api.library_management.data.Library;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.LIBRARY;

@Service
@Transactional(readOnly = true)
public class LibraryService implements InitializingBean {

    @Autowired
    private DSLContext dslContext;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public List<Library> getLibraries() {
        return dslContext.selectFrom(LIBRARY).fetchInto(Library.class);
    }
}
