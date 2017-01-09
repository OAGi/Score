package org.oagi.srt.persistence.populate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.ImportConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListState;
import org.oagi.srt.repository.entity.CodeListValue;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.srt.common.ImportConstants.CODELIST_CHARACTER_SET_CODE_IANA_FILENAME;
import static org.oagi.srt.persistence.populate.DataImportScriptPrinter.printTitle;

/**
 * @author Jaehun Lee
 * @version 1.0
 */
@Component
public class P_1_4_PopulateCodeList {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.4 Start");
        printTitle("Populate Code Lists");

        String tt[][] = {
                {"CodeList_ConditionTypeCode_1", "314"},
                {"CodeList_ConstraintTypeCode_1", "314"},
                {"CodeList_DateFormatCode_1", "314"},
                {"CodeList_DateTimeFormatCode_1", "314"},
                {"CodeList_TimeFormatCode_1", "314"},

                {CODELIST_CHARACTER_SET_CODE_IANA_FILENAME, "379"},
                {"CodeList_MIMEMediaTypeCode_IANA_7_04", "379"},

                {"CodeList_CurrencyCode_ISO_7_04", "5"},
                {"CodeList_LanguageCode_ISO_7_04", "5"},
                {"CodeList_TimeZoneCode_1", "5"},

                {"CodeList_UnitCode_UNECE_7_04", "6"},
                {"CodeLists_1", "314"}
        };

        List<CodeList> codeLists = new ArrayList();
        for (int i = 0; i < tt.length; i++) {
            String fileName = tt[i][0];
            int agencyId = Integer.parseInt(tt[i][1]);

            logger.debug("## Importing Code List from " + fileName + "..");
            String filename = fileName + ".xsd";
            codeLists.addAll(codeList(filename, agencyId));
        }
        codeListRepository.save(codeLists);

        for (int i = 0; i < tt.length; i++) {
            String fileName = tt[i][0];
            int agencyId = Integer.parseInt(tt[i][1]);

            logger.debug("## Updating Code List referring " + fileName + "..");

            String filename = fileName + ".xsd";
            updateBasedCodeListID(filename, agencyId);
        }

        for (int i = 0; i < tt.length; i++) {
            String fileName = tt[i][0];
            int agencyId = Integer.parseInt(tt[i][1]);

            logger.debug("## Impoting Code List Value from " + fileName + "..");

            String filename = fileName + ".xsd";
            codeListValue(filename);
        }

        logger.info("### 1.4 End");
    }

    private List<CodeList> codeList(String fileinput, int agencyId) throws Exception {
        List<CodeList> codeLists = new ArrayList();
        String path1 = new File(ImportConstants.filepath("CodeList") + fileinput).getCanonicalPath();
        XPathHandler xh = new XPathHandler(path1);

        NodeList result = xh.getNodeList("//xsd:simpleType");
        int userId = getUserID("oagis");

        for (int i = 0; i < result.getLength(); i++) {
            Element element = (Element) result.item(i);
            String name = element.getAttribute("name");

            if (name.endsWith("CodeContentType")) {
                CodeList codeList = new CodeList();
                codeList.setGuid(element.getAttribute("id"));

                //added by TKim according to design Doc v2.4
                if (name.startsWith("oacl")) {
                    String xyz = name.substring(0, name.lastIndexOf("CodeContentType"));
                    Node enumTypeNode = xh.getNode("//xsd:simpleType[@name='" + xyz + "CodeEnumerationType']/@id");
                    if (enumTypeNode != null) {
                        String enumerationTypeGUID = enumTypeNode.getTextContent();
                        codeList.setEnumTypeGuid(enumerationTypeGUID);
                    }
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

                Element definitionNode = (Element) xh.getNode("//xsd:simpleType[@name = '" + name + "']/xsd:annotation/xsd:documentation");
                if (definitionNode != null) {
                    codeList.setDefinition(definitionNode.getTextContent());
                    codeList.setDefinitionSource(definitionNode.getAttribute("source"));
                }
                codeList.setExtensibleIndicator(true);  //logic changed. extensible indicator is always TRUE.

                codeList.setCreatedBy(userId);
                codeList.setLastUpdatedBy(userId);
                codeList.setState(CodeListState.Published);
                String moduleName = Utility.extractModuleName(path1);
                Module module = moduleRepository.findByModule(moduleName);
                codeList.setModule(module);
                codeLists.add(codeList);
            } else if (!name.endsWith("EnumerationType")) {
                logger.warn("Check !!  " + name);
            }
        }

        return codeLists;
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

    private long getAgencyID(int value) {
        return agencyIdListValueRepository.findOneByValue(String.valueOf(value)).getAgencyIdListValueId();
    }

    private int getUserID(String userName) {
        return userRepository.findAppUserIdByLoginId(userName);
    }

    private void updateBasedCodeListID(String fileinput, int agencyId) throws Exception {
        String path1 = ImportConstants.filepath("CodeList") + fileinput;
        XPathHandler xh = new XPathHandler(path1);

        NodeList result = xh.getNodeList("//xsd:simpleType");

        for (int i = 0; i < result.getLength(); i++) {
            Element element = (Element) result.item(i);
            String elementName = element.getAttribute("name");
            if (elementName.endsWith("CodeContentType")) {
                //added by T.Kim according to design doc v2.4
                //based_code_list_id
                String xyz = elementName.substring(0, elementName.lastIndexOf("CodeContentType"));
                Node unionNode = xh.getNode("//xsd:simpleType[@name='" + xyz + "CodeContentType']//xsd:union/@memberTypes");

                if (unionNode != null) {
                    String unionMemberStr;
                    unionMemberStr = unionNode.getTextContent();
                    unionMemberStr = unionMemberStr.replace("xsd:token", "");
                    unionMemberStr = unionMemberStr.trim();
                    String checkEnumType = xyz + "CodeEnumerationType";

                    if (!unionMemberStr.equals(checkEnumType)) { //find base code list!
                        unionMemberStr = unionMemberStr.replace("ContentType", "");

                        CodeList baseCodelistVO = codeListRepository.findByNameContaining(unionMemberStr).get(0);

                        String guid = element.getAttribute("id");
                        CodeList codelistVO = codeListRepository.findOneByGuid(guid);

                        if (baseCodelistVO != null && codelistVO != null) {
                            codelistVO.setBasedCodeListId(baseCodelistVO.getCodeListId());
                            codeListRepository.saveAndFlush(codelistVO);
                            logger.debug(" Update Based Code List ID: " + elementName.substring(0, elementName.lastIndexOf("ContentType")) + " is based on " + baseCodelistVO.getName());
                        } else {
                            logger.warn(" Update Based Code List ID Is Failed! Check CodeListID: " + elementName);
                            return;
                        }
                    }
                }
            } else if (!elementName.endsWith("EnumerationType"))
                logger.warn("Check !!  " + elementName);
        }

    }

    private void codeListValue(String fileinput) throws Exception {
        String path1 = ImportConstants.filepath("CodeList") + fileinput;
        XPathHandler xh = new XPathHandler(path1);

        NodeList result = xh.getNodeList("//xsd:simpleType");

        for (int i = 0; i < result.getLength(); i++) {
            Element element = (Element) result.item(i);
            String elementName = element.getAttribute("name");

            NodeList enumeration = null;

            if (elementName.endsWith("CodeContentType")) {
                String guid = element.getAttribute("id");
                CodeList codeList = codeListRepository.findOneByGuid(guid);

                if (codeList.getEnumTypeGuid() != null) {
                    //Get code_list_value from enumerationType
                    enumeration = xh.getNodeList("//xsd:simpleType[@id='" + codeList.getEnumTypeGuid() + "']//xsd:enumeration");
                    if (enumeration.getLength() < 1) {
                        logger.debug("   EnumerationType for " + codeList.getName() + " has no enumerations");
                    } else {
                        logger.debug("   Start import from EnumerationType to: " + codeList.getName());
                    }
                } else {//if enum_type_guid is null
                    if (codeList.getBasedCodeListId() > 0) {
                        //Inherit from based code list
                        List<CodeListValue> codeListValuesFromBase = codeListValueRepository.findByCodeListId(codeList.getBasedCodeListId());

                        if (codeListValuesFromBase.size() > 0) {
                            logger.debug("   Start inherit from BasedCodeList to : " + codeList.getName());
                            for (int j = 0; j < codeListValuesFromBase.size(); j++) {
                                CodeListValue codeListValue = new CodeListValue();
                                CodeListValue codeListValueFromBase = codeListValuesFromBase.get(j);
                                codeListValue.setCodeListId(codeList.getCodeListId());
                                codeListValue.setDefinition(codeListValueFromBase.getDefinition());
                                codeListValue.setDefinitionSource(codeListValueFromBase.getDefinitionSource());
                                codeListValue.setValue(codeListValueFromBase.getValue());
                                codeListValue.setName(codeListValueFromBase.getName());
                                codeListValue.setUsedIndicator(true);
                                codeListValue.setLockedIndicator(false);
                                codeListValue.setExtensionIndicator(false);
                                codeListValueRepository.saveAndFlush(codeListValue);
                            }
                        }
                    } else {//if based code list is null
                        //Get the values from local xsd:enumeration
                        enumeration = xh.getNodeList("//xsd:simpleType[@name='" + elementName + "']//xsd:enumeration");
                        if (enumeration.getLength() < 1) {
                            logger.debug("   " + codeList.getName() + " has no local enumerations");
                        } else {
                            logger.debug("   Start import from LocalEnumeration: ");
                        }
                    }
                }

                if (enumeration != null && enumeration.getLength() > 0) {
                    int count = 0;
                    for (int j = 0; j < enumeration.getLength(); j++) {
                        Element aEnum = (Element) enumeration.item(j);
                        CodeListValue codeListValue = new CodeListValue();
                        codeListValue.setCodeListId(codeList.getCodeListId());
                        codeListValue.setValue(aEnum.getAttribute("value"));
                        codeListValue.setName(aEnum.getAttribute("value"));
                        codeListValue.setUsedIndicator(true);
                        codeListValue.setLockedIndicator(false);
                        codeListValue.setExtensionIndicator(false);

                        Node definitionNode = xh.getNode("//xsd:simpleType[@name='" + elementName + "']//xsd:enumeration[@value='" + codeListValue.getValue() + "']//xsd:documentation");
                        if (definitionNode == null) {
                            definitionNode = xh.getNode("//xsd:simpleType[@id='" + codeList.getEnumTypeGuid() + "']//xsd:enumeration[@value='" + codeListValue.getValue() + "']//xsd:documentation");
                        }
                        if (definitionNode != null) {
                            Element definition = (Element) definitionNode;
                            codeListValue.setDefinition(definition.getTextContent().trim());
                            codeListValue.setDefinitionSource(definition.getAttribute("source"));
                        }

                        codeListValueRepository.saveAndFlush(codeListValue);
                        count++;
                    }
                    if (count > 0) {
                        logger.info("\t\t" + count + " enumerations are imported!");
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_4_PopulateCodeList populateCodeList = ctx.getBean(P_1_4_PopulateCodeList.class);
            populateCodeList.run(ctx);
        }
    }
}
