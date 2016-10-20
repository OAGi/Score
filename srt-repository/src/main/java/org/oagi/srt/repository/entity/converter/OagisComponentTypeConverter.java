package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.OagisComponentType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class OagisComponentTypeConverter
        implements AttributeConverter<OagisComponentType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OagisComponentType attribute) {
        return (attribute == null) ? -1 : attribute.getValue();
    }

    @Override
    public OagisComponentType convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData < 0) {
            return null;
        }
        return OagisComponentType.valueOf(dbData);
    }
}