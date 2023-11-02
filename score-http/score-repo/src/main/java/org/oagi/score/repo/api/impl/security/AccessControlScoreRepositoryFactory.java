package org.oagi.score.repo.api.impl.security;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.agency.AgencyIdListReadRepository;
import org.oagi.score.repo.api.agency.AgencyIdListWriteRepository;
import org.oagi.score.repo.api.base.Request;
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
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.security.AccessControlException;
import org.oagi.score.repo.api.user.ScoreUserReadRepository;
import org.oagi.score.repo.api.user.model.ScoreRole;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public abstract class AccessControlScoreRepositoryFactory implements ScoreRepositoryFactory {

    private ScoreRepositoryFactory delegate;

    public AccessControlScoreRepositoryFactory(ScoreRepositoryFactory delegate) {
        this.delegate = delegate;
    }

    private <T> T wrapForAccessControl(T obj, Class<T> targetInterface) {
        return (T) Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{targetInterface},
                (proxy, method, args) -> {
                    // Check if there is a requester.
                    ScoreUser requester = null;
                    for (Object arg : args) {
                        if (arg instanceof Request) {
                            Request request = (Request) arg;
                            requester = request.getRequester();
                            break;
                        }
                    }

                    AccessControl accessControl = method.getAnnotation(AccessControl.class);
                    if (accessControl != null) {
                        if (!accessControl.ignore()) {
                            if (requester == null) {
                                throw new AccessControlException("Not allowed to access without a granted requester.");
                            }
                            ensureRequester(requester);

                            // 'requiredAnyRole' processing
                            ScoreRole[] requiredAnyRole = accessControl.requiredAnyRole();
                            if (requiredAnyRole != null && requiredAnyRole.length > 0) {
                                boolean validate = requester.hasAnyRole(requiredAnyRole);
                                if (!validate) {
                                    throw new AccessControlException(requester);
                                }
                            }
                        }
                    }

                    try {
                        return method.invoke(obj, args);
                    } catch (ScoreDataAccessException e) {
                        throw e;
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        throw new ScoreDataAccessException(cause.getMessage(), e);
                    } catch (Throwable e) {
                        throw new ScoreDataAccessException(e);
                    }
                });
    }

    protected abstract void ensureRequester(ScoreUser requester) throws ScoreDataAccessException;

    @Override
    public ConfigurationWriteRepository createConfigurationWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createConfigurationWriteRepository(), ConfigurationWriteRepository.class);
    }

    @Override
    public ScoreUserReadRepository createScoreUserReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createScoreUserReadRepository(), ScoreUserReadRepository.class);
    }

    @Override
    public ContextCategoryReadRepository createContextCategoryReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createContextCategoryReadRepository(), ContextCategoryReadRepository.class);
    }

    @Override
    public ContextCategoryWriteRepository createContextCategoryWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createContextCategoryWriteRepository(), ContextCategoryWriteRepository.class);
    }

    @Override
    public ContextSchemeReadRepository createContextSchemeReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createContextSchemeReadRepository(), ContextSchemeReadRepository.class);
    }

    @Override
    public ContextSchemeWriteRepository createContextSchemeWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createContextSchemeWriteRepository(), ContextSchemeWriteRepository.class);
    }

    @Override
    public BusinessContextReadRepository createBusinessContextReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBusinessContextReadRepository(), BusinessContextReadRepository.class);
    }

    @Override
    public BusinessContextWriteRepository createBusinessContextWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBusinessContextWriteRepository(), BusinessContextWriteRepository.class);
    }

    @Override
    public ReleaseReadRepository createReleaseReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createReleaseReadRepository(), ReleaseReadRepository.class);
    }

    @Override
    public SeqKeyReadRepository createSeqKeyReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createSeqKeyReadRepository(), SeqKeyReadRepository.class);
    }

    @Override
    public SeqKeyWriteRepository createSeqKeyWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createSeqKeyWriteRepository(), SeqKeyWriteRepository.class);
    }

    @Override
    public CcReadRepository createCcReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createCcReadRepository(), CcReadRepository.class);
    }

    @Override
    public CodeListReadRepository createCodeListReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createCodeListReadRepository(), CodeListReadRepository.class);
    }

    @Override
    public ValueDomainReadRepository createValueDomainReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createValueDomainReadRepository(), ValueDomainReadRepository.class);
    }

    @Override
    public BieReadRepository createBieReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBieReadRepository(), BieReadRepository.class);
    }

    @Override
    public BieWriteRepository createBieWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBieWriteRepository(), BieWriteRepository.class);
    }

    @Override
    public ModuleWriteRepository createModuleWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createModuleWriteRepository(), ModuleWriteRepository.class);
    }

    @Override
    public ModuleSetReadRepository createModuleSetReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createModuleSetReadRepository(), ModuleSetReadRepository.class);
    }

    @Override
    public ModuleSetWriteRepository createModuleSetWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createModuleSetWriteRepository(), ModuleSetWriteRepository.class);
    }

    @Override
    public ModuleSetReleaseReadRepository createModuleSetReleaseReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createModuleSetReleaseReadRepository(), ModuleSetReleaseReadRepository.class);
    }

    @Override
    public ModuleSetReleaseWriteRepository createModuleSetReleaseWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createModuleSetReleaseWriteRepository(), ModuleSetReleaseWriteRepository.class);
    }

    @Override
    public AgencyIdListReadRepository createAgencyIdListReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createAgencyIdListReadRepository(), AgencyIdListReadRepository.class);
    }

    @Override
    public AgencyIdListWriteRepository createAgencyIdListWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createAgencyIdListWriteRepository(), AgencyIdListWriteRepository.class);
    }

    @Override
    public MessageReadRepository createMessageReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createMessageReadRepository(), MessageReadRepository.class);
    }

    @Override
    public MessageWriteRepository createMessageWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createMessageWriteRepository(), MessageWriteRepository.class);
    }

    @Override
    public BusinessTermReadRepository createBusinessTermReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBusinessTermReadRepository(), BusinessTermReadRepository.class);
    }

    @Override
    public BusinessTermWriteRepository createBusinessTermWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBusinessTermWriteRepository(), BusinessTermWriteRepository.class);
    }

    @Override
    public BusinessTermAssignmentWriteRepository createBusinessTermAssignmentWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBusinessTermAssignmentWriteRepository(), BusinessTermAssignmentWriteRepository.class);
    }

    @Override
    public OasDocReadRepository createOasDocReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createOasDocReadRepository(), OasDocReadRepository.class);
    }

    @Override
    public OasDocWriteRepository createOasDocWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createOasDocWriteRepository(), OasDocWriteRepository.class);
    }

    @Override
    public BieForOasDocReadRepository createBieForOasDocReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBieForOasDocReadRepository(), BieForOasDocReadRepository.class);
    }

    @Override
    public BieForOasDocWriteRepository createBieForOasDocWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBieForOasDocWriteRepository(), BieForOasDocWriteRepository.class);
    }
}
