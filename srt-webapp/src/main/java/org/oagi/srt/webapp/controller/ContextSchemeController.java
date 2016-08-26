package org.oagi.srt.webapp.controller;

import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.service.ContextSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/context/scheme")
public class ContextSchemeController {

    @Autowired
    private ContextSchemeService contextSchemeService;

    @RequestMapping("/list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("context-scheme");
        List<ContextScheme> contextSchemes =
                contextSchemeService.findAll(Sort.Direction.DESC, "ctxSchemeId");
        modelAndView.addObject("contextSchemes", contextSchemes);
        return modelAndView;
    }
}
