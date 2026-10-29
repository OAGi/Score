package org.oagi.score.gateway.http.api.plantuml.service;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;
import net.sourceforge.plantuml.version.Version;
import org.oagi.score.gateway.http.api.plantuml.data.GeneratePlantUmlDiagramRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;

@Service
public class PlantUmlService {

    // PlantUML reads the profile from the environment variable or, for this version, from a system property
    // with the same upper-case name ("plantuml.security.profile" is not honoured).
    private static final String SECURITY_PROFILE = "PLANTUML_SECURITY_PROFILE";

    static {
        // The diagram text is caller-controlled. PlantUML's default LEGACY profile lets "!include <path>" and
        // "!includeurl <url>" read server files and make server-side requests, so default to SANDBOX before
        // PlantUML initialises. An operator can still choose another profile explicitly.
        if (System.getProperty(SECURITY_PROFILE) == null && System.getenv(SECURITY_PROFILE) == null) {
            System.setProperty(SECURITY_PROFILE, "SANDBOX");
        }
    }

    public String getEncodedText(String text) throws IOException {
        Transcoder transcoder = TranscoderUtil.getDefaultTranscoder();
        return transcoder.encode(text);
    }

    public void generatePlantUmlDiagram(GeneratePlantUmlDiagramRequest request, OutputStream outputStream) throws IOException {
        String encodedText = request.getEncodedText();
        SourceStringReader reader;
        if (StringUtils.hasLength(encodedText)) {
            reader = new SourceStringReader(TranscoderUtil.getDefaultTranscoder().decode(encodedText));
        } else {
            reader = new SourceStringReader(request.getText());
        }

        reader.outputImage(outputStream, new FileFormatOption(toFileFormat(request.getFormat())));
    }

    public FileFormat toFileFormat(String format) {
        try {
            return FileFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    public String getVersion() {
        return Version.versionString();
    }

}
