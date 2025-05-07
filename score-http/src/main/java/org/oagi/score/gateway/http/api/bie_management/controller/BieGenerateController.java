package org.oagi.score.gateway.http.api.bie_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.expression.BieGenerateExpressionResult;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.BieGenerateService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
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

import static org.oagi.score.gateway.http.common.util.ControllerUtils.getRequestHostname;
import static org.oagi.score.gateway.http.common.util.ControllerUtils.getRequestScheme;

@RestController
public class BieGenerateController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BieGenerateService service;

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/profile_bie/generate", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> generate(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                        @RequestParam("data") String data,
                                                        HttpServletRequest httpServletRequest) throws IOException {

        Map<String, Object> params = convertValue(data);
        List<TopLevelAsbiepId> topLevelAsbiepIds = popTopLevelAsbiepIds(params);
        GenerateExpressionOption option =
                objectMapper.convertValue(params, GenerateExpressionOption.class);
        option.setScheme(getRequestScheme(httpServletRequest));
        option.setHost(getRequestHostname(httpServletRequest));
        BieGenerateExpressionResult bieGenerateExpressionResult = service.generate(
                sessionService.asScoreUser(user), topLevelAsbiepIds, option);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bieGenerateExpressionResult.filename() + "\"")
                .contentType(MediaType.parseMediaType(bieGenerateExpressionResult.contentType()))
                .contentLength(bieGenerateExpressionResult.file().length())
                .body(new InputStreamResource(new FileInputStream(bieGenerateExpressionResult.file())));
    }

    private Map<String, Object> convertValue(String data) {
        Map<String, Object> params = new HashMap();
        Arrays.stream(new String(Base64.getDecoder().decode(data)).split("&")).forEach(e -> {
            String[] keyValue = e.split("=");
            if (keyValue[0].startsWith("filenames")) {
                if (!params.containsKey("filenames")) {
                    params.put("filenames", new HashMap());
                }
                Map<TopLevelAsbiepId, String> filenames =
                        (Map<TopLevelAsbiepId, String>) params.get("filenames");

                try {
                    keyValue[0] = URLDecoder.decode(keyValue[0], "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new IllegalArgumentException(ex);
                }
                TopLevelAsbiepId topLevelAsbiepId =
                        TopLevelAsbiepId.from(keyValue[0].substring(keyValue[0].indexOf('[') + 1, keyValue[0].indexOf(']')));
                filenames.put(topLevelAsbiepId, keyValue[1]);
            } else if (keyValue[0].startsWith("bizCtxIds")) {
                if (!params.containsKey("bizCtxIds")) {
                    params.put("bizCtxIds", new HashMap());
                }
                Map<TopLevelAsbiepId, BigInteger> bizCtxIds =
                        (Map<TopLevelAsbiepId, BigInteger>) params.get("bizCtxIds");

                try {
                    keyValue[0] = URLDecoder.decode(keyValue[0], "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new IllegalArgumentException(ex);
                }
                TopLevelAsbiepId topLevelAsbiepId =
                        TopLevelAsbiepId.from(keyValue[0].substring(keyValue[0].indexOf('[') + 1, keyValue[0].indexOf(']')));
                bizCtxIds.put(topLevelAsbiepId, new BigInteger(keyValue[1]));
            } else {
                params.put(keyValue[0], keyValue[1]);
            }
        });
        return params;
    }

    private List<TopLevelAsbiepId> popTopLevelAsbiepIds(Map<String, Object> params) {
        Object obj = params.remove("topLevelAsbiepIds");
        if (obj == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(((String) obj).split(",")).stream()
                .map(s -> TopLevelAsbiepId.from(s)).collect(Collectors.toList());
    }
}
