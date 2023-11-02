package org.oagi.score.repo.api;

import org.oagi.score.repo.api.agency.AgencyIdListReadRepository;
import org.oagi.score.repo.api.agency.AgencyIdListWriteRepository;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.BieReadRepository;
import org.oagi.score.repo.api.bie.BieWriteRepository;
import org.oagi.score.repo.api.businesscontext.*;
import org.oagi.score.repo.api.businessterm.BusinessTermAssignmentWriteRepository;
import org.oagi.score.repo.api.businessterm.BusinessTermReadRepository;
import org.oagi.score.repo.api.businessterm.BusinessTermWriteRepository;
import org.oagi.score.repo.api.configuration.ConfigurationWriteRepository;
import org.oagi.score.repo.api.corecomponent.CcReadRepository;
import org.oagi.score.repo.api.corecomponent.CodeListReadRepository;
import org.oagi.score.repo.api.corecomponent.ValueDomainReadRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyReadRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyWriteRepository;
import org.oagi.score.repo.api.message.MessageReadRepository;
import org.oagi.score.repo.api.message.MessageWriteRepository;
import org.oagi.score.repo.api.module.*;
import org.oagi.score.repo.api.openapidoc.BieForOasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.BieForOasDocWriteRepository;
import org.oagi.score.repo.api.openapidoc.OasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.OasDocWriteRepository;
import org.oagi.score.repo.api.release.ReleaseReadRepository;
import org.oagi.score.repo.api.user.ScoreUserReadRepository;

public interface ScoreRepositoryFactory {

    ConfigurationWriteRepository createConfigurationWriteRepository() throws ScoreDataAccessException;

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

    BusinessTermReadRepository createBusinessTermReadRepository() throws ScoreDataAccessException;
    BusinessTermWriteRepository createBusinessTermWriteRepository() throws ScoreDataAccessException;
    BusinessTermAssignmentWriteRepository createBusinessTermAssignmentWriteRepository() throws ScoreDataAccessException;

    OasDocReadRepository createOasDocReadRepository() throws ScoreDataAccessException;
    OasDocWriteRepository createOasDocWriteRepository() throws ScoreDataAccessException;
    BieForOasDocWriteRepository createBieForOasDocWriteRepository() throws ScoreDataAccessException;
    BieForOasDocReadRepository createBieForOasDocReadRepository() throws ScoreDataAccessException;
}
