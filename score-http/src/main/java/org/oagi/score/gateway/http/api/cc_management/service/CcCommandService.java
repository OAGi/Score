package org.oagi.score.gateway.http.api.cc_management.service;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.acc.AccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.acc.AccUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc.AsccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.ascc.AsccUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.asccp.*;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc.BccCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bcc.BccUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bccp.BccpCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.bccp.BccpUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.dt.DtCreateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.dt.DtUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.controller.payload.dt_sc.DtScUpdateRequest;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.acc.*;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.graph.model.CoreComponentGraphContext;
import org.oagi.score.gateway.http.api.graph.model.Node;
import org.oagi.score.gateway.http.api.graph.repository.GraphContextRepository;
import org.oagi.score.gateway.http.api.log_management.model.LogAction;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.log_management.model.LogUtils;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.oagi.score.gateway.http.common.util.Utility;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.compare;
import static org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType.Extension;
import static org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType.UserExtensionGroup;
import static org.oagi.score.gateway.http.api.log_management.model.LogAction.Modified;
import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional
public class CcCommandService {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private GraphContextRepository graphContextRepository;

    public AccManifestId createAcc(ScoreUser requester, AccCreateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        assertReleaseIsPublished(requester, request.releaseId());

        var command = repositoryFactory.accCommandRepository(requester);

        AccManifestId accManifestId = command.create(
                request.releaseId(),
                request.basedAccManifestId(),
                request.initialObjectClassTerm(),
                request.initialComponentType(),
                request.initialType(),
                request.initialDefinition(),
                request.namespaceId());
        if (accManifestId != null) {
            makeLog(requester, accManifestId, LogAction.Added);

            if (hasLength(request.tag())) {
                TagSummaryRecord tag = repositoryFactory.tagQueryRepository(requester).getTagSummaryByName(request.tag());
                if (tag != null) {
                    repositoryFactory.tagCommandRepository(requester).addTag(tag.tagId(), accManifestId);
                }
            }
        }

        return accManifestId;
    }

    private void makeLog(ScoreUser requester, AccManifestId accManifestId, LogAction action) {
        makeLog(requester, accManifestId, action, LogUtils.generateHash());
    }

    private void makeLog(ScoreUser requester, AccManifestId accManifestId, LogAction action, String logHash) {
        AccDetailsRecord accDetailsRecord = repositoryFactory.accQueryRepository(requester)
                .getAccDetails(accManifestId);
        LogId logId = repositoryFactory.logCommandRepository(requester)
                .create(accDetailsRecord, action, logHash);

        repositoryFactory.accCommandRepository(requester).updateLogId(accManifestId, logId);
    }

    public AsccManifestId createAscc(
            ScoreUser requester, AsccCreateRequest request) {
        return createAscc(requester, request, LogAction.Modified, LogUtils.generateHash(), false);
    }

    public AsccManifestId createAscc(
            ScoreUser requester, AsccCreateRequest request, LogAction logAction, String logHash, boolean skipDependencyCheck) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        if (logAction == null) {
            throw new IllegalArgumentException("'logAction' must not be null.");
        }

        var accQuery = repositoryFactory.accQueryRepository(requester);

        AccSummaryRecord fromAcc = accQuery.getAccSummary(request.accManifestId());
        if (fromAcc == null) {
            throw new IllegalArgumentException("Source ACC does not exist.");
        }

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);

        AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(request.asccpManifestId());
        if (toAsccp == null) {
            throw new IllegalArgumentException("Target ASCCP does not exist.");
        }

        if (!request.skipReusableCheck() && !toAsccp.reusable()) {
            throw new IllegalArgumentException("Target ASCCP is not reusable.");
        }

        if (toAsccp.type() == AsccpType.Extension) {
            if (accQuery.getAsccSummaryList(fromAcc.accManifestId()).stream()
                    .map(e -> asccpQuery.getAsccpSummary(e.toAsccpManifestId()))
                    .filter(e -> e.type() == AsccpType.Extension)
                    .count() > 0) {
                throw new IllegalArgumentException("This ACC already has Extension ASCCP.");
            }
        }

        AsccManifestId asccManifestId = repositoryFactory.accCommandRepository(requester)
                .createAscc(request.accManifestId(), request.asccpManifestId(), request.pos(), request.cardinality(), skipDependencyCheck);
        if (logAction != null) {
            makeLog(requester, fromAcc.accManifestId(), logAction, logHash);
        }

        return asccManifestId;
    }

    public BccManifestId createBcc(
            ScoreUser requester, BccCreateRequest request) {
        return createBcc(requester, request, LogAction.Modified, LogUtils.generateHash(), false);
    }

    public BccManifestId createBcc(
            ScoreUser requester, BccCreateRequest request, LogAction logAction, String logHash, boolean skipDependencyCheck) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        if (logAction == null) {
            throw new IllegalArgumentException("'logAction' must not be null.");
        }

        var accQuery = repositoryFactory.accQueryRepository(requester);

        AccSummaryRecord fromAcc = accQuery.getAccSummary(request.accManifestId());
        if (fromAcc == null) {
            throw new IllegalArgumentException("Source ACC does not exist.");
        }

        var bccpQuery = repositoryFactory.bccpQueryRepository(requester);

        BccpSummaryRecord toBccp = bccpQuery.getBccpSummary(request.bccpManifestId());
        if (toBccp == null) {
            throw new IllegalArgumentException("Target BCCP does not exist.");
        }

        BccManifestId bccManifestId = repositoryFactory.accCommandRepository(requester)
                .createBcc(request.accManifestId(), request.bccpManifestId(),
                        request.pos(), request.cardinality(), skipDependencyCheck);
        if (logAction != null) {
            makeLog(requester, fromAcc.accManifestId(), logAction, logHash);
        }

        return bccManifestId;
    }

    public AsccpManifestId createAsccp(ScoreUser requester, AsccpCreateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        assertReleaseIsPublished(requester, request.releaseId());

        AccSummaryRecord roleOfAcc = repositoryFactory.accQueryRepository(requester)
                .getAccSummary(request.roleOfAccManifestId());
        if (roleOfAcc.isAbstract()) {
            throw new IllegalArgumentException("An abstract ACC cannot be used to create a new ASCCP.");
        }

        var command = repositoryFactory.asccpCommandRepository(requester);

        AsccpManifestId asccpManifestId = command.create(
                request.releaseId(),
                request.roleOfAccManifestId(),
                request.initialPropertyTerm(),
                request.asccpType(),
                request.reusable(),
                request.initialState(),
                request.namespaceId(),
                request.definition());
        if (asccpManifestId != null) {
            makeLog(requester, asccpManifestId, LogAction.Added);

            if (hasLength(request.tag())) {
                TagSummaryRecord tag = repositoryFactory.tagQueryRepository(requester).getTagSummaryByName(request.tag());
                if (tag != null) {
                    repositoryFactory.tagCommandRepository(requester).addTag(tag.tagId(), asccpManifestId);
                }
            }
        }

        return asccpManifestId;
    }

    private void makeLog(ScoreUser requester, AsccpManifestId asccpManifestId, LogAction action) {
        AsccpDetailsRecord asccpDetailsRecord = repositoryFactory.asccpQueryRepository(requester)
                .getAsccpDetails(asccpManifestId);
        LogId logId = repositoryFactory.logCommandRepository(requester)
                .create(asccpDetailsRecord, action);
        repositoryFactory.asccpCommandRepository(requester).updateLogId(asccpManifestId, logId);
    }

    public CreateOagisBodResponse createOagisBod(
            ScoreUser requester, CreateOagisBodRequest request) {

        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException();
        }

        var query = repositoryFactory.asccpQueryRepository(requester);

        List<AsccpManifestId> bodManifestIdList = new ArrayList();
        for (AsccpManifestId verbManifestId : request.verbManifestIdList()) {
            for (AsccpManifestId nounManifestId : request.nounManifestIdList()) {
                AsccpSummaryRecord verb = query.getAsccpSummary(verbManifestId);
                AsccpSummaryRecord noun = query.getAsccpSummary(nounManifestId);

                if (verb.namespaceId() == null) {
                    throw new IllegalArgumentException("'" + verb.propertyTerm() + "' dose not have a namespace.");
                }

                if (noun.namespaceId() == null) {
                    throw new IllegalArgumentException("'" + noun.propertyTerm() + "' dose not have a namespace.");
                }

                ReleaseId releaseId = verb.release().releaseId();
                NamespaceId verbNamespaceId = verb.namespaceId();

                AccManifestId dataAreaAccManifestId = createAcc(requester, AccCreateRequest.builder(releaseId)
                        .initialObjectClassTerm(String.join(" ", Arrays.asList(verb.propertyTerm(), noun.propertyTerm(), "Data Area")))
                        .namespaceId(verbNamespaceId)
                        .build());

                createAscc(requester, AsccCreateRequest.builder(dataAreaAccManifestId, verbManifestId)
                        .cardinalityMin(1)
                        .cardinalityMax(1)
                        .build());

                createAscc(requester, AsccCreateRequest.builder(dataAreaAccManifestId, nounManifestId)
                        .cardinalityMin(1)
                        .cardinalityMax(-1)
                        .build());

                String name = String.join(" ", Arrays.asList(verb.propertyTerm(), noun.propertyTerm()))
                        .replaceAll(" ", "");
                String dataAreaDefinition = "Is where the information that the BOD message carries is provided, in this case " + name + ". The information consists of a Verb and one or more Nouns. The verb (" + verb.propertyTerm().replaceAll(" ", "") + ") indicates the action to be performed on the Noun (" + noun.propertyTerm().replaceAll(" ", "") + ").";
                String dataAreaDefinitionSource = "http://www.openapplications.org/oagis/10";

                AsccpCreateRequest dataAreaAsccpRequest = AsccpCreateRequest.builder(releaseId, dataAreaAccManifestId)
                        .initialPropertyTerm("Data Area")
                        .asccpType(AsccpType.Extension)
                        .reusable(false)
                        .namespaceId(verbNamespaceId)
                        .definition(new Definition(dataAreaDefinition, dataAreaDefinitionSource))
                        .build();

                AsccpManifestId dataAreaAsccpManifestId = createAsccp(requester, dataAreaAsccpRequest);

                AccSummaryRecord bodAcc = repositoryFactory.accQueryRepository(requester)
                        .getAccSummaryList(List.of(releaseId), "Business Object Document")
                        .stream().findAny().orElse(null);
                if (bodAcc == null) {
                    throw new IllegalStateException("'Business Object Document' ACC not found.");
                }

                var bodCreateRequest = AccCreateRequest.builder(releaseId)
                        .basedAccManifestId(bodAcc.accManifestId())
                        .initialObjectClassTerm(String.join(" ", Arrays.asList(verb.propertyTerm(), noun.propertyTerm())))
                        .namespaceId(verbNamespaceId)
                        .build();
                AccManifestId bodAccManifestId = createAcc(requester, bodCreateRequest);

                createAscc(requester, AsccCreateRequest.builder(bodAccManifestId, dataAreaAsccpManifestId)
                        .cardinalityMin(1)
                        .cardinalityMax(1)
                        .build());

                AsccpCreateRequest bodAsccpRequest = AsccpCreateRequest.builder(releaseId, bodAccManifestId)
                        .initialPropertyTerm(bodCreateRequest.initialObjectClassTerm())
                        .namespaceId(verbNamespaceId)
                        .tag("BOD")
                        .build();
                AsccpManifestId bodAsccpManifestId = createAsccp(requester, bodAsccpRequest);
                bodManifestIdList.add(bodAsccpManifestId);
            }
        }

        return new CreateOagisBodResponse(bodManifestIdList);
    }

    public CreateOagisVerbResponse createOagisVerb(
            ScoreUser requester, CreateOagisVerbRequest request) {

        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException();
        }

        AccSummaryRecord verbAcc = repositoryFactory.accQueryRepository(requester)
                .getAccSummary(request.basedVerbAccManifestId());

        ReleaseId releaseId = verbAcc.release().releaseId();
        if (verbAcc.namespaceId() == null) {
            throw new IllegalArgumentException("'" + verbAcc.objectClassTerm() + "' dose not have Namespace Id.");
        }
        NamespaceId namespaceId = verbAcc.namespaceId();

        AccManifestId verbAccManifestId = createAcc(requester, AccCreateRequest.builder(releaseId)
                .basedAccManifestId(request.basedVerbAccManifestId())
                .initialObjectClassTerm(verbAcc.objectClassTerm())
                .namespaceId(namespaceId)
                .build());

        AsccpCreateRequest verbAsccpRequest = AsccpCreateRequest.builder(releaseId, verbAccManifestId)
                .initialPropertyTerm(verbAcc.objectClassTerm())
                .namespaceId(namespaceId)
                .tag("Verb")
                .build();

        AsccpManifestId verbAsccpManifestId = createAsccp(requester, verbAsccpRequest);
        return new CreateOagisVerbResponse(verbAsccpManifestId);
    }

    public BccpManifestId createBccp(ScoreUser requester, BccpCreateRequest request) {
        assertReleaseIsPublished(requester, request.releaseId());

        var command = repositoryFactory.bccpCommandRepository(requester);

        BccpManifestId bccpManifestId = command.create(
                request.releaseId(),
                request.basedDtManifestId(),
                request.initialPropertyTerm());
        if (bccpManifestId != null) {
            makeLog(requester, bccpManifestId, LogAction.Added);

            if (hasLength(request.tag())) {
                TagSummaryRecord tag = repositoryFactory.tagQueryRepository(requester).getTagSummaryByName(request.tag());
                if (tag != null) {
                    repositoryFactory.tagCommandRepository(requester).addTag(tag.tagId(), bccpManifestId);
                }
            }
        }

        return bccpManifestId;
    }

    private void makeLog(ScoreUser requester, BccpManifestId bccpManifestId, LogAction action) {
        BccpDetailsRecord bccpDetailsRecord = repositoryFactory.bccpQueryRepository(requester)
                .getBccpDetails(bccpManifestId);
        LogId logId = repositoryFactory.logCommandRepository(requester)
                .create(bccpDetailsRecord, action);
        repositoryFactory.bccpCommandRepository(requester).updateLogId(bccpManifestId, logId);
    }

    public DtManifestId createDt(ScoreUser requester, DtCreateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        assertReleaseIsPublished(requester, request.releaseId());

        var command = repositoryFactory.dtCommandRepository(requester);
        DtManifestId dtManifestId = command.create(request.releaseId(), request.basedDtManifestId());
        if (dtManifestId != null) {
            makeLog(requester, dtManifestId, LogAction.Added);

            if (hasLength(request.tag())) {
                TagSummaryRecord tag = repositoryFactory.tagQueryRepository(requester).getTagSummaryByName(request.tag());
                if (tag != null) {
                    repositoryFactory.tagCommandRepository(requester).addTag(tag.tagId(), dtManifestId);
                }
            }
        }

        return dtManifestId;
    }

    private void makeLog(ScoreUser requester, DtManifestId dtManifestId, LogAction action) {
        DtDetailsRecord dtDetailsRecord = repositoryFactory.dtQueryRepository(requester)
                .getDtDetails(dtManifestId);
        LogId logId = repositoryFactory.logCommandRepository(requester)
                .create(dtDetailsRecord, action);
        repositoryFactory.dtCommandRepository(requester).updateLogId(dtManifestId, logId);
    }

    public AccManifestId createAccExtension(ScoreUser requester, AccManifestId accManifestId) {

        var accQueryRepository = repositoryFactory.accQueryRepository(requester);
        AccDetailsRecord acc = accQueryRepository.getAccDetails(accManifestId);

        if (!hasLength(acc.objectClassTerm())) {
            throw new IllegalArgumentException("Object Class Term is required.");
        }

        if (acc.namespace() == null) {
            throw new IllegalArgumentException("Namespace is required.");
        }

        ReleaseId releaseId = acc.release().releaseId();

        AccSummaryRecord allExtension
                = accQueryRepository.getAllExtensionAccManifest(releaseId);

        // create extension ACC
        AccManifestId extensionAccManifestId = createAcc(requester, AccCreateRequest.builder(releaseId)
                .basedAccManifestId(allExtension.accManifestId())
                .initialObjectClassTerm(acc.objectClassTerm() + " Extension")
                .initialComponentType(Extension)
                .initialType(AccType.Extension)
                .namespaceId((acc.namespace() != null) ? acc.namespace().namespaceId() : null)
                .build());

        // create extension ASCCP

        String extensionAsccpDefintion = "Allows the user of OAGIS to extend the specification in order to " +
                "provide additional information that is not captured in OAGIS.";
        String extensionAsccpDefintionSource = "http://www.openapplications.org/oagis/10/platform/2";

        AsccpCreateRequest asccpCreateRequest = AsccpCreateRequest.builder(releaseId, extensionAccManifestId)
                .initialPropertyTerm("Extension")
                .asccpType(AsccpType.Extension)
                .reusable(false)
                .namespaceId((acc.namespace() != null) ? acc.namespace().namespaceId() : null)
                .definition(new Definition(extensionAsccpDefintion, extensionAsccpDefintionSource))
                .build();

        AsccpManifestId extensionAsccpManifestId = createAsccp(requester, asccpCreateRequest);

        // create ASCC between extension ACC and extension ASCCP
        createAscc(requester, AsccCreateRequest.builder(accManifestId, extensionAsccpManifestId)
                .skipReusableCheck(true)
                .build());

        return accManifestId;
    }

    private void assertReleaseIsPublished(ScoreUser requester, ReleaseId releaseId) {
        ReleaseSummaryRecord release =
                repositoryFactory.releaseQueryRepository(requester).getReleaseSummary(releaseId);
        if (release.state() != ReleaseState.Published) {
            throw new IllegalStateException("'" + release.state() + "' Release cannot be modified.");
        }
    }

    public List<AccManifestId> updateAccList(ScoreUser requester, List<AccUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }

        List<AccManifestId> updatedAccManifestIdList = new ArrayList<>();
        for (AccUpdateRequest request : requestList) {
            if (updateAcc(requester, request)) {
                updatedAccManifestIdList.add(request.accManifestId());
            }
        }
        return updatedAccManifestIdList;
    }

    public boolean updateAcc(ScoreUser requester, AccUpdateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = query.getAccSummary(request.accManifestId());

        if (CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be updated " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.accCommandRepository(requester);
        boolean updated = command.update(request.accManifestId(),
                request.objectClassTerm(),
                request.componentType(),
                request.isAbstract(),
                request.deprecated(),
                request.namespaceId(),
                (hasLength(request.definition()) || hasLength(request.definitionSource())) ?
                        new Definition(request.definition(), request.definitionSource()) : null);
        if (updated) {
            makeLog(requester, request.accManifestId(), LogAction.Modified);

            // Do not sync data b/w ACC and Extension components #916
//            if (hasExtensionAssociation(requester, ccAccNode.getManifestId())) {
//                updateExtensionComponentProperties(requester, detail);
//            }
            if (acc.componentType() == OagisComponentType.UserExtensionGroup) {
                updateUserExtensionNamespace(requester, acc.accManifestId());
            }
        }

        return updated;
    }

    private boolean updateUserExtensionNamespace(ScoreUser requester, AccManifestId accManifestId) {
        if (accManifestId == null) {
            return false;
        }

        AccSummaryRecord acc = repositoryFactory.accQueryRepository(requester).getAccSummary(accManifestId);
        if (acc.namespaceId() == null) {
            return false;
        }

        boolean updated = false;
        for (AsccpSummaryRecord asccp : repositoryFactory.asccpQueryRepository(requester).getAsccpSummaryList(accManifestId)) {
            updated = repositoryFactory.asccpCommandRepository(requester)
                    .updateNamespace(asccp.asccpManifestId(), acc.namespaceId());
        }
        return updated;
    }

    public List<AsccManifestId> updateAsccList(ScoreUser requester, List<AsccUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }

        List<AsccManifestId> updatedAsccManifestId = new ArrayList<>();
        for (AsccUpdateRequest request : requestList) {
            if (updateAscc(requester, request)) {
                updatedAsccManifestId.add(request.asccManifestId());
            }
        }
        return updatedAsccManifestId;
    }

    public List<AsccpManifestId> updateAsccpList(ScoreUser requester, List<AsccpUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }

        List<AsccpManifestId> updatedAsccpManifestIdList = new ArrayList<>();
        for (AsccpUpdateRequest request : requestList) {
            if (updateAsccp(requester, request)) {
                updatedAsccpManifestIdList.add(request.asccpManifestId());
            }
        }
        return updatedAsccpManifestIdList;
    }

    public boolean updateAscc(ScoreUser requester, AsccUpdateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AsccSummaryRecord ascc = query.getAsccSummary(request.asccManifestId());
        if (ascc == null) {
            return false;
        }
        AccSummaryRecord acc = query.getAccSummary(ascc.fromAccManifestId());
        if (acc == null) {
            return false;
        }

        if (CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be updated " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.accCommandRepository(requester);
        boolean updated = command.update(request.asccManifestId(),
                (request.cardinalityMin() != null || request.cardinalityMax() != null) ?
                        new Cardinality(request.cardinalityMin(), request.cardinalityMax()) : null,
                request.deprecated(),
                (hasLength(request.definition()) || hasLength(request.definitionSource())) ?
                        new Definition(request.definition(), request.definitionSource()) : null);
        if (updated) {
            makeLog(requester, acc.accManifestId(), LogAction.Modified);
        }

        return updated;
    }

    private boolean updateAsccp(ScoreUser requester, AsccpUpdateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        var query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = query.getAsccpSummary(request.asccpManifestId());
        if (CcState.WIP != asccp.state()) {
            throw new IllegalArgumentException("The ASCCP '" + asccp.den() + "' cannot be updated " +
                    "because it is in the '" + asccp.state() + "' state. "
                    + "Only ASCCPs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !asccp.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.asccpCommandRepository(requester);
        boolean updated = command.update(request.asccpManifestId(),
                request.propertyTerm(),
                request.reusable(),
                request.deprecated(),
                request.nillable(),
                request.namespaceId(),
                (hasLength(request.definition()) || hasLength(request.definitionSource())) ?
                        new Definition(request.definition(), request.definitionSource()) : null);
        if (updated) {
            makeLog(requester, asccp.asccpManifestId(), LogAction.Modified);

            if (compare(asccp.propertyTerm(), request.propertyTerm()) != 0) {
                // propagate DEN changes
                var accQuery = repositoryFactory.accQueryRepository(requester);
                var accCommand = repositoryFactory.accCommandRepository(requester);
                for (AsccSummaryRecord ascc : accQuery.getAsccSummaryList(asccp.asccpManifestId())) {
                    accCommand.syncDen(ascc.asccManifestId());
                    makeLog(requester, ascc.fromAccManifestId(), LogAction.Modified);
                }
            }
        }

        return updated;
    }

    public List<BccManifestId> updateBccList(ScoreUser requester, List<BccUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }

        List<BccManifestId> updatedBccManifestIdList = new ArrayList<>();
        for (BccUpdateRequest request : requestList) {
            if (updateBcc(requester, request)) {
                updatedBccManifestIdList.add(request.bccManifestId());
            }
        }
        return updatedBccManifestIdList;
    }

    public boolean updateBcc(ScoreUser requester, BccUpdateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        BccSummaryRecord bcc = query.getBccSummary(request.bccManifestId());
        if (bcc == null) {
            return false;
        }
        AccSummaryRecord acc = query.getAccSummary(bcc.fromAccManifestId());
        if (acc == null) {
            return false;
        }

        if (CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be updated " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.accCommandRepository(requester);
        boolean updated = command.update(request.bccManifestId(),
                request.entityType(),
                (request.cardinalityMin() != null || request.cardinalityMax() != null) ?
                        new Cardinality(request.cardinalityMin(), request.cardinalityMax()) : null,
                request.deprecated(),
                request.nillable(),
                hasLength(request.fixedValue()) ?
                        new ValueConstraint(null, request.fixedValue()) :
                        (hasLength(request.defaultValue()) ? new ValueConstraint(request.defaultValue(), null) : null),
                (hasLength(request.definition()) || hasLength(request.definitionSource())) ?
                        new Definition(request.definition(), request.definitionSource()) : null);
        if (updated) {
            makeLog(requester, acc.accManifestId(), LogAction.Modified);
        }

        return updated;
    }

    public List<BccpManifestId> updateBccpList(ScoreUser requester, List<BccpUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }

        List<BccpManifestId> updatedBccpManifestIdList = new ArrayList<>();
        for (BccpUpdateRequest request : requestList) {
            if (updateBccp(requester, request)) {
                updatedBccpManifestIdList.add(request.bccpManifestId());
            }
        }
        return updatedBccpManifestIdList;
    }

    private boolean updateBccp(ScoreUser requester, BccpUpdateRequest request) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        if (request.bccpManifestId() == null) {
            throw new IllegalArgumentException("The request does not contain a 'bccpManifestId', " +
                    "which is required to identify the BCCP to be updated.");
        }

        var query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord bccp = query.getBccpSummary(request.bccpManifestId());
        if (bccp == null) {
            throw new IllegalArgumentException("No BCCP exists for the provided manifest ID: "
                    + request.bccpManifestId() + ". Please ensure the manifest ID is correct.");
        }

        if (CcState.WIP != bccp.state()) {
            throw new IllegalArgumentException("The BCCP '" + bccp.den() + "' cannot be updated " +
                    "because it is in the '" + bccp.state() + "' state. "
                    + "Only BCCPs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !bccp.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("You can't modify this BCCP because you're not the owner. " +
                    "Only the owner of this BCCP (" + bccp.owner().loginId() + ") can make changes.");
        }

        var command = repositoryFactory.bccpCommandRepository(requester);
        boolean updated = command.update(request.bccpManifestId(),
                request.propertyTerm(),
                request.deprecated(),
                request.nillable(),
                request.namespaceId(),
                hasLength(request.fixedValue()) ?
                        new ValueConstraint(null, request.fixedValue()) :
                        (hasLength(request.defaultValue()) ? new ValueConstraint(request.defaultValue(), null) : null),
                (hasLength(request.definition()) || hasLength(request.definitionSource())) ?
                        new Definition(request.definition(), request.definitionSource()) : null);
        if (updated) {
            makeLog(requester, bccp.bccpManifestId(), LogAction.Modified);

            if (hasLength(request.propertyTerm()) && compare(bccp.propertyTerm(), request.propertyTerm()) != 0) {
                // propagate DEN changes
                var accQuery = repositoryFactory.accQueryRepository(requester);
                var accCommand = repositoryFactory.accCommandRepository(requester);
                for (BccSummaryRecord bcc : accQuery.getBccSummaryList(bccp.bccpManifestId())) {
                    accCommand.syncDen(bcc.bccManifestId());
                    makeLog(requester, bcc.fromAccManifestId(), LogAction.Modified);
                }
            }
        }

        return updated;
    }

    public List<DtManifestId> updateDtList(ScoreUser requester, List<DtUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }

        List<DtManifestId> updatedDtDetailsList = new ArrayList<>();
        for (DtUpdateRequest request : requestList) {
            if (updateDt(requester, request, false)) {
                updatedDtDetailsList.add(request.dtManifestId());
            }
        }
        return updatedDtDetailsList;
    }

    private boolean updateDt(ScoreUser requester, DtUpdateRequest request, boolean skipOwnershipCheck) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord dt = query.getDtSummary(request.dtManifestId());
        if (dt == null) {
            return false;
        }

        if (CcState.WIP != dt.state()) {
            throw new IllegalArgumentException("The DT '" + dt.den() + "' cannot be updated " +
                    "because it is in the '" + dt.state() + "' state. "
                    + "Only DTs in the 'WIP' state can be updated.");
        }

        if (!skipOwnershipCheck && !requester.isAdministrator() && !dt.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // Ensure exactly one 'isDefault' = true
        long defaultCount = request.dtAwdPriList().stream()
                .filter(DtAwdPriSummaryRecord::isDefault)
                .count();

        if (defaultCount > 1) {
            throw new IllegalArgumentException("Only one primitive value can be set as default.");
        } else if (defaultCount == 0) {
            throw new IllegalArgumentException("At least one primitive value must be set as default.");
        }

        var command = repositoryFactory.dtCommandRepository(requester);
        boolean updated = command.update(request.dtManifestId(),
                request.qualifier(),
                request.sixDigitId(),
                request.deprecated(),
                request.namespaceId(),
                request.contentComponentDefinition(),
                (hasLength(request.definition()) || hasLength(request.definitionSource())) ?
                        new Definition(request.definition(), request.definitionSource()) : null);

        // Fetch existing values as a map
        Map<DtAwdPriId, DtAwdPriSummaryRecord> existingValues = query
                .getDtAwdPriSummaryList(dt.dtManifestId())
                .stream()
                .collect(Collectors.toMap(DtAwdPriSummaryRecord::dtAwdPriId, Function.identity()));

        // Process updates and additions of primitive values
        for (var value : request.dtAwdPriList()) {
            DtAwdPriId valueId = value.dtAwdPriId();
            if (existingValues.containsKey(valueId)) {
                boolean updatedAwdPri = command.updateDtAwdPri(valueId, value.cdtPriName(),
                        value.xbtManifestId(), value.codeListManifestId(), value.agencyIdListManifestId(), value.isDefault());
                if (updatedAwdPri) {
                    updated = true;
                }
                existingValues.remove(valueId);
            } else {
                DtAwdPriId createdId = command.createDtAwdPri(dt.release().releaseId(), dt.dtId(), value.cdtPriName(),
                        value.xbtManifestId(), value.codeListManifestId(), value.agencyIdListManifestId(), value.isDefault());
                if (createdId != null) {
                    updated = true;
                }
            }
        }

        // Remove any remaining (deleted) values
        for (DtAwdPriId valueId : existingValues.keySet()) {
            boolean deleted = command.deleteDtAwdPri(valueId);
            if (deleted) {
                updated = true;
            }
        }

        if (updated) {
            makeLog(requester, dt.dtManifestId(), LogAction.Modified);

            // propagate
            for (DtSummaryRecord inheritedDt : query.getInheritedDtSummaryList(dt.dtManifestId())) {

                DtUpdateRequest.Builder builder = DtUpdateRequest.builder(inheritedDt.dtManifestId())
                        .qualifier(inheritedDt.qualifier())
                        .sixDigitId(inheritedDt.sixDigitId())
                        .deprecated(!inheritedDt.deprecated() ? request.deprecated() : null)
                        .namespaceId(request.namespaceId())
                        .dtAwdPriList(request.dtAwdPriList());
                if (StringUtils.equals(dt.contentComponentDefinition(), inheritedDt.contentComponentDefinition())) {
                    builder = builder.contentComponentDefinition(request.contentComponentDefinition());
                }
                if (dt.definition() != null && inheritedDt.definition() != null) {
                    if (StringUtils.equals(dt.definition().content(), inheritedDt.definition().content())) {
                        builder = builder.definition(request.definition());
                    }
                    if (StringUtils.equals(dt.definition().source(), inheritedDt.definition().source())) {
                        builder = builder.definitionSource(request.definitionSource());
                    }
                } else {
                    builder = builder.definition(request.definition())
                            .definitionSource(request.definitionSource());
                }

                updateDt(requester, builder.build(), true);
            }
        }

        return updated;
    }

    public List<DtScManifestId> updateDtScList(ScoreUser requester, List<DtScUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }

        List<DtScManifestId> updatedDtScManifestIdList = new ArrayList<>();
        for (DtScUpdateRequest request : requestList) {
            if (updateDtSc(requester, request, false)) {
                updatedDtScManifestIdList.add(request.dtScManifestId());
            }
        }
        return updatedDtScManifestIdList;
    }

    private boolean updateDtSc(ScoreUser requester, DtScUpdateRequest request, boolean skipOwnershipCheck) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("'request' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);

        DtScSummaryRecord dtSc = query.getDtScSummary(request.dtScManifestId());
        if (dtSc == null) {
            return false;
        }

        DtSummaryRecord dt = query.getDtSummary(dtSc.ownerDtManifestId());
        if (dt == null) {
            return false;
        }

        if (CcState.WIP != dt.state()) {
            throw new IllegalArgumentException("The DT '" + dt.den() + "' cannot be updated " +
                    "because it is in the '" + dt.state() + "' state. "
                    + "Only DTs in the 'WIP' state can be updated.");
        }

        if (!skipOwnershipCheck && !requester.isAdministrator() && !dt.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        // Ensure exactly one 'isDefault' = true
        long defaultCount = request.dtScAwdPriList().stream()
                .filter(DtScAwdPriSummaryRecord::isDefault)
                .count();

        if (defaultCount > 1) {
            throw new IllegalArgumentException("Only one primitive value can be set as default.");
        } else if (defaultCount == 0) {
            throw new IllegalArgumentException("At least one primitive value must be set as default.");
        }

        // Issue #1240
        ensureUniquenessOfPropertyTerm(requester, dtSc, request.propertyTerm());

        ensureUniquenessOfDen(requester, dtSc,
                dtSc.objectClassTerm(),
                request.propertyTerm(),
                request.representationTerm());

        var command = repositoryFactory.dtCommandRepository(requester);
        boolean updated = command.updateDtSc(request.dtScManifestId(),
                request.propertyTerm(),
                request.representationTerm(),
                (request.cardinalityMin() != null || request.cardinalityMax() != null) ?
                        new Cardinality(request.cardinalityMin(), request.cardinalityMax()) : null,
                request.deprecated(),
                hasLength(request.fixedValue()) ?
                        new ValueConstraint(null, request.fixedValue()) :
                        (hasLength(request.defaultValue()) ? new ValueConstraint(request.defaultValue(), null) : null),
                (hasLength(request.definition()) || hasLength(request.definitionSource())) ?
                        new Definition(request.definition(), request.definitionSource()) : null);


        // Fetch existing values as a map
        Map<DtScAwdPriId, DtScAwdPriSummaryRecord> existingValues = query
                .getDtScAwdPriSummaryList(dtSc.dtScManifestId())
                .stream()
                .collect(Collectors.toMap(DtScAwdPriSummaryRecord::dtScAwdPriId, Function.identity()));

        // Process updates and additions of primitive values
        for (var value : request.dtScAwdPriList()) {
            DtScAwdPriId valueId = value.dtScAwdPriId();
            if (existingValues.containsKey(valueId)) {
                boolean updatedScAwdPri = command.updateDtScAwdPri(valueId, value.cdtPriName(),
                        value.xbtManifestId(), value.codeListManifestId(), value.agencyIdListManifestId(), value.isDefault());
                if (updatedScAwdPri) {
                    updated = true;
                }
                existingValues.remove(valueId);
            } else {
                DtScAwdPriId createdId = command.createDtScAwdPri(dt.release().releaseId(), dtSc.dtScId(), value.cdtPriName(),
                        value.xbtManifestId(), value.codeListManifestId(), value.agencyIdListManifestId(), value.isDefault());
                if (createdId != null) {
                    updated = true;
                }
            }
        }

        // Remove any remaining (deleted) values
        for (DtScAwdPriId valueId : existingValues.keySet()) {
            boolean deleted = command.deleteDtScAwdPri(valueId);
            if (deleted) {
                updated = true;
            }
        }

        if (updated) {
            makeLog(requester, dt.dtManifestId(), LogAction.Modified);

            for (DtScSummaryRecord inheritedDtSc : query.getInheritedDtScSummaryList(dtSc.dtScManifestId())) {
                updateDtSc(requester, DtScUpdateRequest.builder(inheritedDtSc.dtScManifestId())
                        .objectClassTerm(request.objectClassTerm())
                        .propertyTerm(request.propertyTerm())
                        .representationTerm(request.representationTerm())
                        .cardinalityMin(request.cardinalityMin())
                        .cardinalityMax(request.cardinalityMax())
                        .definition(request.definition())
                        .definitionSource(request.definitionSource())
                        .deprecated(request.deprecated())
                        .defaultValue(request.defaultValue())
                        .fixedValue(request.fixedValue())
                        .dtScAwdPriList(request.dtScAwdPriList())
                        .build(), true);
            }
        }

        return updated;
    }

    private void ensureUniquenessOfPropertyTerm(ScoreUser requester, DtScSummaryRecord dtSc, String propertyTerm) {
        if (repositoryFactory.dtQueryRepository(requester)
                .hasSamePropertyTerm(dtSc.dtScManifestId(), propertyTerm)) {
            throw new IllegalArgumentException("There is an another supplementary component whose property term is same with the request: " + propertyTerm);
        }
    }

    private void ensureUniquenessOfDen(ScoreUser requester, DtScSummaryRecord dtSc,
                                       String objectClassTerm,
                                       String propertyTerm,
                                       String representationTerm) {
        var dtQuery = repositoryFactory.dtQueryRepository(requester);
        if (dtQuery.hasSameDen(dtSc.dtScManifestId(), objectClassTerm, propertyTerm, representationTerm)) {
            String den = getDen(objectClassTerm, propertyTerm, representationTerm);
            throw new IllegalArgumentException("There is an another supplementary component whose DEN is same with the request: " + den);
        }

        if (dtSc.basedDtScManifestId() != null) {
            DtScSummaryRecord basedDtSc = dtQuery.getDtScSummary(dtSc.basedDtScManifestId());
            ensureUniquenessOfDen(requester, basedDtSc, objectClassTerm, propertyTerm, representationTerm);
        }
    }

    private String getDen(
            String objectClassTerm,
            String propertyTerm,
            String representationTerm) {
        String middleTerm = null;
        if (hasLength(propertyTerm)) {
            middleTerm = propertyTerm.replaceAll(representationTerm, "").trim();
        }
        if (middleTerm != null) {
            return objectClassTerm + ". " + middleTerm + ". " + representationTerm;
        }
        return objectClassTerm + ". " + representationTerm;
    }

    public boolean updateBasedAccManifestId(
            ScoreUser requester, AccManifestId accManifestId, AccManifestId basedAccManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("'accManifestId' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = query.getAccSummary(accManifestId);

        if (CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be updated " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.accCommandRepository(requester);
        return command.updateBasedAccManifestId(accManifestId, basedAccManifestId);
    }

    public boolean updateState(ScoreUser requester, AccManifestId accManifestId, CcState state) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("'accManifestId' must not be null.");
        }

        if (state == null) {
            throw new IllegalArgumentException("'state' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = query.getAccSummary(accManifestId);

        CcState prevState = acc.state();
        CcState nextState = state;

        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        boolean restore = prevState == CcState.Deleted && nextState == CcState.WIP;
        if (!restore) {
            if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId()) && !prevState.isImplicitMove(nextState)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }

            if (nextState != CcState.Deleted && acc.namespaceId() == null) {
                throw new IllegalArgumentException("'" + acc.den() + "' namespace required.");
            }
        }

        boolean updated = repositoryFactory.accCommandRepository(requester).updateState(accManifestId, state);
        if (updated) {
            makeLog(requester, accManifestId,
                    (nextState == CcState.Deleted) ? LogAction.Deleted :
                            (restore ? LogAction.Restored : LogAction.Modified));
        }

        return updated;
    }

    public boolean updateAsccpRoleOfAcc(ScoreUser requester, AsccpManifestId asccpManifestId, AccManifestId roleOfAccManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("'asccpManifestId' must not be null.");
        }

        if (roleOfAccManifestId == null) {
            throw new IllegalArgumentException("'roleOfAccManifestId' must not be null.");
        }

        var query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);

        CcState state = asccp.state();
        if (CcState.WIP != state) {
            throw new IllegalArgumentException("The ASCCP '" + asccp.den() + "' cannot be updated " +
                    "because it is in the '" + asccp.state() + "' state. "
                    + "Only ASCCPs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !asccp.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        boolean updated = repositoryFactory.asccpCommandRepository(requester).updateRoleOfAcc(asccpManifestId, roleOfAccManifestId);
        if (updated) {
            var accCommand = repositoryFactory.accCommandRepository(requester);
            for (AsccSummaryRecord ascc : repositoryFactory.accQueryRepository(requester).getAsccSummaryList(asccpManifestId)) {
                accCommand.syncDen(ascc.asccManifestId());
            }
            makeLog(requester, asccpManifestId, LogAction.Modified);
        }

        return updated;
    }

    public boolean updateState(ScoreUser requester, AsccpManifestId asccpManifestId, CcState state) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("'asccpManifestId' must not be null.");
        }

        if (state == null) {
            throw new IllegalArgumentException("'state' must not be null.");
        }

        var query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);

        CcState prevState = asccp.state();
        CcState nextState = state;

        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        boolean restore = prevState == CcState.Deleted && nextState == CcState.WIP;
        if (!restore) {
            if (!requester.isAdministrator() && !asccp.owner().userId().equals(requester.userId()) && !prevState.isImplicitMove(nextState)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }

            if (nextState != CcState.Deleted && asccp.namespaceId() == null) {
                throw new IllegalArgumentException("'" + asccp.den() + "' namespace required.");
            }
        }

        boolean updated = repositoryFactory.asccpCommandRepository(requester).updateState(asccpManifestId, state);
        if (updated) {
            makeLog(requester, asccpManifestId,
                    (nextState == CcState.Deleted) ? LogAction.Deleted :
                            (restore ? LogAction.Restored : LogAction.Modified));
        }

        return updated;
    }

    public boolean updateBccpDt(ScoreUser requester, BccpManifestId bccpManifestId, DtManifestId dtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("'bccpManifestId' must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("'dtManifestId' must not be null.");
        }

        var query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);

        CcState state = bccp.state();
        if (CcState.WIP != state) {
            throw new IllegalArgumentException("Only the core component in 'WIP' state can be modified.");
        }

        if (!requester.isAdministrator() && !bccp.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        boolean updated = repositoryFactory.bccpCommandRepository(requester).updateDt(bccpManifestId, dtManifestId);
        if (updated) {
            var accCommand = repositoryFactory.accCommandRepository(requester);
            for (BccSummaryRecord bcc : repositoryFactory.accQueryRepository(requester).getBccSummaryList(bccpManifestId)) {
                accCommand.syncDen(bcc.bccManifestId());
            }
            makeLog(requester, bccpManifestId, LogAction.Modified);
        }

        return updated;
    }

    public boolean updateState(ScoreUser requester, BccpManifestId bccpManifestId, CcState state) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("'bccpManifestId' must not be null.");
        }

        if (state == null) {
            throw new IllegalArgumentException("'state not be null.");
        }

        var query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);

        CcState prevState = bccp.state();
        CcState nextState = state;

        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        boolean restore = prevState == CcState.Deleted && nextState == CcState.WIP;
        if (!restore) {
            if (!requester.isAdministrator() && !bccp.owner().userId().equals(requester.userId()) && !prevState.isImplicitMove(nextState)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }

            if (nextState != CcState.Deleted && bccp.namespaceId() == null) {
                throw new IllegalArgumentException("'" + bccp.den() + "' namespace required.");
            }
        }

        boolean updated = repositoryFactory.bccpCommandRepository(requester).updateState(bccpManifestId, state);
        if (updated) {
            makeLog(requester, bccpManifestId,
                    (nextState == CcState.Deleted) ? LogAction.Deleted :
                            (restore ? LogAction.Restored : LogAction.Modified));
        }

        return updated;
    }

    public boolean updateState(ScoreUser requester, DtManifestId dtManifestId, CcState state) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("'dtManifestId' must not be null.");
        }

        if (state == null) {
            throw new IllegalArgumentException("'state not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord dt = query.getDtSummary(dtManifestId);

        CcState prevState = dt.state();
        CcState nextState = state;

        if (!prevState.canMove(nextState)) {
            throw new IllegalArgumentException("The core component in '" + prevState + "' state cannot move to '" + nextState + "' state.");
        }

        boolean restore = prevState == CcState.Deleted && nextState == CcState.WIP;
        if (!restore) {
            if (!requester.isAdministrator() && !dt.owner().userId().equals(requester.userId()) && !prevState.isImplicitMove(nextState)) {
                throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
            }

            if (nextState != CcState.Deleted && dt.namespaceId() == null) {
                throw new IllegalArgumentException("'" + dt.den() + "' namespace required.");
            }
        }

        boolean updated = repositoryFactory.dtCommandRepository(requester).updateState(dtManifestId, state);
        if (updated) {
            makeLog(requester, dtManifestId,
                    (nextState == CcState.Deleted) ? LogAction.Deleted :
                            (restore ? LogAction.Restored : LogAction.Modified));
        }

        return updated;
    }

    public boolean purge(ScoreUser requester, AccManifestId accManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("'accManifestId' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = query.getAccSummary(accManifestId);
        if (acc.componentType() == UserExtensionGroup) {
            var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
            AsccpSummaryRecord groupAsccp = asccpQuery.getAsccpSummaryList(accManifestId).stream().findFirst().orElse(null);

            var accQuery = repositoryFactory.accQueryRepository(requester);
            AsccSummaryRecord ascc = accQuery.getAsccSummaryList(groupAsccp.asccpManifestId()).stream().findFirst().orElse(null);

            discard(sessionService.getScoreSystemUser(), ascc.asccManifestId(), true, Modified, LogUtils.generateHash());
            purge(sessionService.getScoreSystemUser(), groupAsccp.asccpManifestId(), true);
        }

        if (CcState.Deleted != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be purged " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'Deleted' state can be purged.");
        }

        if (!repositoryFactory.asccpQueryRepository(requester)
                .getAsccpSummaryList(accManifestId)
                .isEmpty()) {
            throw new IllegalArgumentException("Please purge related-ASCCPs first before purging the ACC '" + acc.den() + "'.");
        }

        if (!query.getInheritedAccSummaryList(acc.basedAccManifestId()).isEmpty()) {
            throw new IllegalArgumentException("Please purge derivations first before purging the ACC '" + acc.den() + "'.");
        }

        return repositoryFactory.accCommandRepository(requester).delete(accManifestId);
    }

    public boolean purge(ScoreUser requester, AsccpManifestId asccpManifestId) {
        return purge(requester, asccpManifestId, false);
    }

    public boolean purge(ScoreUser requester, AsccpManifestId asccpManifestId, boolean skipStateCheck) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("'asccpManifestId' must not be null.");
        }

        var query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);

        if (!skipStateCheck && CcState.Deleted != asccp.state()) {
            throw new IllegalArgumentException("The ASCCP '" + asccp.den() + "' cannot be purged " +
                    "because it is in the '" + asccp.state() + "' state. "
                    + "Only ASCCPs in the 'Deleted' state can be purged.");
        }

        if (!repositoryFactory.accQueryRepository(requester)
                .getAsccSummaryList(asccpManifestId)
                .isEmpty()) {
            throw new IllegalArgumentException("Please purge related-ASCCs first before purging the ASCCP '" + asccp.den() + "'.");
        }

        return repositoryFactory.asccpCommandRepository(requester).delete(asccpManifestId);
    }

    public boolean purge(ScoreUser requester, BccpManifestId bccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("'bccpManifestId' must not be null.");
        }

        var query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);

        if (CcState.Deleted != bccp.state()) {
            throw new IllegalArgumentException("The BCCP '" + bccp.den() + "' cannot be purged " +
                    "because it is in the '" + bccp.state() + "' state. "
                    + "Only BCCPs in the 'Deleted' state can be purged.");
        }

        if (!repositoryFactory.accQueryRepository(requester)
                .getBccSummaryList(bccpManifestId)
                .isEmpty()) {
            new IllegalArgumentException("Please purge related-BCCs first before purging the BCCP '" + bccp.den() + "'.");
        }

        return repositoryFactory.bccpCommandRepository(requester).delete(bccpManifestId);
    }

    public boolean purge(ScoreUser requester, DtManifestId dtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("'dtManifestId' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord dt = query.getDtSummary(dtManifestId);

        if (CcState.Deleted != dt.state()) {
            throw new IllegalArgumentException("The DT '" + dt.den() + "' cannot be purged " +
                    "because it is in the '" + dt.state() + "' state. "
                    + "Only DTs in the 'Deleted' state can be purged.");
        }

        if (!query.getInheritedDtSummaryList(dtManifestId).isEmpty()) {
            throw new IllegalArgumentException("Please purge derivations first before purging the DT '" + dt.den() + "'.");
        }

        if (!repositoryFactory.bccpQueryRepository(requester).getBccpSummaryList(dtManifestId).isEmpty()) {
            throw new IllegalArgumentException("Please purge related-BCCPs first before purging the DT '" + dt.den() + "'.");
        }

        return repositoryFactory.dtCommandRepository(requester).delete(dtManifestId);
    }

    public boolean discard(ScoreUser requester, AsccManifestId asccManifestId) {
        return discard(requester, asccManifestId, false, Modified, LogUtils.generateHash());
    }

    public boolean discard(ScoreUser requester, AsccManifestId asccManifestId, boolean skipStateCheck,
                           LogAction logAction, String logHash) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (asccManifestId == null) {
            throw new IllegalArgumentException("'asccManifestId' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AsccSummaryRecord ascc = query.getAsccSummary(asccManifestId);
        if (ascc == null) {
            return false;
        }

        AccSummaryRecord acc = query.getAccSummary(ascc.fromAccManifestId());
        if (acc == null) {
            return false;
        }

        if (!skipStateCheck && CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be deleted " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be deleted.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        int bieUsageCount = repositoryFactory.topLevelAsbiepQueryRepository(requester).countReferences(ascc.asccManifestId());
        if (bieUsageCount > 0) {
            throw new IllegalArgumentException("This association is referenced in " + bieUsageCount + " BIE(s) and cannot be deleted.");
        }

        var command = repositoryFactory.accCommandRepository(requester);
        boolean updated = command.delete(asccManifestId);
        if (updated) {
            makeLog(requester, acc.accManifestId(), logAction, logHash);
        }

        return updated;
    }

    public boolean discard(ScoreUser requester, BccManifestId bccManifestId) {
        return discard(requester, bccManifestId, false, Modified, LogUtils.generateHash());
    }

    public boolean discard(ScoreUser requester, BccManifestId bccManifestId, boolean skipStateCheck,
                           LogAction logAction, String logHash) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (bccManifestId == null) {
            throw new IllegalArgumentException("'bccManifestId' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        BccSummaryRecord bcc = query.getBccSummary(bccManifestId);
        if (bcc == null) {
            return false;
        }

        AccSummaryRecord acc = query.getAccSummary(bcc.fromAccManifestId());
        if (acc == null) {
            return false;
        }

        if (!skipStateCheck && CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be deleted " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be deleted.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        int bieUsageCount = repositoryFactory.topLevelAsbiepQueryRepository(requester).countReferences(bcc.bccManifestId());
        if (bieUsageCount > 0) {
            throw new IllegalArgumentException("This association is referenced in " + bieUsageCount + " BIE(s) and cannot be deleted.");
        }

        var command = repositoryFactory.accCommandRepository(requester);
        boolean updated = command.delete(bccManifestId);
        if (updated) {
            makeLog(requester, acc.accManifestId(), logAction, logHash);
        }

        return updated;
    }

    public void updateAccSequence(ScoreUser requester, AccManifestId accManifestId,
                                  AsccpOrBccpManifestId item, @Nullable AsccpOrBccpManifestId after) {

        if (requester == null) {
            throw new IllegalArgumentException("`requester` must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("`accManifestId` must not be null.");
        }

        if (item == null) {
            throw new IllegalArgumentException("`item` must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = query.getAccSummary(accManifestId);
        if (acc == null) {
            return;
        }

        if (CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be updated " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be updated.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        repositoryFactory.seqKeyCommandRepository(requester).move(accManifestId, item, after);
        makeLog(requester, acc.accManifestId(), LogAction.Modified);
    }

    public void reviseAcc(ScoreUser requester, AccManifestId accManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("'accManifestId' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord prevAcc = query.getAccSummary(accManifestId);

        if (requester.isDeveloper()) {
            if (CcState.Published != prevAcc.state()) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
        } else {
            if (CcState.Production != prevAcc.state()) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevAcc.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevAcc.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevAcc.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.accCommandRepository(requester).revise(accManifestId);
        makeLog(requester, accManifestId, LogAction.Revised);
    }

    public void cancelAcc(ScoreUser requester, AccManifestId accManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("'accManifestId' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord prevAcc = query.getAccSummary(accManifestId);

        if (CcState.WIP != prevAcc.state()) {
            throw new IllegalArgumentException("The ACC '" + prevAcc.den() + "' cannot be cancelled " +
                    "because it is in the '" + prevAcc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be cancelled.");
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevAcc.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevAcc.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevAcc.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.accCommandRepository(requester).cancel(accManifestId);
    }

    public void reviseAsccp(ScoreUser requester, AsccpManifestId asccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("'asccpManifestId' must not be null.");
        }

        var query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord prevAsccp = query.getAsccpSummary(asccpManifestId);

        if (requester.isDeveloper()) {
            if (CcState.Published != prevAsccp.state()) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
        } else {
            if (CcState.Production != prevAsccp.state()) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevAsccp.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevAsccp.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevAsccp.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.asccpCommandRepository(requester).revise(asccpManifestId);
        makeLog(requester, asccpManifestId, LogAction.Revised);
    }

    public void cancelAsccp(ScoreUser requester, AsccpManifestId asccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("'asccpManifestId' must not be null.");
        }

        var query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord prevAsccp = query.getAsccpSummary(asccpManifestId);

        if (CcState.WIP != prevAsccp.state()) {
            throw new IllegalArgumentException("The ASCCP '" + prevAsccp.den() + "' cannot be cancelled " +
                    "because it is in the '" + prevAsccp.state() + "' state. "
                    + "Only ASCCPs in the 'WIP' state can be cancelled.");
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevAsccp.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevAsccp.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevAsccp.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.asccpCommandRepository(requester).cancel(asccpManifestId);
    }

    public void reviseBccp(ScoreUser requester, BccpManifestId bccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("'bccpManifestId' must not be null.");
        }

        var query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord prevBccp = query.getBccpSummary(bccpManifestId);

        if (requester.isDeveloper()) {
            if (CcState.Published != prevBccp.state()) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
        } else {
            if (CcState.Production != prevBccp.state()) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevBccp.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevBccp.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevBccp.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.bccpCommandRepository(requester).revise(bccpManifestId);
        makeLog(requester, bccpManifestId, LogAction.Revised);
    }

    public void cancelBccp(ScoreUser requester, BccpManifestId bccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("'bccpManifestId' must not be null.");
        }

        var query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord prevBccp = query.getBccpSummary(bccpManifestId);

        if (CcState.WIP != prevBccp.state()) {
            throw new IllegalArgumentException("The BCCP '" + prevBccp.den() + "' cannot be cancelled " +
                    "because it is in the '" + prevBccp.state() + "' state. "
                    + "Only BCCPs in the 'WIP' state can be cancelled.");
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevBccp.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevBccp.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevBccp.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.bccpCommandRepository(requester).cancel(bccpManifestId);
    }

    public void reviseDt(ScoreUser requester, DtManifestId dtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("'dtManifestId' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord prevDt = query.getDtSummary(dtManifestId);

        if (requester.isDeveloper()) {
            if (CcState.Published != prevDt.state()) {
                throw new IllegalArgumentException("Only the core component in 'Published' state can be revised.");
            }
        } else {
            if (CcState.Production != prevDt.state()) {
                throw new IllegalArgumentException("Only the core component in 'Production' state can be revised.");
            }
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevDt.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevDt.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevDt.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.dtCommandRepository(requester).revise(dtManifestId);
        makeLog(requester, dtManifestId, LogAction.Revised);
    }

    public void cancelDt(ScoreUser requester, DtManifestId dtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("'dtManifestId' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord prevDt = query.getDtSummary(dtManifestId);

        if (CcState.WIP != prevDt.state()) {
            throw new IllegalArgumentException("The DT '" + prevDt.den() + "' cannot be cancelled " +
                    "because it is in the '" + prevDt.state() + "' state. "
                    + "Only DTs in the 'WIP' state can be cancelled.");
        }

        ReleaseSummaryRecord workingRelease = repositoryFactory.releaseQueryRepository(requester)
                .getReleaseSummary(prevDt.release().libraryId(), "Working");

        ReleaseId targetReleaseId = prevDt.release().releaseId();
        if (requester.isDeveloper()) {
            if (!targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in 'Working' branch for developers.");
            }
        } else {
            if (targetReleaseId.equals(workingRelease.releaseId())) {
                throw new IllegalArgumentException("It only allows to revise the component in non-'Working' branch for end-users.");
            }
        }

        boolean ownerIsDeveloper = prevDt.owner().isDeveloper();

        if (requester.isDeveloper() != ownerIsDeveloper) {
            throw new IllegalArgumentException("It only allows to revise the component for users in the same roles.");
        }

        repositoryFactory.dtCommandRepository(requester).cancel(dtManifestId);
    }

    public void transferOwnership(
            ScoreUser requester, ScoreUser targetUser, AccManifestId accManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (targetUser == null) {
            throw new IllegalArgumentException("'targetUser' must not be null.");
        }

        if (accManifestId == null) {
            throw new IllegalArgumentException("'accManifestId' must not be null.");
        }

        var query = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = query.getAccSummary(accManifestId);
        if (acc == null) {
            throw new IllegalStateException("ACC not found.");
        }

        if (CcState.WIP != acc.state()) {
            throw new IllegalArgumentException("The ACC '" + acc.den() + "' cannot be transferred " +
                    "because it is in the '" + acc.state() + "' state. "
                    + "Only ACCs in the 'WIP' state can be transferred.");
        }

        if (!requester.isAdministrator() && !acc.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.accCommandRepository(requester);

        // Perform ownership transfer
        boolean success = command.updateOwnership(targetUser, accManifestId);

        if (!success) {
            throw new AccessDeniedException("Ownership transfer failed due to insufficient permissions.");
        }

        makeLog(requester, accManifestId, Modified);
    }

    public void transferOwnership(
            ScoreUser requester, ScoreUser targetUser, AsccpManifestId asccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (targetUser == null) {
            throw new IllegalArgumentException("'targetUser' must not be null.");
        }

        if (asccpManifestId == null) {
            throw new IllegalArgumentException("'asccpManifestId' must not be null.");
        }

        var query = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord asccp = query.getAsccpSummary(asccpManifestId);
        if (asccp == null) {
            throw new IllegalStateException("ASCCP not found.");
        }

        if (CcState.WIP != asccp.state()) {
            throw new IllegalArgumentException("The ASCCP '" + asccp.den() + "' cannot be transferred " +
                    "because it is in the '" + asccp.state() + "' state. "
                    + "Only ASCCPs in the 'WIP' state can be transferred.");
        }

        if (!requester.isAdministrator() && !asccp.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.asccpCommandRepository(requester);

        // Perform ownership transfer
        boolean success = command.updateOwnership(targetUser, asccpManifestId);

        if (!success) {
            throw new AccessDeniedException("Ownership transfer failed due to insufficient permissions.");
        }

        makeLog(requester, asccpManifestId, Modified);
    }

    public void transferOwnership(
            ScoreUser requester, ScoreUser targetUser, BccpManifestId bccpManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (targetUser == null) {
            throw new IllegalArgumentException("'targetUser' must not be null.");
        }

        if (bccpManifestId == null) {
            throw new IllegalArgumentException("'bccpManifestId' must not be null.");
        }

        var query = repositoryFactory.bccpQueryRepository(requester);
        BccpSummaryRecord bccp = query.getBccpSummary(bccpManifestId);
        if (bccp == null) {
            throw new IllegalStateException("BCCP not found.");
        }

        if (CcState.WIP != bccp.state()) {
            throw new IllegalArgumentException("The BCCP '" + bccp.den() + "' cannot be transferred " +
                    "because it is in the '" + bccp.state() + "' state. "
                    + "Only BCCPs in the 'WIP' state can be transferred.");
        }

        if (!requester.isAdministrator() && !bccp.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.bccpCommandRepository(requester);

        // Perform ownership transfer
        boolean success = command.updateOwnership(targetUser, bccpManifestId);

        if (!success) {
            throw new AccessDeniedException("Ownership transfer failed due to insufficient permissions.");
        }

        makeLog(requester, bccpManifestId, Modified);
    }

    public void transferOwnership(
            ScoreUser requester, ScoreUser targetUser, DtManifestId dtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (targetUser == null) {
            throw new IllegalArgumentException("'targetUser' must not be null.");
        }

        if (dtManifestId == null) {
            throw new IllegalArgumentException("'dtManifestId' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtSummaryRecord dt = query.getDtSummary(dtManifestId);
        if (dt == null) {
            throw new IllegalStateException("DT not found.");
        }

        if (CcState.WIP != dt.state()) {
            throw new IllegalArgumentException("The DT '" + dt.den() + "' cannot be transferred " +
                    "because it is in the '" + dt.state() + "' state. "
                    + "Only DTs in the 'WIP' state can be transferred.");
        }

        if (!requester.isAdministrator() && !dt.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.dtCommandRepository(requester);

        // Perform ownership transfer
        boolean success = command.updateOwnership(targetUser, dtManifestId);

        if (!success) {
            throw new AccessDeniedException("Ownership transfer failed due to insufficient permissions.");
        }

        makeLog(requester, dtManifestId, Modified);
    }

    public boolean discard(ScoreUser requester, DtScManifestId dtScManifestId, boolean skipOwnershipCheck) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (dtScManifestId == null) {
            throw new IllegalArgumentException("'dtScManifestId' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);
        DtScSummaryRecord dtSc = query.getDtScSummary(dtScManifestId);
        if (dtSc == null) {
            throw new IllegalArgumentException("DT_SC record not found.");
        }

        DtSummaryRecord dt = query.getDtSummary(dtSc.ownerDtManifestId());
        if (dt == null) {
            throw new IllegalArgumentException("DT record not found.");
        }

        if (CcState.WIP != dt.state()) {
            throw new IllegalArgumentException("The DT_SC '" + dtSc.propertyTerm() + "' cannot be deleted " +
                    "because it is in the '" + dt.state() + "' state. "
                    + "Only DT_SCs in the 'WIP' state can be deleted.");
        }

        if (!skipOwnershipCheck && !requester.isAdministrator() && !dt.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        int bieUsageCount = repositoryFactory.topLevelAsbiepQueryRepository(requester).countReferences(dtScManifestId);
        if (bieUsageCount > 0) {
            throw new IllegalArgumentException("This association is referenced in " + bieUsageCount + " BIE(s) and cannot be deleted.");
        }

        for (DtScSummaryRecord inheritedDtSc : query.getInheritedDtScSummaryList(dtScManifestId)) {
            discard(requester, inheritedDtSc.dtScManifestId(), true);
        }

        var command = repositoryFactory.dtCommandRepository(requester);
        boolean updated = command.delete(dtScManifestId);
        if (updated) {
            makeLog(requester, dt.dtManifestId(), LogAction.Modified);
        }

        return updated;
    }

    public DtScManifestId createDtSc(ScoreUser requester, DtManifestId ownerDtManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (ownerDtManifestId == null) {
            throw new IllegalArgumentException("'ownerDtManifestId' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);

        DtSummaryRecord dt = query.getDtSummary(ownerDtManifestId);
        if (dt == null) {
            throw new IllegalArgumentException("DT record not found.");
        }

        if (CcState.WIP != dt.state()) {
            throw new IllegalArgumentException("The DT '" + dt.den() + "' cannot be transferred " +
                    "because it is in the '" + dt.state() + "' state. "
                    + "Only DTs in the 'WIP' state can be transferred.");
        }

        if (!requester.isAdministrator() && !dt.owner().userId().equals(requester.userId())) {
            throw new IllegalArgumentException("It only allows to modify the core component by the owner.");
        }

        var command = repositoryFactory.dtCommandRepository(requester);
        DtScManifestId dtScManifestId = command.appendDtSc(ownerDtManifestId);
        makeLog(requester, ownerDtManifestId, Modified);

        for (DtSummaryRecord inheritedDt : query.getInheritedDtSummaryList(ownerDtManifestId)) {
            createDtScFromBase(requester, inheritedDt.dtManifestId(), dtScManifestId);
        }

        return dtScManifestId;
    }

    public DtScManifestId createDtScFromBase(ScoreUser requester, DtManifestId ownerDtManifestId, DtScManifestId basedDtScManifestId) {

        if (requester == null) {
            throw new IllegalArgumentException("'requester' must not be null.");
        }

        if (ownerDtManifestId == null) {
            throw new IllegalArgumentException("'ownerDtManifestId' must not be null.");
        }

        var query = repositoryFactory.dtQueryRepository(requester);

        DtSummaryRecord dt = query.getDtSummary(ownerDtManifestId);
        if (dt == null) {
            throw new IllegalArgumentException("DT record not found.");
        }

        if (CcState.WIP != dt.state()) {
            throw new IllegalArgumentException("The DT '" + dt.den() + "' cannot be transferred " +
                    "because it is in the '" + dt.state() + "' state. "
                    + "Only DTs in the 'WIP' state can be transferred.");
        }

        var command = repositoryFactory.dtCommandRepository(requester);
        DtScManifestId dtScManifestId = command.createDtScFromBase(ownerDtManifestId, basedDtScManifestId);
        makeLog(requester, ownerDtManifestId, Modified);

        for (DtSummaryRecord inheritedDt : query.getInheritedDtSummaryList(ownerDtManifestId)) {
            createDtScFromBase(requester, inheritedDt.dtManifestId(), dtScManifestId);
        }

        return dtScManifestId;
    }

    public void discardCoreComponents(ScoreUser requester, Collection<ReleaseId> releaseIdSet) {
        // TODO
    }

    public AccManifestId appendUserExtension(
            ScoreUser requester, AccSummaryRecord eAcc, AccSummaryRecord ueAcc, ReleaseId releaseId) {
        if (requester.isDeveloper()) {
            throw new IllegalArgumentException("Developer cannot create User Extension.");
        }

        if (ueAcc != null) {
            if (ueAcc.state() == CcState.Production) {
                reviseAcc(requester, ueAcc.accManifestId());
            }
            return ueAcc.accManifestId();
        } else {
            return createNewUserExtensionGroupACC(requester, eAcc, releaseId);
        }
    }

    private AccManifestId createNewUserExtensionGroupACC(ScoreUser requester, AccSummaryRecord eAcc, ReleaseId releaseId) {
        String objectClassTerm = Utility.getUserExtensionGroupObjectClassTerm(eAcc.objectClassTerm());

        AccManifestId ueAccManifestId = createAcc(requester, new AccCreateRequest(
                releaseId, null,
                objectClassTerm, OagisComponentType.UserExtensionGroup, AccType.Extension,
                "A system created component containing user extension to the " + eAcc.objectClassTerm() + ".",
                null, null));

        AsccpManifestId ueAsccpManifestId = createAsccp(requester, new AsccpCreateRequest(
                releaseId, ueAccManifestId,
                objectClassTerm, AsccpType.Default, false,
                CcState.Production, null,
                new Definition("A system created component containing user extension to the " + eAcc.objectClassTerm() + ".", null),
                null));

        createAscc(requester, AsccCreateRequest.builder(eAcc.accManifestId(), ueAsccpManifestId)
                .cardinalityMin(1)
                .cardinalityMax(1)
                .skipReusableCheck(true)
                .build());

        return ueAccManifestId;
    }

    public AsccManifestId refactorAscc(ScoreUser requester, AsccManifestId asccManifestId, AccManifestId accManifestId) {

        var accQuery = repositoryFactory.accQueryRepository(requester);
        AsccSummaryRecord targetAscc = accQuery.getAsccSummary(asccManifestId);

        List<AsccSummaryRecord> targetAsccList = accQuery.getRefactorTargetAsccManifestList(asccManifestId, accManifestId);

        String hash = LogUtils.generateHash();

        for (AsccSummaryRecord ascc : targetAsccList) {
            discard(requester, ascc.asccManifestId(), false, LogAction.Refactored, hash);
        }

        return createAscc(requester, new AsccCreateRequest(
                accManifestId,
                targetAscc.toAsccpManifestId(),
                -1,
                targetAscc.cardinality(),
                false), LogAction.Refactored, hash, false);
    }

    public BccManifestId refactorBcc(ScoreUser requester, BccManifestId bccManifestId, AccManifestId accManifestId) {

        var accQuery = repositoryFactory.accQueryRepository(requester);
        BccSummaryRecord targetBcc = accQuery.getBccSummary(bccManifestId);

        List<BccSummaryRecord> targetBccList = accQuery.getRefactorTargetBccManifestList(bccManifestId, accManifestId);

        String hash = LogUtils.generateHash();

        for (BccSummaryRecord bcc : targetBccList) {
            discard(requester, bcc.bccManifestId(), false, LogAction.Refactored, hash);
        }

        return createBcc(requester, new BccCreateRequest(
                accManifestId,
                targetBcc.toBccpManifestId(),
                -1,
                targetBcc.cardinality()), LogAction.Refactored, hash, false);
    }

    public void ungroup(ScoreUser requester, AccManifestId accManifestId, AsccManifestId asccManifestId, int pos) {

        var accQuery = repositoryFactory.accQueryRepository(requester);
        AccSummaryRecord acc = accQuery.getAccSummary(accManifestId);
        if (acc == null) {
            throw new IllegalArgumentException("'accManifestId' parameter must not be null.");
        }

        AsccSummaryRecord ascc = accQuery.getAsccSummary(asccManifestId);
        if (ascc == null) {
            throw new IllegalArgumentException("'asccManifestId' parameter must not be null.");
        }

        var asccpQuery = repositoryFactory.asccpQueryRepository(requester);
        AsccpSummaryRecord toAsccp = asccpQuery.getAsccpSummary(ascc.toAsccpManifestId());
        AccSummaryRecord roleOfAcc = accQuery.getAccSummary(toAsccp.roleOfAccManifestId());

        Stack<AccSummaryRecord> accManifestRecordStack = new Stack();
        accManifestRecordStack.add(roleOfAcc);

        while (roleOfAcc.basedAccManifestId() != null) {
            roleOfAcc = accQuery.getAccSummary(roleOfAcc.basedAccManifestId());
            accManifestRecordStack.add(roleOfAcc);
        }

        String logHash = LogUtils.generateHash();

        var accCommand = repositoryFactory.accCommandRepository(requester);

        while (!accManifestRecordStack.isEmpty()) {
            roleOfAcc = accManifestRecordStack.pop();

            CoreComponentGraphContext coreComponentGraphContext =
                    graphContextRepository.buildGraphContext(requester, roleOfAcc.release().releaseId());
            List<Node> children = coreComponentGraphContext.findChildren(
                    coreComponentGraphContext.toNode(roleOfAcc), true);

            for (Node child : children) {
                if (child.getType() == Node.NodeType.ASCC) {
                    AsccSummaryRecord asccChild = accQuery.getAsccSummary((AsccManifestId) child.getManifestId());

                    createAscc(requester, AsccCreateRequest.builder(accManifestId, asccChild.toAsccpManifestId())
                            .pos(pos).build(), LogAction.IGNORE, logHash, true);
                } else if (child.getType() == Node.NodeType.BCC) {
                    BccSummaryRecord bccChild = accQuery.getBccSummary((BccManifestId) child.getManifestId());

                    createBcc(requester, BccCreateRequest.builder(accManifestId, bccChild.toBccpManifestId())
                            .pos(pos).build(), LogAction.IGNORE, logHash, true);
                }

                pos++;
            }
        }

        discard(requester, asccManifestId, false, LogAction.Ungrouped, logHash);
    }
}
