package org.oagi.score.gateway.http.api.oas_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIGenerateService;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.common.util.ControllerHelper.getRequestHostname;
import static org.oagi.score.common.util.ControllerHelper.getRequestScheme;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

@RestController
public class OpenAPIGenerateController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private OpenAPIDocService oasDocService;

    @Autowired
    private OpenAPIGenerateService generateService;

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/generate", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> generate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @PathVariable("id") BigInteger oasDocId,
                                                        @RequestParam(name = "sortActives") String sortActives,
                                                        @RequestParam(name = "sortDirections") String sortDirections,
                                                        HttpServletRequest httpServletRequest) throws IOException {

        GetBieForOasDocRequest request = new GetBieForOasDocRequest(authenticationService.asScoreUser(user));
        request.setPageIndex(-1);
        request.setPageSize(-1);
        request.setSortActives(!hasLength(sortActives) ? Collections.emptyList() :
                Arrays.asList(sortActives.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setSortDirections(!hasLength(sortDirections) ? Collections.emptyList() :
                Arrays.asList(sortDirections.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> SortDirection.valueOf(e.toUpperCase())).collect(Collectors.toList()));

        request.setOasDocId(oasDocId);
        GetBieForOasDocResponse bieForOasDocTable = oasDocService.getBieForOasDoc(request);
        List<BigInteger> topLevelAsbiepIds = new ArrayList<>();
        List<BieForOasDoc> bieListForOasDoc = bieForOasDocTable.getResults();
        Map<String, OpenAPIGenerateExpressionOption> params = new HashMap<>();
        GetOasDocRequest getOasDocRequest = new GetOasDocRequest(authenticationService.asScoreUser(user));
        getOasDocRequest.setOasDocId(oasDocId);
        GetOasDocResponse getOasDocResponse = oasDocService.getOasDoc(getOasDocRequest);
        if (bieListForOasDoc != null) {
            for (BieForOasDoc bieForOasDoc : bieListForOasDoc) {
                String paramsKey = bieForOasDoc.getVerb() + bieForOasDoc.getResourceName() + bieForOasDoc.getMessageBody();
                GetAssignedOasTagRequest getAssignedOasTagRequest = new GetAssignedOasTagRequest(authenticationService.asScoreUser(user));
                getAssignedOasTagRequest.setOasOperationId(bieForOasDoc.getOasOperationId());
                getAssignedOasTagRequest.setMessageBodyType(bieForOasDoc.getMessageBody());
                GetAssignedOasTagResponse getAssignedOasTagResponse = oasDocService.getAssignedOasTag(getAssignedOasTagRequest);
                OpenAPIGenerateExpressionOption openAPIGenerateExpressionOption = new OpenAPIGenerateExpressionOption();
                openAPIGenerateExpressionOption.setOasDoc(getOasDocResponse.getOasDoc());
                openAPIGenerateExpressionOption.setMessageBodyType(bieForOasDoc.getMessageBody());
                BigInteger topLevelAsbiepId = bieForOasDoc.getTopLevelAsbiepId();
                if (topLevelAsbiepIds != null) {
                    topLevelAsbiepIds.add(topLevelAsbiepId);
                }
                if (getAssignedOasTagResponse != null) {
                    openAPIGenerateExpressionOption.setTagName(getAssignedOasTagResponse.getOasTag().getName());
                }
                openAPIGenerateExpressionOption.setTopLevelAsbiepId(topLevelAsbiepId);
                openAPIGenerateExpressionOption.setResourceName(bieForOasDoc.getResourceName());
                openAPIGenerateExpressionOption.setOperationId(bieForOasDoc.getOperationId());
                openAPIGenerateExpressionOption.setVerb(bieForOasDoc.getVerb());
                openAPIGenerateExpressionOption.setOpenAPICodeGenerationFriendly(true);
                openAPIGenerateExpressionOption.setScheme(getRequestScheme(httpServletRequest));
                openAPIGenerateExpressionOption.setHost(getRequestHostname(httpServletRequest));

                String verbOption = openAPIGenerateExpressionOption.getVerb();
                String templateKey = "";
                switch (verbOption) {
                    case "GET":
                        OpenAPITemplateForVerbOption openAPI30GetTemplate = new OpenAPITemplateForVerbOption("GET");
                        openAPI30GetTemplate.setArrayForJsonExpression(bieForOasDoc.isArrayIndicator());
                        openAPI30GetTemplate.setSuppressRootProperty(bieForOasDoc.isSuppressRootIndicator());
                        templateKey = "GET-" + bieForOasDoc.getResourceName();
                        openAPIGenerateExpressionOption.getOpenAPI30TemplateMap().put(templateKey, openAPI30GetTemplate);
                        break;
                    case "POST":
                        OpenAPITemplateForVerbOption openAPI30PostTemplate = new OpenAPITemplateForVerbOption("POST");
                        openAPI30PostTemplate.setArrayForJsonExpression(bieForOasDoc.isArrayIndicator());
                        openAPI30PostTemplate.setSuppressRootProperty(bieForOasDoc.isSuppressRootIndicator());
                        templateKey = "POST-" + bieForOasDoc.getResourceName();
                        openAPIGenerateExpressionOption.getOpenAPI30TemplateMap().put(templateKey, openAPI30PostTemplate);
                        break;
                    case "PATCH":
                        OpenAPITemplateForVerbOption openAPI30PatchTemplate = new OpenAPITemplateForVerbOption("PATCH");
                        openAPI30PatchTemplate.setArrayForJsonExpression(bieForOasDoc.isArrayIndicator());
                        openAPI30PatchTemplate.setSuppressRootProperty(bieForOasDoc.isSuppressRootIndicator());
                        templateKey = "PATCH-" + bieForOasDoc.getResourceName();
                        openAPIGenerateExpressionOption.getOpenAPI30TemplateMap().put(templateKey, openAPI30PatchTemplate);
                        break;
                    case "PUT":
                        OpenAPITemplateForVerbOption openAPI30PutTemplate = new OpenAPITemplateForVerbOption("PUT");
                        openAPI30PutTemplate.setArrayForJsonExpression(bieForOasDoc.isArrayIndicator());
                        openAPI30PutTemplate.setSuppressRootProperty(bieForOasDoc.isSuppressRootIndicator());
                        templateKey = "PUT-" + bieForOasDoc.getResourceName();
                        openAPIGenerateExpressionOption.getOpenAPI30TemplateMap().put(templateKey, openAPI30PutTemplate);
                        break;
                    case "DELETE":
                        OpenAPITemplateForVerbOption openAPI30DeleteTemplate = new OpenAPITemplateForVerbOption("DELETE");
                        openAPI30DeleteTemplate.setArrayForJsonExpression(bieForOasDoc.isArrayIndicator());
                        openAPI30DeleteTemplate.setSuppressRootProperty(bieForOasDoc.isSuppressRootIndicator());
                        templateKey = "DELETE-" + bieForOasDoc.getResourceName();
                        openAPIGenerateExpressionOption.getOpenAPI30TemplateMap().put(templateKey, openAPI30DeleteTemplate);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown verb option: " + verbOption);
                }
                if (!params.containsKey(paramsKey)) {
                    params.put(paramsKey, openAPIGenerateExpressionOption);
                } else {
                    params.put(paramsKey, openAPIGenerateExpressionOption);
                }

            }
        }

        BieGenerateExpressionResult bieGenerateExpressionResult = generateService.generate(user, params, topLevelAsbiepIds);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bieGenerateExpressionResult.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(bieGenerateExpressionResult.getContentType()))
                .contentLength(bieGenerateExpressionResult.getFile().length())
                .body(new InputStreamResource(new FileInputStream(bieGenerateExpressionResult.getFile())));
    }
}
