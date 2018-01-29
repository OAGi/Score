package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.RevisionAction;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class RevisionActionConverter
        implements AttributeConverter<RevisionAction, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RevisionAction attribute) {
        return (attribute == null) ? null : attribute.getValue();
    }

    @Override
    public RevisionAction convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData == 0) {
            return null;
        }
        return RevisionAction.valueOf(dbData);
    }
}