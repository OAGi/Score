package org.oagi.srt.persistence.populate;

import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.repository.BlobContentRepository;
import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.entity.BlobContent;
import org.oagi.srt.repository.entity.Release;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Component
public class PopulateBlobContents {

    @Autowired
    private RepositoryFactory repositoryFactory;
    private BlobContentRepository blobContentRepository;
    private ReleaseRepository releaseRepository;
    private File baseDataDirectory;

    @PostConstruct
    public void init() {
        blobContentRepository = repositoryFactory.blobContentRepository();
        releaseRepository = repositoryFactory.releaseRepository();

        baseDataDirectory = new File(SRTConstants.BASE_DATA_PATH).getAbsoluteFile();
        if (!baseDataDirectory.exists()) {
            throw new IllegalStateException("Couldn't find data directory. Please check your environments.");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws IOException {
        populate("Model/Platform/2_1/Common/DataTypes");
        populate("Model/Platform/2_1/Common/ISO20022");
        populate("Model/Platform/2_1/OAGi-Platform.xsd");
    }

    private void populate(String path) throws IOException {
        Release release = releaseRepository.findOneByReleaseNum("10.1");
        doPopulate(new File(baseDataDirectory, path).getAbsoluteFile(), release);
    }

    private void doPopulate(File file, Release release) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                doPopulate(child, release);
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
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            PopulateBlobContents populateBlobContents = ctx.getBean(PopulateBlobContents.class);
            populateBlobContents.run(ctx);
        }
    }
}
