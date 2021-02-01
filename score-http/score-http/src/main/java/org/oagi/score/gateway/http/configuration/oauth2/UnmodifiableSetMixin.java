package org.oagi.score.gateway.http.configuration.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonDeserialize(using = UnmodifiableSetDeserializer.class)
public class UnmodifiableSetMixin {

    @JsonCreator
    UnmodifiableSetMixin(Set<?> set) {
    }
}