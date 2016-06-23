package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.populate.helper.Context;
import org.oagi.srt.repository.ModuleDepRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.NamespaceRepository;
import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.entity.Module;
import org.oagi.srt.repository.entity.ModuleDep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

@Component
public class PopulateModules {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleDepRepository moduleDepRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    private File baseDataDirectory;
    private int releaseId;
    private int namespaceId;
    private XPath xPath;

    @PostConstruct
    public void init() {
        baseDataDirectory = new File(SRTConstants.BASE_DATA_PATH, "Model").getAbsoluteFile();
        if (!baseDataDirectory.exists()) {
            throw new IllegalStateException("Couldn't find data directory: " + baseDataDirectory +
                    ". Please check your environments.");
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### Module population Start");

        releaseId = releaseRepository.findReleaseIdByReleaseNum("10.1");
        namespaceId = namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10");
        xPath = XPathFactory.newInstance().newXPath();

        populateModule(baseDataDirectory);
        populateModuleDep(baseDataDirectory);

        logger.info("### Module population End");
    }

    private void populateModule(File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                populateModule(child);
            }
        } else if (file.getName().endsWith(".xsd")) {
            String moduleName = Utility.extractModuleName(file.getAbsolutePath());
            if (!moduleRepository.existsByModule(moduleName)) {
                Module module = new Module();
                module.setModule(moduleName);
                module.setReleaseId(releaseId);
                module.setNamespaceId(namespaceId);

                moduleRepository.save(module);
            }
        }
    }

    private void populateModuleDep(File file) throws Exception {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                populateModuleDep(child);
            }
        } else if (file.getName().endsWith(".xsd")) {
            Module module = findModule(file);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            NodeList includeNodeList = (NodeList) Context.xPath.evaluate("//xsd:include", document, XPathConstants.NODESET);
            for (int i = 0, len = includeNodeList.getLength(); i < len; ++i) {
                Element includeElement = (Element) includeNodeList.item(i);
                Module includeModule = findModule(file, includeElement);

                ModuleDep moduleDep = new ModuleDep();
                moduleDep.setDependencyType(ModuleDep.DependencyType.INCLUDE);
                moduleDep.setDependingModule(includeModule);
                moduleDep.setDependedModule(module);

                moduleDepRepository.save(moduleDep);
            }

            NodeList importNodeList = (NodeList) Context.xPath.evaluate("//xsd:import", document, XPathConstants.NODESET);
            for (int i = 0, len = importNodeList.getLength(); i < len; ++i) {
                Element importElement = (Element) importNodeList.item(i);
                Module importModule = findModule(file, importElement);

                ModuleDep moduleDep = new ModuleDep();
                moduleDep.setDependencyType(ModuleDep.DependencyType.IMPORT);
                moduleDep.setDependingModule(importModule);
                moduleDep.setDependedModule(module);
                moduleDepRepository.save(moduleDep);
            }
        }
    }

    private Module findModule(File file) throws IOException {
        String path = file.getCanonicalPath();
        String moduleName = Utility.extractModuleName(path);
        return moduleRepository.findByModule(moduleName);
    }

    private Module findModule(File file, Element element) throws IOException {
        String schemaLocation = element.getAttribute("schemaLocation");
        File schemaLocationFile = new File(file.getParent(), schemaLocation);
        return findModule(schemaLocationFile);
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            PopulateModules populateModules = ctx.getBean(PopulateModules.class);
            populateModules.run(ctx);
        }
    }

}
