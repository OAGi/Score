package org.oagi.score.gateway.http.api.plantuml.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.oagi.score.gateway.http.api.plantuml.data.GeneratePlantUmlDiagramRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlantUmlServiceTest {

    private final PlantUmlService service = new PlantUmlService();

    @Test
    void includeOfALocalFileIsNotRendered(@TempDir Path dir) throws IOException {
        Path secret = Files.writeString(dir.resolve("secret.txt"), "TOP-SECRET-CONTENT");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.generatePlantUmlDiagram(new GeneratePlantUmlDiagramRequest()
                .withText("@startuml\nnote as N\n!include " + secret + "\nend note\n@enduml")
                .withFormat("atxt"), out);

        assertThat(out.toString(StandardCharsets.UTF_8)).doesNotContain("TOP-SECRET-CONTENT");
    }

    @Test
    void ordinaryDiagramsStillRender() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.generatePlantUmlDiagram(new GeneratePlantUmlDiagramRequest()
                .withText("@startuml\nAlice -> Bob : hello\n@enduml")
                .withFormat("svg"), out);

        assertThat(out.toString(StandardCharsets.UTF_8)).contains("<svg");
    }

    @Test
    void unknownFormatIsRejectedWithIllegalArgumentException() {
        assertThatThrownBy(() -> service.toFileFormat("nope")).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.toFileFormat("svg")).isNotNull();
    }

    @Test
    void versionIsReadWithoutRenderingADiagram() {
        assertThat(service.getVersion()).matches("\\d+(\\.\\d+){1,3}");
    }
}
