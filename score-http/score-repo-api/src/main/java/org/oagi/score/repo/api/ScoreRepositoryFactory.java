package org.oagi.score.repo.api;

import org.oagi.score.repo.api.agency.AgencyIdListReadRepository;
import org.oagi.score.repo.api.agency.AgencyIdListWriteRepository;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.BieReadRepository;
import org.oagi.score.repo.api.bie.BieWriteRepository;
import org.oagi.score.repo.api.businesscontext.*;
import org.oagi.score.repo.api.corecomponent.CcReadRepository;
import org.oagi.score.repo.api.corecomponent.CodeListReadRepository;
import org.oagi.score.repo.api.corecomponent.ValueDomainReadRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyReadRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyWriteRepository;
import org.oagi.score.repo.api.message.MessageReadRepository;
import org.oagi.score.repo.api.message.MessageWriteRepository;
import org.oagi.score.repo.api.module.*;
import org.oagi.score.repo.api.release.ReleaseReadRepository;
import org.oagi.score.repo.api.user.ScoreUserReadRepository;

public interface ScoreRepositoryFactory {

    ScoreUserReadRepository createScoreUserReadRepository() throws ScoreDataAccessException;

    ContextCategoryReadRepository createContextCategoryReadRepository() throws ScoreDataAccessException;
    ContextCategoryWriteRepository createContextCategoryWriteRepository() throws ScoreDataAccessException;

    ContextSchemeReadRepository createContextSchemeReadRepository() throws ScoreDataAccessException;
    ContextSchemeWriteRepository createContextSchemeWriteRepository() throws ScoreDataAccessException;

    BusinessContextReadRepository createBusinessContextReadRepository() throws ScoreDataAccessException;
    BusinessContextWriteRepository createBusinessContextWriteRepository() throws ScoreDataAccessException;

    ReleaseReadRepository createReleaseReadRepository() throws ScoreDataAccessException;

    SeqKeyReadRepository createSeqKeyReadRepository() throws ScoreDataAccessException;
    SeqKeyWriteRepository createSeqKeyWriteRepository() throws ScoreDataAccessException;

    CcReadRepository createCcReadRepository() throws ScoreDataAccessException;
    CodeListReadRepository createCodeListReadRepository() throws ScoreDataAccessException;

    ValueDomainReadRepository createValueDomainReadRepository() throws ScoreDataAccessException;

    BieReadRepository createBieReadRepository() throws ScoreDataAccessException;
    BieWriteRepository createBieWriteRepository() throws ScoreDataAccessException;

    ModuleWriteRepository createModuleWriteRepository() throws ScoreDataAccessException;

    ModuleSetReadRepository createModuleSetReadRepository() throws ScoreDataAccessException;
    ModuleSetWriteRepository createModuleSetWriteRepository() throws ScoreDataAccessException;

    ModuleSetReleaseReadRepository createModuleSetReleaseReadRepository() throws ScoreDataAccessException;
    ModuleSetReleaseWriteRepository createModuleSetReleaseWriteRepository() throws ScoreDataAccessException;

    AgencyIdListReadRepository createAgencyIdListReadRepository() throws  ScoreDataAccessException;
    AgencyIdListWriteRepository createAgencyIdListWriteRepository() throws  ScoreDataAccessException;

    MessageReadRepository createMessageReadRepository() throws ScoreDataAccessException;
    MessageWriteRepository createMessageWriteRepository() throws ScoreDataAccessException;

}
