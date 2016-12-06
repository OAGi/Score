package org.oagi.srt.validate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.ImportConstants;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.CodeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

@Component
public class CodeListTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    public void validate(String path1) throws Exception {

        XPathHandler xh = new XPathHandler(path1);
        NodeList codelist = xh.getNodeList("//xsd:simpleType");

        for (int i = 0; i < codelist.getLength(); i++) {
            Element content = (Element) codelist.item(i);
            if (content.getAttribute("name").endsWith("CodeContentType")) {
                String contentTypeGuid = content.getAttribute("id");
                String name = content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("ContentType"));
                String enumTypeGuid = null;
                NodeList enumList = null;
                for (int j = 0; j < codelist.getLength(); j++) {
                    Element enumType = (Element) codelist.item(j);
                    if ((content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("CodeContentType")) + "EnumerationType").equalsIgnoreCase(enumType.getAttribute("name")) ||
                            (content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("CodeContentType")) + "CodeEnumerationType").equalsIgnoreCase(enumType.getAttribute("name"))
                            ) {
                        enumTypeGuid = enumType.getAttribute("id");
                        enumList = xh.getNodeList("//xsd:simpleType[@name = '" + content.getAttribute("name").substring(0, content.getAttribute("name").indexOf("CodeContentType")) + "EnumerationType or CodeEnumerationType" + "']/xsd:restriction/xsd:enumeration");
                        break;
                    } else if (xh.getNodeList("//xsd:simpleType[@name = '" + content.getAttribute("name") + "']/xsd:restriction/xsd:enumeration") != null) {
                        enumTypeGuid = enumType.getAttribute("id");
                        enumList = xh.getNodeList("//xsd:simpleType[@name = '" + content.getAttribute("name") + "']/xsd:restriction/xsd:enumeration");
                        break;
                    }

                }

                if (contentTypeGuid == null || enumTypeGuid == null || name == null) {
                    System.out.println("### Codelist Error : contentTypeGuid = " + contentTypeGuid + "  enum_type_guid = " + enumTypeGuid + "  name = " + name);
                    continue;
                }

                long codelist_id = getCodeListId(contentTypeGuid, enumTypeGuid, name);
                for (int j = 0; j < enumList.getLength(); j++) {
                    Element codelistvalue = (Element) enumList.item(j);
                    long codelistvalue_id = getCodeListValueID(codelist_id, codelistvalue.getAttribute("value"));
                    if (codelist_id == 0 || codelistvalue_id == 0)
                        System.out.println("### Codelist value Error : code_list_id = " + codelist_id + "  enum_type_guid = " + enumTypeGuid + "  name = " + name + "  enum_value = " + codelistvalue.getAttribute("value"));
                }
            }
        }

        NodeList result = xh.getNodeList("//xsd:simpleType");
        NodeList union = xh.getNodeList("//xsd:simpleType[xsd:union]");
        List<Long> unionInt = new ArrayList();
        for (int i = 0; i < result.getLength(); i++) {
            Element tmp = (Element) result.item(i);
            Node union_check = xh.getNode("//xsd:simpleType[@name = '" + tmp.getAttribute("name") + "']//xsd:union");
            if (union_check != null) {
                CodeList codelistVO = codeListRepository.findOneByGuid(tmp.getAttribute("id"));
                unionInt.add(codelistVO.getCodeListId());
            }
        }

        for (CodeList aCodelist : codeListRepository.findAll()) {
            if (aCodelist.getBasedCodeListId() != 0)
                System.out.println("Error, based code list id is not null when code list id =" + aCodelist.getCodeListId());
            if (aCodelist.getCreatedBy() != getUserID("oagis"))
                System.out.println("Error, createby is not correct when code list id =" + aCodelist.getCodeListId());
            if (aCodelist.getLastUpdatedBy() != getUserID("oagis"))
                System.out.println("Error, LastUpdatedBy is not correct when code list id =" + aCodelist.getCodeListId());
            if (path1.endsWith("CodeLists_1.xsd") && aCodelist.isExtensibleIndicator() == true && unionInt.indexOf(aCodelist.getCodeListId()) == -1)
                System.out.println("Error, Extensible Indicator is not correct when code list id =" + aCodelist.getCodeListId());
        }
    }

    public int getUserID(String userName) {
        return userRepository.findAppUserIdByLoginId(userName);
    }

    public long getCodeListId(String codelistGuid, String enumTypeguid, String name) {
        return codeListRepository.findOneByGuidAndEnumTypeGuidAndName(codelistGuid, enumTypeguid, name).getCodeListId();
    }

    public long getCodeListValueID(long codelist_id, String value) {
        return codeListValueRepository.findOneByCodeListIdAndValue(codelist_id, value).getCodeListValueId();
    }

    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### Codelist validation Start");

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
            String path = ImportConstants.filepath("CodeList") + tt[i][0] + ".xsd";
            System.out.println("#### " + tt[i][0] + " ing..");
            validate(path);
        }

        System.out.println("### Codelist validation End");
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            CodeListTest codeListTest = ctx.getBean(CodeListTest.class);
            codeListTest.run(ctx);
        }
    }

}