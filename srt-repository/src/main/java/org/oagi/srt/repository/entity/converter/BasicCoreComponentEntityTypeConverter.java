package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.BasicCoreComponentEntityType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class BasicCoreComponentEntityTypeConverter
        implements AttributeConverter<BasicCoreComponentEntityType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(BasicCoreComponentEntityType attribute) {
        return (attribute == null) ? -1 : attribute.getValue();
    }

    @Override
    public BasicCoreComponentEntityType convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData < 0) {
            return null;
        }
        return BasicCoreComponentEntityType.valueOf(dbData);
    }
}