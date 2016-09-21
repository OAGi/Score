package org.oagi.srt.api.controller;

import org.oagi.srt.api.model.ACCDetailsResponse;
import org.oagi.srt.api.model.ACCResponse;
import org.oagi.srt.api.model.ASCCPResponse;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.oagi.srt.service.ACCService;
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
@RequestMapping("/v1/ACCs")
public class ACCController {

    @Autowired
    private ACCService accService;

    @RequestMapping(method = RequestMethod.GET, produces = {"application/json"})
    public PagedResources<ASCCPResponse> showAll(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Page<AggregateCoreComponent> pageResponse = accService.findAll(page, size);
        ACCController methodOn = methodOn(ACCController.class);

        List<ACCResponse> contents = pageResponse.getContent().stream()
                .map(acc -> {
                    ACCResponse accResponse = new ACCResponse(acc);
                    Link self = linkTo(methodOn.showDetails(acc.getGuid())).withSelfRel();
                    accResponse.add(self);
                    return accResponse;
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
    public ACCDetailsResponse showDetails(
            @PathVariable("guid") String guid) {
        AggregateCoreComponent acc = accService.findByGuid(guid);
        ACCDetailsResponse accDetailsResponse = new ACCDetailsResponse(acc);

        ACCController methodOn = methodOn(ACCController.class);
        Link self = linkTo(methodOn.showDetails(acc.getGuid())).withSelfRel();
        accDetailsResponse.add(self);

        return accDetailsResponse;
    }


}
