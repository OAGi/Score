package org.oagi.score.gateway.http.api.tag_management.controller;

import org.oagi.score.gateway.http.api.tag_management.data.Tag;
import org.oagi.score.gateway.http.api.tag_management.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
public class TagController {

    @Autowired
    private TagService tagService;

    @RequestMapping(value = "/tags", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tag> getTags(@AuthenticationPrincipal AuthenticatedPrincipal user) {
        return tagService.getTags();
    }

    @RequestMapping(value = "/tags/{type}/{manifestId:[\\d]+}", method = RequestMethod.POST)
    public void toggleTag(@AuthenticationPrincipal AuthenticatedPrincipal user,
                            @PathVariable("type") String type,
                            @PathVariable("manifestId") BigInteger manifestId,
                            @RequestBody Map<String, Object> body) {
        tagService.toggleTag(user, type, manifestId, (String) body.get("name"));
    }

    @RequestMapping(value = "/tags", method = RequestMethod.PUT)
    public void add(@AuthenticationPrincipal AuthenticatedPrincipal user,
                    @RequestBody Tag tag) {
        tagService.add(user, tag);
    }

    @RequestMapping(value = "/tags/{tagId:[\\d]+}", method = RequestMethod.POST)
    public void update(@AuthenticationPrincipal AuthenticatedPrincipal user,
                       @PathVariable("tagId") BigInteger tagId,
                       @RequestBody Tag tag) {
        tag.setTagId(tagId);
        tagService.update(user, tag);
    }

    @RequestMapping(value = "/tags/{tagId:[\\d]+}", method = RequestMethod.DELETE)
    public void discard(@AuthenticationPrincipal AuthenticatedPrincipal user,
                        @PathVariable("tagId") BigInteger tagId) {
        tagService.discard(user, tagId);
    }

}
