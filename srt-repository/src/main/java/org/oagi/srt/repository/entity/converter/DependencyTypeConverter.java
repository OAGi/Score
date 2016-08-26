package org.oagi.srt.repository.entity.converter;

import org.oagi.srt.repository.entity.ModuleDep;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DependencyTypeConverter implements AttributeConverter<ModuleDep.DependencyType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ModuleDep.DependencyType attribute) {
        return attribute.getValue();
    }

    @Override
    public ModuleDep.DependencyType convertToEntityAttribute(Integer dbData) {
        return ModuleDep.DependencyType.valueOf(dbData);
    }
}

