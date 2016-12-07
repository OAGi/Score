package org.oagi.srt.webapp.controller;

import org.oagi.srt.repository.AssociationCoreComponentPropertyRepository;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.service.CodeListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/bod")
public class BODController {

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @RequestMapping("/create/select_top_level_concept")
    public ModelAndView createSelectTopLevelConcept() {
        ModelAndView modelAndView = new ModelAndView("bod/create_select_top_level_concept");
        List<AssociationCoreComponentProperty> asccpList = asccpRepository.findAllOrderByPropertyTermAsc();
        modelAndView.addObject("asccpList", asccpList);
        return modelAndView;
    }
}
