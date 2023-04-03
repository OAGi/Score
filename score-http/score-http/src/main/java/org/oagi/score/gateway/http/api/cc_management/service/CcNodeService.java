package org.oagi.score.gateway.http.api.cc_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.data.Release;
import org.oagi.score.data.Xbt;
import org.oagi.score.gateway.http.api.cc_management.data.*;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;
import org.oagi.score.gateway.http.api.cc_management.repository.CcNodeRepository;
import org.oagi.score.gateway.http.api.graph.data.Node;
import org.oagi.score.gateway.http.api.graph.service.GraphService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.redis.event.EventHandler;
import org.oagi.score.repo.CoreComponentRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.acc.*;
import org.oagi.score.repo.component.ascc.*;
import org.oagi.score.repo.component.asccp.*;
import org.oagi.score.repo.component.bcc.*;
import org.oagi.score.repo.component.bccp.*;
import org.oagi.score.repo.component.dt.*;
import org.oagi.score.repo.component.dt_sc.*;
import org.oagi.score.repo.component.graph.CoreComponentGraphContext;
import org.oagi.score.repo.component.graph.GraphContextRepository;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.BCCEntityType;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.service.log.model.LogAction;
import org.oagi.score.service.log.model.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ACC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ACC_MANIFEST;

@Service
@Transactional(readOnly = true)
public class CcNodeService extends EventHandler {

    @Autowired
    private CcNodeRepository repository;

    @Autowired
    private CoreComponentRepository ccRepository;

    @Autowired
    private AccReadRepository accReadRepository;

    @Autowired
    private AccWriteRepository accWriteRepository;

    @Autowired
    private AsccpWriteRepository asccpWriteRepository;

    @Autowired
    private AsccpReadRepository asccpReadRepository;

    @Autowired
    private BccpWriteRepository bccpWriteRepository;

    @Autowired
    private DtWriteRepository dtWriteRepository;

    @Autowired
    private DtScWriteRepository dtScWriteRepository;

    @Autowired
    private AsccReadRepository asccReadRepository;

    @Autowired
    private AsccWriteRepository asccWriteRepository;

    @Autowired
    private BccReadRepository bccReadRepository;

    @Autowired
    private BccWriteRepository bccWriteRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private GraphService graphService;

    @Autowired
    private GraphContextRepository graphContextRepository;

    public CcAccNode getAccNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getAccNodeByAccManifestId(user, manifestId);
    }

    public CcAsccpNode getAsccpNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getAsccpNodeByAsccpManifestId(user, manifestId);
    }

    public CcBccpNode getBccpNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getBccpNodeByBccpManifestId(user, manifestId);
    }

    public CcBdtNode getBdtNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getBdtNodeByBdtManifestId(user, manifestId);
    }

    public CcBdtScNode getDtScNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getDtScNodeByManifestId(user, manifestId);
    }

    @Transactional
    public void deleteAcc(AuthenticatedPrincipal user, BigInteger manifestId) {
        DeleteAccRepositoryRequest repositoryRequest =
                new DeleteAccRepositoryRequest(user, manifestId);

        DeleteAccRepositoryResponse repositoryResponse =
                accWriteRepository.deleteAcc(repositoryRequest);

        fireEvent(new DeletedAccEvent());
    }

    @Transactional
    public void deleteAsccp(AuthenticatedPrincipal user, BigInteger manifestId) {
        DeleteAsccpRepositoryRequest repositoryRequest =
                new DeleteAsccpRepositoryRequest(user, manifestId);

        DeleteAsccpRepositoryResponse repositoryResponse =
                asccpWriteRepository.deleteAsccp(repositoryRequest);

        fireEvent(new DeletedAsccpEvent());
    }

    @Transactional
    public void deleteBccp(AuthenticatedPrincipal user, BigInteger manifestId) {
        DeleteBccpRepositoryRequest repositoryRequest =
                new DeleteBccpRepositoryRequest(user, manifestId);

        DeleteBccpRepositoryResponse repositoryResponse =
                bccpWriteRepository.deleteBccp(repositoryRequest);

        fireEvent(new DeletedBccpEvent());
    }

    @Transactional
    public void deleteAscc(AuthenticatedPrincipal user, BigInteger asccManifestId, boolean ignoreState) {
        deleteAscc(user, asccManifestId, LogUtils.generateHash(), LogAction.Modified, ignoreState);
    }

    @Transactional
    public void deleteAscc(AuthenticatedPrincipal user, BigInteger asccManifestId) {
        deleteAscc(user, asccManifestId, LogUtils.generateHash(), LogAction.Modified, false);
    }

    @Transactional
    public void deleteAscc(AuthenticatedPrincipal user, BigInteger asccManifestId, String logHash, LogAction action, boolean ignoreState) {
        DeleteAsccRepositoryRequest request =
                new DeleteAsccRepositoryRequest(user, asccManifestId);

        request.setIgnoreState(ignoreState);
        request.setLogHash(logHash);
        request.setLogAction(action);

        asccWriteRepository.deleteAscc(request);

        fireEvent(new DeletedAsccEvent());
    }

    @Transactional
    public void deleteBcc(AuthenticatedPrincipal user, BigInteger bccManifestId) {
        DeleteBccRepositoryRequest request =
                new DeleteBccRepositoryRequest(user, bccManifestId);

        bccWriteRepository.deleteBcc(request);

        fireEvent(new DeletedBccEvent());
    }

    @Transactional
    public void deleteDt(AuthenticatedPrincipal user, BigInteger dtManifestId) {
        DeleteDtRepositoryRequest request =
                new DeleteDtRepositoryRequest(user, dtManifestId);

        dtWriteRepository.deleteDt(request);

        fireEvent(new DeletedDtEvent());
    }

    @Transactional
    public void deleteDtSc(AuthenticatedPrincipal user, BigInteger dtScManifestId) {
        DeleteDtScRepositoryRequest request =
                new DeleteDtScRepositoryRequest(user, dtScManifestId);

        dtScWriteRepository.deleteDtSc(request);

        fireEvent(new DeletedDtScEvent());
    }

    @Transactional
    public void purgeAcc(AuthenticatedPrincipal user, BigInteger manifestId) {
        purgeAcc(user, manifestId, false);
    }

    @Transactional
    public void purgeAcc(AuthenticatedPrincipal user, BigInteger manifestId, boolean ignoreOnError) {
        PurgeAccRepositoryRequest repositoryRequest =
                new PurgeAccRepositoryRequest(user, manifestId);
        repositoryRequest.setIgnoreOnError(ignoreOnError);

        PurgeAccRepositoryResponse repositoryResponse =
                accWriteRepository.purgeAcc(repositoryRequest);

        fireEvent(new DeletedAccEvent());
    }

    @Transactional
    public void purgeAsccp(AuthenticatedPrincipal user, BigInteger manifestId) {
        purgeAsccp(user, manifestId, false, false);
    }

    @Transactional
    public void purgeAsccp(AuthenticatedPrincipal user, BigInteger manifestId, boolean ignoreOnError, boolean ignoreState) {
        PurgeAsccpRepositoryRequest repositoryRequest =
                new PurgeAsccpRepositoryRequest(user, manifestId);
        repositoryRequest.setIgnoreState(ignoreState);
        repositoryRequest.setIgnoreOnError(ignoreOnError);

        PurgeAsccpRepositoryResponse repositoryResponse =
                asccpWriteRepository.purgeAsccp(repositoryRequest);

        fireEvent(new DeletedAsccpEvent());
    }

    @Transactional
    public void purgeBccp(AuthenticatedPrincipal user, BigInteger manifestId) {
        purgeBccp(user, manifestId, false);
    }

    @Transactional
    public void purgeBccp(AuthenticatedPrincipal user, BigInteger manifestId, boolean ignoreOnError) {
        PurgeBccpRepositoryRequest repositoryRequest =
                new PurgeBccpRepositoryRequest(user, manifestId);
        repositoryRequest.setIgnoreOnError(ignoreOnError);

        PurgeBccpRepositoryResponse repositoryResponse =
                bccpWriteRepository.purgeBccp(repositoryRequest);

        fireEvent(new DeletedBccpEvent());
    }

    @Transactional
    public void purgeDt(AuthenticatedPrincipal user, BigInteger dtManifestId) {
        purgeDt(user, dtManifestId);
    }

    @Transactional
    public void purgeDt(AuthenticatedPrincipal user, BigInteger dtManifestId, boolean ignoreOnError) {
        PurgeDtRepositoryRequest repositoryRequest =
                new PurgeDtRepositoryRequest(user, dtManifestId);
        repositoryRequest.setIgnoreOnError(ignoreOnError);

        dtWriteRepository.purgeDt(repositoryRequest);

        fireEvent(new DeletedDtEvent());
    }

    public CcAccNodeDetail getAccNodeDetail(AuthenticatedPrincipal user, CcAccNode accNode) {
        CcAccNodeDetail accNodeDetail = repository.getAccNodeDetail(user, accNode);
        if (accNodeDetail == null) {
            return accNodeDetail;
        }

        if (accNodeDetail.getReplacementAccManifestId() != null) {
            CcAccNode replacementAccNode = new CcAccNode();
            replacementAccNode.setManifestId(accNodeDetail.getReplacementAccManifestId());
            accNodeDetail.setReplacement(repository.getAccNodeDetail(user, replacementAccNode));
        }
        return accNodeDetail;
    }

    public CcAsccpNodeDetail getAsccpNodeDetail(AuthenticatedPrincipal user, CcAsccpNode asccpNode) {
        CcAsccpNodeDetail asccpNodeDetail = repository.getAsccpNodeDetail(user, asccpNode);
        if (asccpNodeDetail == null) {
            return asccpNodeDetail;
        }

        if ((asccpNodeDetail.getAscc() != null && asccpNodeDetail.getAscc().getReplacementAsccManifestId() != null) ||
            (asccpNodeDetail.getAsccp() != null && asccpNodeDetail.getAsccp().getReplacementAsccpManifestId() != null)) {

            CcAsccpNode replacementAsccpNode = new CcAsccpNode();
            if (asccpNodeDetail.getAscc() != null) {
                replacementAsccpNode.setAsccManifestId(asccpNodeDetail.getAscc().getReplacementAsccManifestId());
            }
            if (asccpNodeDetail.getAsccp() != null) {
                replacementAsccpNode.setManifestId(asccpNodeDetail.getAsccp().getReplacementAsccpManifestId());
            }

            CcAsccpNodeDetail replacement = repository.getAsccpNodeDetail(user, replacementAsccpNode);

            if (asccpNodeDetail.getAscc() != null) {
                asccpNodeDetail.getAscc().setReplacement(replacement.getAscc());
            }
            if (asccpNodeDetail.getAsccp() != null) {
                asccpNodeDetail.getAsccp().setReplacement(replacement.getAsccp());
            }
        }
        return asccpNodeDetail;
    }

    public CcBccpNodeDetail getBccpNodeDetail(AuthenticatedPrincipal user, CcBccpNode bccpNode) {
        CcBccpNodeDetail bccpNodeDetail = repository.getBccpNodeDetail(user, bccpNode);
        if (bccpNodeDetail == null) {
            return bccpNodeDetail;
        }

        if ((bccpNodeDetail.getBcc() != null && bccpNodeDetail.getBcc().getReplacementBccManifestId() != null) ||
            (bccpNodeDetail.getBccp() != null && bccpNodeDetail.getBccp().getReplacementBccpManifestId() != null)) {

            CcBccpNode replacementBccpNode = new CcBccpNode();
            if (bccpNodeDetail.getBcc() != null) {
                replacementBccpNode.setBccManifestId(bccpNodeDetail.getBcc().getReplacementBccManifestId());
            }
            if (bccpNodeDetail.getBccp() != null) {
                replacementBccpNode.setManifestId(bccpNodeDetail.getBccp().getReplacementBccpManifestId());
            }

            CcBccpNodeDetail replacement = repository.getBccpNodeDetail(user, replacementBccpNode);

            if (bccpNodeDetail.getBcc() != null) {
                bccpNodeDetail.getBcc().setReplacement(replacement.getBcc());
            }
            if (bccpNodeDetail.getBccp() != null) {
                bccpNodeDetail.getBccp().setReplacement(replacement.getBccp());
            }
        }
        return bccpNodeDetail;
    }

    public CcBdtNodeDetail getBdtNodeDetail(AuthenticatedPrincipal user, CcBdtNode bdtNode) {
        CcBdtNodeDetail bdtNodeDetail = repository.getBdtNodeDetail(user, bdtNode);
        if (bdtNodeDetail == null) {
            return bdtNodeDetail;
        }

        if (bdtNodeDetail.getReplacementDtManifestId() != null) {
            CcBdtNode replacementDtNode = new CcBdtNode();
            replacementDtNode.setManifestId(bdtNodeDetail.getReplacementDtManifestId());
            bdtNodeDetail.setReplacement(repository.getBdtNodeDetail(user, replacementDtNode));
        }
        return bdtNodeDetail;
    }

    public CcBdtScNodeDetail getBdtScNodeDetail(AuthenticatedPrincipal user, CcBdtScNode bdtScNode) {
        CcBdtScNodeDetail bdtScNodeDetail = repository.getBdtScNodeDetail(user, bdtScNode);
        if (bdtScNodeDetail == null) {
            return bdtScNodeDetail;
        }

        if (bdtScNodeDetail.getReplacementDtScManifestId() != null) {
            CcBdtScNode replacementDtScNode = new CcBdtScNode();
            replacementDtScNode.setManifestId(bdtScNodeDetail.getReplacementDtScManifestId());
            bdtScNodeDetail.setReplacement(repository.getBdtScNodeDetail(user, replacementDtScNode));
        }
        return bdtScNodeDetail;
    }

    public CcAsccpNodeDetail.Asccp getAsccp(BigInteger asccpId) {
        return repository.getAsccp(asccpId);
    }

    @Transactional
    public BigInteger createAcc(AuthenticatedPrincipal user, CcAccCreateRequest request) {
        isPublishedRelease(request.getReleaseId());
        CreateAccRepositoryRequest repositoryRequest =
                new CreateAccRepositoryRequest(user, request.getReleaseId());

        CreateAccRepositoryResponse repositoryResponse =
                accWriteRepository.createAcc(repositoryRequest);

        fireEvent(new CreatedAccEvent());

        return repositoryResponse.getAccManifestId();
    }

    @Transactional
    public BigInteger createAsccp(AuthenticatedPrincipal user, CcAsccpCreateRequest request) {
        isPublishedRelease(request.getReleaseId());
        CreateAsccpRepositoryRequest repositoryRequest =
                new CreateAsccpRepositoryRequest(user,
                        request.getRoleOfAccManifestId(), request.getReleaseId());

        if (StringUtils.hasLength(repositoryRequest.getInitialPropertyTerm())) {
            repositoryRequest.setInitialPropertyTerm(request.getInitialPropertyTerm());
        }

        if (request.getAsccpType() != null) {
            repositoryRequest.setInitialType(CcASCCPType.valueOf(request.getAsccpType()));
            repositoryRequest.setTag(repositoryRequest.getInitialType().name());
        }

        CreateAsccpRepositoryResponse repositoryResponse =
                asccpWriteRepository.createAsccp(repositoryRequest);

        fireEvent(new CreatedAsccpEvent());

        return repositoryResponse.getAsccpManifestId();
    }

    @Transactional
    public BigInteger createBccp(AuthenticatedPrincipal user, CcBccpCreateRequest request) {
        isPublishedRelease(request.getReleaseId());
        CreateBccpRepositoryRequest repositoryRequest =
                new CreateBccpRepositoryRequest(user,
                        request.getBdtManifestId(), request.getReleaseId());

        CreateBccpRepositoryResponse repositoryResponse =
                bccpWriteRepository.createBccp(repositoryRequest);

        fireEvent(new CreatedBccpEvent());

        return repositoryResponse.getBccpManifestId();
    }

    @Transactional
    public BigInteger createBdt(AuthenticatedPrincipal user, CcBdtCreateRequest request) {
        isPublishedRelease(request.getReleaseId());
        CreateBdtRepositoryRequest repositoryRequest =
                new CreateBdtRepositoryRequest(user,
                        request.getBdtManifestId(), request.getReleaseId(), request.getSpecId());

        CreateBdtRepositoryResponse repositoryResponse =
                dtWriteRepository.createBdt(repositoryRequest);

        fireEvent(new CreatedBdtEvent());

        return repositoryResponse.getBdtManifestId();
    }

    @Transactional
    public BigInteger createAccExtension(AuthenticatedPrincipal user, CcExtensionCreateRequest request) {
        LocalDateTime timestamp = LocalDateTime.now();
        AccManifestRecord accManifestRecord = accReadRepository.getAccManifest(request.getAccManifestId());
        BigInteger releaseId = accManifestRecord.getReleaseId().toBigInteger();
        AccRecord accRecord = accReadRepository.getAccByManifestId(request.getAccManifestId());

        if (!StringUtils.hasLength(accRecord.getObjectClassTerm())) {
            throw new IllegalArgumentException("Object Class Term is required.");
        }

        if (accRecord.getNamespaceId() == null) {
            throw new IllegalArgumentException("Namespace is required.");
        }

        AccManifestRecord allExtension
                = accReadRepository.getAllExtensionAccManifest(releaseId);

        // create extension ACC
        CreateAccRepositoryRequest createAccRepositoryRequest
                = new CreateAccRepositoryRequest(user, timestamp, releaseId);

        createAccRepositoryRequest.setInitialComponentType(OagisComponentType.Extension);
        createAccRepositoryRequest.setInitialType(CcACCType.Extension);
        createAccRepositoryRequest.setInitialObjectClassTerm(accRecord.getObjectClassTerm() + " Extension");
        createAccRepositoryRequest.setBasedAccManifestId(allExtension.getAccManifestId().toBigInteger());
        if (accRecord.getNamespaceId() != null) {
            createAccRepositoryRequest.setNamespaceId(accRecord.getNamespaceId().toBigInteger());
        }

        CreateAccRepositoryResponse createAccRepositoryResponse
                = accWriteRepository.createAcc(createAccRepositoryRequest);

        BigInteger extensionAccManifestId = createAccRepositoryResponse.getAccManifestId();

        // create extension ASCCP
        CreateAsccpRepositoryRequest createAsccpRepositoryRequest
                = new CreateAsccpRepositoryRequest(user, timestamp, extensionAccManifestId, releaseId);

        String extensionAsccpDefintion = "Allows the user of OAGIS to extend the specification in order to " +
                "provide additional information that is not captured in OAGIS.";
        String extensionAsccpDefintionSource = "http://www.openapplications.org/oagis/10/platform/2";
        createAsccpRepositoryRequest.setInitialPropertyTerm("Extension");
        createAsccpRepositoryRequest.setInitialType(CcASCCPType.Extension);
        createAsccpRepositoryRequest.setDefinition(extensionAsccpDefintion);
        createAsccpRepositoryRequest.setDefinitionSource(extensionAsccpDefintionSource);
        if (accRecord.getNamespaceId() != null) {
            createAsccpRepositoryRequest.setNamespaceId(accRecord.getNamespaceId().toBigInteger());
        }
        createAsccpRepositoryRequest.setReusable(false);

        CreateAsccpRepositoryResponse createAsccpRepositoryResponse
                = asccpWriteRepository.createAsccp(createAsccpRepositoryRequest);
        BigInteger extensionAsccpManifestId = createAsccpRepositoryResponse.getAsccpManifestId();

        // create ASCC between extension ACC and extension ASCCP
        CreateAsccRepositoryRequest createAsccRepositoryRequest
                = new CreateAsccRepositoryRequest(user, timestamp, releaseId,
                request.getAccManifestId(), extensionAsccpManifestId);

        asccWriteRepository.createAscc(createAsccRepositoryRequest);

        return request.getAccManifestId();
    }

    private void updateExtensionComponentProperties(AuthenticatedPrincipal user, CcAccNodeDetail accNodeDetail) {
        LocalDateTime timestamp = LocalDateTime.now();
        AccManifestRecord accManifestRecord = accReadRepository.getAccManifest(accNodeDetail.getManifestId());
        AccRecord accRecord = accReadRepository.getAccByManifestId(accManifestRecord.getAccManifestId().toBigInteger());

        AsccpManifestRecord extensionManifestAsccp = asccpReadRepository.getExtensionAsccpManifestByAccManifestId(
                accManifestRecord.getAccManifestId().toBigInteger()
        );
        AsccpRecord extensionAsccp
                = asccpReadRepository.getAsccpByManifestId(extensionManifestAsccp.getAsccpManifestId().toBigInteger());

        AccManifestRecord extensionAcc
                = accReadRepository.getAccManifest(extensionManifestAsccp.getRoleOfAccManifestId().toBigInteger());

        // update extension ACC
        UpdateAccPropertiesRepositoryRequest updateAccPropertiesRepositoryRequest
                = new UpdateAccPropertiesRepositoryRequest(
                        user,
                        timestamp,
                        extensionAcc.getAccManifestId().toBigInteger());

        updateAccPropertiesRepositoryRequest.setObjectClassTerm(accRecord.getObjectClassTerm() + " Extension");
        if (accRecord.getNamespaceId() != null) {
            updateAccPropertiesRepositoryRequest.setNamespaceId(accRecord.getNamespaceId().toBigInteger());
        }
        accWriteRepository.updateAccProperties(updateAccPropertiesRepositoryRequest);

        // update extension ASCCP
        UpdateAsccpPropertiesRepositoryRequest updateAsccpPropertiesRepositoryRequest
                = new UpdateAsccpPropertiesRepositoryRequest(
                user,
                timestamp,
                extensionManifestAsccp.getAsccpManifestId().toBigInteger());

        updateAsccpPropertiesRepositoryRequest.setPropertyTerm(extensionAsccp.getPropertyTerm());
        updateAsccpPropertiesRepositoryRequest.setDefinition(extensionAsccp.getDefinition());
        updateAsccpPropertiesRepositoryRequest.setDefinitionSource(extensionAsccp.getDefinitionSource());

        if (accRecord.getNamespaceId() != null) {
            updateAsccpPropertiesRepositoryRequest.setNamespaceId(accRecord.getNamespaceId().toBigInteger());
        }

        asccpWriteRepository.updateAsccpProperties(updateAsccpPropertiesRepositoryRequest);
    }

    private void updateUserExtensionNamespace(AuthenticatedPrincipal user, CcAccNodeDetail accNodeDetail) {
        LocalDateTime timestamp = LocalDateTime.now();
        AccManifestRecord accManifestRecord = accReadRepository.getAccManifest(accNodeDetail.getManifestId());
        AccRecord accRecord = accReadRepository.getAccByManifestId(accManifestRecord.getAccManifestId().toBigInteger());

        AsccpManifestRecord extensionManifestAsccp = asccpReadRepository.getUserExtensionAsccpManifestByAccManifestId(
                accManifestRecord.getAccManifestId().toBigInteger()
        );

        // update extension ASCCP
        UpdateAsccpPropertiesRepositoryRequest updateAsccpPropertiesRepositoryRequest
                = new UpdateAsccpPropertiesRepositoryRequest(
                user,
                timestamp,
                extensionManifestAsccp.getAsccpManifestId().toBigInteger());

        if (accRecord.getNamespaceId() != null) {
            updateAsccpPropertiesRepositoryRequest.setNamespaceId(accRecord.getNamespaceId().toBigInteger());
        }
        asccpWriteRepository.updateAsccpNamespace(updateAsccpPropertiesRepositoryRequest);
    }

    private void updateExtensionComponentState(AuthenticatedPrincipal user, BigInteger accManifestId,
                                               CcState fromState, CcState toState) {
        LocalDateTime timestamp = LocalDateTime.now();
        AccManifestRecord accManifestRecord = accReadRepository.getAccManifest(accManifestId);

        AsccpManifestRecord extensionAsccp = asccpReadRepository.getExtensionAsccpManifestByAccManifestId(
                accManifestRecord.getAccManifestId().toBigInteger()
        );

        AccManifestRecord extensionAcc
                = accReadRepository.getAccManifest(extensionAsccp.getRoleOfAccManifestId().toBigInteger());

        // update extension ACC
        UpdateAccStateRepositoryRequest updateAccStateRepositoryRequest
                = new UpdateAccStateRepositoryRequest(
                user,
                timestamp,
                extensionAcc.getAccManifestId().toBigInteger(),
                fromState,
                toState);

        accWriteRepository.updateAccState(updateAccStateRepositoryRequest);

        // update extension ASCCP
        UpdateAsccpStateRepositoryRequest updateAsccpStateRepositoryRequest
                = new UpdateAsccpStateRepositoryRequest(
                user,
                timestamp,
                extensionAsccp.getAsccpManifestId().toBigInteger(),
                fromState,
                toState);

        asccpWriteRepository.updateAsccpState(updateAsccpStateRepositoryRequest);
    }

    @Transactional
    public CcUpdateResponse updateCcDetails(AuthenticatedPrincipal user, CcUpdateRequest ccUpdateRequest) {
        CcUpdateResponse ccUpdateResponse = new CcUpdateResponse();

        ccUpdateResponse.setAccNodeResults(
                updateAccDetail(user, ccUpdateRequest.getAccNodeDetails()));
        ccUpdateResponse.setAsccpNodeResults(
                updateAsccp(user, ccUpdateRequest.getAsccpNodeDetails()));
        ccUpdateResponse.setBccpNodeResults(
                updateBccpDetail(user, ccUpdateRequest.getBccpNodeDetails()));
        ccUpdateResponse.setDtNodeResults(
                updateDtDetail(user, ccUpdateRequest.getDtNodeDetails()));
        ccUpdateResponse.setBdtScNodeResults(
                updateDtScDetail(user, ccUpdateRequest.getDtScNodeDetails()));

        return ccUpdateResponse;
    }

    @Transactional
    public void updateCcSeq(AuthenticatedPrincipal user,
                            BigInteger accManifestId,
                            Pair<CcId, CcId> itemAfterPair) {

        UpdateSeqKeyRequest request =
                new UpdateSeqKeyRequest(user, accManifestId, itemAfterPair);

        accWriteRepository.moveSeq(request);
    }

    @Transactional
    public List<CcAccNodeDetail> updateAccDetail(AuthenticatedPrincipal user, List<CcAccNodeDetail> ccAccNodeDetails) {
        LocalDateTime timestamp = LocalDateTime.now();
        List<CcAccNodeDetail> updatedAccNodeDetails = new ArrayList<>();
        for (CcAccNodeDetail detail : ccAccNodeDetails) {
            CcAccNode ccAccNode = updateAccDetail(user, timestamp, detail);
            updatedAccNodeDetails.add(getAccNodeDetail(user, ccAccNode));
            // Do not sync data b/w ACC and Extension components #916
//            if (hasExtensionAssociation(user, ccAccNode.getManifestId())) {
//                updateExtensionComponentProperties(user, detail);
//            }
            if (isUserExtensionGroup(user, ccAccNode.getManifestId())) {
                updateUserExtensionNamespace(user, detail);
            }
        }
        return updatedAccNodeDetails;
    }

    private boolean hasExtensionAssociation(AuthenticatedPrincipal user, BigInteger accManifestId) {
        AsccpManifestRecord extension
                = asccpReadRepository.getExtensionAsccpManifestByAccManifestId(accManifestId);
        return extension != null;
    }

    private boolean isUserExtensionGroup(AuthenticatedPrincipal user, BigInteger accManifestId) {
        AccRecord acc
                = accReadRepository.getAccByManifestId(accManifestId);
        return acc.getOagisComponentType().equals(OagisComponentType.UserExtensionGroup.getValue());
    }

    @Transactional
    public List<CcAsccpNodeDetail> updateAsccp(AuthenticatedPrincipal user, List<CcAsccpNodeDetail> asccpNodeDetails) {
        LocalDateTime timestamp = LocalDateTime.now();
        List<CcAsccpNodeDetail> updatedAsccpNodeDetails = new ArrayList<>();
        for (CcAsccpNodeDetail detail : asccpNodeDetails) {
            if (detail.getAscc() != null) {
                updateAsccDetail(user, timestamp, detail.getAscc());
            } else {
                updateAsccpDetail(user, timestamp, detail.getAsccp());
            }
            CcAsccpNode ccAsccpNode = getAsccpNode(user, detail.getAsccp().getManifestId());
            updatedAsccpNodeDetails.add(getAsccpNodeDetail(user, ccAsccpNode));
        }
        return updatedAsccpNodeDetails;
    }

    @Transactional
    public List<CcBccpNodeDetail> updateBccpDetail(AuthenticatedPrincipal user, List<CcBccpNodeDetail> bccpNodeDetails) {
        LocalDateTime timestamp = LocalDateTime.now();
        List<CcBccpNodeDetail> updatedBccpNodeDetails = new ArrayList<>();
        for (CcBccpNodeDetail detail : bccpNodeDetails) {
            if(detail.getBcc() != null) {
                updateBccDetail(user, timestamp, detail.getBcc());
            } else {
                updateBccpDetail(user, timestamp, detail.getBccp());
            }
            CcBccpNode ccBccpNode = getBccpNode(user, detail.getBccp().getManifestId());
            updatedBccpNodeDetails.add(getBccpNodeDetail(user, ccBccpNode));
        }
        return updatedBccpNodeDetails;
    }

    @Transactional
    public List<CcBdtNodeDetail> updateDtDetail(AuthenticatedPrincipal user, List<CcBdtNodeDetail> dtNodeDetails) {
        LocalDateTime timestamp = LocalDateTime.now();
        List<CcBdtNodeDetail> updatedDtNodeDetails = new ArrayList<>();
        for (CcBdtNodeDetail detail : dtNodeDetails) {
            updateDtDetail(user, timestamp, detail);
            CcBdtNode ccDtNode = getBdtNode(user, detail.getManifestId());
            updatedDtNodeDetails.add(getBdtNodeDetail(user, ccDtNode));
        }
        return updatedDtNodeDetails;
    }

    @Transactional
    public List<CcBdtScNodeDetail> updateDtScDetail(AuthenticatedPrincipal user, List<CcBdtScNodeDetail> dtScNodeDetails) {
        LocalDateTime timestamp = LocalDateTime.now();
        List<CcBdtScNodeDetail> updatedDtScNodeDetails = new ArrayList<>();
        for (CcBdtScNodeDetail detail : dtScNodeDetails) {
            updateDtScDetail(user, timestamp, detail);
            CcBdtScNode ccDtScNode = getDtScNode(user, detail.getManifestId());
            updatedDtScNodeDetails.add(getBdtScNodeDetail(user, ccDtScNode));
        }
        return updatedDtScNodeDetails;
    }

    private CcAccNode updateAccDetail(AuthenticatedPrincipal user, LocalDateTime timestamp, CcAccNodeDetail detail) {
        UpdateAccPropertiesRepositoryRequest request =
                new UpdateAccPropertiesRepositoryRequest(user, timestamp, detail.getManifestId());

        request.setObjectClassTerm(detail.getObjectClassTerm());
        request.setDefinition(detail.getDefinition());
        request.setDefinitionSource(detail.getDefinitionSource());
        request.setComponentType(OagisComponentType.valueOf(detail.getOagisComponentType()));
        request.setAbstract(detail.isAbstracted());
        request.setDeprecated(detail.isDeprecated());
        request.setNamespaceId(detail.getNamespaceId());

        UpdateAccPropertiesRepositoryResponse response =
                accWriteRepository.updateAccProperties(request);

        fireEvent(new UpdatedAccPropertiesEvent());

        return repository.getAccNodeByAccManifestId(user, response.getAccManifestId());
    }

    public CcAsccpNode updateAsccpDetail(AuthenticatedPrincipal user, LocalDateTime timestamp, CcAsccpNodeDetail.Asccp detail) {
        UpdateAsccpPropertiesRepositoryRequest request =
                new UpdateAsccpPropertiesRepositoryRequest(user, timestamp, detail.getManifestId());

        request.setPropertyTerm(detail.getPropertyTerm());
        request.setDefinition(detail.getDefinition());
        request.setDefinitionSource(detail.getDefinitionSource());
        request.setReusable(detail.isReusable());
        request.setDeprecated(detail.isDeprecated());
        request.setNillable(detail.isNillable());
        request.setNamespaceId(detail.getNamespaceId());

        UpdateAsccpPropertiesRepositoryResponse response =
                asccpWriteRepository.updateAsccpProperties(request);

        fireEvent(new UpdatedAsccpPropertiesEvent());

        return repository.getAsccpNodeByAsccpManifestId(user, response.getAsccpManifestId());
    }

    private void updateAsccDetail(AuthenticatedPrincipal user, LocalDateTime timestamp, CcAsccpNodeDetail.Ascc detail) {
        UpdateAsccPropertiesRepositoryRequest request =
                new UpdateAsccPropertiesRepositoryRequest(user, timestamp, detail.getManifestId());

        request.setCardinalityMin(detail.getCardinalityMin());
        request.setCardinalityMax(detail.getCardinalityMax());
        request.setDefinition(detail.getDefinition());
        request.setDefinitionSource(detail.getDefinitionSource());
        request.setDeprecated(detail.isDeprecated());

        asccWriteRepository.updateAsccProperties(request);

        fireEvent(new UpdatedAsccPropertiesEvent());
    }

    private void updateBccDetail(AuthenticatedPrincipal user, LocalDateTime timestamp, CcBccpNodeDetail.Bcc detail) {
        UpdateBccPropertiesRepositoryRequest request =
                new UpdateBccPropertiesRepositoryRequest(user, timestamp, detail.getManifestId());

        request.setCardinalityMin(detail.getCardinalityMin());
        request.setCardinalityMax(detail.getCardinalityMax());
        request.setDefinition(detail.getDefinition());
        request.setDefinitionSource(detail.getDefinitionSource());
        request.setEntityType(BCCEntityType.valueOf(detail.getEntityType()));
        request.setDeprecated(detail.isDeprecated());
        request.setNillable(detail.isNillable());

        if (detail.getDefaultValue() != null) {
            request.setDefaultValue(detail.getDefaultValue());
            request.setFixedValue(null);
        } else if (detail.getFixedValue() != null) {
            request.setDefaultValue(null);
            request.setFixedValue(detail.getFixedValue());
        } else {
            request.setDefaultValue(null);
            request.setFixedValue(null);
        }

        bccWriteRepository.updateBccProperties(request);

        fireEvent(new UpdatedBccPropertiesEvent());
    }

    private CcBccpNode updateBccpDetail(AuthenticatedPrincipal user, LocalDateTime timestamp, CcBccpNodeDetail.Bccp detail) {
        UpdateBccpPropertiesRepositoryRequest request =
                new UpdateBccpPropertiesRepositoryRequest(user, timestamp, detail.getManifestId());

        request.setPropertyTerm(detail.getPropertyTerm());
        request.setDefaultValue(detail.getDefaultValue());
        request.setFixedValue(detail.getFixedValue());
        request.setDefinition(detail.getDefinition());
        request.setDefinitionSource(detail.getDefinitionSource());
        request.setDeprecated(detail.isDeprecated());
        request.setNillable(detail.isNillable());
        request.setNamespaceId(detail.getNamespaceId());

        UpdateBccpPropertiesRepositoryResponse response =
                bccpWriteRepository.updateBccpProperties(request);

        fireEvent(new UpdatedBccpPropertiesEvent());

        return repository.getBccpNodeByBccpManifestId(user, response.getBccpManifestId());
    }

    private CcBdtNode updateDtDetail(AuthenticatedPrincipal user, LocalDateTime timestamp, CcBdtNodeDetail detail) {
        UpdateDtPropertiesRepositoryRequest request =
                new UpdateDtPropertiesRepositoryRequest(user, timestamp, detail.getManifestId());

        request.setQualifier(detail.getQualifier());
        request.setSixDigitId(detail.getSixDigitId());
        request.setContentComponentDefinition(detail.getContentComponentDefinition());
        request.setDefinition(detail.getDefinition());
        request.setDefinitionSource(detail.getDefinitionSource());
        request.setDeprecated(detail.isDeprecated());
        request.setNamespaceId(detail.getNamespaceId());
        request.setBdtPriRestriList(detail.getBdtPriRestriList());

        UpdateDtPropertiesRepositoryResponse response =
                dtWriteRepository.updateDtProperties(request);

        fireEvent(new UpdatedDtPropertiesEvent());

        return repository.getBdtNodeByBdtManifestId(user, response.getDtManifestId());
    }

    private CcBdtScNode updateDtScDetail(AuthenticatedPrincipal user, LocalDateTime timestamp, CcBdtScNodeDetail detail) {
        UpdateDtScPropertiesRepositoryRequest request =
                new UpdateDtScPropertiesRepositoryRequest(user, timestamp, detail.getManifestId());

        request.setPropertyTerm(detail.getPropertyTerm());
        request.setDefaultValue(detail.getDefaultValue());
        request.setFixedValue(detail.getFixedValue());
        request.setDefinition(detail.getDefinition());
        request.setCardinalityMax(detail.getCardinalityMax());
        request.setCardinalityMin(detail.getCardinalityMin());
        request.setDefinitionSource(detail.getDefinitionSource());
        request.setDeprecated(detail.getDeprecated());
        request.setCcBdtScPriResriList(detail.getBdtScPriRestriList());
        request.setRepresentationTerm(detail.getRepresentationTerm());

        UpdateDtScPropertiesRepositoryResponse response =
                dtScWriteRepository.updateDtScProperties(request);

        fireEvent(new UpdatedDtScPropertiesEvent());

        return repository.getDtScNodeByManifestId(user, response.getDtScManifestId());
    }

    @Transactional
    public BigInteger appendAsccp(AuthenticatedPrincipal user, BigInteger releaseId,
                                  BigInteger accManifestId, BigInteger asccpManifestId,
                                  int pos) {
        return appendAsccp(user, releaseId, accManifestId, asccpManifestId, pos, LogUtils.generateHash(), LogAction.Modified);
    }

    @Transactional
    public BigInteger appendAsccp(AuthenticatedPrincipal user, BigInteger releaseId,
                                  BigInteger accManifestId, BigInteger asccpManifestId,
                                  int pos, String logHash, LogAction action) {
        LocalDateTime timestamp = LocalDateTime.now();
        CreateAsccRepositoryRequest request =
                new CreateAsccRepositoryRequest(user, timestamp, releaseId, accManifestId, asccpManifestId);
        request.setPos(pos);
        request.setLogHash(logHash);
        request.setLogAction(action);

        CreateAsccRepositoryResponse response = asccWriteRepository.createAscc(request);
        fireEvent(new CreatedAsccEvent());
        return response.getAsccManifestId();
    }

    @Transactional
    public BigInteger appendBccp(AuthenticatedPrincipal user, BigInteger releaseId,
                                 BigInteger accManifestId, BigInteger bccpManifestId,
                                 int pos) {
        return appendBccp(user, releaseId, accManifestId, bccpManifestId, pos, LogUtils.generateHash(), LogAction.Modified);
    }

    @Transactional
    public BigInteger appendBccp(AuthenticatedPrincipal user, BigInteger releaseId,
                                 BigInteger accManifestId, BigInteger bccpManifestId,
                                 int pos, String logHash, LogAction action) {
        LocalDateTime timestamp = LocalDateTime.now();
        CreateBccRepositoryRequest request =
                new CreateBccRepositoryRequest(user, timestamp, releaseId, accManifestId, bccpManifestId);
        request.setPos(pos);
        request.setLogHash(logHash);
        request.setLogAction(action);

        CreateBccRepositoryResponse response = bccWriteRepository.createBcc(request);
        fireEvent(new CreatedBccEvent());
        return response.getBccManifestId();
    }

    @Transactional
    public BigInteger appendDtSc(AuthenticatedPrincipal user, BigInteger ownerDtManifestId) {
        CreateDtScRepositoryRequest request =
                new CreateDtScRepositoryRequest(user, ownerDtManifestId);

        CreateDtScRepositoryResponse response = dtWriteRepository.createDtSc(request);
        fireEvent(new CreatedDtScEvent());
        return response.getDtScManifestId();
    }

    @Transactional
    public void addDtRestriction(AuthenticatedPrincipal user, CcCreateRestrictionRequest createRestrictionRequest) {

        if (createRestrictionRequest.getRestrictionType().equals(PrimitiveRestriType.Primitive.name())) {

            createRestrictionRequest.getPrimitiveXbtMapList().forEach(e -> {
                CreatePrimitiveRestrictionRepositoryRequest request =
                        new CreatePrimitiveRestrictionRepositoryRequest(user, createRestrictionRequest.getDtManifestId(), createRestrictionRequest.getReleaseId());

                request.setPrimitive(e.getPrimitive());
                request.setXbtManifestIdList(e.getXbtList().stream().map(Xbt::getManifestId).collect(Collectors.toList()));
                dtWriteRepository.addDtPrimitiveRestriction(request);
            });

        } else if (createRestrictionRequest.getRestrictionType().equals(PrimitiveRestriType.CodeList.name())) {
            CreateCodeListRestrictionRepositoryRequest request =
                    new CreateCodeListRestrictionRepositoryRequest(user, createRestrictionRequest.getDtManifestId(), createRestrictionRequest.getReleaseId());

            request.setCodeListManifestId(createRestrictionRequest.getCodeListManifestId());
            dtWriteRepository.addDtCodeListRestriction(request);
        } else if (createRestrictionRequest.getRestrictionType().equals(PrimitiveRestriType.AgencyIdList.name())) {
            CreateAgencyIdListRestrictionRepositoryRequest request =
                    new CreateAgencyIdListRestrictionRepositoryRequest(user, createRestrictionRequest.getDtManifestId(), createRestrictionRequest.getReleaseId());

            request.setAgencyIdListManifestId(createRestrictionRequest.getAgencyIdListManifestId());
            dtWriteRepository.addDtAgencyIdListRestriction(request);
        }

        fireEvent(new UpdatedDtEvent());
    }

    public List<CcBdtScPriRestri> getDefaultPrimitiveValues(AuthenticatedPrincipal user,
                                                            String representationTerm, BigInteger bdtScManifestId) {
        if (repository.bdtScHasRepresentationTermSameAs(representationTerm, bdtScManifestId)) {
            CcBdtScNode bdtScNode = new CcBdtScNode();
            bdtScNode.setManifestId(bdtScManifestId);
            CcBdtScNodeDetail bdtScNodeDetail = getBdtScNodeDetail(user, bdtScNode);
            return bdtScNodeDetail.getBdtScPriRestriList();
        }
        return repository.getDefaultPrimitiveValues(representationTerm);
    }

    @Transactional
    public BigInteger updateAccBasedAcc(AuthenticatedPrincipal user, BigInteger accManifestId, BigInteger basedAccManifestId) {
        UpdateAccBasedAccRepositoryRequest repositoryRequest =
                new UpdateAccBasedAccRepositoryRequest(user, accManifestId, basedAccManifestId);

        UpdateAccBasedAccRepositoryResponse repositoryResponse =
                accWriteRepository.updateAccBasedAcc(repositoryRequest);

        fireEvent(new UpdatedAccBasedAccEvent());

        return repositoryResponse.getAccManifestId();
    }

    @Transactional
    public UpdateAsccpRoleOfAccRepositoryResponse updateAsccpRoleOfAcc(AuthenticatedPrincipal user, BigInteger asccpManifestId, BigInteger roleOfAccManifestId) {
        UpdateAsccpRoleOfAccRepositoryRequest repositoryRequest =
                new UpdateAsccpRoleOfAccRepositoryRequest(user, asccpManifestId, roleOfAccManifestId);

        UpdateAsccpRoleOfAccRepositoryResponse repositoryResponse =
                asccpWriteRepository.updateAsccpBdt(repositoryRequest);

        fireEvent(new UpdatedAsccpRoleOfAccEvent());

        return repositoryResponse;
    }

    @Transactional
    public UpdateBccpBdtRepositoryResponse updateBccpBdt(AuthenticatedPrincipal user, BigInteger bccpManifestId, BigInteger bdtManifestId) {
        UpdateBccpBdtRepositoryRequest repositoryRequest =
                new UpdateBccpBdtRepositoryRequest(user, bccpManifestId, bdtManifestId);

        UpdateBccpBdtRepositoryResponse repositoryResponse =
                bccpWriteRepository.updateBccpBdt(repositoryRequest);

        fireEvent(new UpdatedBccpBdtEvent());

        return repositoryResponse;
    }

    @Transactional
    public BigInteger updateAccState(AuthenticatedPrincipal user, BigInteger accManifestId, CcState toState) {
        CcState fromState = repository.getAccState(accManifestId);
        return updateAccState(user, accManifestId, fromState, toState);
    }

    @Transactional
    public BigInteger updateAccState(AuthenticatedPrincipal user, BigInteger accManifestId, CcState fromState, CcState toState) {
        UpdateAccStateRepositoryRequest repositoryRequest =
                new UpdateAccStateRepositoryRequest(user, accManifestId, fromState, toState);

        UpdateAccStateRepositoryResponse repositoryResponse =
                accWriteRepository.updateAccState(repositoryRequest);

        fireEvent(new UpdatedAccStateEvent());

        return repositoryResponse.getAccManifestId();
    }

    @Transactional
    public BigInteger updateAsccpState(AuthenticatedPrincipal user, BigInteger asccpManifestId, CcState toState) {
        CcState fromState = repository.getAsccpState(asccpManifestId);
        return updateAsccpState(user, asccpManifestId, fromState, toState);
    }

    @Transactional
    public BigInteger updateAsccpState(AuthenticatedPrincipal user, BigInteger asccpManifestId, CcState fromState, CcState toState) {
        UpdateAsccpStateRepositoryRequest repositoryRequest =
                new UpdateAsccpStateRepositoryRequest(user, asccpManifestId, fromState, toState);

        UpdateAsccpStateRepositoryResponse repositoryResponse =
                asccpWriteRepository.updateAsccpState(repositoryRequest);

        fireEvent(new UpdatedAsccpStateEvent());

        return repositoryResponse.getAsccpManifestId();
    }

    @Transactional
    public BigInteger updateBccpState(AuthenticatedPrincipal user, BigInteger bccpManifestId, CcState toState) {
        CcState fromState = repository.getBccpState(bccpManifestId);
        return updateBccpState(user, bccpManifestId, fromState, toState);
    }

    @Transactional
    public BigInteger updateBccpState(AuthenticatedPrincipal user, BigInteger bccpManifestId, CcState fromState, CcState toState) {
        UpdateBccpStateRepositoryRequest repositoryRequest =
                new UpdateBccpStateRepositoryRequest(user, bccpManifestId, fromState, toState);

        UpdateBccpStateRepositoryResponse repositoryResponse =
                bccpWriteRepository.updateBccpState(repositoryRequest);

        fireEvent(new UpdatedBccpStateEvent());

        return repositoryResponse.getBccpManifestId();
    }

    @Transactional
    public BigInteger updateDtState(AuthenticatedPrincipal user, BigInteger dtManifestId, CcState toState) {
        CcState fromState = repository.getDtState(dtManifestId);
        return updateDtState(user, dtManifestId, fromState, toState);
    }

    @Transactional
    public BigInteger updateDtState(AuthenticatedPrincipal user, BigInteger dtManifestId, CcState fromState, CcState toState) {
        UpdateDtStateRepositoryRequest repositoryRequest =
                new UpdateDtStateRepositoryRequest(user, dtManifestId, fromState, toState);

        UpdateDtStateRepositoryResponse repositoryResponse =
                dtWriteRepository.updateDtState(repositoryRequest);

        fireEvent(new UpdatedDtStateEvent());

        return repositoryResponse.getDtManifestId();
    }

    @Transactional
    public BigInteger makeNewRevisionForAcc(AuthenticatedPrincipal user, BigInteger accManifestId) {
        ReviseAccRepositoryRequest repositoryRequest =
                new ReviseAccRepositoryRequest(user, accManifestId);

        ReviseAccRepositoryResponse repositoryResponse =
                accWriteRepository.reviseAcc(repositoryRequest);

        fireEvent(new RevisedAccEvent());

        return repositoryResponse.getAccManifestId();
    }

    @Transactional
    public BigInteger makeNewRevisionForAsccp(AuthenticatedPrincipal user, BigInteger asccpManifestId) {
        ReviseAsccpRepositoryRequest repositoryRequest =
                new ReviseAsccpRepositoryRequest(user, asccpManifestId);

        ReviseAsccpRepositoryResponse repositoryResponse =
                asccpWriteRepository.reviseAsccp(repositoryRequest);

        fireEvent(new RevisedAsccpEvent());

        return repositoryResponse.getAsccpManifestId();
    }

    @Transactional
    public BigInteger makeNewRevisionForBccp(AuthenticatedPrincipal user, BigInteger bccpManifestId) {
        ReviseBccpRepositoryRequest repositoryRequest =
                new ReviseBccpRepositoryRequest(user, bccpManifestId);

        ReviseBccpRepositoryResponse repositoryResponse =
                bccpWriteRepository.reviseBccp(repositoryRequest);

        fireEvent(new RevisedBccpEvent());

        return repositoryResponse.getBccpManifestId();
    }

    @Transactional
    public BigInteger makeNewRevisionForDt(AuthenticatedPrincipal user, BigInteger dtManifestId) {
        ReviseDtRepositoryRequest repositoryRequest =
                new ReviseDtRepositoryRequest(user, dtManifestId);

        ReviseDtRepositoryResponse repositoryResponse =
                dtWriteRepository.reviseDt(repositoryRequest);

        fireEvent(new RevisedDtEvent());

        return repositoryResponse.getDtManifestId();
    }

    public CcRevisionResponse getAccNodeRevision(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcAccNode accNode = getAccNode(user, manifestId);
        BigInteger lastPublishedCcId = getLastPublishedCcId(accNode.getAccId(), CcType.ACC);
        CcRevisionResponse ccRevisionResponse = new CcRevisionResponse();
        if (lastPublishedCcId != null) {
            AccRecord accRecord = ccRepository.getAccById(ULong.valueOf(lastPublishedCcId));
            ccRevisionResponse.setCcId(accRecord.getAccId().longValue());
            ccRevisionResponse.setType(CcType.ACC.toString());
            ccRevisionResponse.setIsDeprecated(accRecord.getIsDeprecated() == 1);
            ccRevisionResponse.setIsAbstract(accRecord.getIsAbstract() == 1);
            ccRevisionResponse.setName(accRecord.getObjectClassTerm());
            ccRevisionResponse.setHasBaseCc(accRecord.getBasedAccId() != null);
            List<AsccManifestRecord> asccManifestRecordList
                    = ccRepository.getAsccManifestByFromAccManifestId(ULong.valueOf(manifestId));
            Map<String, CcNode> associations = new HashMap<>();
            for (AsccManifestRecord asccManifestRecord : asccManifestRecordList) {
                BigInteger lastAsccId = getLastPublishedCcId(asccManifestRecord.getAsccId().toBigInteger(), CcType.ASCC);
                if (lastAsccId != null) {
                    CcAsccNode ascc = new CcAsccNode();
                    AsccRecord asccRecord = ccRepository.getAsccById(ULong.valueOf(lastAsccId));
                    ascc.setAsccId(asccRecord.getAsccId().toBigInteger());
                    ascc.setCardinalityMin(BigInteger.valueOf(asccRecord.getCardinalityMin()));
                    ascc.setCardinalityMax(BigInteger.valueOf(asccRecord.getCardinalityMax()));
                    ascc.setDeprecated(asccRecord.getIsDeprecated() == 1);
                    associations.put(CcType.ASCCP.toString() + "-" + asccManifestRecord.getToAsccpManifestId(), ascc);
                }
            }
            List<BccManifestRecord> bccManifestRecordList
                    = ccRepository.getBccManifestByFromAccManifestId(ULong.valueOf(manifestId));
            for (BccManifestRecord bccManifestRecord : bccManifestRecordList) {
                BigInteger lastBccId = getLastPublishedCcId(bccManifestRecord.getBccId().toBigInteger(), CcType.BCC);
                if (lastBccId != null) {
                    CcBccNode bcc = new CcBccNode();
                    BccRecord bccRecord = ccRepository.getBccById(ULong.valueOf(lastBccId));
                    bcc.setBccId(bccRecord.getBccId().toBigInteger());
                    bcc.setCardinalityMin(BigInteger.valueOf(bccRecord.getCardinalityMin()));
                    bcc.setCardinalityMax(BigInteger.valueOf(bccRecord.getCardinalityMax()));
                    bcc.setEntityType(BCCEntityType.valueOf(bccRecord.getEntityType()));
                    bcc.setDeprecated(bccRecord.getIsDeprecated() == 1);
                    associations.put(CcType.BCCP.toString() + "-" + bccManifestRecord.getToBccpManifestId(), bcc);
                }
            }
            ccRevisionResponse.setAssociations(associations);
        }
        return ccRevisionResponse;
    }

    public CcRevisionResponse getBccpNodeRevision(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcBccpNode bccpNode = getBccpNode(user, manifestId);
        BigInteger lastPublishedCcId = getLastPublishedCcId(bccpNode.getBccpId(), CcType.BCCP);
        CcRevisionResponse ccRevisionResponse = new CcRevisionResponse();
        if (lastPublishedCcId != null) {
            BccpRecord bccpRecord = ccRepository.getBccpById(ULong.valueOf(lastPublishedCcId));
            ccRevisionResponse.setCcId(bccpRecord.getBccpId().longValue());
            ccRevisionResponse.setType(CcType.BCCP.toString());
            ccRevisionResponse.setIsDeprecated(bccpRecord.getIsDeprecated() == 1);
            ccRevisionResponse.setIsNillable(bccpRecord.getIsNillable() == 1);
            ccRevisionResponse.setName(bccpRecord.getPropertyTerm());
            ccRevisionResponse.setFixedValue(bccpRecord.getFixedValue());
            ccRevisionResponse.setHasBaseCc(bccpRecord.getBdtId() != null);
        }
        return ccRevisionResponse;
    }

    public CcRevisionResponse getBdtNodeRevision(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcBdtNode bdtNode = getBdtNode(user, manifestId);
        BigInteger lastPublishedCcId = getLastPublishedCcId(bdtNode.getId(), CcType.DT);
        CcRevisionResponse ccRevisionResponse = new CcRevisionResponse();
        if (lastPublishedCcId != null) {
            DtRecord bdtRecord = ccRepository.getBdtById(ULong.valueOf(lastPublishedCcId));
            ccRevisionResponse.setCcId(bdtRecord.getDtId().longValue());
            ccRevisionResponse.setType(CcType.BCCP.toString());
            ccRevisionResponse.setIsDeprecated(bdtRecord.getIsDeprecated() == 1);
            ccRevisionResponse.setName(bdtRecord.getDataTypeTerm());
            ccRevisionResponse.setHasBaseCc(bdtRecord.getBasedDtId() != null);
        }
        return ccRevisionResponse;
    }

    public CcRevisionResponse getAsccpNodeRevision(AuthenticatedPrincipal user, BigInteger manifestId) {
        CcAsccpNode asccpNode = getAsccpNode(user, manifestId);
        BigInteger lastPublishedCcId = getLastPublishedCcId(asccpNode.getAsccpId(), CcType.ASCCP);
        CcRevisionResponse ccRevisionResponse = new CcRevisionResponse();
        if (lastPublishedCcId != null) {
            AsccpRecord asccpRecord = ccRepository.getAsccpById(ULong.valueOf(lastPublishedCcId));
            ccRevisionResponse.setCcId(asccpRecord.getAsccpId().longValue());
            ccRevisionResponse.setType(CcType.ASCCP.toString());
            ccRevisionResponse.setIsDeprecated(asccpRecord.getIsDeprecated() == 1);
            ccRevisionResponse.setIsNillable(asccpRecord.getIsNillable() == 1);
            ccRevisionResponse.setIsReusable(asccpRecord.getReusableIndicator() == 1);
            ccRevisionResponse.setName(asccpRecord.getPropertyTerm());
            ccRevisionResponse.setHasBaseCc(asccpRecord.getRoleOfAccId() != null);
        }
        return ccRevisionResponse;
    }

    private BigInteger getLastPublishedCcId(BigInteger ccId, CcType type) {
        if (ccId == null) {
            return null;
        }
        switch (type) {
            case ACC:
                AccRecord accRecord = ccRepository.getAccById(ULong.valueOf(ccId));
                if (accRecord.getState().equals(CcState.Published.name()) ||
                        accRecord.getState().equals(CcState.Production.name())) {
                    return ccId;
                }
                if (accRecord.getPrevAccId() == null) {
                    return null;
                }
                return getLastPublishedCcId(accRecord.getPrevAccId().toBigInteger(), CcType.ACC);
            case ASCC:
                AsccRecord asccRecord = ccRepository.getAsccById(ULong.valueOf(ccId));
                if (asccRecord.getState().equals(CcState.Published.name()) ||
                        asccRecord.getState().equals(CcState.Production.name())) {
                    return ccId;
                }
                if (asccRecord.getPrevAsccId() == null) {
                    return null;
                }
                return getLastPublishedCcId(asccRecord.getPrevAsccId().toBigInteger(), CcType.ASCC);
            case BCC:
                BccRecord bccRecord = ccRepository.getBccById(ULong.valueOf(ccId));
                if (bccRecord.getState().equals(CcState.Published.name()) ||
                        bccRecord.getState().equals(CcState.Production.name())) {
                    return ccId;
                }
                if (bccRecord.getPrevBccId() == null) {
                    return null;
                }
                return getLastPublishedCcId(bccRecord.getPrevBccId().toBigInteger(), CcType.BCC);
            case ASCCP:
                AsccpRecord asccpRecord = ccRepository.getAsccpById(ULong.valueOf(ccId));
                if (asccpRecord.getState().equals(CcState.Published.name()) ||
                        asccpRecord.getState().equals(CcState.Production.name())) {
                    return ccId;
                }
                if (asccpRecord.getPrevAsccpId() == null) {
                    return null;
                }
                return getLastPublishedCcId(asccpRecord.getPrevAsccpId().toBigInteger(), CcType.ASCCP);
            case BCCP:
                BccpRecord bccpRecord = ccRepository.getBccpById(ULong.valueOf(ccId));
                if (bccpRecord.getState().equals(CcState.Published.name()) ||
                        bccpRecord.getState().equals(CcState.Production.name())) {
                    return ccId;
                }
                if (bccpRecord.getPrevBccpId() == null) {
                    return null;
                }
                return getLastPublishedCcId(bccpRecord.getPrevBccpId().toBigInteger(), CcType.BCCP);

            case DT:
                DtRecord bdtRecord = ccRepository.getBdtById(ULong.valueOf(ccId));
                if (bdtRecord.getState().equals(CcState.Published.name()) ||
                        bdtRecord.getState().equals(CcState.Production.name())) {
                    return ccId;
                }
                if (bdtRecord.getPrevDtId() == null) {
                    return null;
                }
                return getLastPublishedCcId(bdtRecord.getPrevDtId().toBigInteger(), CcType.DT);

            case DT_SC:
                return null;

            case XBT:
                return null;

            default:
                return null;

        }
    }

    public void updateAccOwnerUserId(AuthenticatedPrincipal user, BigInteger accManifestId, BigInteger ownerUserId) {
        UpdateAccOwnerRepositoryRequest request =
                new UpdateAccOwnerRepositoryRequest(user, accManifestId, ownerUserId);
        accWriteRepository.updateAccOwner(request);

        fireEvent(new UpdatedAccOwnerEvent());
    }

    public void updateAsccpOwnerUserId(AuthenticatedPrincipal user, BigInteger asccpManifestId, BigInteger ownerUserId) {
        UpdateAsccpOwnerRepositoryRequest request =
                new UpdateAsccpOwnerRepositoryRequest(user, asccpManifestId, ownerUserId);
        asccpWriteRepository.updateAsccpOwner(request);

        fireEvent(new UpdatedAsccpOwnerEvent());
    }

    public void updateBccpOwnerUserId(AuthenticatedPrincipal user, BigInteger bccpManifestId, BigInteger ownerUserId) {
        UpdateBccpOwnerRepositoryRequest request =
                new UpdateBccpOwnerRepositoryRequest(user, bccpManifestId, ownerUserId);
        bccpWriteRepository.updateBccpOwner(request);

        fireEvent(new UpdatedBccpOwnerEvent());
    }

    public void updateDtOwnerUserId(AuthenticatedPrincipal user, BigInteger dtManifestId, BigInteger ownerUserId) {
        UpdateDtOwnerRepositoryRequest request =
                new UpdateDtOwnerRepositoryRequest(user, dtManifestId, ownerUserId);
        dtWriteRepository.updateDtOwner(request);

        fireEvent(new UpdatedDtOwnerEvent());
    }

    @Transactional
    public void cancelRevisionBccp(AuthenticatedPrincipal user, BigInteger bccpManifestId) {
        CancelRevisionBccpRepositoryRequest request
                = new CancelRevisionBccpRepositoryRequest(user, bccpManifestId);
        bccpWriteRepository.cancelRevisionBccp(request);

        fireEvent(new CancelRevisionBccpEvent());
    }

    @Transactional
    public void cancelRevisionDt(AuthenticatedPrincipal user, BigInteger dtManifestId) {
        CancelRevisionDtRepositoryRequest request
                = new CancelRevisionDtRepositoryRequest(user, dtManifestId);
        dtWriteRepository.cancelRevisionDt(request);

        fireEvent(new CancelRevisionDtEvent());
    }

    @Transactional
    public void cancelRevisionAsccp(AuthenticatedPrincipal user, BigInteger asccpManifestId) {
        CancelRevisionAsccpRepositoryRequest request
                = new CancelRevisionAsccpRepositoryRequest(user, asccpManifestId);
        asccpWriteRepository.cancelRevisionAsccp(request);

        fireEvent(new CancelRevisionAsccpEvent());
    }

    @Transactional
    public void cancelRevisionAcc(AuthenticatedPrincipal user, BigInteger accManifestId) {
        CancelRevisionAccRepositoryRequest request
                = new CancelRevisionAccRepositoryRequest(user, accManifestId);
        accWriteRepository.cancelRevisionAcc(request);

        fireEvent(new CancelRevisionAccEvent());
    }


    @Transactional
    public CreateOagisBodResponse createOagisBod(AuthenticatedPrincipal user,
                                                 CreateOagisBodRequest request) {

        CreateOagisBodResponse response = new CreateOagisBodResponse();

        List<BigInteger> bodManifestIdList = _createOagisBod(user, request);
        response.setManifestIdList(bodManifestIdList);

        return response;
    }

    private List<BigInteger> _createOagisBod(AuthenticatedPrincipal user, CreateOagisBodRequest request) {
        AppUser requester = sessionService.getAppUserByUsername(user);
        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException();
        }

        List<BigInteger> bodManifestIdList = new ArrayList();
        for (BigInteger verbManifestId : request.getVerbManifestIdList()) {
            for (BigInteger nounManifestId : request.getNounManifestIdList()) {
                AsccpManifestRecord verbManifestRecord = asccpReadRepository.getAsccpManifestById(verbManifestId);
                BigInteger releaseId = verbManifestRecord.getReleaseId().toBigInteger();
                AsccpRecord verb = asccpReadRepository.getAsccpByManifestId(verbManifestId);
                AsccpRecord noun = asccpReadRepository.getAsccpByManifestId(nounManifestId);

                if (verb.getNamespaceId() == null) {
                    throw new IllegalArgumentException("'" + verb.getPropertyTerm() + "' dose not have a namespace.");
                }

                if (noun.getNamespaceId() == null) {
                    throw new IllegalArgumentException("'" + noun.getPropertyTerm() + "' dose not have a namespace.");
                }

                BigInteger namespaceId = verb.getNamespaceId().toBigInteger();

                CreateAccRepositoryRequest dataAreaAccRequest = new CreateAccRepositoryRequest(user, releaseId);
                dataAreaAccRequest.setInitialComponentType(OagisComponentType.Semantics);
                dataAreaAccRequest.setInitialObjectClassTerm(String.join(" ", Arrays.asList(verb.getPropertyTerm(), noun.getPropertyTerm(), "Data Area")));
                dataAreaAccRequest.setNamespaceId(namespaceId);
                BigInteger dataAreaAccManifestId = accWriteRepository.createAcc(dataAreaAccRequest).getAccManifestId();

                CreateAsccRepositoryRequest verbAsccRequest = new CreateAsccRepositoryRequest(user, releaseId,
                        dataAreaAccManifestId, verbManifestId);
                verbAsccRequest.setCardinalityMin(1);
                verbAsccRequest.setCardinalityMax(1);
                asccWriteRepository.createAscc(verbAsccRequest);

                CreateAsccRepositoryRequest nounAsccRequest = new CreateAsccRepositoryRequest(user, releaseId,
                        dataAreaAccManifestId, nounManifestId);
                nounAsccRequest.setCardinalityMin(1);
                nounAsccRequest.setCardinalityMax(-1);
                asccWriteRepository.createAscc(nounAsccRequest);

                CreateAsccpRepositoryRequest dataAreaAsccpRequest = new CreateAsccpRepositoryRequest(user, dataAreaAccManifestId, releaseId);
                dataAreaAsccpRequest.setInitialPropertyTerm("Data Area");
                dataAreaAsccpRequest.setNamespaceId(namespaceId);
                dataAreaAsccpRequest.setReusable(false);
                String name = String.join(" ", Arrays.asList(verb.getPropertyTerm(), noun.getPropertyTerm()))
                        .replaceAll(" ", "");
                dataAreaAsccpRequest.setDefinition("Is where the information that the BOD message carries is provided, in this case " + name + ". The information consists of a Verb and one or more Nouns. The verb (" + verb.getPropertyTerm().replaceAll(" ", "") + ") indicates the action to be performed on the Noun (" + noun.getPropertyTerm().replaceAll(" ", "") + ").");
                dataAreaAsccpRequest.setDefinitionSource("http://www.openapplications.org/oagis/10");
                BigInteger dataAreaAsccpManifestId = asccpWriteRepository.createAsccp(dataAreaAsccpRequest).getAsccpManifestId();

                ULong bodBasedAccManifestId = dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                        .from(ACC_MANIFEST)
                        .join(ACC).on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                        .where(and(
                                ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                                ACC.OBJECT_CLASS_TERM.eq("Business Object Document")
                        ))
                        .fetchOneInto(ULong.class);

                CreateAccRepositoryRequest bodAccRequest = new CreateAccRepositoryRequest(user, releaseId);
                bodAccRequest.setBasedAccManifestId(bodBasedAccManifestId.toBigInteger());
                bodAccRequest.setInitialComponentType(OagisComponentType.Semantics);
                bodAccRequest.setInitialObjectClassTerm(String.join(" ", Arrays.asList(verb.getPropertyTerm(), noun.getPropertyTerm())));
                bodAccRequest.setNamespaceId(namespaceId);
                BigInteger bodAccManifestId = accWriteRepository.createAcc(bodAccRequest).getAccManifestId();

                CreateAsccRepositoryRequest dataAreaAsccRequest = new CreateAsccRepositoryRequest(user, releaseId,
                        bodAccManifestId, dataAreaAsccpManifestId);
                dataAreaAsccRequest.setCardinalityMin(1);
                dataAreaAsccRequest.setCardinalityMax(1);
//        dataAreaAsccRequest.setDefinition(dataAreaAsccpRequest.getDefinition());
//        dataAreaAsccRequest.setDefinitionSource(dataAreaAsccpRequest.getDefinitionSoruce());
                asccWriteRepository.createAscc(dataAreaAsccRequest);

                CreateAsccpRepositoryRequest bodAsccpRequest = new CreateAsccpRepositoryRequest(user, bodAccManifestId, releaseId);
                bodAsccpRequest.setInitialPropertyTerm(bodAccRequest.getInitialObjectClassTerm());
                bodAsccpRequest.setNamespaceId(namespaceId);
                bodAsccpRequest.setTag("BOD");
                BigInteger bodAsccpManifestId = asccpWriteRepository.createAsccp(bodAsccpRequest).getAsccpManifestId();
                bodManifestIdList.add(bodAsccpManifestId);
            }
        }

        return bodManifestIdList;
    }

    @Transactional
    public CreateOagisVerbResponse createOagisVerb(AuthenticatedPrincipal user,
                                                   CreateOagisVerbRequest request) {

        CreateOagisVerbResponse response = new CreateOagisVerbResponse();

        BigInteger verbAsccpManifestId = _createOagisVerb(user, request);
        response.setBasedVerbAsccpManifestId(verbAsccpManifestId);

        return response;
    }

    private BigInteger _createOagisVerb(AuthenticatedPrincipal user,
                                        CreateOagisVerbRequest request) {

        AppUser requester = sessionService.getAppUserByUsername(user);
        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException();
        }

        AccManifestRecord verbManifestRecord = accReadRepository.getAccManifest(request.getBasedVerbAccManifestId());
        AccRecord verbRecord = accReadRepository.getAccByManifestId(verbManifestRecord.getAccManifestId().toBigInteger());

        BigInteger releaseId = verbManifestRecord.getReleaseId().toBigInteger();
        if (verbRecord.getNamespaceId() == null) {
            throw new IllegalArgumentException("'" + verbRecord.getObjectClassTerm() + "' dose not have Namespace Id.");
        }
        BigInteger namespaceId = verbRecord.getNamespaceId().toBigInteger();

        CreateAccRepositoryRequest verbAccRequest = new CreateAccRepositoryRequest(user, releaseId);
        verbAccRequest.setBasedAccManifestId(request.getBasedVerbAccManifestId());
        verbAccRequest.setInitialComponentType(OagisComponentType.Semantics);
        verbAccRequest.setInitialObjectClassTerm(request.getInitialObjectClassTerm());
        verbAccRequest.setNamespaceId(namespaceId);
        BigInteger verbAccManifestId = accWriteRepository.createAcc(verbAccRequest).getAccManifestId();

        CreateAsccpRepositoryRequest verbAsccpRequest = new CreateAsccpRepositoryRequest(user, verbAccManifestId, releaseId);
        verbAsccpRequest.setInitialPropertyTerm(request.getInitialPrpertyTerm());
        verbAsccpRequest.setNamespaceId(namespaceId);
        verbAsccpRequest.setTag("Verb");
        BigInteger verbAsccpManifestId = asccpWriteRepository.createAsccp(verbAsccpRequest).getAsccpManifestId();

        return verbAsccpManifestId;
    }

    boolean isPublishedRelease(BigInteger releaseId) {
        Release release = releaseRepository.findById(releaseId);
        if(!release.getState().equals(CcState.Published.name())) {
            throw new IllegalStateException("'" + release.getState() + "' Release cannot be modified.");
        }
        return true;
    }

    @Transactional
    public void refactorAscc(AuthenticatedPrincipal user, BigInteger asccManifestId, BigInteger destinationAccManifestId) {
        RefactorAsccRepositoryRequest request
                = new RefactorAsccRepositoryRequest(user, asccManifestId, destinationAccManifestId);
        asccWriteRepository.refactor(request);

        fireEvent(new RefactorAsccEvent());
    }

    @Transactional
    public void refactorBcc(AuthenticatedPrincipal user, BigInteger bccManifestId, BigInteger destinationAccManifestId) {
        RefactorBccRepositoryRequest request
                = new RefactorBccRepositoryRequest(user, bccManifestId, destinationAccManifestId);
        bccWriteRepository.refactor(request);

        fireEvent(new RefactorBccEvent());
    }

    public CcRefactorValidationResponse validateBccRefactoring(AuthenticatedPrincipal user, BigInteger bccManifestId, BigInteger destinationAccManifestId) {
        AppUser requester = sessionService.getAppUserByUsername(user);

        return bccReadRepository.validateBccRefactoring(requester, bccManifestId, destinationAccManifestId);
    }

    public CcRefactorValidationResponse validateAsccRefactoring(AuthenticatedPrincipal user, BigInteger asccManifestId, BigInteger destinationAccManifestId) {
        AppUser requester = sessionService.getAppUserByUsername(user);

        return asccReadRepository.validateAsccRefactoring(requester, asccManifestId, destinationAccManifestId);
    }

    public List<CcList> getBaseAccList(AuthenticatedPrincipal user, BigInteger accManifestId) {

        AccManifestRecord accManifestRecord = accReadRepository.getAccManifest(accManifestId);
        return accReadRepository.getBaseAccList(accManifestRecord.getAccManifestId().toBigInteger(), accManifestRecord.getReleaseId().toBigInteger());
    }

    @Transactional
    public CcUngroupResponse ungroup(AuthenticatedPrincipal user, CcUngroupRequest request) {
        AccManifestRecord accManifestRecord = accReadRepository.getAccManifest(request.getAccManifestId());
        AccRecord accRecord = accReadRepository.getAccByManifestId(request.getAccManifestId());
        if (accRecord == null) {
            throw new IllegalArgumentException("'accManifestId' parameter must not be null.");
        }

        AsccManifestRecord asccManifestRecord = asccReadRepository.getAsccManifestById(request.getAsccManifestId());
        AsccRecord asccRecord = asccReadRepository.getAsccByManifestId(request.getAsccManifestId());
        if (asccRecord == null) {
            throw new IllegalArgumentException("'asccManifestId' parameter must not be null.");
        }

        AsccpManifestRecord asccpManifestRecord =
                asccpReadRepository.getAsccpManifestById(asccManifestRecord.getToAsccpManifestId().toBigInteger());
        AccManifestRecord roleOfAccManifestRecord =
                accReadRepository.getAccManifest(asccpManifestRecord.getRoleOfAccManifestId().toBigInteger());

        Stack<AccManifestRecord> accManifestRecordStack = new Stack();
        accManifestRecordStack.add(roleOfAccManifestRecord);

        while (roleOfAccManifestRecord.getBasedAccManifestId() != null) {
            roleOfAccManifestRecord = accReadRepository.getAccManifest(
                    roleOfAccManifestRecord.getBasedAccManifestId().toBigInteger());
            accManifestRecordStack.add(roleOfAccManifestRecord);
        }

        String logHash = LogUtils.generateHash();

        while (!accManifestRecordStack.isEmpty()) {
            roleOfAccManifestRecord = accManifestRecordStack.pop();

            CoreComponentGraphContext coreComponentGraphContext =
                    graphContextRepository.buildGraphContext(roleOfAccManifestRecord);
            List<Node> children = coreComponentGraphContext.findChildren(
                    coreComponentGraphContext.toNode(roleOfAccManifestRecord), true);

            int pos = request.getPos();
            for (Node child : children) {
                if (child.getType() == Node.NodeType.ASCC) {
                    AsccManifestRecord asccChild =
                            asccReadRepository.getAsccManifestById(child.getManifestId().toBigInteger());

                    appendAsccp(user, accManifestRecord.getReleaseId().toBigInteger(),
                            accManifestRecord.getAccManifestId().toBigInteger(),
                            asccChild.getToAsccpManifestId().toBigInteger(), pos, logHash, LogAction.IGNORE);
                } else if (child.getType() == Node.NodeType.BCC) {
                    BccManifestRecord bccChild =
                            bccReadRepository.getBccManifestById(child.getManifestId().toBigInteger());

                    appendBccp(user, accManifestRecord.getReleaseId().toBigInteger(),
                            accManifestRecord.getAccManifestId().toBigInteger(),
                            bccChild.getToBccpManifestId().toBigInteger(), pos, logHash, LogAction.IGNORE);
                }

                pos++;
            }
        }

        deleteAscc(user, asccManifestRecord.getAsccManifestId().toBigInteger(), logHash, LogAction.Ungrouped, false);

        CcUngroupResponse response = new CcUngroupResponse();
        response.setAccManifestId(accManifestRecord.getAccManifestId().toBigInteger());
        return response;
    }
}