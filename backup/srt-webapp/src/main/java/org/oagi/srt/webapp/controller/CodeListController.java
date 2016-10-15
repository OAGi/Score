package org.oagi.srt.webapp.controller;

import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.service.CodeListService;
import org.oagi.srt.service.ContextSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/code-list")
public class CodeListController {

    @Autowired
    private CodeListService codeListService;

    @RequestMapping("/list")
    public ModelAndView list() {
        ModelAndView modelAndView = new ModelAndView("code_list/list");
        List<CodeList> codeLists =
                codeListService.findAll(Sort.Direction.ASC, "name");
        modelAndView.addObject("codeLists", codeLists);
        return modelAndView;
    }
}
