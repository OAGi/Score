package org.oagi.score.repo.component.module;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.time.LocalDateTime;

public class CreateModuleSetRepositoryRequest extends RepositoryRequest {

    private String name;
    private String description;

    public CreateModuleSetRepositoryRequest(AuthenticatedPrincipal user) {
        super(user);
    }

    public CreateModuleSetRepositoryRequest(AuthenticatedPrincipal user,
                                            LocalDateTime localDateTime) {
        super(user, localDateTime);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
