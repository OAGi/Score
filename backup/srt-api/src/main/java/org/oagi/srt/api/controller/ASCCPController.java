package org.oagi.srt.api.controller;

import org.oagi.srt.api.model.ASCCPDetailsResponse;
import org.oagi.srt.api.model.ASCCPResponse;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.repository.entity.AssociationCoreComponentProperty;
import org.oagi.srt.service.ACCService;
import org.oagi.srt.service.ASCCPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.srt.api.controller.utils.ControllerUtils.links;
import static org.oagi.srt.api.controller.utils.ControllerUtils.pageMetadata;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/ASCCPs")
public class ASCCPController {

    @Autowired
    private ASCCPService asccpService;

    @Autowired
    private ACCService accService;

    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"})
    public PagedResources<ASCCPResponse> showAll(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<AssociationCoreComponentProperty> pageResponse = asccpService.findAll(page, size);
        ASCCPController methodOn = methodOn(ASCCPController.class);

        List<ASCCPResponse> contents = pageResponse.getContent().stream()
                .map(asccp -> {
                    ASCCPResponse asccpResponse = new ASCCPResponse(asccp);
                    Link self = linkTo(methodOn.showDetails(asccp.getGuid())).withSelfRel();
                    asccpResponse.add(self);
                    return asccpResponse;
                })
                .collect(Collectors.toList());


        int totalPages = pageResponse.getTotalPages();
        Link previous = (page - 1) < 0 ? null : linkTo(methodOn.showAll((page - 1), size))
                .withRel(Link.REL_PREVIOUS);
        Link next = (page + 1) >= totalPages ? null : linkTo(methodOn.showAll((page + 1), size))
                .withRel(Link.REL_NEXT);
        Link first = linkTo(methodOn.showAll(1, size)).withRel(Link.REL_FIRST);
        Link last = linkTo(methodOn.showAll(totalPages - 1, size)).withRel(Link.REL_LAST);
        Link self = linkTo(methodOn.showAll(page, size)).withSelfRel();

        return new PagedResources(contents, pageMetadata(pageResponse), links(self, first, previous, next, last));
    }

    @RequestMapping(path = "/{guid}", method = RequestMethod.GET, produces = {"application/json"})
    public ASCCPDetailsResponse showDetails(
            @PathVariable("guid") String guid) {
        AssociationCoreComponentProperty asccp = asccpService.findByGuid(guid);
        ASCCPDetailsResponse asccpDetailsResponse = new ASCCPDetailsResponse(asccp);

        ASCCPController methodOn = methodOn(ASCCPController.class);
        Link self = linkTo(methodOn.showDetails(asccp.getGuid())).withSelfRel();
        AggregateCoreComponent acc = accService.findById(asccp.getRoleOfAccId());
        Link accLink = linkTo(methodOn(ACCController.class).showDetails(acc.getGuid())).withRel("ACC");
        asccpDetailsResponse.add(links(self, accLink));

        return asccpDetailsResponse;
    }
}
