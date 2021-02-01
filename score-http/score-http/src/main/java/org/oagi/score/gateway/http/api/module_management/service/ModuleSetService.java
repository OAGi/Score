package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.gateway.http.api.module_management.data.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.component.module.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@Transactional(readOnly = true)
public class ModuleSetService {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ModuleSetReadRepository readRepository;

    @Autowired
    private ModuleSetWriteRepository writeRepository;

    public PageResponse<ModuleSet> getModuleSetList(AuthenticatedPrincipal user, ModuleSetListRequest request) {
        AppUser requester = sessionService.getAppUser(user);
        if (!requester.isDeveloper()) {
            throw new DataAccessForbiddenException(user);
        }
        return readRepository.fetch(requester, request);
    }

    public ModuleSet getModuleSet(AuthenticatedPrincipal user, BigInteger moduleSetId) {
        AppUser requester = sessionService.getAppUser(user);
        if (!requester.isDeveloper()) {
            throw new DataAccessForbiddenException(user);
        }
        return readRepository.getModuleSet(moduleSetId);
    }

    @Transactional
    public ModuleSet createModuleSet(AuthenticatedPrincipal user, CreateModuleSetRequest request) {
        AppUser requester = sessionService.getAppUser(user);
        if (!requester.isDeveloper()) {
            throw new DataAccessForbiddenException(user);
        }

        CreateModuleSetRepositoryRequest repositoryRequest =
                new CreateModuleSetRepositoryRequest(user);
        repositoryRequest.setName(request.getName());
        repositoryRequest.setDescription(request.getDescription());

        BigInteger moduleSetId = writeRepository.createModuleSet(repositoryRequest);
        return getModuleSet(user, moduleSetId);
    }

    @Transactional
    public void updateModuleSet(AuthenticatedPrincipal user, UpdateModuleSetRequest request) {
        AppUser requester = sessionService.getAppUser(user);
        if (!requester.isDeveloper()) {
            throw new DataAccessForbiddenException(user);
        }

        UpdateModuleSetRepositoryRequest repositoryRequest =
                new UpdateModuleSetRepositoryRequest(user, request.getModuleSetId());
        repositoryRequest.setName(request.getName());
        repositoryRequest.setDescription(request.getDescription());

        writeRepository.updateModuleSet(repositoryRequest);
    }

    @Transactional
    public void discardModuleSet(AuthenticatedPrincipal user, BigInteger moduleSetId) {
        AppUser requester = sessionService.getAppUser(user);
        if (!requester.isDeveloper()) {
            throw new DataAccessForbiddenException(user);
        }

        DeleteModuleSetRepositoryRequest repositoryRequest =
                new DeleteModuleSetRepositoryRequest(user, moduleSetId);

        writeRepository.deleteModuleSet(repositoryRequest);
    }

    public PageResponse<ModuleSetModule> getModuleSetModuleList(AuthenticatedPrincipal user, ModuleSetModuleListRequest request) {
        AppUser requester = sessionService.getAppUser(user);
        if (!requester.isDeveloper()) {
            throw new DataAccessForbiddenException(user);
        }
        return readRepository.fetch(requester, request);
    }
}
