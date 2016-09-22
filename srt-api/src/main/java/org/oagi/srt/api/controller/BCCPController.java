package org.oagi.srt.api.controller;

import org.oagi.srt.api.model.BCCPDetailsResponse;
import org.oagi.srt.api.model.BCCPResponse;
import org.oagi.srt.repository.entity.BasicCoreComponentProperty;
import org.oagi.srt.service.BCCPService;
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
@RequestMapping("/v1/BCCPs")
public class BCCPController {

    @Autowired
    private BCCPService bccpService;

    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"})
    public PagedResources<BCCPResponse> showAll(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<BasicCoreComponentProperty> pageResponse = bccpService.findAll(page, size);
        BCCPController methodOn = methodOn(BCCPController.class);

        List<BCCPResponse> contents = pageResponse.getContent().stream()
                .map(bccp -> {
                    BCCPResponse bccpResponse = new BCCPResponse(bccp);
                    Link self = linkTo(methodOn.showDetails(bccp.getGuid())).withSelfRel();
                    bccpResponse.add(self);
                    return bccpResponse;
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
    public BCCPDetailsResponse showDetails(
            @PathVariable("guid") String guid) {
        BasicCoreComponentProperty bccp = bccpService.findByGuid(guid);
        BCCPDetailsResponse bccpDetailsResponse = new BCCPDetailsResponse(bccp);

        BCCPController methodOn = methodOn(BCCPController.class);
        Link self = linkTo(methodOn.showDetails(bccp.getGuid())).withSelfRel();
        bccpDetailsResponse.add(links(self));

        return bccpDetailsResponse;
    }


}
