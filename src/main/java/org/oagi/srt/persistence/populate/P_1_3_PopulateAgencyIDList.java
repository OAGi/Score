package org.oagi.srt.persistence.populate;

import org.oagi.srt.Application;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.AgencyIDListValueVO;
import org.oagi.srt.repository.AgencyIdListRepository;
import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.entity.AgencyIdList;
import org.oagi.srt.repository.entity.AgencyIdListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
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

    public Collection<AgencyIdList> agencyIDList() throws Exception {
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

    public void validateImportAgencyIDList() throws Exception {
        System.out.println("@@ Validating agency_id_list..");

        String path1 = SRTConstants.filepath("AgencyID") + "IdentifierScheme_AgencyIdentification_3055_D08B_merged.xsd";
        XPathHandler xh = new XPathHandler(path1);
        DAOFactory df = DAOFactory.getDAOFactory();
        SRTDAO dao = df.getDAO("AgencyIDList");

        SRTDAO dao2 = df.getDAO("AgencyIDListValue");

        AgencyIDListVO agencyidlistVO = new AgencyIDListVO();
        AgencyIDListValueVO agencyidlistvalueVO = new AgencyIDListValueVO();

        String fromXSD = "";
        String fromDB = "";


        fromXSD = fromXSD + "oagis-id-f1df540ef0db48318f3a423b3057955f";//guid
        fromXSD = fromXSD + "oagis-id-68a3c03a4ea84562bd783fe2dc8f5487";//EnumTypeGuid
        fromXSD = fromXSD + "Agency Identification";//Name
        fromXSD = fromXSD + "3055";//List_ID


        fromXSD = fromXSD + "6";//Agency_ID but we check the login_id of app_user instead
        fromXSD = fromXSD + "D13A";//versionID
        fromXSD = fromXSD + "Schema agency:  UN/CEFACT	Schema version: 4.5	Schema date:    02 February 2014	Code list name:     Agency Identification Code	Code list agency:   UNECE	Code list version:  D13A";//Definition


        QueryCondition qc4AgencyIDList = new QueryCondition();
        qc4AgencyIDList.add("guid", "oagis-id-f1df540ef0db48318f3a423b3057955f");
        agencyidlistVO = (AgencyIDListVO) dao.findObject(qc4AgencyIDList);


        fromDB = fromDB + agencyidlistVO.getAgencyIDListGUID();

        fromDB = fromDB + agencyidlistVO.getEnumerationTypeGUID();
        fromDB = fromDB + agencyidlistVO.getName();
        fromDB = fromDB + agencyidlistVO.getListID();

        String agency_id;

        QueryCondition qc4AgencyIDValue = new QueryCondition();
        qc4AgencyIDValue.add("agency_id_list_value_id", agencyidlistVO.getAgencyID());
        agencyidlistvalueVO = (AgencyIDListValueVO) dao2.findObject(qc4AgencyIDValue);

        agency_id = agencyidlistvalueVO.getValue();

        fromDB = fromDB + agency_id;
        fromDB = fromDB + agencyidlistVO.getVersionID();
        fromDB = fromDB + agencyidlistVO.getDefinition();


        if (!fromXSD.equals(fromDB)) {
            System.out.println("@@@@ AgencyIDList is not imported properly!");
            System.out.println("     FromXSD: " + fromXSD);
            System.out.println("      FromDB: " + fromDB);
        }
    }

    public void updateAgencyIDList(Collection<AgencyIdList> agencyIdLists,
                                   Collection<AgencyIdListValue> agencyIdListValues) throws Exception {
        AgencyIdListRepository agencyIdListRepository = repositoryFactory.agencyIdListRepository();
        agencyIdListValues.stream().filter(agencyIdListValue -> "6".equals(agencyIdListValue.getValue()))
                .forEach(agencyIdListValue -> {
                    agencyIdListRepository.updateAgencyId(agencyIdListValue.getAgencyIdListValueId());
                });

    }

    public Collection<AgencyIdListValue> agencyIDListValue(Collection<AgencyIdList> agencyIdLists) throws Exception {
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
                //System.out.println("@@@  "+agencyidlistvalueVO.getValue()+"  th turn, name = "+agencyidlistvalueVO.getName() +",  definition = "+ agencyidlistvalueVO.getDefinition());
                agencyIdListValueRepository.save(agencyIdListValue);
                agencyIdListValues.add(agencyIdListValue);
            }
        }

        return agencyIdListValues;
    }

    public void validateImportAgencyIDListValue() throws Exception {
        System.out.println("@@ Validationg agency_id_list_value..");

        String path1 = SRTConstants.filepath("AgencyID") + "IdentifierScheme_AgencyIdentification_3055_D08B_merged.xsd";
        XPathHandler xh = new XPathHandler(path1);

        DAOFactory df = DAOFactory.getDAOFactory();
        SRTDAO dao = df.getDAO("AgencyIDListValue");
        AgencyIDListValueVO agencyidlistvalueVO = new AgencyIDListValueVO();

        DAOFactory df2 = DAOFactory.getDAOFactory();
        SRTDAO dao2 = df2.getDAO("AgencyIDList");
        ArrayList<SRTObject> agency_id_fk = dao2.findObjects();

        ArrayList<String> fromXSDList = new ArrayList<String>();
        ArrayList<String> fromDBList = new ArrayList<String>();

        for (SRTObject dvo : agency_id_fk) {
            AgencyIDListVO svo = (AgencyIDListVO) dvo;

            NodeList enumeration = xh.getNodeList("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration");

            for (int i = 0; i < enumeration.getLength(); i++) {
                Element enum_element = (Element) enumeration.item(i);
                String fromXSD = "";

                fromXSD = fromXSD + enum_element.getAttribute("value");

                Node name = xh.getNode("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration[@value = '" + enum_element.getAttribute("value") + "']//*[local-name()=\"ccts_Name\"]");
                Node definition = xh.getNode("//xsd:simpleType[@id = '" + svo.getEnumerationTypeGUID() + "']//xsd:enumeration[@value = '" + enum_element.getAttribute("value") + "']//*[local-name()=\"ccts_Definition\"]");

                if (name != null) {
                    fromXSD = fromXSD + ((Element) name).getTextContent();
                } else {
                    fromXSD = fromXSD + "null";
                }
                if (definition != null && !definition.getTextContent().equals("")) {
                    fromXSD = fromXSD + ((Element) definition).getTextContent();
                } else {
                    fromXSD = fromXSD + "null";
                }
                fromXSDList.add(fromXSD);
            }

            ArrayList<SRTObject> agencyidlistvaluelist = dao.findObjects();

            for (int i = 0; i < agencyidlistvaluelist.size(); i++) {

                AgencyIDListValueVO alvalVO = (AgencyIDListValueVO) agencyidlistvaluelist.get(i);

                if (alvalVO.getOwnerAgencyIDListID() == svo.getAgencyIDListID()) {
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

    @Transactional(rollbackFor = Throwable.class)
    public void run() throws Exception {
        System.out.println("### 1.3 Start");
        Collection<AgencyIdList> agencyIdLists = agencyIDList();
        Collection<AgencyIdListValue> agencyIdListValues = agencyIDListValue(agencyIdLists);
        updateAgencyIDList(agencyIdLists, agencyIdListValues);
        System.out.println("### 1.3 End");
    }

    public void validate_P_1_3_PopulateAgencyIDList() throws Exception {
        System.out.println("### 1.3 Start Validation");
        validateImportAgencyIDList();
        validateImportAgencyIDListValue();
        System.out.println("### 1.3 Validation End");
    }

    public static void main(String args[]) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_3_PopulateAgencyIDList agencyidlist = ctx.getBean(P_1_3_PopulateAgencyIDList.class);
            agencyidlist.run();
        }
    }
}
