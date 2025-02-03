package org.oagi.score.gateway.http.api.cc_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.oagi.score.gateway.http.api.cc_management.data.*;
import org.oagi.score.gateway.http.api.cc_management.data.node.*;
import org.oagi.score.gateway.http.api.cc_management.service.CcNodeService;
import org.oagi.score.gateway.http.api.plantuml.service.PlantUmlService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.component.asccp.UpdateAsccpRoleOfAccRepositoryResponse;
import org.oagi.score.repo.component.bccp.UpdateBccpBdtRepositoryResponse;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.CcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static org.oagi.score.gateway.http.api.cc_management.data.CcType.ACC;
import static org.oagi.score.gateway.http.api.cc_management.data.CcType.valueOf;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

@RestController
public class CcNodeController {

    @Autowired
    private CcNodeService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PlantUmlService plantUmlService;

    @RequestMapping(value = "/core_component/acc/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNode getAccNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("manifestId") BigInteger manifestId) {
        return service.getAccNode(user, manifestId);
    }

    @RequestMapping(value = "/core_component/asccp/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNode getAsccpNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                               @PathVariable("manifestId") BigInteger manifestId) {
        return service.getAsccpNode(user, manifestId);
    }

    @RequestMapping(value = "/core_component/bccp/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNode getBccpNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                              @PathVariable("manifestId") BigInteger manifestId) {
        return service.getBccpNode(user, manifestId);
    }

    @RequestMapping(value = "/core_component/dt/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNode getBdtNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("manifestId") BigInteger manifestId) {
        return service.getBdtNode(user, manifestId);
    }

    @RequestMapping(value = "/core_component",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcUpdateResponse updateCcNodeDetails(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @RequestBody CcUpdateRequest ccUpdateRequest) {
        return service.updateCcDetails(user, ccUpdateRequest);
    }

    @RequestMapping(value = "/core_component/acc/{manifestId:[\\d]+}/seq_key",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateCcSeq(@AuthenticationPrincipal AuthenticatedPrincipal user,
                            @PathVariable("manifestId") BigInteger manifestId,
                            @RequestBody CcSeqUpdateRequest request) {
        service.updateCcSeq(user, manifestId,
                Pair.of(request.getItem(), request.getAfter()));
    }

    @RequestMapping(value = "/core_component/{type}/{manifestId:[\\d]+}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeUpdateResponse updateCcNodeManifest(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                     @PathVariable("type") String type,
                                                     @PathVariable("manifestId") BigInteger manifestId,
                                                     @RequestBody CcUpdateManifestRequest ccUpdateManifestRequest) {

        CcNodeUpdateResponse resp = new CcNodeUpdateResponse();
        resp.setType(valueOf(type.toUpperCase()));

        switch (resp.getType()) {
            case ASCCP:
                UpdateAsccpRoleOfAccRepositoryResponse asccpResp =
                        service.updateAsccpRoleOfAcc(user, manifestId, ccUpdateManifestRequest.getAccManifestId());
                resp.setManifestId(asccpResp.getAsccpManifestId());
                resp.setDen(asccpResp.getDen());
                break;

            case BCCP:
                UpdateBccpBdtRepositoryResponse bccpResp =
                        service.updateBccpBdt(user, manifestId, ccUpdateManifestRequest.getBdtManifestId());
                resp.setManifestId(bccpResp.getBccpManifestId());
                resp.setDen(bccpResp.getDen());
                break;

            default:
                throw new UnsupportedOperationException();
        }

        resp.setState(CcState.WIP.name());
        resp.setAccess(AccessPrivilege.CanEdit.name());

        return resp;
    }

    @RequestMapping(value = "/core_component/{type}/{manifestId:[\\d]+}/state",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeUpdateResponse updateState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("type") String type,
                                            @PathVariable("manifestId") BigInteger manifestId,
                                            @RequestBody CcUpdateStateRequest ccUpdateStateRequest) {

        CcNodeUpdateResponse resp = new CcNodeUpdateResponse();
        resp.setType(valueOf(type.toUpperCase()));

        switch (resp.getType()) {
            case ACC:
                if ("Purge".equals(ccUpdateStateRequest.getState())) {
                    service.purgeAcc(user, manifestId);
                    resp.setManifestId(manifestId);
                } else {
                    resp.setManifestId(
                            service.updateAccState(user, manifestId, CcState.valueOf(ccUpdateStateRequest.getState()))
                    );
                }
                break;
            case ASCCP:
                if ("Purge".equals(ccUpdateStateRequest.getState())) {
                    service.purgeAsccp(user, manifestId);
                    resp.setManifestId(manifestId);
                } else {
                    resp.setManifestId(
                            service.updateAsccpState(user, manifestId, CcState.valueOf(ccUpdateStateRequest.getState()))
                    );
                }
                break;
            case BCCP:
                if ("Purge".equals(ccUpdateStateRequest.getState())) {
                    service.purgeBccp(user, manifestId);
                    resp.setManifestId(manifestId);
                } else {
                    resp.setManifestId(
                            service.updateBccpState(user, manifestId, CcState.valueOf(ccUpdateStateRequest.getState()))
                    );
                }
                break;
            case DT:
                if ("Purge".equals(ccUpdateStateRequest.getState())) {
                    service.purgeDt(user, manifestId);
                    resp.setManifestId(manifestId);
                } else {
                    resp.setManifestId(
                            service.updateDtState(user, manifestId, CcState.valueOf(ccUpdateStateRequest.getState()))
                    );
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        resp.setState(ccUpdateStateRequest.getState());
        if ("Purge".equals(resp.getState())) {
            resp.setAccess(AccessPrivilege.Prohibited.name());
        } else {
            resp.setAccess(
                    ((CcState.WIP == CcState.valueOf(resp.getState())) ? AccessPrivilege.CanEdit : AccessPrivilege.CanMove).name()
            );
        }

        return resp;
    }

    @RequestMapping(value = "/core_component/{type}/{manifestId:[\\d]+}/revision",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeUpdateResponse makeNewRevision(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("type") String type,
                                                @PathVariable("manifestId") BigInteger manifestId) {

        CcNodeUpdateResponse resp = new CcNodeUpdateResponse();
        resp.setType(valueOf(type.toUpperCase()));

        switch (resp.getType()) {
            case ACC:
                resp.setManifestId(
                        service.makeNewRevisionForAcc(user, manifestId)
                );
                break;
            case ASCCP:
                resp.setManifestId(
                        service.makeNewRevisionForAsccp(user, manifestId)
                );
                break;
            case BCCP:
                resp.setManifestId(
                        service.makeNewRevisionForBccp(user, manifestId)
                );
                break;
            case DT:
                resp.setManifestId(
                        service.makeNewRevisionForDt(user, manifestId)
                );
                break;
            default:
                throw new UnsupportedOperationException();
        }

        resp.setState(CcState.WIP.name());
        resp.setAccess(AccessPrivilege.CanEdit.name());

        return resp;
    }

    @RequestMapping(value = "/core_component/{type}/{manifestId:[\\d]+}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteCcNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                       @PathVariable("type") String type,
                                       @PathVariable("manifestId") BigInteger manifestId) {
        switch (valueOf(type.toUpperCase())) {
            case ACC:
                service.deleteAcc(user, manifestId);
                break;
            case ASCCP:
                service.deleteAsccp(user, manifestId);
                break;
            case BCCP:
                service.deleteBccp(user, manifestId);
                break;
            case ASCC:
                service.deleteAscc(user, manifestId);
                break;
            case BCC:
                service.deleteBcc(user, manifestId);
                break;
            case DT:
                service.deleteDt(user, manifestId);
                break;
            case DT_SC:
                service.deleteDtSc(user, manifestId);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return ResponseEntity.accepted().build();
    }

    private <T> T convertValue(String data, Class<T> clazz) {
        Map<String, Object> params = new HashMap();
        Arrays.stream(new String(Base64.getDecoder().decode(data)).split("&")).forEach(e -> {
            String[] keyValue = e.split("=");
            Object value = keyValue[1];
            if (!"null".equals(value) && value != null) {
                params.put(keyValue[0], value);
            }
        });
        return objectMapper.convertValue(params, clazz);
    }

    @RequestMapping(value = "/core_component/{type}/detail",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeDetail getNodeDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                      @PathVariable("type") String type,
                                      @RequestParam("data") String data) {
        switch (valueOf(type.toUpperCase())) {
            case ACC:
                CcAccNode accNode = convertValue(data, CcAccNode.class);
                return service.getAccNodeDetail(user, accNode);
            case ASCCP:
                CcAsccpNode asccpNode = convertValue(data, CcAsccpNode.class);
                return service.getAsccpNodeDetail(user, asccpNode);
            case BCCP:
                CcBccpNode bccpNode = convertValue(data, CcBccpNode.class);
                return service.getBccpNodeDetail(user, bccpNode);
            case DT:
                CcBdtNode bdtNode = convertValue(data, CcBdtNode.class);
                return service.getBdtNodeDetail(user, bdtNode);
            case DT_SC:
                CcBdtScNode bdtScNode = convertValue(data, CcBdtScNode.class);
                return service.getBdtScNodeDetail(user, bdtScNode);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @RequestMapping(value = "/core_component/acc/{manifestId:[\\d]+}/verify_append",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcVerifyAppendResponse verifyAppendNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                   @PathVariable("manifestId") BigInteger manifestId,
                                                   @RequestBody CcVerifyAppendRequest request) {

        request.setAccManifestId(manifestId);

        if (hasLength(request.getPropertyTerm())) {
            return service.hasSamePropertyTerm(sessionService.asScoreUser(user),
                    request.getAccManifestId(), request.getPropertyTerm());
        } else if (request.getBasedAccManifestId() != null) {
            return service.verifySetBasedAcc(sessionService.asScoreUser(user),
                    request.getAccManifestId(), request.getBasedAccManifestId());
        } else if (request.getAsccpManifestId() != null) {
            return service.verifyAppendAsccp(sessionService.asScoreUser(user),
                    request.getAccManifestId(), request.getAsccpManifestId());
        } else if (request.getBccpManifestId() != null) {
            return service.verifyAppendBccp(sessionService.asScoreUser(user),
                    request.getAccManifestId(), request.getBccpManifestId());
        }

        throw new IllegalArgumentException("No association manifest found.");
    }

    @RequestMapping(value = "/core_component/acc/{manifestId:[\\d]+}/append",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcCreateResponse appendNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                       @PathVariable("manifestId") BigInteger manifestId,
                                       @RequestBody CcAppendRequest request) {

        request.setAccManifestId(manifestId);

        if (request.getAsccpManifestId() != null) {
            manifestId = service.appendAsccp(user,
                    request.getReleaseId(),
                    request.getAccManifestId(),
                    request.getAsccpManifestId(),
                    request.getPos());
        }
        if (request.getBccpManifestId() != null) {
            manifestId = service.appendBccp(user,
                    request.getReleaseId(),
                    request.getAccManifestId(),
                    request.getBccpManifestId(),
                    request.getPos());
        }

        CcCreateResponse response = new CcCreateResponse();
        response.setManifestId(manifestId);
        return response;
    }

    @RequestMapping(value = "/core_component/dt/{ownerDtManifestId:[\\d]+}/append_sc",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcCreateResponse appendScNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                         @PathVariable("ownerDtManifestId") BigInteger ownerDtManifestId) {

        BigInteger manifestId = service.appendDtSc(user, ownerDtManifestId);

        CcCreateResponse response = new CcCreateResponse();
        response.setManifestId(manifestId);
        return response;
    }

    @RequestMapping(value = "/core_component/dt/{ownerDtManifestId:[\\d]+}/restriction/add",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcBdtNodeDetail addRestriction(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                          @PathVariable("ownerDtManifestId") BigInteger ownerDtManifestId,
                                          @RequestBody CcCreateRestrictionRequest restrictionRequest) {

        CcBdtNode dtNode = new CcBdtNode();
        dtNode.setManifestId(ownerDtManifestId);

        service.addDtRestriction(user, restrictionRequest);

        return service.getBdtNodeDetail(user, dtNode);
    }

    @RequestMapping(value = "/core_component/dt/{representationTerm}/primitive_values",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CcBdtScPriRestri> getDefaultPrimitiveValues(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                            @PathVariable("representationTerm") String representationTerm,
                                                            @RequestParam(name = "bdtScManifestId") BigInteger bdtScManifestId) {

        return service.getDefaultPrimitiveValues(user, representationTerm, bdtScManifestId);
    }

    @RequestMapping(value = "/core_component/acc/{manifestId:[\\d]+}/base",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeUpdateResponse setBasedNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("manifestId") BigInteger manifestId,
                                             @RequestBody CcSetBaseAccRequest ccSetBaseAccRequest) {
        CcNodeUpdateResponse resp = new CcNodeUpdateResponse();
        resp.setType(ACC);
        resp.setManifestId(
                service.updateAccBasedAcc(user, manifestId, ccSetBaseAccRequest.getBasedAccManifestId())
        );
        resp.setState(CcState.WIP.name());
        resp.setAccess(AccessPrivilege.CanEdit.name());

        return resp;
    }

    @RequestMapping(value = "/core_component/acc", method = RequestMethod.POST)
    public CcCreateResponse createAcc(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                      @RequestBody CcAccCreateRequest request) {
        BigInteger manifestId = service.createAcc(user, request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @RequestMapping(value = "/core_component/asccp", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcCreateResponse createAsccp(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @RequestBody CcAsccpCreateRequest request) {
        BigInteger manifestId = service.createAsccp(user, request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @RequestMapping(value = "/core_component/bccp", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcCreateResponse createBccp(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                       @RequestBody CcBccpCreateRequest request) {
        BigInteger manifestId = service.createBccp(user, request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @RequestMapping(value = "/core_component/dt", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcCreateResponse createBdt(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                      @RequestBody CcBdtCreateRequest request) {
        BigInteger manifestId = service.createBdt(user, request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @RequestMapping(value = "/core_component/acc/extension", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcCreateResponse createAccExtensionComponent(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @RequestBody CcExtensionCreateRequest request) {
        BigInteger manifestId = service.createAccExtension(user, request);

        CcCreateResponse resp = new CcCreateResponse();
        resp.setManifestId(manifestId);
        return resp;
    }

    @RequestMapping(value = "/core_component/{type}/{manifestId:[\\d]+}/revision", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcRevisionResponse getCcNodeRevision(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("type") String type,
                                                @PathVariable("manifestId") BigInteger manifestId) {
        switch (valueOf(type.toUpperCase())) {
            case ACC:
                return service.getAccNodeRevision(user, manifestId);
            case ASCCP:
                return service.getAsccpNodeRevision(user, manifestId);
            case BCCP:
                return service.getBccpNodeRevision(user, manifestId);
            case DT:
                return service.getBdtNodeRevision(user, manifestId);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @RequestMapping(value = "/core_component/{type}/{manifestId:[\\d]+}/revision/cancel",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeUpdateResponse cancelRevision(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @PathVariable("type") String type,
                                               @PathVariable("manifestId") BigInteger manifestId) {

        CcNodeUpdateResponse resp = new CcNodeUpdateResponse();
        resp.setType(valueOf(type.toUpperCase()));

        switch (resp.getType()) {
            case ACC:
                service.cancelRevisionAcc(user, manifestId);
                break;
            case ASCCP:
                service.cancelRevisionAsccp(user, manifestId);
                break;
            case BCCP:
                service.cancelRevisionBccp(user, manifestId);
                break;
            case DT:
                service.cancelRevisionDt(user, manifestId);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return resp;
    }

    @RequestMapping(value = "/core_component/oagis/bod",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateOagisBodResponse createOagisBod(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                 @RequestBody CreateOagisBodRequest request) {

        CreateOagisBodResponse response = service.createOagisBod(user, request);
        return response;
    }

    @RequestMapping(value = "/core_component/oagis/verb",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateOagisVerbResponse createBod(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @RequestBody CreateOagisVerbRequest request) {

        CreateOagisVerbResponse response = service.createOagisVerb(user, request);
        return response;
    }

    @RequestMapping(value = "/core_component/acc/{manifestId:[\\d]+}/base_acc_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CcList> baseAccList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                    @PathVariable("manifestId") BigInteger manifestId) {

        return service.getBaseAccList(user, manifestId);
    }

    @RequestMapping(value = "/core_component/{type}/refactor",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeUpdateResponse refactorAssociation(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                    @PathVariable("type") String type,
                                                    @RequestBody CcRefactorRequest request) {

        CcNodeUpdateResponse resp = new CcNodeUpdateResponse();
        resp.setType(valueOf(type.toUpperCase()));

        switch (resp.getType()) {
            case BCC:
                service.refactorBcc(user, request.getTargetManifestId(), request.getDestinationManifestId());
                break;
            case ASCC:
                service.refactorAscc(user, request.getTargetManifestId(), request.getDestinationManifestId());
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return resp;
    }

    @RequestMapping(value = "/core_component/{type}/refactor",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcRefactorValidationResponse refactorValidation(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                           @PathVariable("type") String type,
                                                           @RequestParam("targetManifestId") BigInteger targetManifestId,
                                                           @RequestParam("destinationManifestId") BigInteger destinationManifestId) {

        CcRefactorRequest request = new CcRefactorRequest();
        request.setDestinationManifestId(destinationManifestId);
        request.setTargetManifestId(targetManifestId);
        request.setType(type);

        CcRefactorValidationResponse resp;

        switch (valueOf(type)) {
            case BCC:
                resp = service.validateBccRefactoring(user, request.getTargetManifestId(), request.getDestinationManifestId());
                break;
            case ASCC:
                resp = service.validateAsccRefactoring(user, request.getTargetManifestId(), request.getDestinationManifestId());
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return resp;
    }

    @RequestMapping(value = "/core_component/acc/{manifestId:[\\d]+}/ungroup",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcUngroupResponse ungroup(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                     @PathVariable("manifestId") BigInteger accManifestId,
                                     @RequestBody CcUngroupRequest ccUngroupRequest) {

        ccUngroupRequest.setAccManifestId(accManifestId);

        return service.ungroup(user, ccUngroupRequest);
    }

    @RequestMapping(value = "/core_component/asccp/{manifestId:[\\d]+}/plantuml",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> generatePlantUml(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("manifestId") BigInteger asccpManifestId,
                                                @RequestParam(value = "asccpLinkTemplate", defaultValue = "/core_component/asccp/{manifestId}") String asccpLinkTemplate,
                                                @RequestParam(value = "bccpLinkTemplate", defaultValue = "/core_component/bccp/{manifestId}") String bccpLinkTemplate) throws IOException {

        String text = service.generatePlantUmlText(asccpManifestId, asccpLinkTemplate, bccpLinkTemplate);

        return Map.of("text", text,
                "encodedText", plantUmlService.getEncodedText(text));
    }
}