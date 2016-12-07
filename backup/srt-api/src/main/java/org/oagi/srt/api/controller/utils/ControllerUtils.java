package org.oagi.srt.api.controller.utils;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControllerUtils {

    private ControllerUtils() {}

    public static PagedResources.PageMetadata pageMetadata(Page<?> pageResponse) {
        return new PagedResources.PageMetadata(
                pageResponse.getSize(),
                pageResponse.getNumber(),
                pageResponse.getTotalElements(),
                pageResponse.getTotalPages());
    }

    public static Iterable<Link> links(Link... links) {
        List<Link> linkList = new ArrayList();
        for (Link link : links) {
            if (link != null) {
                linkList.add(link);
            }
        }
        return Collections.unmodifiableCollection(linkList);
    }
}
