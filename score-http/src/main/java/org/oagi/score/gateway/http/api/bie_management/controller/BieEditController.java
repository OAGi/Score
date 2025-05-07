package org.oagi.score.gateway.http.api.bie_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.DeprecateBIERequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.*;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditAbieNode;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditAsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditRef;
import org.oagi.score.gateway.http.api.bie_management.service.BieCreateFromExistingBieService;
import org.oagi.score.gateway.http.api.bie_management.service.BieEditService;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
public class BieEditController {

    @Autowired
    private BieEditService service;

    @Autowired
    private BieCreateFromExistingBieService createBieFromExistingBieService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/profile_bie/node/root/{id:[\\d]+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditNode getRootNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                   @PathVariable("id") TopLevelAsbiepId topLevelAsbiepId) {
        BieEditAbieNode rootNode = service.getRootNode(sessionService.asScoreUser(user), topLevelAsbiepId);
        return rootNode;
    }

    @GetMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/abie/{manifestId:[\\d]+}")
    public AbieDetailsRecord getAbieDetails(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                                            @PathVariable("manifestId") AccManifestId accManifestId,
                                            @RequestParam("hashPath") String hashPath) {
        return service.getAbieDetails(sessionService.asScoreUser(user), topLevelAsbiepId, accManifestId, hashPath);
    }

    @GetMapping(value = "/profile_bie/abie/{abieId:[\\d]+}")
    public AbieDetailsRecord getAbieDetails(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("abieId") AbieId abieId) {
        return service.getAbieDetails(sessionService.asScoreUser(user), abieId);
    }

    @GetMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/asbie/{manifestId:[\\d]+}")
    public AsbieDetailsRecord getAsbieDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
            @PathVariable("manifestId") AsccManifestId manifestId,
            @RequestParam("hashPath") String hashPath) {
        return service.getAsbieDetails(sessionService.asScoreUser(user), topLevelAsbiepId, manifestId, hashPath);
    }

    @GetMapping(value = "/profile_bie/asbie/{asbieId:[\\d]+}")
    public AsbieDetailsRecord getAsbieDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asbieId") AsbieId asbieId) {
        return service.getAsbieDetails(sessionService.asScoreUser(user), asbieId);
    }

    @GetMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbie/{manifestId:[\\d]+}")
    public BbieDetailsRecord getBbieDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
            @PathVariable("manifestId") BccManifestId manifestId,
            @RequestParam("hashPath") String hashPath) {
        return service.getBbieDetails(sessionService.asScoreUser(user), topLevelAsbiepId, manifestId, hashPath);
    }

    @GetMapping(value = "/profile_bie/bbie/{bbieId:[\\d]+}")
    public BbieDetailsRecord getBbieDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bbieId") BbieId bbieId) {
        return service.getBbieDetails(sessionService.asScoreUser(user), bbieId);
    }

    @GetMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/asbiep/{manifestId:[\\d]+}")
    public AsbiepDetailsRecord getAsbiepDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
            @PathVariable("manifestId") AsccpManifestId asccpManifestId,
            @RequestParam("hashPath") String hashPath) {
        return service.getAsbiepDetails(sessionService.asScoreUser(user), topLevelAsbiepId, asccpManifestId, hashPath);
    }

    @GetMapping(value = "/profile_bie/asbiep/{asbiepId:[\\d]+}")
    public AsbiepDetailsRecord getAsbiepDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("asbiepId") AsbiepId asbiepId) {
        return service.getAsbiepDetails(sessionService.asScoreUser(user), asbiepId);
    }

    @GetMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbiep/{manifestId:[\\d]+}")
    public BbiepDetailsRecord getBbiepDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
            @PathVariable("manifestId") BccpManifestId bccpManifestId,
            @RequestParam("hashPath") String hashPath) {
        return service.getBbiepDetails(sessionService.asScoreUser(user), topLevelAsbiepId, bccpManifestId, hashPath);
    }

    @GetMapping(value = "/profile_bie/bbiep/{bbiepId:[\\d]+}")
    public BbiepDetailsRecord getBbiepDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bbiepId") BbiepId bbiepId) {
        return service.getBbiepDetails(sessionService.asScoreUser(user), bbiepId);
    }

    @GetMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbie_sc/{manifestId:[\\d]+}")
    public BbieScDetailsRecord getBbieScDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
            @PathVariable("manifestId") DtScManifestId manifestId,
            @RequestParam("hashPath") String hashPath) {
        return service.getBbieScDetails(sessionService.asScoreUser(user), topLevelAsbiepId, manifestId, hashPath);
    }

    @GetMapping(value = "/profile_bie/bbie_sc/{bbieScId:[\\d]+}")
    public BbieScDetailsRecord getBbieScDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("bbieScId") BbieScId bbieScId) {
        return service.getBbieScDetails(sessionService.asScoreUser(user), bbieScId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/used_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieEditUsed> getUsedAbieList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId) {
        return service.getBieUsedList(sessionService.asScoreUser(user), topLevelAsbiepId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/ref_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieEditRef> getRefAsbieList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId) {
        return service.getBieRefList(sessionService.asScoreUser(user), topLevelAsbiepId);
    }

    @RequestMapping(value = "/profile_bie/node/root/{id:[\\d]+}/state", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                            @PathVariable("id") TopLevelAsbiepId topLevelAsbiepId,
                            @RequestBody Map<String, Object> body) {
        BieState state = BieState.valueOf((String) body.get("state"));
        service.updateState(sessionService.asScoreUser(user), topLevelAsbiepId, state);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/detail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditUpdateDetailResponse updateDetails(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                     @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                                                     @RequestBody BieEditUpdateDetailRequest request) {
        request.setTopLevelAsbiepId(topLevelAsbiepId);
        return service.updateDetails(sessionService.asScoreUser(user), request);
    }

    @RequestMapping(value = "/profile_bie/node/extension/local", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateExtensionResponse createLocalAbieExtension(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                            @RequestBody BieEditAsbiepNode extensionNode) {
        CreateExtensionResponse response = service.createLocalAbieExtension(sessionService.asScoreUser(user), extensionNode);
        return response;
    }

    @RequestMapping(value = "/profile_bie/node/extension/global", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateExtensionResponse createGlobalAbieExtension(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                             @RequestBody BieEditAsbiepNode extensionNode) {
        CreateExtensionResponse response = service.createGlobalAbieExtension(sessionService.asScoreUser(user), extensionNode);
        return response;
    }

    /* Reuse BIE */

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/asbiep/{manifestId:[\\d]+}/reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void reuseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                         @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                         @PathVariable("manifestId") AsccpManifestId manifestId,
                         @RequestBody ReuseBIERequest reuseBIERequest) {

        reuseBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        reuseBIERequest.setAsccpManifestId(manifestId);
        service.reuseBIE(sessionService.asScoreUser(user), reuseBIERequest);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/remove_reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void reuseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                         @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                         @RequestBody RemoveReusedBIERequest removeReusedBIERequest) {

        removeReusedBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.removeReusedBIE(sessionService.asScoreUser(user), removeReusedBIERequest);
    }


    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/retain_reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void retainReuseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                               @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                               @RequestBody RemoveReusedBIERequest removeReusedBIERequest) {

        removeReusedBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.retainReuseBIE(sessionService.asScoreUser(user), removeReusedBIERequest);
    }

    @RequestMapping(value = "/profile_bie/node/create_bie_from_existing_bie",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void createBieFromExistingBie(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                         @RequestBody CreateBieFromExistingBieRequest request) {

        createBieFromExistingBieService.createBieFromExistingBie(sessionService.asScoreUser(user), request);
    }

    /* Base/Inherited BIE */

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/use_base",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void useBaseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                           @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                           @RequestBody UseBaseBIERequest useBaseBIERequest) {

        useBaseBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.useBaseBIE(sessionService.asScoreUser(user), useBaseBIERequest);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/remove_base",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeBaseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                              @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId) {

        RemoveBaseBIERequest removeBaseBIERequest = new RemoveBaseBIERequest();
        removeBaseBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.removeBaseBIE(sessionService.asScoreUser(user), removeBaseBIERequest);
    }

    @RequestMapping(value = "/profile_bie/{basedTopLevelAsbiepId:[\\d]+}/create_inherited_bie",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void createInheritedBie(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                   @PathVariable("basedTopLevelAsbiepId") BigInteger basedTopLevelAsbiepId) {

        CreateInheritedBieRequest request = new CreateInheritedBieRequest();
        request.setBasedTopLevelAsbiepId(basedTopLevelAsbiepId);
        createBieFromExistingBieService.createInheritedBie(sessionService.asScoreUser(user), request);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/reset_detail",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void resetDetailBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                               @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                               @RequestBody ResetDetailBIERequest request) {

        request.setTopLevelAsbiepId(topLevelAsbiepId);
        service.resetDetailBIE(sessionService.asScoreUser(user), request);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/deprecate",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deprecateBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("topLevelAsbiepId") TopLevelAsbiepId topLevelAsbiepId,
                             @RequestBody DeprecateBIERequest request) {

        request.setTopLevelAsbiepId(topLevelAsbiepId);
        service.deprecateBIE(sessionService.asScoreUser(user), request);
    }
}
