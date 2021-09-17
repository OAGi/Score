package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.PaginationResponse;
import org.oagi.score.repo.api.base.Response;

import java.util.List;

public class GetModuleElementResponse extends Response {

    public List<ModuleElement> getElements() {
        return elements;
    }

    public void setElements(List<ModuleElement> elements) {
        this.elements = elements;
    }

    List<ModuleElement> elements;

}
