package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.AgencyIdListRepository;
import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.AgencyIdList;
import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.oagi.srt.repository.entity.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Jaehun Lee
 * @version 1.0
 */
@Component
public class P_1_3_PopulateAgencyIDList {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.3 Start");
        Collection<AgencyIdList> agencyIdLists = agencyIDList();
        agencyIDListValue(agencyIdLists);
        updateAgencyIDList();
        logger.info("### 1.3 End");
    }

    private Collection<AgencyIdList> agencyIDList() throws Exception {
        String moduleName = "IdentifierScheme_AgencyIdentification_3055_D08B";
        Module module = moduleRepository.findByModuleContaining(moduleName);
        if (module == null) {
            throw new IllegalStateException("Can't find " + moduleName + " module. We need to import `module` first perfectly.");
        }

        String path1 = SRTConstants.filepath("AgencyID") + moduleName + ".xsd";
        XPathHandler xh = new XPathHandler(path1);

        AgencyIdList agencyIdList = new AgencyIdList();

        NodeList result = xh.getNodeList("//xsd:simpleType");
        for (int i = 0; i < result.getLength(); i++) {
            Element tmp = (Element) result.item(i);

            if (tmp.getAttribute("name").endsWith("IdentificationContentType")) {
                agencyIdList.setGuid(tmp.getAttribute("id"));
            }
            if (tmp.getAttribute("name").endsWith("EnumerationType")) {
                agencyIdList.setEnumTypeGuid(tmp.getAttribute("id"));
            }
        }

        agencyIdList.setName("clm63055D08B_AgencyIdentification");
        agencyIdList.setListId("3055");
        agencyIdList.setVersionId("D08B");
        agencyIdList.setModule(module);
        agencyIdList.setDefinition("Schema agency:  UN/CEFACT\n" +
                "Schema version: 4.5\n" +
                "Schema date:    02 February 2014\n" +
                "\n" +
                "Code list name:     Agency Identification Code\n" +
                "Code list agency:   UNECE\n" +
                "Code list version:  D13A");

        agencyIdListRepository.save(agencyIdList);

        return Arrays.asList(agencyIdList);
    }

    private void agencyIDListValue(Collection<AgencyIdList> agencyIdLists) throws Exception {
        String path1 = SRTConstants.filepath("AgencyID") + "IdentifierScheme_AgencyIdentification_3055_D08B.xsd";
        XPathHandler xh = new XPathHandler(path1);

        XPathExpression cctsNameExp = xh.compile(".//*[local-name()=\"ccts_Name\"]");
        XPathExpression cctsDefinitionExp = xh.compile(".//*[local-name()=\"ccts_Definition\"]");

        List<AgencyIdListValue> agencyIdListValues = new ArrayList();
        for (AgencyIdList agencyIdList : agencyIdLists) {
            NodeList enumeration = xh.getNodeList("//xsd:simpleType[@id = '" + agencyIdList.getEnumTypeGuid() + "']//xsd:enumeration");

            for (int i = 0; i < enumeration.getLength(); i++) {
                Element enumElement = (Element) enumeration.item(i);

                AgencyIdListValue agencyIdListValue = new AgencyIdListValue();
                agencyIdListValue.setValue(enumElement.getAttribute("value"));

                Node name = xh.getNode(cctsNameExp, enumElement);
                Node definition = xh.getNode(cctsDefinitionExp, enumElement);

                agencyIdListValue.setName(name.getTextContent());
                agencyIdListValue.setDefinition(definition.getTextContent());
                agencyIdListValue.setOwnerListId(agencyIdList.getAgencyIdListId());

                agencyIdListValues.add(agencyIdListValue);
            }
        }

        agencyIdListValueRepository.save(agencyIdListValues);
    }

    private void updateAgencyIDList() throws Exception {
        AgencyIdListValue agencyIdListValue = agencyIdListValueRepository.findOneByValue("6");
        agencyIdListRepository.findAll().forEach(e -> {
            e.setAgencyId(agencyIdListValue.getAgencyIdListValueId());
            agencyIdListRepository.save(e);
        });
    }

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_3_PopulateAgencyIDList populateAgencyIDList = ctx.getBean(P_1_3_PopulateAgencyIDList.class);
            populateAgencyIDList.run(ctx);
        }
    }
}
