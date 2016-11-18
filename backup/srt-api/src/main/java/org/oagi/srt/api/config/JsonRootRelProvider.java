package org.oagi.srt.api.config;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.atteo.evo.inflector.English;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.DefaultRelProvider;

class JsonRootRelProvider implements RelProvider {

    private DefaultRelProvider defaultRelProvider = new DefaultRelProvider();

    @Override
    public String getItemResourceRelFor(Class<?> type) {
        JsonRootName[] annotations = type.getAnnotationsByType(JsonRootName.class);
        if (annotations != null) {
            for (int i = 0, len = annotations.length; i < len; ++i) {
                return annotations[i].value();
            }
        }
        return defaultRelProvider.getItemResourceRelFor(type);
    }

    @Override
    public String getCollectionResourceRelFor(Class<?> type) {
        JsonRootName[] annotations = type.getAnnotationsByType(JsonRootName.class);
        if (annotations != null) {
            for (int i = 0, len = annotations.length; i < len; ++i) {
                return English.plural(annotations[i].value());
            }
        }
        return English.plural(defaultRelProvider.getCollectionResourceRelFor(type));
    }

    @Override
    public boolean supports(Class<?> delimiter) {
        return defaultRelProvider.supports(delimiter);
    }
}
