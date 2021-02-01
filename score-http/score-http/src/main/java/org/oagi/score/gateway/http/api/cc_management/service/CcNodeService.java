package org.oagi.score.gateway.http.api.cc_management.service;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.BCCEntityType;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.data.Release;
import org.oagi.score.gateway.http.api.cc_management.data.*;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;
import org.oagi.score.gateway.http.api.cc_management.repository.CcNodeRepository;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.redis.event.EventHandler;
import org.oagi.score.repo.CoreComponentRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.acc.*;
import org.oagi.score.repo.component.ascc.*;
import org.oagi.score.repo.component.asccp.*;
import org.oagi.score.repo.component.bcc.*;
import org.oagi.score.repo.component.bccp.*;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

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
    private AsccWriteRepository asccWriteRepository;

    @Autowired
    private BccWriteRepository bccWriteRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private DSLContext dslContext;

    public CcAccNode getAccNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getAccNodeByAccManifestId(user, manifestId);
    }

    public CcAsccpNode getAsccpNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getAsccpNodeByAsccpManifestId(user, manifestId);
    }

    public CcBccpNode getBccpNode(AuthenticatedPrincipal user, BigInteger manifestId) {
        return repository.getBccpNodeByBccpManifestId(user, manifestId);
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
    public void deleteAscc(AuthenticatedPrincipal user, BigInteger asccManifestId) {
        DeleteAsccRepositoryRequest request =
                new DeleteAsccRepositoryRequest(user, asccManifestId);

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

    public CcAccNodeDetail getAccNodeDetail(AuthenticatedPrincipal user, CcAccNode accNode) {
        return repository.getAccNodeDetail(user, accNode);
    }

    public CcAsccpNodeDetail getAsccpNodeDetail(AuthenticatedPrincipal user, CcAsccpNode asccpNode) {
        return repository.getAsccpNodeDetail(user, asccpNode);
    }

    public CcBccpNodeDetail getBccpNodeDetail(AuthenticatedPrincipal user, CcBccpNode bccpNode) {
        return repository.getBccpNodeDetail(user, bccpNode);
    }

    public CcBdtScNodeDetail getBdtScNodeDetail(AuthenticatedPrincipal user, CcBdtScNode bdtScNode) {
        return repository.getBdtScNodeDetail(user, bdtScNode);
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

    @Transactional
    public BigInteger appendAsccp(AuthenticatedPrincipal user, BigInteger releaseId,
                                  BigInteger accManifestId, BigInteger asccpManifestId,
                                  int pos) {
        LocalDateTime timestamp = LocalDateTime.now();
        CreateAsccRepositoryRequest request =
                new CreateAsccRepositoryRequest(user, timestamp, releaseId, accManifestId, asccpManifestId);
        request.setPos(pos);

        CreateAsccRepositoryResponse response = asccWriteRepository.createAscc(request);
        fireEvent(new CreatedAsccEvent());
        return response.getAsccManifestId();
    }

    @Transactional
    public BigInteger appendBccp(AuthenticatedPrincipal user, BigInteger releaseId,
                                 BigInteger accManifestId, BigInteger bccpManifestId,
                                 int pos) {
        LocalDateTime timestamp = LocalDateTime.now();
        CreateBccRepositoryRequest request =
                new CreateBccRepositoryRequest(user, timestamp, releaseId, accManifestId, bccpManifestId);
        request.setPos(pos);

        CreateBccRepositoryResponse response = bccWriteRepository.createBcc(request);
        fireEvent(new CreatedBccEvent());
        return response.getBccManifestId();
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
                    AsccRecord asccRecord = ccRepository.getAsccById(asccManifestRecord.getAsccId());
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
                    BccRecord bccRecord = ccRepository.getBccById(bccManifestRecord.getBccId());
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

            case BDT:
                return null;

            case BDT_SC:
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

    @Transactional
    public void cancelRevisionBccp(AuthenticatedPrincipal user, BigInteger bccpManifestId) {
        CancelRevisionBccpRepositoryRequest request
                = new CancelRevisionBccpRepositoryRequest(user, bccpManifestId);
        bccpWriteRepository.cancelRevisionBccp(request);

        fireEvent(new CancelRevisionBccpEvent());
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

        BigInteger bodManifestId = _createOagisBod(user, request);
        response.setManifestId(bodManifestId);

        return response;
    }

    private BigInteger _createOagisBod(AuthenticatedPrincipal user, CreateOagisBodRequest request) {
        AppUser requester = sessionService.getAppUser(user);
        if (!requester.isDeveloper()) {
            throw new IllegalArgumentException();
        }

        AsccpManifestRecord verbManifestRecord = asccpReadRepository.getAsccpManifestById(request.getVerbManifestId());
        BigInteger releaseId = verbManifestRecord.getReleaseId().toBigInteger();
        AsccpRecord verb = asccpReadRepository.getAsccpByManifestId(request.getVerbManifestId());
        AsccpRecord noun = asccpReadRepository.getAsccpByManifestId(request.getNounManifestId());

        if(verb.getNamespaceId() == null) {
            throw new IllegalArgumentException("'" + verb.getPropertyTerm() + "' dose not have Namespace Id.");
        }

        if(noun.getNamespaceId() == null) {
            throw new IllegalArgumentException("'" + noun.getPropertyTerm() + "' dose not have Namespace Id.");
        }

        BigInteger namespaceId = verb.getNamespaceId().toBigInteger();

        CreateAccRepositoryRequest dataAreaAccRequest = new CreateAccRepositoryRequest(user, releaseId);
        dataAreaAccRequest.setInitialComponentType(OagisComponentType.Semantics);
        dataAreaAccRequest.setInitialObjectClassTerm(String.join(" ", Arrays.asList(verb.getPropertyTerm(), noun.getPropertyTerm(), "Data Area")));
        dataAreaAccRequest.setNamespaceId(namespaceId);
        BigInteger dataAreaAccManifestId = accWriteRepository.createAcc(dataAreaAccRequest).getAccManifestId();

        CreateAsccRepositoryRequest verbAsccRequest = new CreateAsccRepositoryRequest(user, releaseId,
                dataAreaAccManifestId, request.getVerbManifestId());
        verbAsccRequest.setCardinalityMin(1);
        verbAsccRequest.setCardinalityMax(1);
        asccWriteRepository.createAscc(verbAsccRequest);

        CreateAsccRepositoryRequest nounAsccRequest = new CreateAsccRepositoryRequest(user, releaseId,
                dataAreaAccManifestId, request.getNounManifestId());
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
        BigInteger bodAsccpManifestId = asccpWriteRepository.createAsccp(bodAsccpRequest).getAsccpManifestId();

        return bodAsccpManifestId;
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

        AppUser requester = sessionService.getAppUser(user);
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
}

