package org.oagi.score.gateway.http.api.oas_management.controller;

import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.oas_management.data.OpenAPIGenerateExpressionOption;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIDocService;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIGenerateService;
import org.oagi.score.repo.api.openapidoc.model.BieForOasDoc;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocRequest;
import org.oagi.score.repo.api.openapidoc.model.GetBieForOasDocResponse;
import org.oagi.score.service.authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                                        @PathVariable("id") BigInteger oasDocId) throws IOException {

        GetBieForOasDocRequest request = new GetBieForOasDocRequest(authenticationService.asScoreUser(user));

        request.setOasDocId(oasDocId);

        GetBieForOasDocResponse bieForOasDocTable = oasDocService.getBieForOasDoc(request);

        List<BieForOasDoc> bieListForOasDoc = bieForOasDocTable.getResults();
        Map<BigInteger, OpenAPIGenerateExpressionOption> params = new HashMap<>();
        if (bieListForOasDoc != null) {
            for (BieForOasDoc bieForOasDoc : bieListForOasDoc) {
                OpenAPIGenerateExpressionOption openAPIGenerateExpressionOption = new OpenAPIGenerateExpressionOption();
                openAPIGenerateExpressionOption.setVerb(bieForOasDoc.getVerbs().get(0));
                String verbOption = openAPIGenerateExpressionOption.getVerb();
                switch (verbOption) {
                    case "GET":
                        openAPIGenerateExpressionOption.setOpenAPI30GetTemplate(true);
                        openAPIGenerateExpressionOption.setArrayForJsonExpressionForOpenAPI30GetTemplate(bieForOasDoc.isArrayIndicator());
                        openAPIGenerateExpressionOption.setSuppressRootPropertyForOpenAPI30GetTemplate(bieForOasDoc.isSuppressRootIndicator());
                        break;
                    case "POST":
                        openAPIGenerateExpressionOption.setOpenAPI30PostTemplate(true);
                        openAPIGenerateExpressionOption.setArrayForJsonExpressionForOpenAPI30PostTemplate(bieForOasDoc.isArrayIndicator());
                        openAPIGenerateExpressionOption.setSuppressRootPropertyForOpenAPI30PostTemplate(bieForOasDoc.isSuppressRootIndicator());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown verb option: " + verbOption);
                }
                if (!params.containsKey(bieForOasDoc.getTopLevelAsbiepId())) {
                    params.put(bieForOasDoc.getTopLevelAsbiepId(), openAPIGenerateExpressionOption);
                } else {
                    params.put(bieForOasDoc.getTopLevelAsbiepId(), openAPIGenerateExpressionOption);
                }

            }
        }

        BieGenerateExpressionResult bieGenerateExpressionResult = generateService.generate(user, params);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bieGenerateExpressionResult.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(bieGenerateExpressionResult.getContentType()))
                .contentLength(bieGenerateExpressionResult.getFile().length())
                .body(new InputStreamResource(new FileInputStream(bieGenerateExpressionResult.getFile())));
    }
}
