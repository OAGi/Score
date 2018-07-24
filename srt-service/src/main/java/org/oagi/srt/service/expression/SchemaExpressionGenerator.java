package org.oagi.srt.service.expression;

import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.entity.TopLevelAbie;

import java.io.File;
import java.io.IOException;

interface SchemaExpressionGenerator {

    void generate(GenerationContext generationContext,
                  TopLevelAbie topLevelAbie,
                  ProfileBODGenerationOption option) throws Exception;

    File asFile(String filename) throws IOException;

}
