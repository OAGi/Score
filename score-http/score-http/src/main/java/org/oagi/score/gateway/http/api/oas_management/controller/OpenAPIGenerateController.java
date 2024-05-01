package org.oagi.score.gateway.http.api.oas_management.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPITemplateForVerbOption;
import org.oagi.score.gateway.http.api.oas_management.data.Operation;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIGenerateService;
import org.oagi.score.repo.api.base.SortDirection;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.user.model.ScoreUser;
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

        ScoreUser requester = authenticationService.asScoreUser(user);
        GetBieForOasDocRequest request = new GetBieForOasDocRequest(requester);
        request.setPageIndex(-1);
        request.setPageSize(-1);
        request.setSortActives(!hasLength(sortActives) ? Collections.emptyList() :
                Arrays.asList(sortActives.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).collect(Collectors.toList()));
        request.setSortDirections(!hasLength(sortDirections) ? Collections.emptyList() :
                Arrays.asList(sortDirections.split(",")).stream().map(e -> e.trim()).filter(e -> hasLength(e)).map(e -> SortDirection.valueOf(e.toUpperCase())).collect(Collectors.toList()));
        request.setOasDocId(oasDocId);
        GetBieForOasDocResponse bieListForOasDocResponse = oasDocService.getBieForOasDoc(request);
        List<BieForOasDoc> bieListForOasDoc = bieListForOasDocResponse.getResults();
        if (bieListForOasDoc == null || bieListForOasDoc.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        OasDoc oasDoc = oasDocService.getOasDoc(new GetOasDocRequest(requester)
                .withOasDocId(oasDocId)).getOasDoc();

        OpenAPIGenerateExpressionOption openAPIGenerateExpressionOption = new OpenAPIGenerateExpressionOption();
        openAPIGenerateExpressionOption.setOasDoc(oasDoc);
        openAPIGenerateExpressionOption.setScheme(getRequestScheme(httpServletRequest));
        openAPIGenerateExpressionOption.setHost(getRequestHostname(httpServletRequest));

        for (BieForOasDoc bieForOasDoc : bieListForOasDoc) {
            OpenAPITemplateForVerbOption openAPITemplate = new OpenAPITemplateForVerbOption(Operation.valueOf(bieForOasDoc.getVerb()));
            openAPITemplate.setTopLevelAsbiepId(bieForOasDoc.getTopLevelAsbiepId());
            openAPITemplate.setMessageBodyType(bieForOasDoc.getMessageBody());
            openAPITemplate.setArrayForJsonExpression(bieForOasDoc.isArrayIndicator());
            openAPITemplate.setSuppressRootProperty(bieForOasDoc.isSuppressRootIndicator());
            openAPITemplate.setResourceName(bieForOasDoc.getResourceName());
            openAPITemplate.setOperationId(bieForOasDoc.getOperationId());

            OasTag oasTag = oasDocService.getAssignedOasTag(new GetAssignedOasTagRequest(requester)
                            .withOasOperationId(bieForOasDoc.getOasOperationId())
                            .withMessageBodyType(bieForOasDoc.getMessageBody()))
                    .getOasTag();
            if (oasTag != null) {
                openAPITemplate.setTagName(oasTag.getName());
            }

            openAPIGenerateExpressionOption.addTemplate(openAPITemplate);
        }

        BieGenerateExpressionResult bieGenerateExpressionResult = generateService.generate(user, openAPIGenerateExpressionOption);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bieGenerateExpressionResult.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(bieGenerateExpressionResult.getContentType()))
                .contentLength(bieGenerateExpressionResult.getFile().length())
                .body(new InputStreamResource(new FileInputStream(bieGenerateExpressionResult.getFile())));
    }
}
