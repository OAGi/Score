package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.BusinessDataTypePrimitiveRestrictionRepository;
import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.DataType;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jaehun Lee
 * @author Yunsu Lee
 * @version 1.0
 *
 * This program populates the data that indicate
 * '3.1.1.9 Import additional BDTs from Meta.xsd' section in the design document.
 */
@Component
public class P_1_6_1_to_2_PopulateDTFromMetaXSD {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    private int userId;
    private int releaseId;

    public void importAdditionalBDT(XPathHandler xh) throws Exception {
        NodeList result = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");

        String module = Utility.extractModuleName(SRTConstants.META_XSD_FILE_PATH);

        List<BusinessDataTypePrimitiveRestriction> bdtPriRestris = new ArrayList();
        for (int i = 0; i < result.getLength(); i++) {
            Element ele = (Element) result.item(i);
            String name = ele.getAttribute("name");

            DataType dataType = new DataType();
            dataType.setGuid(ele.getAttribute("id"));
            dataType.setType(1);
            dataType.setVersionNum("1.0");

            Node extension = xh.getNode("//xsd:complexType[@name = '" + name + "']/xsd:simpleContent/xsd:extension");
            String base = Utility.typeToDen(((Element) extension).getAttribute("base"));
            DataType dtVO_01 = dataTypeRepository.findByDen(base).get(0);

            dataType.setBasedDtId(dtVO_01.getDtId());
            dataType.setDataTypeTerm(dtVO_01.getDataTypeTerm());

            dataType.setDen(Utility.typeToDen(name));
            dataType.setContentComponentDen(Utility.typeToContent(name));

            Element definition = (Element) ele.getElementsByTagName("xsd:documentation").item(0);
            if (definition != null)
                dataType.setDefinition(definition.getTextContent());
            else
                dataType.setDefinition(null);

            dataType.setContentComponentDefinition(null);
            dataType.setRevisionDoc(null);
            dataType.setState(3);
            dataType.setCreatedBy(userId);
            dataType.setLastUpdatedBy(userId);
            dataType.setOwnerUserId(userId);
            dataType.setRevisionDoc(null);
            dataType.setRevisionNum(0);
            dataType.setRevisionTrackingNum(0);
            dataType.setDeprecated(false);
            dataType.setReleaseId(releaseId);
            dataType.setModule(module);
            logger.debug("Populating additional BDTs from meta whose name is " + name);
            dataTypeRepository.saveAndFlush(dataType);

            // BDT_Primitive_Restriction
            bdtPriRestris.addAll(
                    loadBDTPrimitiveRestrictions(dtVO_01.getDtId(), dataType.getDtId())
            );
        }

        bdtPriRestriRepository.save(bdtPriRestris);
    }

    private List<BusinessDataTypePrimitiveRestriction> loadBDTPrimitiveRestrictions(
            int basedBdtId, int bdtId) throws Exception {
        List<BusinessDataTypePrimitiveRestriction> result = new ArrayList();
        List<BusinessDataTypePrimitiveRestriction> al = bdtPriRestriRepository.findByBdtId(basedBdtId);

        for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri = new BusinessDataTypePrimitiveRestriction();
            bdtPriRestri.setBdtId(bdtId);
            bdtPriRestri.setCdtAwdPriXpsTypeMapId(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
            bdtPriRestri.setDefault(aBusinessDataTypePrimitiveRestriction.isDefault());
            logger.debug("Populating BDT Primitive Restriction for bdt id = " + bdtId + " cdt primitive expression type map = " + bdtPriRestri.getCdtAwdPriXpsTypeMapId() + " is_default = " + bdtPriRestri.isDefault());

            result.add(bdtPriRestri);
        }

        return result;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.6. Start");

        userId = userRepository.findAppUserIdByLoginId("oagis");
        releaseId = releaseRepository.findReleaseIdByReleaseNum("10.1");

        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
        importAdditionalBDT(meta_xsd);

        P_1_5_3_to_5_PopulateSCInDTSC dtsc = applicationContext.getBean(P_1_5_3_to_5_PopulateSCInDTSC.class);
        dtsc.populateDTSCforUnqualifiedBDT(businessDataType_xsd, meta_xsd, false);

        P_1_5_6_PopulateBDTSCPrimitiveRestriction bdtscpri = applicationContext.getBean(P_1_5_6_PopulateBDTSCPrimitiveRestriction.class);
        bdtscpri.populateBDTSCPrimitiveRestriction(businessDataType_xsd, meta_xsd, false);

        logger.info("### 1.6. End");
    }

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_6_1_to_2_PopulateDTFromMetaXSD populateDTFromMetaXSD = ctx.getBean(P_1_6_1_to_2_PopulateDTFromMetaXSD.class);
            populateDTFromMetaXSD.run(ctx);
        }
    }
}
