package org.oagi.score.gateway.http.api.plantuml.service;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;
import org.oagi.score.gateway.http.api.plantuml.data.GeneratePlantUmlDiagramRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PlantUmlService {

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

        reader.outputImage(outputStream, new FileFormatOption(FileFormat.valueOf(request.getFormat().toUpperCase())));
    }

    public String getVersion() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String result;
        try {
            try (OutputStream outputStream = new BufferedOutputStream(byteArrayOutputStream)) {
                generatePlantUmlDiagram(new GeneratePlantUmlDiagramRequest()
                        .withText("@startuml\nversion\n@enduml")
                        .withFormat("txt"), outputStream);
                outputStream.flush();
            }

            result = byteArrayOutputStream.toString().trim();
        } catch (IOException e) {
            return null;
        }

        String regex = "PlantUML version (\\d+(\\.\\d+){1,3})";
        Matcher matcher = Pattern.compile(regex).matcher(result);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

}
