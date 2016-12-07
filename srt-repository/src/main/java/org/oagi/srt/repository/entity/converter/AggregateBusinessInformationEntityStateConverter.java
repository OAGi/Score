package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.AggregateBusinessInformationEntityState;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AggregateBusinessInformationEntityStateConverter
        implements AttributeConverter<AggregateBusinessInformationEntityState, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AggregateBusinessInformationEntityState attribute) {
        return (attribute == null) ? 0 : attribute.getValue();
    }

    @Override
    public AggregateBusinessInformationEntityState convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData == 0) {
            return null;
        }
        return AggregateBusinessInformationEntityState.valueOf(dbData);
    }
}