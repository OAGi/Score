package org.oagi.score.gateway.http.api.oas_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.gateway.http.api.bie_management.data.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.oagi.score.gateway.http.api.oas_management.service.OpenAPIGenerateService;
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
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class OpenAPIGenerateController {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpenAPIGenerateService service;

    @RequestMapping(value = "/oas_doc/{id:[\\d]+}/generate", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> generate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @RequestParam("data") String data) throws IOException {

        Map<String, Object> params = convertValue(data);
        List<BigInteger> topLevelAsbiepIds = popTopLevelAsbiepIds(params);
        GenerateExpressionOption option =
                objectMapper.convertValue(params, GenerateExpressionOption.class);

        //read arrayIndicator and suppressRoot information from database based on topLevelAsbiepIds


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
            if (keyValue[0].startsWith("filenames")) {
                if (!params.containsKey("filenames")) {
                    params.put("filenames", new HashMap());
                }
                Map<BigInteger, String> filenames =
                        (Map<BigInteger, String>) params.get("filenames");

                try {
                    keyValue[0] = URLDecoder.decode(keyValue[0], "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new IllegalArgumentException(ex);
                }
                BigInteger topLevelAsbiepId =
                        new BigInteger(keyValue[0].substring(keyValue[0].indexOf('[') + 1, keyValue[0].indexOf(']')));
                filenames.put(topLevelAsbiepId, keyValue[1]);
            } else if (keyValue[0].startsWith("bizCtxIds")) {
                if (!params.containsKey("bizCtxIds")) {
                    params.put("bizCtxIds", new HashMap());
                }
                Map<BigInteger, BigInteger> bizCtxIds =
                        (Map<BigInteger, BigInteger>) params.get("bizCtxIds");

                try {
                    keyValue[0] = URLDecoder.decode(keyValue[0], "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new IllegalArgumentException(ex);
                }
                BigInteger topLevelAsbiepId =
                        new BigInteger(keyValue[0].substring(keyValue[0].indexOf('[') + 1, keyValue[0].indexOf(']')));
                bizCtxIds.put(topLevelAsbiepId, new BigInteger(keyValue[1]));
            } else {
                params.put(keyValue[0], keyValue[1]);
            }
        });
        return params;
    }

    private List<BigInteger> popTopLevelAsbiepIds(Map<String, Object> params) {
        Object obj = params.remove("topLevelAsbiepIds");
        if (obj == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(((String) obj).split(",")).stream()
                .map(s -> new BigInteger(s)).collect(Collectors.toList());
    }
}
