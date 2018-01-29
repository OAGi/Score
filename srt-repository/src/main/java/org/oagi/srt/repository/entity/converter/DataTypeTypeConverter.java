package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.DataTypeType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DataTypeTypeConverter
        implements AttributeConverter<DataTypeType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DataTypeType attribute) {
        return attribute.getValue();
    }

    @Override
    public DataTypeType convertToEntityAttribute(Integer dbData) {
        return DataTypeType.valueOf(dbData);
    }
}