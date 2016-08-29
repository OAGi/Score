package org.oagi.srt.webapp.controller;

import org.oagi.srt.repository.entity.BusinessContext;
import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.BusinessContextService;
import org.oagi.srt.service.ContextCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/business-context")
public class BusinessContextController {

    @Autowired
    private BusinessContextService businessContextService;

    @RequestMapping("/list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("business_context/list");
        List<BusinessContext> businessContexts =
                businessContextService.findAll(Sort.Direction.DESC, "lastUpdateTimestamp");
        modelAndView.addObject("businessContexts", businessContexts);
        return modelAndView;
    }

    @RequestMapping("/create")
    public ModelAndView create() {
        ModelAndView modelAndView = new ModelAndView("business_context/create");
        List<BusinessContext> businessContexts =
                businessContextService.findAll(Sort.Direction.DESC, "lastUpdateTimestamp");
        modelAndView.addObject("businessContexts", businessContexts);
        return modelAndView;
    }
}
