package org.oagi.srt.service.expression;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.TopLevelAbieRepository;
import org.oagi.srt.repository.entity.TopLevelAbie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfileBIEGenerateService {

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private ApplicationContext applicationContext;

    public File generateSchema(List<Long> topLevelAbieIds, ProfileBODGenerationOption option) throws Exception {
        if (topLevelAbieIds == null || topLevelAbieIds.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (option == null) {
            throw new IllegalArgumentException();
        }

        switch (option.getSchemaPackage()) {
            case All:
                return generateSchemaForAll(topLevelAbieIds, option);

            case Each:
                Map<Long, File> files = generateSchemaForEach(topLevelAbieIds, option);
                return Zip.compression(files.values(), Utility.generateGUID());

            default:
                throw new IllegalStateException();
        }
    }

    private GenerationContext generationContext(TopLevelAbie topLevelAbie) {
        GenerationContext generationContext = applicationContext.getBean(GenerationContext.class);
        generationContext.init(topLevelAbie);
        return generationContext;
    }

    public File generateSchemaForAll(List<Long> topLevelAbieIds,
                                     ProfileBODGenerationOption option) throws Exception {
        SchemaExpressionGenerator schemaExpressionGenerator = createSchemaExpressionGenerator(option);

        for (long topLevelAbieId : topLevelAbieIds) {
            TopLevelAbie topLevelAbie = topLevelAbieRepository.findById(topLevelAbieId).orElse(null);
            schemaExpressionGenerator.generate(generationContext(topLevelAbie), topLevelAbie, option);
        }

        File schemaExpressionFile = schemaExpressionGenerator.asFile(Utility.generateGUID() + "_standalone");
        return schemaExpressionFile;
    }

    public Map<Long, File> generateSchemaForEach(List<Long> topLevelAbieIds,
                                                 ProfileBODGenerationOption option) throws Exception {
        Map<Long, File> targetFiles = new HashMap();
        for (long topLevelAbieId : topLevelAbieIds) {
            SchemaExpressionGenerator schemaExpressionGenerator = createSchemaExpressionGenerator(option);

            TopLevelAbie topLevelAbie = topLevelAbieRepository.findById(topLevelAbieId).orElse(null);
            schemaExpressionGenerator.generate(generationContext(topLevelAbie), topLevelAbie, option);

            File schemaExpressionFile = schemaExpressionGenerator.asFile(topLevelAbie.getAbie().getGuid());
            targetFiles.put(topLevelAbieId, schemaExpressionFile);
        }

        return targetFiles;
    }

    private SchemaExpressionGenerator createSchemaExpressionGenerator(ProfileBODGenerationOption option) {
        switch (option.getSchemaExpression()) {
            case XML:
                return new XMLSchemaExpressionGenerator();
            case JSON:
                return new JSONSchemaExpressionGenerator();
            default:
                throw new UnsupportedOperationException();
        }
    }
}
