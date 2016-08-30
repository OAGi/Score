package org.oagi.srt.webapp.controller;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.service.ContextCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
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

    @RequestMapping(value = "/edit/{ctxCategoryId}", method = RequestMethod.GET)
    public ModelAndView editView(@PathVariable Long ctxCategoryId) {
        ModelAndView modelAndView = new ModelAndView("context_category/edit");
        ContextCategory contextCategory = contextCategoryService.findById(ctxCategoryId);
        modelAndView.addObject("contextCategory", contextCategory);
        return modelAndView;
    }

    @RequestMapping(value = "/edit/{ctxCategoryId}", method = RequestMethod.POST)
    public ResponseEntity edit(@PathVariable Long ctxCategoryId,
                               @RequestParam String name, @RequestParam String description) {
        ContextCategory contextCategory = contextCategoryService.findById(ctxCategoryId);
        contextCategory.setName(name);
        contextCategory.setDescription(description);
        contextCategoryService.update(contextCategory);

        return new ResponseEntity(HttpStatus.OK);
    }
}
