package org.oagi.score.gateway.http.api.bie_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class BieGenerateController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BieGenerateService service;

    @RequestMapping(value = "/profile_bie/generate", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> generate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @RequestParam("data") String data) throws IOException {

        Map<String, Object> params = convertValue(data);
        List<Long> topLevelAsbiepIds = popTopLevelAsbiepIds(params);
        GenerateExpressionOption option =
                objectMapper.convertValue(params, GenerateExpressionOption.class);

        BieGenerateExpressionResult bieGenerateExpressionResult = service.generate(user, topLevelAsbiepIds, option);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bieGenerateExpressionResult.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(bieGenerateExpressionResult.getContentType()))
                .contentLength(bieGenerateExpressionResult.getFile().length())
                .body(new InputStreamResource(new FileInputStream(bieGenerateExpressionResult.getFile())));
    }

    private Map<String, Object> convertValue(String data) {
        Map<String, Object> params = new HashMap();
        Arrays.stream(new String(Base64.getDecoder().decode(data)).split("&")).forEach(e -> {
            String[] keyValue = e.split("=");
            params.put(keyValue[0], keyValue[1]);
        });
        return params;
    }

    private List<Long> popTopLevelAsbiepIds(Map<String, Object> params) {
        Object obj = params.remove("topLevelAsbiepIds");
        if (obj == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(((String) obj).split(",")).stream()
                .map(s -> Long.parseLong(s)).collect(Collectors.toList());
    }
}
