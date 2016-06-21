package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.config.ImportConfig;
import org.oagi.srt.config.RepositoryConfig;
import org.oagi.srt.repository.BlobContentRepository;
import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.entity.BlobContent;
import org.oagi.srt.repository.entity.Release;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Component
public class PopulateBlobContents {

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private BlobContentRepository blobContentRepository;
    private File baseDataDirectory;

    @PostConstruct
    public void init() {
        baseDataDirectory = new File(SRTConstants.BASE_DATA_PATH).getAbsoluteFile();
        if (!baseDataDirectory.exists()) {
            throw new IllegalStateException("Couldn't find data directory. Please check your environments.");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws IOException {
        Release release = releaseRepository.findOneByReleaseNum("10.1");

        Collection<File> files = Arrays.asList(
                new File(baseDataDirectory, "Model/Platform/2_1/Common/DataTypes"),
                new File(baseDataDirectory, "Model/Platform/2_1/Common/ISO20022"),
                new File(baseDataDirectory, "Model/Platform/2_1/OAGi-Platform.xsd"));

        for (File file : files) {
            populate(file, release);
        }
        for (File file : new File(baseDataDirectory, "Model/BODs")
                .listFiles((dir, name) -> name.endsWith("IST.xsd"))) {
            populate(file, release);
        }
        for (File file : new File(baseDataDirectory, "Model/Nouns")
                .listFiles((dir, name) -> name.endsWith("IST.xsd"))) {
            populate(file, release);
        }
    }

    private void populate(File file, Release release) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                populate(child, release);
            }
        } else {
            if (!isXMLSchemaFile(file)) {
                return;
            }

            BlobContent blobContent = new BlobContent(file);
            blobContent.setReleaseId(release.getReleaseId());
            blobContentRepository.save(blobContent);
        }
    }

    private boolean isXMLSchemaFile(File file) {
        return file.getName().endsWith(".xsd");
    }

    public static void main(String[] args) throws IOException {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(RepositoryConfig.class, ImportConfig.class)) {
            PopulateBlobContents populateBlobContents = ctx.getBean(PopulateBlobContents.class);
            populateBlobContents.run(ctx);
        }
    }
}
