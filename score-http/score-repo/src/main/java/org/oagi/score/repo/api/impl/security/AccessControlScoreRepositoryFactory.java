package org.oagi.score.repo.api.impl.security;

import org.oagi.score.repo.api.ScoreRepositoryFactory;
import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.BieReadRepository;
import org.oagi.score.repo.api.bie.BieWriteRepository;
import org.oagi.score.repo.api.businesscontext.*;
import org.oagi.score.repo.api.corecomponent.CcReadRepository;
import org.oagi.score.repo.api.corecomponent.CodeListReadRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyReadRepository;
import org.oagi.score.repo.api.corecomponent.seqkey.SeqKeyWriteRepository;
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
                        throw new ScoreDataAccessException(e.getCause());
                    } catch (Throwable e) {
                        throw new ScoreDataAccessException(e);
                    }
                });
    }

    protected abstract void ensureRequester(ScoreUser requester) throws ScoreDataAccessException;

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
    public BieReadRepository createBieReadRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBieReadRepository(), BieReadRepository.class);
    }

    @Override
    public BieWriteRepository createBieWriteRepository() throws ScoreDataAccessException {
        return wrapForAccessControl(delegate.createBieWriteRepository(), BieWriteRepository.class);
    }
}
