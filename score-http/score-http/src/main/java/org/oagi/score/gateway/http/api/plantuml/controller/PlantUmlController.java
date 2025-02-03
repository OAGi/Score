package org.oagi.score.gateway.http.api.plantuml.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.oagi.score.gateway.http.api.plantuml.data.GeneratePlantUmlDiagramRequest;
import org.oagi.score.gateway.http.api.plantuml.service.PlantUmlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

@Controller
public class PlantUmlController {

    @Autowired
    private PlantUmlService plantUmlService;

    @RequestMapping(value = "/plantuml/{format}/{encodedText}", method = RequestMethod.GET)
    public void generatePlantUmlDiagram(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                        @PathVariable(value = "format") String format,
                                        @PathVariable(value = "encodedText") String encodedText,
                                        HttpServletResponse response) throws IOException {
        GeneratePlantUmlDiagramRequest request = new GeneratePlantUmlDiagramRequest()
                .withEncodedText(encodedText)
                .withFormat(format);
        response.setContentType(request.getContentType());
        plantUmlService.generatePlantUmlDiagram(request, response.getOutputStream());
    }


}
