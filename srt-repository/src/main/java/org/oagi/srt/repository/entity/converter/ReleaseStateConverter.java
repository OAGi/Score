package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.ReleaseState;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ReleaseStateConverter
        implements AttributeConverter<ReleaseState, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ReleaseState attribute) {
        return (attribute == null) ? 0 : attribute.getValue();
    }

    @Override
    public ReleaseState convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData <= 0) {
            return null;
        }
        return ReleaseState.valueOf(dbData);
    }
}
