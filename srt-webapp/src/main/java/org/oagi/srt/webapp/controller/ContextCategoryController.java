package org.oagi.srt.webapp.controller;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/context/category")
public class ContextCategoryController {

    @Autowired
    private ContextCategoryService contextCategoryService;

    @RequestMapping("/list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("context-category");
        List<ContextCategory> contextCategories =
                contextCategoryService.findAll(Sort.Direction.DESC, "ctxCategoryId");
        modelAndView.addObject("contextCategories", contextCategories);
        return modelAndView;
    }
}
