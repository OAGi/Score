package org.oagi.score.gateway.http.api.bie_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.bie_management.data.DeprecateBIERequest;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.*;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAbieNode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditAsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.BieEditRef;
import org.oagi.score.gateway.http.api.bie_management.service.BieCreateFromExistingBieService;
import org.oagi.score.gateway.http.api.bie_management.service.BieEditService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.bie.model.BieState;
import org.oagi.score.repo.component.abie.AbieNode;
import org.oagi.score.repo.component.agency_id_list.AvailableAgencyIdList;
import org.oagi.score.repo.component.asbie.AsbieNode;
import org.oagi.score.repo.component.asbiep.AsbiepNode;
import org.oagi.score.repo.component.bbie.BbieNode;
import org.oagi.score.repo.component.bbie_sc.BbieScNode;
import org.oagi.score.repo.component.bbiep.BbiepNode;
import org.oagi.score.repo.component.bdt_pri_restri.AvailableBdtPriRestri;
import org.oagi.score.repo.component.bdt_sc_pri_restri.AvailableBdtScPriRestri;
import org.oagi.score.repo.component.code_list.AvailableCodeList;
import org.oagi.score.repo.component.dt.BdtNode;
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
                                   @PathVariable("id") BigInteger topLevelAsbiepId) {
        BieEditAbieNode rootNode = service.getRootNode(user, topLevelAsbiepId);
        return rootNode;
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/abie/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AbieNode getAbieDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                  @PathVariable("manifestId") BigInteger manifestId,
                                  @RequestParam("hashPath") String hashPath) {
        return service.getAbieDetail(user, topLevelAsbiepId, manifestId, hashPath);
    }

    @RequestMapping(value = "/profile_bie/abie/{abieId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AbieNode getAbieDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("abieId") BigInteger abieId) {
        return service.getAbieDetail(user, abieId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/asbie/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AsbieNode getAsbieDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                    @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                    @PathVariable("manifestId") BigInteger manifestId,
                                    @RequestParam("hashPath") String hashPath) {
        return service.getAsbieDetail(user, topLevelAsbiepId, manifestId, hashPath);
    }

    @RequestMapping(value = "/profile_bie/asbie/{asbieId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AsbieNode getAsbieDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                    @PathVariable("asbieId") BigInteger asbieId) {
        return service.getAsbieDetail(user, asbieId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbie/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BbieNode getBbieDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                  @PathVariable("manifestId") BigInteger manifestId,
                                  @RequestParam("hashPath") String hashPath) {
        return service.getBbieDetail(user, topLevelAsbiepId, manifestId, hashPath);
    }

    @RequestMapping(value = "/profile_bie/bbie/{bbieId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BbieNode getBbieDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                  @PathVariable("bbieId") BigInteger bbieId) {
        return service.getBbieDetail(user, bbieId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/asbiep/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AsbiepNode getAsbiepDetailByManifestId(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                  @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                                  @PathVariable("manifestId") BigInteger manifestId,
                                                  @RequestParam("hashPath") String hashPath) {
        return service.getAsbiepDetail(user, topLevelAsbiepId, manifestId, hashPath);
    }

    @RequestMapping(value = "/profile_bie/asbiep/{asbiepId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AsbiepNode getAsbiepDetailByAsbiepId(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                @PathVariable("asbiepId") BigInteger asbiepId) {
        return service.getAsbiepDetail(user, asbiepId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbiep/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BbiepNode getBbiepDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                    @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                    @PathVariable("manifestId") BigInteger manifestId,
                                    @RequestParam("hashPath") String hashPath) {
        return service.getBbiepDetail(user, topLevelAsbiepId, manifestId, hashPath);
    }

    @RequestMapping(value = "/profile_bie/bbiep/{bbiepId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BbiepNode getBbiepDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                    @PathVariable("bbiepId") BigInteger bbiepId) {
        return service.getBbiepDetail(user, bbiepId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbiep/{manifestId:[\\d]+}/bdt_pri_restri",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AvailableBdtPriRestri> availableBdtPriRestriListByBccpManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
            @PathVariable("manifestId") BigInteger manifestId) {
        return service.availableBdtPriRestriListByBccpManifestId(user, topLevelAsbiepId, manifestId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbiep/{manifestId:[\\d]+}/code_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AvailableCodeList> availableCodeListListByBccpManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
            @PathVariable("manifestId") BigInteger manifestId) {
        return service.availableCodeListListByBccpManifestId(user, topLevelAsbiepId, manifestId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbiep/{manifestId:[\\d]+}/agency_id_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AvailableAgencyIdList> availableAgencyIdListListByBccpManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
            @PathVariable("manifestId") BigInteger manifestId) {
        return service.availableAgencyIdListListByBccpManifestId(user, topLevelAsbiepId, manifestId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbie_sc/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BbieScNode getBbieScDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                      @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                      @PathVariable("manifestId") BigInteger manifestId,
                                      @RequestParam("hashPath") String hashPath) {
        return service.getBbieScDetail(user, topLevelAsbiepId, manifestId, hashPath);
    }

    @RequestMapping(value = "/profile_bie/bbie_sc/{bbieScId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BbieScNode getBbieScDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                      @PathVariable("bbieScId") BigInteger bbieScId) {
        return service.getBbieScDetail(user, bbieScId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbie_sc/{manifestId:[\\d]+}/bdt_sc_pri_restri",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AvailableBdtScPriRestri> availableBdtScPriRestriListByBdtScManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
            @PathVariable("manifestId") BigInteger manifestId) {
        return service.availableBdtScPriRestriListByBdtScManifestId(user, topLevelAsbiepId, manifestId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbie_sc/{manifestId:[\\d]+}/code_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AvailableCodeList> availableCodeListListByBdtScManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
            @PathVariable("manifestId") BigInteger manifestId) {
        return service.availableCodeListListByBdtScManifestId(user, topLevelAsbiepId, manifestId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/bbie_sc/{manifestId:[\\d]+}/agency_id_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AvailableAgencyIdList> availableAgencyIdListListByBdtScManifestId(
            @AuthenticationPrincipal AuthenticatedPrincipal user,
            @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
            @PathVariable("manifestId") BigInteger manifestId) {
        return service.availableAgencyIdListListByBdtScManifestId(user, topLevelAsbiepId, manifestId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/dt/{manifestId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BdtNode getBdtDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                @PathVariable("manifestId") BigInteger manifestId,
                                @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId) {
        return service.getBdtDetail(user, topLevelAsbiepId, manifestId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/used_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieEditUsed> getUsedAbieList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId) {
        return service.getBieUsedList(user, topLevelAsbiepId);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/ref_list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieEditRef> getRefAsbieList(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId) {
        return service.getBieRefList(user, topLevelAsbiepId);
    }

    @RequestMapping(value = "/profile_bie/node/root/{id:[\\d]+}/state", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                            @PathVariable("id") BigInteger topLevelAsbiepId,
                            @RequestBody Map<String, Object> body) {
        BieState state = BieState.valueOf((String) body.get("state"));
        service.updateState(user, topLevelAsbiepId, state);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/detail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditUpdateDetailResponse updateDetails(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                     @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                                                     @RequestBody BieEditUpdateDetailRequest request) {
        request.setTopLevelAsbiepId(topLevelAsbiepId);
        return service.updateDetails(user, request);
    }

    @RequestMapping(value = "/profile_bie/node/extension/local", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateExtensionResponse createLocalAbieExtension(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                            @RequestBody BieEditAsbiepNode extensionNode) {
        CreateExtensionResponse response = service.createLocalAbieExtension(user, extensionNode);
        return response;
    }

    @RequestMapping(value = "/profile_bie/node/extension/global", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateExtensionResponse createGlobalAbieExtension(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                             @RequestBody BieEditAsbiepNode extensionNode) {
        CreateExtensionResponse response = service.createGlobalAbieExtension(user, extensionNode);
        return response;
    }

    /* Reuse BIE */

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/asbiep/{manifestId:[\\d]+}/reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void reuseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                         @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                         @PathVariable("manifestId") BigInteger manifestId,
                         @RequestBody ReuseBIERequest reuseBIERequest) {

        reuseBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        reuseBIERequest.setAsccpManifestId(manifestId);
        service.reuseBIE(user, reuseBIERequest);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/remove_reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void reuseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                         @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                         @RequestBody RemoveReusedBIERequest removeReusedBIERequest) {

        removeReusedBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.removeReusedBIE(user, removeReusedBIERequest);
    }


    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/retain_reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void retainReuseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                               @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                               @RequestBody RemoveReusedBIERequest removeReusedBIERequest) {

        removeReusedBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.retainReuseBIE(user, removeReusedBIERequest);
    }

    @RequestMapping(value = "/profile_bie/node/create_bie_from_existing_bie",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void createBieFromExistingBie(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                         @RequestBody CreateBieFromExistingBieRequest request) {

        createBieFromExistingBieService.createBieFromExistingBie(user, request);
    }

    /* Base/Inherited BIE */

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/use_base",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void useBaseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                           @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                           @RequestBody UseBaseBIERequest useBaseBIERequest) {

        useBaseBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.useBaseBIE(user, useBaseBIERequest);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/remove_base",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeBaseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                              @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId) {

        RemoveBaseBIERequest removeBaseBIERequest = new RemoveBaseBIERequest();
        removeBaseBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        service.removeBaseBIE(user, removeBaseBIERequest);
    }

    @RequestMapping(value = "/profile_bie/{basedTopLevelAsbiepId:[\\d]+}/create_inherited_bie",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void createInheritedBie(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                   @PathVariable("basedTopLevelAsbiepId") BigInteger basedTopLevelAsbiepId) {

        CreateInheritedBieRequest request = new CreateInheritedBieRequest();
        request.setBasedTopLevelAsbiepId(basedTopLevelAsbiepId);
        createBieFromExistingBieService.createInheritedBie(user, request);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/reset_detail",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void resetDetailBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                               @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                               @RequestBody ResetDetailBIERequest request) {

        request.setTopLevelAsbiepId(topLevelAsbiepId);
        service.resetDetailBIE(user, request);
    }

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId:[\\d]+}/deprecate",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deprecateBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                             @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                             @RequestBody DeprecateBIERequest request) {

        request.setTopLevelAsbiepId(topLevelAsbiepId);
        service.deprecateBIE(user, request);
    }
}
