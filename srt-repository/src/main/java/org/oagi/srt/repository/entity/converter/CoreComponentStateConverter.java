package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.CoreComponentState;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class CoreComponentStateConverter
        implements AttributeConverter<CoreComponentState, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CoreComponentState attribute) {
        return (attribute == null) ? 0 : attribute.getValue();
    }

    @Override
    public CoreComponentState convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData <= 0) {
            return null;
        }
        return CoreComponentState.valueOf(dbData);
    }
}