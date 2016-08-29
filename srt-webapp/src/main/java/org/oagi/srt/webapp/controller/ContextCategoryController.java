package org.oagi.srt.webapp.controller;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/context-category")
public class ContextCategoryController {

    @Autowired
    private ContextCategoryService contextCategoryService;

    @RequestMapping("/list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("context_category/list");
        List<ContextCategory> contextCategories =
                contextCategoryService.findAll(Sort.Direction.ASC, "name");
        modelAndView.addObject("contextCategories", contextCategories);
        return modelAndView;
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public ModelAndView createView() {
        ModelAndView modelAndView = new ModelAndView("context_category/create");
        List<ContextCategory> contextCategories =
                contextCategoryService.findAll(Sort.Direction.ASC, "name");
        modelAndView.addObject("contextCategories", contextCategories);
        return modelAndView;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity create(@RequestParam String name, @RequestParam String description) {
        contextCategoryService.newContextCategoryBuilder()
                .name(name)
                .description(description)
                .build();

        return new ResponseEntity(HttpStatus.OK);
    }
}
