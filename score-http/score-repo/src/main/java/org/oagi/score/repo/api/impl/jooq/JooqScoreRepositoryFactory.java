package org.oagi.score.repo.api.impl.jooq;

import org.jooq.DSLContext;
import org.oagi.score.repo.api.ScoreRepositoryFactory;
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
import org.oagi.score.repo.api.impl.jooq.agency.JooqAgencyIdListReadRepository;
import org.oagi.score.repo.api.impl.jooq.agency.JooqAgencyIdListWriteRepository;
import org.oagi.score.repo.api.impl.jooq.bie.JooqBieReadRepository;
import org.oagi.score.repo.api.impl.jooq.bie.JooqBieWriteRepository;
import org.oagi.score.repo.api.impl.jooq.businesscontext.*;
import org.oagi.score.repo.api.impl.jooq.businessterm.JooqBusinessTermAssignmentWriteRepository;
import org.oagi.score.repo.api.impl.jooq.businessterm.JooqBusinessTermReadRepository;
import org.oagi.score.repo.api.impl.jooq.businessterm.JooqBusinessTermWriteRepository;
import org.oagi.score.repo.api.impl.jooq.configuration.JooqConfigurationWriteRepository;
import org.oagi.score.repo.api.impl.jooq.corecomponent.*;
import org.oagi.score.repo.api.impl.jooq.message.JooqMessageReadRepository;
import org.oagi.score.repo.api.impl.jooq.message.JooqMessageWriteRepository;
import org.oagi.score.repo.api.impl.jooq.module.*;
import org.oagi.score.repo.api.impl.jooq.openapidoc.JooqOasDocReadRepository;
import org.oagi.score.repo.api.impl.jooq.openapidoc.JooqOasDocWriteRepository;
import org.oagi.score.repo.api.impl.jooq.release.JooqReleaseReadRepository;
import org.oagi.score.repo.api.impl.jooq.user.JooqScoreUserReadRepository;
import org.oagi.score.repo.api.message.MessageReadRepository;
import org.oagi.score.repo.api.message.MessageWriteRepository;
import org.oagi.score.repo.api.module.*;
import org.oagi.score.repo.api.openapidoc.OasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.OasDocWriteRepository;
import org.oagi.score.repo.api.release.ReleaseReadRepository;
import org.oagi.score.repo.api.user.ScoreUserReadRepository;

public class JooqScoreRepositoryFactory implements ScoreRepositoryFactory {

    private final DSLContext dslContext;

    public JooqScoreRepositoryFactory(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public final DSLContext getDslContext() {
        return dslContext;
    }

    @Override
    public ConfigurationWriteRepository createConfigurationWriteRepository() throws ScoreDataAccessException {
        return new JooqConfigurationWriteRepository(this.dslContext);
    }

    @Override
    public ScoreUserReadRepository createScoreUserReadRepository() throws ScoreDataAccessException {
        return new JooqScoreUserReadRepository(this.dslContext);
    }

    @Override
    public ContextCategoryReadRepository createContextCategoryReadRepository() throws ScoreDataAccessException {
        return new JooqContextCategoryReadRepository(this.dslContext);
    }

    @Override
    public ContextCategoryWriteRepository createContextCategoryWriteRepository() throws ScoreDataAccessException {
        return new JooqContextCategoryWriteRepository(this.dslContext);
    }

    @Override
    public ContextSchemeReadRepository createContextSchemeReadRepository() throws ScoreDataAccessException {
        return new JooqContextSchemeReadRepository(this.dslContext);
    }

    @Override
    public ContextSchemeWriteRepository createContextSchemeWriteRepository() throws ScoreDataAccessException {
        return new JooqContextSchemeWriteRepository(this.dslContext);
    }

    @Override
    public BusinessContextReadRepository createBusinessContextReadRepository() throws ScoreDataAccessException {
        return new JooqBusinessContextReadRepository(this.dslContext);
    }

    @Override
    public BusinessContextWriteRepository createBusinessContextWriteRepository() throws ScoreDataAccessException {
        return new JooqBusinessContextWriteRepository(this.dslContext);
    }

    @Override
    public ReleaseReadRepository createReleaseReadRepository() throws ScoreDataAccessException {
        return new JooqReleaseReadRepository(this.dslContext);
    }

    @Override
    public SeqKeyReadRepository createSeqKeyReadRepository() throws ScoreDataAccessException {
        return new JooqSeqKeyReadRepository(this.dslContext);
    }

    @Override
    public SeqKeyWriteRepository createSeqKeyWriteRepository() throws ScoreDataAccessException {
        return new JooqSeqKeyWriteRepository(this.dslContext);
    }

    @Override
    public CcReadRepository createCcReadRepository() throws ScoreDataAccessException {
        return new JooqCcReadRepository(this.dslContext);
    }

    @Override
    public CodeListReadRepository createCodeListReadRepository() throws ScoreDataAccessException {
        return new JooqCodeListReadRepository(this.dslContext);
    }

    @Override
    public ValueDomainReadRepository createValueDomainReadRepository() throws ScoreDataAccessException {
        return new JooqValueDomainReadRepository(this.dslContext);
    }

    @Override
    public BieReadRepository createBieReadRepository() throws ScoreDataAccessException {
        return new JooqBieReadRepository(this.dslContext);
    }

    @Override
    public BieWriteRepository createBieWriteRepository() throws ScoreDataAccessException {
        return new JooqBieWriteRepository(this.dslContext);
    }

    @Override
    public ModuleWriteRepository createModuleWriteRepository() throws ScoreDataAccessException {
        return new JooqModuleWriteRepository(this.dslContext);
    }

    @Override
    public ModuleSetReadRepository createModuleSetReadRepository() throws ScoreDataAccessException {
        return new JooqModuleSetReadRepository(this.dslContext);
    }

    @Override
    public ModuleSetWriteRepository createModuleSetWriteRepository() throws ScoreDataAccessException {
        return new JooqModuleSetWriteRepository(this.dslContext);
    }

    @Override
    public ModuleSetReleaseReadRepository createModuleSetReleaseReadRepository() throws ScoreDataAccessException {
        return new JooqModuleSetReleaseReadRepository(this.dslContext);
    }

    @Override
    public ModuleSetReleaseWriteRepository createModuleSetReleaseWriteRepository() throws ScoreDataAccessException {
        return new JooqModuleSetReleaseWriteRepository(this.dslContext);
    }

    @Override
    public AgencyIdListReadRepository createAgencyIdListReadRepository() throws ScoreDataAccessException {
        return new JooqAgencyIdListReadRepository(this.dslContext);
    }

    @Override
    public AgencyIdListWriteRepository createAgencyIdListWriteRepository() throws ScoreDataAccessException {
        return new JooqAgencyIdListWriteRepository(this.dslContext);
    }

    @Override
    public MessageReadRepository createMessageReadRepository() throws ScoreDataAccessException {
        return new JooqMessageReadRepository(this.dslContext);
    }

    @Override
    public MessageWriteRepository createMessageWriteRepository() throws ScoreDataAccessException {
        return new JooqMessageWriteRepository(this.dslContext);
    }

    @Override
    public BusinessTermReadRepository createBusinessTermReadRepository() throws ScoreDataAccessException {
        return new JooqBusinessTermReadRepository(this.dslContext);
    }

    @Override
    public BusinessTermWriteRepository createBusinessTermWriteRepository() throws ScoreDataAccessException {
        return new JooqBusinessTermWriteRepository(this.dslContext);
    }

    @Override
    public BusinessTermAssignmentWriteRepository createBusinessTermAssignmentWriteRepository() throws ScoreDataAccessException {
        return new JooqBusinessTermAssignmentWriteRepository(this.dslContext);
    }

    @Override
    public OasDocReadRepository createOasDocReadRepository() throws ScoreDataAccessException {
        return new JooqOasDocReadRepository(this.dslContext);
    }

    @Override
    public OasDocWriteRepository createOasDocWriteRepository() throws ScoreDataAccessException {
        return new JooqOasDocWriteRepository(this.dslContext);
    }

}
