package org.oagi.srt.persistence.populate;

import com.sun.xml.internal.xsom.XSElementDecl;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.persistence.populate.helper.Context;
import org.oagi.srt.persistence.populate.helper.ElementDecl;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.AggregateCoreComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

import static org.oagi.srt.common.SRTConstants.MODEL_FOLDER_PATH;

@Component
public class P_1_8_2_PopulateOAGISType {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private P_1_8_1_PopulateAccAsccpBccAscc populateAccAsccpBccAscc;

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_8_2_PopulateOAGISType populateOAGISType =
                    ctx.getBean(P_1_8_2_PopulateOAGISType.class);
            populateOAGISType.run(ctx);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.8.2 Start");

        populate(new File(MODEL_FOLDER_PATH + "/OAGIS.xsd"));
        populate(new File(MODEL_FOLDER_PATH + "/OAGIS-Nouns.xsd"));

        logger.info("### 1.8.2 End");
    }

    private void populate(File file) throws Exception {
        if (file == null || !file.exists()) {
            return;
        }

        Context context = new Context(file, moduleRepository);
        Document document = context.loadDocument(file);
        NodeList elements = context.evaluateNodeList("//xsd:element", document);
        for (int i = 0, len = elements.getLength(); i < len; ++i) {
            Element element = (Element) elements.item(i);
            String name = element.getAttribute("name");
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            XSElementDecl xsElementDecl = context.getXSElementDecl(SRTConstants.OAGI_NS, name);
            ElementDecl elementDecl = new ElementDecl(context, xsElementDecl, element);

            int oagisComponentType = 5;
            AggregateCoreComponent acc = populateAccAsccpBccAscc.doCreateACC(elementDecl.getTypeDecl(), oagisComponentType);
            populateAccAsccpBccAscc.createASCCP(elementDecl, acc, true);
        }
    }

}
