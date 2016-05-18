package org.oagi.srt.persistence.populate;

import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.AgencyIdListRepository;
import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.entity.AgencyIdList;
import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.3 Start");
        Collection<AgencyIdList> agencyIdLists = agencyIDList();
        Collection<AgencyIdListValue> agencyIdListValues = agencyIDListValue(agencyIdLists);
        updateAgencyIDList(agencyIdLists, agencyIdListValues);
        System.out.println("### 1.3 End");
    }

    private Collection<AgencyIdList> agencyIDList() throws Exception {
        String path1 = SRTConstants.filepath("AgencyID") + "IdentifierScheme_AgencyIdentification_3055_D08B_merged.xsd";
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

        agencyIdList.setName("Agency Identification");
        agencyIdList.setListId("3055");
        agencyIdList.setVersionId("D13A");
        agencyIdList.setDefinition("Schema agency:  UN/CEFACT	Schema version: 4.5	Schema date:    02 February 2014	Code list name:     Agency Identification Code	Code list agency:   UNECE	Code list version:  D13A");

        AgencyIdListRepository agencyIdListRepository = repositoryFactory.agencyIdListRepository();
        agencyIdListRepository.save(agencyIdList);

        return Arrays.asList(agencyIdList);
    }

    private Collection<AgencyIdListValue> agencyIDListValue(Collection<AgencyIdList> agencyIdLists) throws Exception {
        String path1 = SRTConstants.filepath("AgencyID") + "IdentifierScheme_AgencyIdentification_3055_D08B_merged.xsd";
        XPathHandler xh = new XPathHandler(path1);

        AgencyIdListValueRepository agencyIdListValueRepository = repositoryFactory.agencyIdListValueRepository();
        List<AgencyIdListValue> agencyIdListValues = new ArrayList();

        for (AgencyIdList agencyIdList : agencyIdLists) {
            NodeList enumeration = xh.getNodeList("//xsd:simpleType[@id = '" + agencyIdList.getEnumTypeGuid() + "']//xsd:enumeration");

            for (int i = 0; i < enumeration.getLength(); i++) {
                Element enum_element = (Element) enumeration.item(i);

                AgencyIdListValue agencyIdListValue = new AgencyIdListValue();
                agencyIdListValue.setValue(enum_element.getAttribute("value"));

                Node name = xh.getNode("//xsd:simpleType[@id = '" + agencyIdList.getEnumTypeGuid() + "']//xsd:enumeration[@value = '" + agencyIdListValue.getValue() + "']//*[local-name()=\"ccts_Name\"]");
                Node definition = xh.getNode("//xsd:simpleType[@id = '" + agencyIdList.getEnumTypeGuid() + "']//xsd:enumeration[@value = '" + agencyIdListValue.getValue() + "']//*[local-name()=\"ccts_Definition\"]");

                agencyIdListValue.setName(name.getTextContent());
                agencyIdListValue.setDefinition(definition.getTextContent());
                agencyIdListValue.setOwnerListId(agencyIdList.getAgencyIdListId());
                //System.out.println("@@@  "+agencyIdListValue.getValue()+"  th turn, name = "+agencyIdListValue.getName() +",  definition = "+ agencyIdListValue.getDefinition());
                agencyIdListValueRepository.save(agencyIdListValue);
                agencyIdListValues.add(agencyIdListValue);
            }
        }

        return agencyIdListValues;
    }

    private void updateAgencyIDList(Collection<AgencyIdList> agencyIdLists,
                                   Collection<AgencyIdListValue> agencyIdListValues) throws Exception {
        AgencyIdListRepository agencyIdListRepository = repositoryFactory.agencyIdListRepository();
        agencyIdListValues.stream().filter(agencyIdListValue -> "6".equals(agencyIdListValue.getValue()))
                .forEach(agencyIdListValue -> {
                    agencyIdListRepository.updateAgencyId(agencyIdListValue.getAgencyIdListValueId());
                });
    }

    public void validate() throws Exception {
        System.out.println("### 1.3 Start Validation");
        validateImportAgencyIDList();
        validateImportAgencyIDListValue();
        System.out.println("### 1.3 Validation End");
    }

    private void validateImportAgencyIDList() throws Exception {
        System.out.println("@@ Validating agency_id_list..");

        String path1 = SRTConstants.filepath("AgencyID") + "IdentifierScheme_AgencyIdentification_3055_D08B_merged.xsd";
        XPathHandler xh = new XPathHandler(path1);

        AgencyIdListRepository agencyIdListRepository = repositoryFactory.agencyIdListRepository();
        AgencyIdListValueRepository agencyIdListValueRepository = repositoryFactory.agencyIdListValueRepository();

        AgencyIdList agencyIdList;
        AgencyIdListValue agencyIdListValue;

        String fromXSD = "";
        String fromDB = "";

        fromXSD = fromXSD + "oagis-id-f1df540ef0db48318f3a423b3057955f";//guid
        fromXSD = fromXSD + "oagis-id-68a3c03a4ea84562bd783fe2dc8f5487";//EnumTypeGuid
        fromXSD = fromXSD + "Agency Identification";//Name
        fromXSD = fromXSD + "3055";//List_ID

        fromXSD = fromXSD + "6";//Agency_ID but we check the login_id of app_user instead
        fromXSD = fromXSD + "D13A";//versionID
        fromXSD = fromXSD + "Schema agency:  UN/CEFACT\n" +
                "Schema version: 4.5\n" +
                "Schema date:    02 February 2014\n" +
                "\n" +
                "Code list name:     Agency Identification Code\n" +
                "Code list agency:   UNECE\n" +
                "Code list version:  D13A";//Definition

        agencyIdList = agencyIdListRepository.findOneByGuid("oagis-id-f1df540ef0db48318f3a423b3057955f");

        fromDB = fromDB + agencyIdList.getGuid();
        fromDB = fromDB + agencyIdList.getEnumTypeGuid();
        fromDB = fromDB + agencyIdList.getName();
        fromDB = fromDB + agencyIdList.getListId();

        agencyIdListValue = agencyIdListValueRepository.findOneByAgencyIdListValueId(agencyIdList.getAgencyId());

        fromDB = fromDB + agencyIdListValue.getValue();
        fromDB = fromDB + agencyIdList.getVersionId();
        fromDB = fromDB + agencyIdList.getDefinition();

        if (!fromXSD.equals(fromDB)) {
            System.out.println("@@@@ AgencyIDList is not imported properly!");
            System.out.println("     FromXSD: " + fromXSD);
            System.out.println("      FromDB: " + fromDB);
        }
    }

    private void validateImportAgencyIDListValue() throws Exception {
        System.out.println("@@ Validationg agency_id_list_value..");

        String path1 = SRTConstants.filepath("AgencyID") + "IdentifierScheme_AgencyIdentification_3055_D08B_merged.xsd";
        XPathHandler xh = new XPathHandler(path1);

        AgencyIdListRepository agencyIdListRepository = repositoryFactory.agencyIdListRepository();
        AgencyIdListValueRepository agencyIdListValueRepository = repositoryFactory.agencyIdListValueRepository();

        ArrayList<String> fromXSDList = new ArrayList<String>();
        ArrayList<String> fromDBList = new ArrayList<String>();

        for (AgencyIdList svo : agencyIdListRepository.findAll()) {
            NodeList enumeration = xh.getNodeList("//xsd:simpleType[@id = '" + svo.getEnumTypeGuid() + "']//xsd:enumeration");

            for (int i = 0; i < enumeration.getLength(); i++) {
                Element enum_element = (Element) enumeration.item(i);
                String fromXSD = "";

                fromXSD = fromXSD + enum_element.getAttribute("value");

                Node name = xh.getNode("//xsd:simpleType[@id = '" + svo.getEnumTypeGuid() + "']//xsd:enumeration[@value = '" + enum_element.getAttribute("value") + "']//*[local-name()=\"ccts_Name\"]");
                Node definition = xh.getNode("//xsd:simpleType[@id = '" + svo.getEnumTypeGuid() + "']//xsd:enumeration[@value = '" + enum_element.getAttribute("value") + "']//*[local-name()=\"ccts_Definition\"]");

                if (name != null) {
                    fromXSD = fromXSD + name.getTextContent();
                } else {
                    fromXSD = fromXSD + "null";
                }
                if (definition != null && !definition.getTextContent().equals("")) {
                    fromXSD = fromXSD + definition.getTextContent();
                } else {
                    fromXSD = fromXSD + "null";
                }
                fromXSDList.add(fromXSD);
            }

            for (AgencyIdListValue alvalVO : agencyIdListValueRepository.findAll()) {
                if (alvalVO.getOwnerListId() == svo.getAgencyIdListId()) {
                    String fromDB = "";

                    fromDB = fromDB + alvalVO.getValue();
                    fromDB = fromDB + alvalVO.getName();
                    fromDB = fromDB + alvalVO.getDefinition();

                    fromDBList.add(fromDB);
                }
            }
        }

        fromXSDList.sort(null);
        fromDBList.sort(null);

        if (fromXSDList.size() != fromDBList.size()) {
            System.out.println("Size of AgencyIDListValues differs!");
        } else {
            for (int i = 0; i < fromDBList.size(); i++) {

                if (!fromDBList.get(i).equals(fromXSDList.get(i))) {
                    System.out.println("@@@@ AgencyIDListValue has different values!");
                    System.out.println("     FromXSD: " + fromXSDList.get(i));
                    System.out.println("      FromDB: " + fromDBList.get(i));
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_3_PopulateAgencyIDList populateAgencyIDList = ctx.getBean(P_1_3_PopulateAgencyIDList.class);
            populateAgencyIDList.run(ctx);
            populateAgencyIDList.validate();
        }
    }
}
