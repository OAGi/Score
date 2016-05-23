package org.oagi.srt.persistence.populate;

import org.apache.commons.io.FilenameUtils;
import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jaehun Lee
 * @version 1.0
 */
@Component
public class P_1_4_PopulateCodeList {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.4 Start");

        String tt[][] = {
                {"CodeLists_1", "314"},
                {"CodeList_ConditionTypeCode_1", "314"},
                {"CodeList_ConstraintTypeCode_1", "314"},
                {"CodeList_DateFormatCode_1", "314"},
                {"CodeList_DateTimeFormatCode_1", "314"},
                {"CodeList_TimeFormatCode_1", "314"},

                {"CodeList_CharacterSetCode_IANA_20070514", "379"},
                {"CodeList_MIMEMediaTypeCode_IANA_7_04", "379"},

                {"CodeList_CurrencyCode_ISO_7_04", "5"},
                {"CodeList_LanguageCode_ISO_7_04", "5"},
                {"CodeList_TimeZoneCode_1", "5"},

                {"CodeList_UnitCode_UNECE_7_04", "6"}
        };

        for (int i = 0; i < tt.length; i++) {
            String fileName = tt[i][0];
            int agencyId = Integer.parseInt(tt[i][1]);

            System.out.println("## Importing Code List from " + fileName + "..");
            String filename = fileName + ".xsd";
            codeList(filename, agencyId);
        }

        for (int i = 0; i < tt.length; i++) {
            String fileName = tt[i][0];
            int agencyId = Integer.parseInt(tt[i][1]);

            System.out.println("## Updating Code List referring " + fileName + "..");

            String filename = fileName + ".xsd";
            updateBasedCodeListID(filename, agencyId);
        }

        for (int i = 0; i < tt.length; i++) {
            String fileName = tt[i][0];
            int agencyId = Integer.parseInt(tt[i][1]);

            System.out.println("## Impoting Code List Value from " + fileName + "..");

            String filename = fileName + ".xsd";
            codeListValue(filename);
        }

        System.out.println("### 1.4 End");
    }

    private void codeList(String fileinput, int agencyId) throws Exception {
        String path1 = SRTConstants.filepath("CodeList") + fileinput;
        XPathHandler xh = new XPathHandler(path1);

        CodeListRepository codeListRepository = repositoryFactory.codeListRepository();
        CodeList codeList = new CodeList();

        NodeList result = xh.getNodeList("//xsd:simpleType");

        int userId = getUserID("oagis");

        for (int i = 0; i < result.getLength(); i++) {
            Element element = (Element) result.item(i);
            String name = element.getAttribute("name");
            if (name.endsWith("CodeContentType")) {
                codeList.setGuid(element.getAttribute("id"));

                //added by TKim according to design Doc v2.4
                if (name.startsWith("oacl")) {
                    String xyz = name.substring(0, name.lastIndexOf("CodeContentType"));
                    //System.out.print("  "+name + " with ");
                    Node enumTypeNode = xh.getNode("//xsd:simpleType[@name='" + xyz + "CodeEnumerationType']/@id");
                    String enumerationTypeGUID = null;
                    if (enumTypeNode != null) {
                        enumerationTypeGUID = enumTypeNode.getTextContent();
                    }
                    codeList.setEnumTypeGuid(enumerationTypeGUID);
                } else {
                    codeList.setEnumTypeGuid(null);
                }

                codeList.setName(name.substring(0, name.lastIndexOf("ContentType")));
                codeList.setListId(element.getAttribute("id"));
                codeList.setAgencyId(getAgencyID(agencyId));

                if (codeList.getName().startsWith("oacl")) {
                    codeList.setVersionId("1");
                } else if (codeList.getName().equals("clm6Recommendation205_MeasurementUnitCommonCode")) {
                    codeList.setVersionId("5");
                } else {
                    codeList.setVersionId(extractVersionId(name));
                }

                Node definition_node = xh.getNode("//xsd:simpleType[@name = '" + name + "']/xsd:annotation/xsd:documentation");
                if (definition_node != null) {
                    Element definition_element2 = (Element) definition_node;
                    codeList.setDefinition(definition_element2.getTextContent());
                    codeList.setDefinitionSource(definition_element2.getAttribute("source"));
                } else {
                    codeList.setDefinition(null);
                    codeList.setDefinitionSource(null);
                }
                codeList.setExtensibleIndicator(true);  //logic changed. extensible indicator is always TRUE.

                codeList.setCreatedBy(userId);
                codeList.setLastUpdatedBy(userId);
                codeList.setState(SRTConstants.CODE_LIST_STATE_PUBLISHED);
                codeList.setModule(extractModuleName(path1));

                codeListRepository.save(codeList);
            } else if (!name.endsWith("EnumerationType"))
                System.out.println("Check !!  " + name);
        }
    }

    private String extractVersionId(String name) {
        for (int j = 0; j < name.length(); j++) {
            if (name.charAt(j) > 47 && name.charAt(j) < 58) {
                for (int k = j + 1; k < name.length(); k++) {
                    if (name.charAt(k) == '_') {
                        String versionId = name.substring(j, k);
                        for (int l = versionId.length() - 1; l >= 0; l--) {
                            if (!(versionId.charAt(l) > 47 && versionId.charAt(l) < 58)) {
                                versionId = versionId.substring(l + 1);
                                break;
                            }
                        }
                        return versionId;
                    }
                }
                break;
            }
        }
        return null;
    }

    private String extractModuleName(String filePath) {
        int idx = filePath.indexOf("Model");
        return FilenameUtils.separatorsToWindows(filePath.substring(idx));
    }

    private int getAgencyID(int value) {
        AgencyIdListValueRepository agencyIdListValueRepository = repositoryFactory.agencyIdListValueRepository();
        return agencyIdListValueRepository.findOneByValue(String.valueOf(value)).getAgencyIdListValueId();
    }

    private int getUserID(String userName) {
        UserRepository userRepository = repositoryFactory.userRepository();
        return userRepository.findOneByLoginId(userName).getAppUserId();
    }

    private void updateBasedCodeListID(String fileinput, int agencyId) throws Exception {
        String path1 = SRTConstants.filepath("CodeList") + fileinput;
        XPathHandler xh = new XPathHandler(path1);

        CodeListRepository codeListRepository = repositoryFactory.codeListRepository();

        NodeList result = xh.getNodeList("//xsd:simpleType");

        for (int i = 0; i < result.getLength(); i++) {
            Element tmp = (Element) result.item(i);
            if (tmp.getAttribute("name").endsWith("CodeContentType")) {
                //added by T.Kim according to design doc v2.4
                //based_code_list_id
                String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
                Node unionNode = xh.getNode("//xsd:simpleType[@name='" + xyz + "CodeContentType']//xsd:union/@memberTypes");

                if (unionNode != null) {
                    String unionMemberStr = null;
                    unionMemberStr = unionNode.getTextContent();
                    unionMemberStr = unionMemberStr.replace("xsd:token", "");
                    unionMemberStr = unionMemberStr.trim();
                    String checkEnumType = xyz + "CodeEnumerationType";

                    if (!unionMemberStr.equals(checkEnumType)) { //find base code list!
                        unionMemberStr = unionMemberStr.replace("ContentType", "");

                        CodeList baseCodelistVO = codeListRepository.findByNameContaining(unionMemberStr).get(0);

                        String guid = tmp.getAttribute("id");
                        CodeList codelistVO = codeListRepository.findOneByGuid(guid);

                        if (baseCodelistVO != null && codelistVO != null) {
                            codelistVO.setBasedCodeListId(baseCodelistVO.getCodeListId());
                            codeListRepository.update(codelistVO);
                            System.out.println(" Update Based Code List ID: " + tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType")) + " is based on " + baseCodelistVO.getName());
                        } else {
                            System.out.println(" Update Based Code List ID Is Failed! Check CodeListID: " + tmp.getAttribute("name"));
                            return;
                        }
                    }
                }
            } else if (!tmp.getAttribute("name").endsWith("EnumerationType"))
                System.out.println("Check !!  " + tmp.getAttribute("name"));
        }

    }

    private void codeListValue(String fileinput) throws Exception {
        String path1 = SRTConstants.filepath("CodeList") + fileinput;
        XPathHandler xh = new XPathHandler(path1);

        CodeListRepository codeListRepository = repositoryFactory.codeListRepository();
        CodeListValueRepository codeListValueRepository = repositoryFactory.codeListValueRepository();

        NodeList result = xh.getNodeList("//xsd:simpleType");
        NodeList enumeration = null;

        for (int i = 0; i < result.getLength(); i++) {
            Element element = (Element) result.item(i);

            enumeration = null;

            if (element.getAttribute("name").endsWith("CodeContentType")) {
                String guid = new String(element.getAttribute("id"));
                CodeList codeList = codeListRepository.findOneByGuid(guid);

                if (codeList.getEnumTypeGuid() != null) {
                    //Get code_list_value from enumerationType
                    enumeration = xh.getNodeList("//xsd:simpleType[@id='" + codeList.getEnumTypeGuid() + "']//xsd:enumeration");
                    if (enumeration.getLength() < 1) {
                        System.out.println("   EnumerationType " + codeList.getEnumTypeGuid() + " has no enumerations");
                    } else {
                        System.out.println("   Start import from EnumerationType: " + codeList.getEnumTypeGuid());
                    }
                } else {//if enum_type_guid is null
                    if (codeList.getBasedCodeListId() > 0) {
                        //Inherit from based code list
                        List<CodeListValue> codeListValuesFromBase = codeListValueRepository.findByCodeListId(codeList.getBasedCodeListId());

                        if (codeListValuesFromBase.size() > 0) {
                            System.out.println("   Start inherit from BasedCodeList: " + codeList.getName());
                            for (int j = 0; j < codeListValuesFromBase.size(); j++) {
                                CodeListValue codelistvalVO = codeListValuesFromBase.get(j);
                                codelistvalVO.setCodeListId(codeList.getCodeListId());
                                codeListValueRepository.save(codelistvalVO);
                            }
                        }
                    } else {//if based code list is null
                        //Get the values from local xsd:enumeration
                        enumeration = xh.getNodeList("//xsd:simpleType[@name='" + element.getAttribute("name") + "']//xsd:enumeration");
                        if (enumeration.getLength() < 1) {
                            System.out.println("   " + codeList.getEnumTypeGuid() + " has no enumerations");
                        } else {
                            System.out.println("   Start import from LocalEnumeration: ");
                        }
                    }
                }

                if (enumeration != null) {
                    for (int j = 0; j < enumeration.getLength(); j++) {
                        Element aEnum = (Element) enumeration.item(j);
                        CodeListValue codelistvalueVO = new CodeListValue();
                        codelistvalueVO.setCodeListId(codeList.getCodeListId());
                        codelistvalueVO.setValue(aEnum.getAttribute("value"));
                        codelistvalueVO.setName(aEnum.getAttribute("value"));
                        codelistvalueVO.setUsedIndicator(true);
                        codelistvalueVO.setLockedIndicator(false);
                        codelistvalueVO.setExtensionIndicator(false);

                        Node definitionNode = xh.getNode("//xsd:simpleType[@name='" + element.getAttribute("name") + "']//xsd:enumeration[@value='" + codelistvalueVO.getValue() + "']//xsd:documentation");
                        if (definitionNode == null) {
                            definitionNode = xh.getNode("//xsd:simpleType[@id='" + codeList.getEnumTypeGuid() + "']//xsd:enumeration[@value='" + codelistvalueVO.getValue() + "']//xsd:documentation");
                        }
                        if (definitionNode != null) {
                            Element definition = (Element) definitionNode;
                            codelistvalueVO.setDefinition(definition.getTextContent().trim());
                            codelistvalueVO.setDefinitionSource(definition.getAttribute("source"));
                        } else {
                            codelistvalueVO.setDefinition(null);
                            codelistvalueVO.setDefinitionSource(null);
                        }
                        codeListValueRepository.save(codelistvalueVO);
                    }
                }
            }
        }
    }

    public void validate() throws Exception {
        System.out.println("### 1.4 Start Validation");

        String tt[][] = {{"CodeList_ConditionTypeCode_1", "314"}, {"CodeList_ConstraintTypeCode_1", "314"},
                {"CodeList_DateFormatCode_1", "314"}, {"CodeList_DateTimeFormatCode_1", "314"}, {"CodeList_TimeFormatCode_1", "314"},
                {"CodeList_CharacterSetCode_IANA_20070514", "379"}, {"CodeList_MIMEMediaTypeCode_IANA_7_04", "379"},
                {"CodeList_CurrencyCode_ISO_7_04", "5"}, {"CodeList_LanguageCode_ISO_7_04", "5"}, {"CodeList_TimeZoneCode_1", "5"},
                {"CodeList_UnitCode_UNECE_7_04", "6"}, {"CodeLists_1", "314"}};

        for (int i = 0; i < tt.length; i++) {
            System.out.println("@@ Validating Import Code List from " + tt[i][0] + "..");
            String filename = tt[i][0] + ".xsd";
            validateImportCodeList(filename, Integer.parseInt(tt[i][1]));
        }

        for (int i = 0; i < tt.length; i++) {
            System.out.println("@@ Validating Import Code List Value from " + tt[i][0] + "..");
            String filename = tt[i][0] + ".xsd";
            validateImportCodeListValue(filename, tt);
        }

        System.out.println("### 1.4 Validation End");
    }

    private void validateImportCodeList(String fileinput, int agencyId) throws Exception {
        String path1 = SRTConstants.filepath("CodeList") + fileinput;
        XPathHandler xh = new XPathHandler(path1);

        CodeListRepository codeListRepository = repositoryFactory.codeListRepository();
        CodeList codeList = new CodeList();

        CodeList codelistVO = new CodeList();
        CodeList codelistFromDBVO = new CodeList();

        NodeList result = xh.getNodeList("//xsd:simpleType");
        NodeList union = xh.getNodeList("//xsd:simpleType//xsd:union");

        Timestamp current_stamp = new Timestamp(System.currentTimeMillis());

        for (int i = 0; i < result.getLength(); i++) {
            Element tmp = (Element) result.item(i);

            if (tmp.getAttribute("name").endsWith("CodeContentType")) {

                String fromXSD = "";
                String fromDB = "";

                fromXSD = fromXSD + tmp.getAttribute("id");
                if (tmp.getAttribute("name").startsWith("oacl")) {
                    String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
                    Node enumTypeNode = xh.getNode("//xsd:simpleType[@name='" + xyz + "CodeEnumerationType']/@id");
                    if (enumTypeNode != null) {
                        fromXSD = fromXSD + enumTypeNode.getTextContent();

                    } else {
                        fromXSD = fromXSD + "null";
                    }
                } else {
                    fromXSD = fromXSD + "null";
                }

                fromXSD = fromXSD + tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("ContentType"));
                fromXSD = fromXSD + tmp.getAttribute("id");
                fromXSD = fromXSD + getAgencyID(agencyId);

                if (tmp.getAttribute("name").startsWith("oacl")) {
                    fromXSD = fromXSD + "1";
                } else if (tmp.getAttribute("name").equals("clm6Recommendation205_MeasurementUnitCommonCode")) {
                    fromXSD = fromXSD + "5";
                } else {
                    for (int j = 0; j < tmp.getAttribute("name").length(); j++) {
                        if (tmp.getAttribute("name").charAt(j) > 47 && tmp.getAttribute("name").charAt(j) < 58) {
                            for (int k = j + 1; k < tmp.getAttribute("name").length(); k++) {
                                if (tmp.getAttribute("name").charAt(k) == '_') {
                                    String complicated_version = tmp.getAttribute("name").substring(j, k);
                                    for (int l = complicated_version.length() - 1; l >= 0; l--) {
                                        if (!(complicated_version.charAt(l) > 47 && complicated_version.charAt(l) < 58)) {
                                            complicated_version = complicated_version.substring(l + 1);
                                            break;
                                        }
                                    }
                                    fromXSD = fromXSD + complicated_version;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }

                Node definition = xh.getNode("//xsd:simpleType[@name='" + tmp.getAttribute("name") + "']/xsd:annotation/xsd:documentation");

                if (definition != null) {
                    Element definition_element = (Element) definition;
                    fromXSD = fromXSD + definition_element.getTextContent();
                    fromXSD = fromXSD + definition_element.getAttribute("source");
                } else {
                    fromXSD = fromXSD + "null" + "null";
                }
                fromXSD = fromXSD + "null"; // remark

                String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
                Node unionNode = xh.getNode("//xsd:simpleType[@name='" + xyz + "CodeContentType']//xsd:union/@memberTypes");

                if (unionNode != null) {
                    String unionMemberStr = null;
                    unionMemberStr = unionNode.getTextContent();
                    unionMemberStr = unionMemberStr.replace("xsd:token", "");
                    unionMemberStr = unionMemberStr.trim();

                    if (!unionMemberStr.equals(xyz + "CodeEnumerationType")) {
                        unionMemberStr = unionMemberStr.replace("CodeContentType", "Code");
                        fromXSD = fromXSD + unionMemberStr;
                    } else {
                        fromXSD = fromXSD + "null";
                    }
                } else {
                    fromXSD = fromXSD + "null";
                }

                fromXSD = fromXSD + "true"; //for extensible indicator True
                int id = getUserID("oagis");
                fromXSD = fromXSD + id + id + "Published";

                String guid = tmp.getAttribute("id");
                codelistFromDBVO = codeListRepository.findOneByGuid(guid);

                if (codelistFromDBVO == null) {
                    System.out.println("@@@@@@@Check: " + tmp.getAttribute("id") + " is not imported!");
                } else {
                    fromDB = fromDB + codelistFromDBVO.getGuid()
                            + codelistFromDBVO.getEnumTypeGuid()
                            + codelistFromDBVO.getName()
                            + codelistFromDBVO.getListId()
                            + codelistFromDBVO.getAgencyId()
                            + codelistFromDBVO.getVersionId()
                            + codelistFromDBVO.getDefinition()
                            + codelistFromDBVO.getDefinitionSource()
                            + codelistFromDBVO.getRemark();

                    int basedCodeListId = codelistFromDBVO.getBasedCodeListId();
                    if (basedCodeListId > 0) {
                        System.out.println("Code List ID: " + codelistFromDBVO.getCodeListId() + ", Based Code List ID: " + basedCodeListId);
                        CodeList baseCodeList = codeListRepository.findOneByCodeListId(basedCodeListId);
                        fromDB = fromDB + baseCodeList.getName();  //base code list's Name is used instead of its ID
                    }

                    fromDB = fromDB + codelistFromDBVO.isExtensibleIndicator()
                            + codelistFromDBVO.getCreatedBy()
                            + codelistFromDBVO.getLastUpdatedBy()
                            + codelistFromDBVO.getState();

                    if (!fromDB.equals(fromXSD)) {
                        System.out.println("@@@@@@@Check: " + tmp.getAttribute("id") + " has different values!");
                        System.out.println("            FromXSD: " + fromXSD);
                        System.out.println("             FromDB: " + fromDB);
                    }
                }

            } else if (!tmp.getAttribute("name").endsWith("EnumerationType"))
                System.out.println("Check !!  " + tmp.getAttribute("name"));
        }
    }

    private void validateImportCodeListValue(String fileinput, String[][] reference) throws Exception {
        String path1 = SRTConstants.filepath("CodeList") + fileinput;
        XPathHandler xh = new XPathHandler(path1);

        CodeListRepository codeListRepository = repositoryFactory.codeListRepository();
        CodeListValueRepository codeListValueRepository = repositoryFactory.codeListValueRepository();
        NodeList result = xh.getNodeList("//xsd:simpleType");

        for (int i = 0; i < result.getLength(); i++) {
            Element tmp = (Element) result.item(i);

            if (tmp.getAttribute("name").endsWith("CodeContentType")) {
                String guid = new String(tmp.getAttribute("id"));
                CodeList codelistVO = codeListRepository.findOneByGuid(guid);

                List<CodeListValue> clvalListVOFromDB = codeListValueRepository.findByCodeListId(codelistVO.getCodeListId());
                List<String> clvalListFromXSD = new ArrayList();
                List<String> clvalListFromDB = new ArrayList();

                if (tmp.getAttribute("name").startsWith("oacl")) {

                    //check its enum type is corresponding to it
                    // corresponding means that //xsd:simpleType[@name = 'xyzCodeEnumerationType'] exists
                    String xyz = tmp.getAttribute("name").substring(0, tmp.getAttribute("name").lastIndexOf("CodeContentType"));
                    Node unionNode = xh.getNode("//xsd:simpleType[@name='" + xyz + "CodeContentType']//xsd:union/@memberTypes");

                    if (unionNode != null) {
                        String unionMemberStr = null;
                        unionMemberStr = unionNode.getTextContent();
                        unionMemberStr = unionMemberStr.replace("xsd:token", "");
                        unionMemberStr = unionMemberStr.trim();
                        String checkEnumType = xyz + "CodeEnumerationType";

                        if (unionMemberStr.equals(checkEnumType)) {
                            //When corresponding Enum Type Exists, codelistvalues are from that enumType

                            NodeList enumerations = xh.getNodeList("//xsd:simpleType[@name='" + checkEnumType + "']//xsd:enumeration");

                            for (int j = 0; j < enumerations.getLength(); j++) {
                                Element itsEnum = (Element) enumerations.item(j);

                                String codeListVal = "";
                                codeListVal = codeListVal + itsEnum.getAttribute("value");
                                Node enumNode = xh.getNode("//xsd:simpleType[@name='" + checkEnumType + "']//xsd:enumeration[@value='" + codeListVal + "']//xsd:documentation");

                                if (enumNode != null) {
                                    Element definition = (Element) enumNode;
                                    codeListVal = codeListVal + definition.getTextContent();
                                    codeListVal = codeListVal + definition.getAttribute("source");
                                } else {
                                    codeListVal = codeListVal + "null" + "null";
                                }

                                codeListVal = codeListVal + "true" + "false" + "false";

                                clvalListFromXSD.add(codeListVal);
                            }

                        } else if (unionMemberStr.endsWith("CodeContentType")) {
                            //When the enumeration exists but not corresponding (except xsd:token), copy the union member type's local enumerations

                            XPathHandler xh4Base = new XPathHandler(path1);
                            //At first, find the Type referring the codeList files
                            NodeList enumerations = null;
                            for (int j = 0; j < reference.length; j++) {

                                String searchFileName = SRTConstants.filepath("CodeList") + reference[j][0] + ".xsd";
                                xh4Base = new XPathHandler(searchFileName);

                                enumerations = xh4Base.getNodeList("//xsd:simpleType[@name='" + unionMemberStr + "']//xsd:enumeration");
                                if (enumerations != null && enumerations.getLength() > 0) {
                                    System.out.println("     " + unionMemberStr + ": BaseType is In File--> " + reference[j][0] + ".xsd");
                                    break;
                                }
                            }

                            if (enumerations != null) {
                                for (int j = 0; j < enumerations.getLength(); j++) {
                                    Element itsEnum = (Element) enumerations.item(j);

                                    String codeListVal = "";
                                    codeListVal = codeListVal + itsEnum.getAttribute("value");
                                    Node enumNode = xh4Base.getNode("//xsd:simpleType[@name='" + unionMemberStr + "']//xsd:enumeration[@value='" + codeListVal + "']//xsd:documentation");

                                    if (enumNode != null) {
                                        Element definition = (Element) enumNode;
                                        codeListVal = codeListVal + definition.getTextContent();
                                        codeListVal = codeListVal + definition.getAttribute("source");
                                    } else {
                                        codeListVal = codeListVal + "null" + "null";
                                    }

                                    codeListVal = codeListVal + "true" + "false" + "false";

                                    clvalListFromXSD.add(codeListVal);
                                }
                            }

                        }
                    }
                } else {//has no union (Not oacl_~~), get them from its local enumeration
                    NodeList enumerations = xh.getNodeList("//xsd:simpleType[@name='" + tmp.getAttribute("name") + "']//xsd:enumeration");

                    for (int j = 0; j < enumerations.getLength(); j++) {
                        Element itsEnum = (Element) enumerations.item(j);

                        String codeListVal = "";
                        codeListVal = codeListVal + itsEnum.getAttribute("value");
                        Node enumNode = xh.getNode("//xsd:simpleType[@name='" + tmp.getAttribute("name") + "']//xsd:enumeration[@value='" + codeListVal + "']//xsd:documentation");

                        if (enumNode != null) {

                            Element definition = (Element) enumNode;

                            codeListVal = codeListVal + definition.getTextContent();

                            codeListVal = codeListVal + definition.getAttribute("source");
                        } else {
                            codeListVal = codeListVal + "null" + "null";
                        }
                        codeListVal = codeListVal + "true" + "false" + "false";


                        clvalListFromXSD.add(codeListVal);
                    }
                }

                for (int j = 0; j < clvalListVOFromDB.size(); j++) {
                    String clval = "";
                    CodeListValue thisClvalVO = clvalListVOFromDB.get(j);
                    clval = clval + thisClvalVO.getValue();
                    clval = clval + thisClvalVO.getDefinition();
                    clval = clval + thisClvalVO.getDefinitionSource();
                    clval = clval + thisClvalVO.isUsedIndicator();
                    clval = clval + thisClvalVO.isLockedIndicator();
                    clval = clval + thisClvalVO.isExtensionIndicator();
                    clvalListFromDB.add(clval);
                }

                clvalListFromDB.sort(null);
                clvalListFromXSD.sort(null);

                if (clvalListFromDB.size() != clvalListFromXSD.size()) {
                    System.out.print("@@@@ Some CodeListValues are not imported! Check code list: " + codelistVO.getCodeListId());
                    System.out.println("      CountfromXSD: " + clvalListFromXSD.size() + " CountfromDB: " + clvalListFromDB.size());
                } else {
                    for (int j = 0; j < clvalListFromXSD.size(); j++) {
                        if (!clvalListFromXSD.get(j).equals(clvalListFromDB.get(j))) {
                            System.out.println("@@@@CodeListValue has different values! Check code list: " + codelistVO.getCodeListId());
                            System.out.println(clvalListFromXSD.get(j));
                            System.out.println(clvalListFromDB.get(j));
                        }
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_4_PopulateCodeList populateCodeList = ctx.getBean(P_1_4_PopulateCodeList.class);
            populateCodeList.run(ctx);
            populateCodeList.validate();
        }
    }
}
