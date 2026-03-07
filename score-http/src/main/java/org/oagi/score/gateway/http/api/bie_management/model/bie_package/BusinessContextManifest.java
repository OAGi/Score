package org.oagi.score.gateway.http.api.bie_management.model.bie_package;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;

@JsonInclude
public record BusinessContextManifest(String name,
                                      Collection<BusinessContextValueManifest> businessContextValues) {
}
