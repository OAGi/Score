package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
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
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_5_3_to_5_PopulateSCInDTSC {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;


    private void populateDTSCforDefaultBDT(XPathHandler xh, XPathHandler xh2) throws Exception {
        List<DataType> srtObjects = dataTypeRepository.findByType(1);
        for (DataType dt : srtObjects) {
            DataType dt2 = dataTypeRepository.findOne(dt.getBasedDtId());

            // default BDT
            if (dt2.getType() == 0) {
                logger.debug("Popuating SCs for default BDT with type = " + Utility.denToTypeName(dt.getDen()));
                List<DataTypeSupplementaryComponent> cdtSCList = dtScRepository.findByOwnerDtId(dt2.getDtId());

                NodeList attributeNodeList = xh2.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                if (attributeNodeList == null || attributeNodeList.getLength() < 1) {
                    attributeNodeList = xh.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                }
                for (int i = 0; i < attributeNodeList.getLength(); i++) {
                    DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
                    int min_cardinality = -1;
                    int max_cardinality = -1;
                    Element attrElement = (Element) attributeNodeList.item(i);
                    String attribute_name = attrElement.getAttribute("name");
                    String attribute_id = attrElement.getAttribute("id");

                    vo.setGuid(attribute_id);
                    vo.setOwnerDtId(dt.getDtId());
//					Node propertyTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
//					if(propertyTermNode==null){
//						propertyTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
//					}
//					if(propertyTermNode!=null){
//						vo.setPropertyTerm(propertyTermNode.getTextContent());
//					}
//					
//					Node repTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
//					if(repTermNode==null){
//						repTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
//					}
//					if(repTermNode!=null){
//						vo.setRepresentationTerm(repTermNode.getTextContent());
//					}
//					Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
//					if(defNode==null){
//						defNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='"+attribute_id+"']/xsd:annotation/xsd:documentation/ccts_Definition");
//					}
//					if(defNode!=null){
//						vo.setDefinition(defNode.getTextContent());
//					}

                    if (attrElement.getAttribute("use") == null) {
                        min_cardinality = 0;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("optional")) {
                        min_cardinality = 0;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("required")) {
                        min_cardinality = 1;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited")) {
                        min_cardinality = 0;
                        max_cardinality = 0;
                    }

                    vo.setMinCardinality(min_cardinality);
                    vo.setMaxCardinality(max_cardinality);

                    int baseInd = -1;
                    for (int j = 0; j < cdtSCList.size(); j++) {

                        DataTypeSupplementaryComponent baseCDTSC = (DataTypeSupplementaryComponent) cdtSCList.get(j);
                        String basePropertyTerm = baseCDTSC.getPropertyTerm();
                        String baseRepresentationTerm = baseCDTSC.getRepresentationTerm();
                        String baseStr = basePropertyTerm + " " + baseRepresentationTerm;
                        String thisStr = Utility.spaceSeparator(attribute_name);

                        logger.debug(baseStr + " vs " + thisStr);
                        if (baseStr.equals(thisStr)) {
                            baseInd = j;
                            vo.setPropertyTerm(baseCDTSC.getPropertyTerm());
                            vo.setRepresentationTerm(baseCDTSC.getRepresentationTerm());
                            vo.setDefinition(baseCDTSC.getDefinition());
                            vo.setBasedDtScId(baseCDTSC.getDtScId());
                            break;
                        }
                    }
                    if (baseInd > -1) {
                        cdtSCList.remove(baseInd);
                        logger.debug("~~~ " + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This sc has corresponding base!");
                        dtScRepository.save(vo);
                    } else {
                        String propertyTerm = "";
                        String representationTerm = "";
                        String definition = "";

                        propertyTerm = Utility.spaceSeparator(attribute_name);
                        representationTerm = Utility.getRepresentationTerm(attribute_name);

                        Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation/ccts_Definition");
                        if (defNode == null) {
                            defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation");
                        }
                        if (defNode == null) {
                            defNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation/ccts_Definition");
                        }
                        if (defNode != null) {
                            definition = defNode.getTextContent();
                        }

                        vo.setPropertyTerm(propertyTerm);
                        vo.setRepresentationTerm(representationTerm);
                        vo.setDefinition(definition);
                        logger.debug("~~~ " + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by default BDT is new from Attribute!");
                        dtScRepository.save(vo);
                    }
                }

                //Inherit From the remain SCs based on CDT because they don't have corresponding attributes
                //Just copy and get the values from remain cdtSCList
                for (int i = 0; i < cdtSCList.size(); i++) {
                    DataTypeSupplementaryComponent baseCDTSC = (DataTypeSupplementaryComponent) cdtSCList.get(i);
                    DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();

                    vo.setOwnerDtId(dt.getDtId());
                    vo.setGuid(Utility.generateGUID());
                    vo.setPropertyTerm(baseCDTSC.getPropertyTerm());
                    vo.setRepresentationTerm(baseCDTSC.getRepresentationTerm());
                    vo.setDefinition(baseCDTSC.getDefinition());

                    //we already know it doesn't have attributes
                    //so according to design doc,
                    //min_cardinality = 0, max_cardinality = 0
                    vo.setMinCardinality(0);
                    vo.setMaxCardinality(0);
                    vo.setBasedDtScId(baseCDTSC.getDtScId());
                    logger.debug("~~~ " + baseCDTSC.getPropertyTerm() + " " + baseCDTSC.getRepresentationTerm() + ". This SC owned by default BDT is inherited from Base!");
                    dtScRepository.save(vo);
                }
            }
        }
    }

    private void validatePopulateDTSCforDefaultBDT(XPathHandler xh, XPathHandler xh2) throws Exception {
        List<DataType> srtObjects = dataTypeRepository.findByType(1);
        for (DataType dt : srtObjects) {
            DataType dt2 = dataTypeRepository.findOne(dt.getBasedDtId());

            // default BDT
            if (dt2.getType() == 0) {
                logger.debug("Validating SCs for default BDT with type = " + Utility.denToTypeName(dt.getDen()));
                //Inherit from based CDT
                ArrayList<String> fromXSDwAttrs = new ArrayList<String>();
                ArrayList<String> fromDBwAttrs = new ArrayList<String>();

                //Let's check BDT has attribute or not
                NodeList attributeNodeList = xh2.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                if (attributeNodeList == null) {
                    attributeNodeList = xh.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                }
                if (attributeNodeList != null && attributeNodeList.getLength() > 0) {//Get it from Attributes

                    for (int i = 0; i < attributeNodeList.getLength(); i++) {
                        String aStrFromXSDwAttr = "";

                        Element attrNode = (Element) attributeNodeList.item(i);
                        aStrFromXSDwAttr = aStrFromXSDwAttr + attrNode.getAttribute("id");
                        String attrPropertyTerm = "";
                        String attrRepTerm = "";
                        String attrDefinition = "";

                        Node propertyTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attrNode.getAttribute("id") + "']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
                        if (propertyTermNode == null) {
                            propertyTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attrNode.getAttribute("id") + "']/xsd:annotation/xsd:documentation/ccts_PropertyTermName");
                        }
                        if (propertyTermNode != null) {
                            attrPropertyTerm = propertyTermNode.getTextContent();
                        } else {
                            attrPropertyTerm = "null";
                        }

                        Node repTermNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attrNode.getAttribute("id") + "']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
                        if (repTermNode == null) {
                            repTermNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attrNode.getAttribute("id") + "']/xsd:annotation/xsd:documentation/ccts_RepresentationTermName");
                        }
                        if (repTermNode != null) {
                            attrRepTerm = repTermNode.getTextContent();
                        } else {
                            attrRepTerm = "null";
                        }

                        Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attrNode.getAttribute("id") + "']/xsd:annotation/xsd:documentation/ccts_Definition");
                        if (defNode == null) {
                            defNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attrNode.getAttribute("id") + "']/xsd:annotation/xsd:documentation/ccts_Definition");
                        }
                        if (defNode != null) {
                            attrDefinition = defNode.getTextContent();
                        } else {
                            attrDefinition = "null";
                        }
                        aStrFromXSDwAttr = aStrFromXSDwAttr + attrPropertyTerm;
                        aStrFromXSDwAttr = aStrFromXSDwAttr + attrRepTerm;
                        aStrFromXSDwAttr = aStrFromXSDwAttr + attrDefinition;

                        String minCar = "";
                        String maxCar = "";
                        if (attrNode.getAttribute("use") != null) {
                            if (attrNode.getAttribute("use").equalsIgnoreCase("required")) {
                                minCar = "1";
                                maxCar = "1";
                            } else if (attrNode.getAttribute("use").equalsIgnoreCase("optional")) {
                                minCar = "0";
                                maxCar = "1";
                            } else if (attrNode.getAttribute("use").equalsIgnoreCase("prohibited")) {
                                minCar = "0";
                                maxCar = "0";
                            }
                        } else {
                            minCar = "0";
                            maxCar = "1";
                        }
                        aStrFromXSDwAttr = aStrFromXSDwAttr + minCar;
                        aStrFromXSDwAttr = aStrFromXSDwAttr + maxCar;


                        String fromDBStr = "";
                        DataTypeSupplementaryComponent adtsc = dtScRepository.findOneByGuid(attrNode.getAttribute("id"));

                        if (adtsc == null) {
                            logger.debug("@@@@ DTSC from Attributes is not imported! Check DTSC (guid=" + attrNode.getAttribute("id") + ")");
                        } else {
                            fromDBStr = fromDBStr + adtsc.getGuid();
                            fromDBStr = fromDBStr + adtsc.getPropertyTerm();
                            fromDBStr = fromDBStr + adtsc.getRepresentationTerm();
                            fromDBStr = fromDBStr + adtsc.getDefinition();
                            fromDBStr = fromDBStr + adtsc.getMinCardinality();
                            fromDBStr = fromDBStr + adtsc.getMaxCardinality();
                        }

                        if (!fromDBStr.equals(aStrFromXSDwAttr)) {
                            logger.debug("@@@@ DTSC from Attributes has different values! Check DTSC (guid=" + attrNode.getAttribute("id") + ")");
                            logger.debug("     FromXSD: " + aStrFromXSDwAttr);
                            logger.debug("      FromDB: " + fromDBStr);
                        }
                    }
                }
                //Copy the CDT's SC
                //Check BDT SC >= CDT SC (it could be if it's from attr)

                List<DataTypeSupplementaryComponent> CDTSCs = dtScRepository.findByOwnerDtId(dt.getBasedDtId());
                List<DataTypeSupplementaryComponent> BDTSCs = dtScRepository.findByOwnerDtId(dt.getDtId());

                for (int i = CDTSCs.size() - 1; i > -1; i--) {
                    DataTypeSupplementaryComponent cdtsc = CDTSCs.get(i);
                    System.out.print("    Default SC:" + cdtsc.getPropertyTerm() + " " + cdtsc.getRepresentationTerm());
                    for (int j = 0; j < BDTSCs.size(); j++) {

                        DataTypeSupplementaryComponent bdtsc = (DataTypeSupplementaryComponent) BDTSCs.get(j);

                        if (cdtsc.getPropertyTerm().equals(bdtsc.getPropertyTerm())
                                && cdtsc.getRepresentationTerm().equals(bdtsc.getRepresentationTerm())
                                && cdtsc.getDefinition().equals(bdtsc.getDefinition())
                                && cdtsc.getDtScId() == bdtsc.getBasedDtScId()) {
                            System.out.print(" has corresponding Attr= " + cdtsc.getPropertyTerm() + " " + cdtsc.getRepresentationTerm());
                            Node attrCheckNode = xh2.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id = '" + bdtsc.getGuid() + "']");
                            if (attrCheckNode == null) {
                                attrCheckNode = xh.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id = '" + bdtsc.getGuid() + "']");
                            }

                            if (attrCheckNode != null) {
                                Element ele = (Element) attrCheckNode;
                                if (ele.getAttribute("use") == null) {
                                    if (bdtsc.getMinCardinality() == 0 && bdtsc.getMaxCardinality() == 1) {
                                        CDTSCs.remove(i);
                                        break;
                                    }
                                } else if (ele.getAttribute("use").equalsIgnoreCase("optional")) {
                                    if (bdtsc.getMinCardinality() == 0 && bdtsc.getMaxCardinality() == 1) {
                                        CDTSCs.remove(i);
                                        break;
                                    }
                                } else if (ele.getAttribute("use").equalsIgnoreCase("required")) {
                                    if (bdtsc.getMinCardinality() == 1 && bdtsc.getMaxCardinality() == 1) {
                                        CDTSCs.remove(i);
                                        break;
                                    }
                                } else if (ele.getAttribute("use").equalsIgnoreCase("prohibited")) {
                                    if (bdtsc.getMinCardinality() == 0 && bdtsc.getMaxCardinality() == 0) {
                                        CDTSCs.remove(i);
                                        break;
                                    }
                                }
                            } else {
                                if (bdtsc.getMinCardinality() == 0 && bdtsc.getMaxCardinality() == 0) {
                                    CDTSCs.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                    logger.debug("");

                }

                for (int i = CDTSCs.size() - 1; i > -1; i--) {
                    DataTypeSupplementaryComponent cdtsc = (DataTypeSupplementaryComponent) CDTSCs.get(i);
                    logger.debug("@@@@ " + cdtsc.getPropertyTerm() + " " + cdtsc.getRepresentationTerm() + " is not imported! Check Default BDT: " + dt.getGuid());
                }
            }
        }
    }

    public void populateDTSCforUnqualifiedBDT(XPathHandler xh, XPathHandler xh2, boolean is_fields_xsd) throws Exception {
        List<DataType> srtObjects = new ArrayList();
        if (is_fields_xsd) {
            srtObjects = dataTypeRepository.findByType(1);
        } else {
            String metalist[] = {"ExpressionType", "ActionExpressionType", "ResponseExpressionType"};
            for (int k = 0; k < metalist.length; k++) {
                srtObjects.add(k, dataTypeRepository.findOneByTypeAndDen(1, Utility.typeToDen(metalist[k])));
            }
        }

        for (DataType dt : srtObjects) {
            DataType dt2 = dataTypeRepository.findOne(dt.getBasedDtId());
            // unqualified BDT
            if (dt2.getType() != 0) {
                //inheritance
                String denType = Utility.denToName(dt.getDen());
                logger.debug("Popuating SCs for unqualified bdt with type = " + denType);
                Node extensionNode = xh2.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension");
                if (extensionNode == null)
                    extensionNode = xh2.getNode("//xsd:simpleType[@name = '" + denType + "']/xsd:restriction");
                String base = ((Element) extensionNode).getAttribute("base");
                Node baseNode = xh2.getNode("//xsd:complexType[@name = '" + base + "']");
                if (baseNode == null)
                    baseNode = xh.getNode("//xsd:complexType[@name = '" + base + "']");
                if (baseNode == null)
                    baseNode = xh2.getNode("//xsd:simpleType[@name = '" + base + "']");
                if (baseNode == null)
                    baseNode = xh.getNode("//xsd:simpleType[@name = '" + base + "']");

                DataType basedDt = dataTypeRepository.findOneByGuid(((Element) baseNode).getAttribute("id"));
                int based_dt_id = basedDt.getDtId();
                List<DataTypeSupplementaryComponent> baseDefaultDTSCs = dtScRepository.findByOwnerDtId(based_dt_id);

                //adding additional SCs for attributes
                NodeList attributeList = xh2.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                if (attributeList == null) {
                    attributeList = xh.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                }
                int min_cardinality = 0;
                int max_cardinality = 1;

                for (int i = 0; i < attributeList.getLength(); i++) {
                    Element attrElement = (Element) attributeList.item(i);
                    String attribute_name = attrElement.getAttribute("name");
                    String attribute_id = attrElement.getAttribute("id");

                    DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
                    vo.setOwnerDtId(dt.getDtId());
                    vo.setGuid(attribute_id);
                    if (attrElement.getAttribute("use") == null) {
                        min_cardinality = 0;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("optional")) {
                        min_cardinality = 0;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("required")) {
                        min_cardinality = 1;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited")) {
                        min_cardinality = 0;
                        max_cardinality = 0;
                    }

                    vo.setMinCardinality(min_cardinality);
                    vo.setMaxCardinality(max_cardinality);


                    int baseInd = -1;

                    for (int j = 0; j < baseDefaultDTSCs.size(); j++) {
                        DataTypeSupplementaryComponent baseDefaultBDTSC = baseDefaultDTSCs.get(j);
                        String basePropertyTerm = baseDefaultBDTSC.getPropertyTerm();
                        String baseRepresentationTerm = baseDefaultBDTSC.getRepresentationTerm();
                        String baseStr = basePropertyTerm + " " + baseRepresentationTerm;
                        String thisStr = Utility.spaceSeparator(attribute_name);

                        logger.debug(baseStr + " vs " + thisStr);
                        if (baseStr.equals(thisStr)) {
                            baseInd = j;
                            vo.setPropertyTerm(baseDefaultBDTSC.getPropertyTerm());
                            vo.setRepresentationTerm(baseDefaultBDTSC.getRepresentationTerm());
                            vo.setDefinition(baseDefaultBDTSC.getDefinition());
                            vo.setBasedDtScId(baseDefaultBDTSC.getDtScId());
                            break;
                        }
                    }

                    if (baseInd > -1) {
                        baseDefaultDTSCs.remove(baseInd);
                        logger.debug("~~~" + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by unqualified BDT has corresponding base!");
                        dtScRepository.save(vo);
                    } else {

                        String propertyTerm = "";
                        String representationTerm = "";
                        String definition = "";

                        propertyTerm = Utility.spaceSeparator(attribute_name);
                        if (!is_fields_xsd) {
                            propertyTerm = Utility.spaceSeparatorBeforeStr(attribute_name, "Code");
                        }
                        representationTerm = Utility.getRepresentationTerm(attribute_name);

                        Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
                        if (defNode == null) {
                            defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation");
                        }
                        if (defNode != null) {
                            definition = defNode.getTextContent();
                        }

                        vo.setPropertyTerm(propertyTerm);
                        vo.setRepresentationTerm(representationTerm);
                        vo.setDefinition(definition);
                        logger.debug("~~~" + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by unqualified BDT is new from Attribute!");

                        dtScRepository.save(vo);

                        //if it has new attribute extension,
                        //it needs to have records in cdt_sc_awd_pri and cdt_sc_awd_pri_xps_type_map
                        DataTypeSupplementaryComponent insertedSC = dtScRepository.findOneByGuid(vo.getGuid());

                        List<DataType> DataTypewDataTypeTerm = dataTypeRepository.findByDataTypeTerm(vo.getRepresentationTerm());

                        for (int j = 0; j < DataTypewDataTypeTerm.size(); j++) {
                            DataType aDataType = DataTypewDataTypeTerm.get(j);
                            List<CoreDataTypeAllowedPrimitive> CDTAwdPris = cdtAwdPriRepository.findByCdtId(aDataType.getDtId());

                            if (CDTAwdPris.size() > 0 && !CDTAwdPris.isEmpty()) {
                                for (int k = 0; k < CDTAwdPris.size(); k++) {
                                    CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAP = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                                    CoreDataTypeAllowedPrimitive cdtAP = CDTAwdPris.get(k);
                                    cdtSCAP.setCdtScId(insertedSC.getDtScId());
                                    cdtSCAP.setCdtPriId(cdtAP.getCdtPriId());
                                    CoreDataTypePrimitive tmpPri = cdtPriRepository.findOne(cdtAP.getCdtPriId());
     
                                    if(!is_fields_xsd){ 
                                    	if(tmpPri.getName().equals("Token") && (insertedSC.getPropertyTerm().equals("Action") || insertedSC.getPropertyTerm().equals("Language"))){
                                    		cdtSCAP.setDefault(true);
                                    	}
                                    	else if (tmpPri.getName().equals("NormalizedString") && insertedSC.getPropertyTerm().equals("Expression Language")) {
                                    		cdtSCAP.setDefault(true);
                                    	}
                                    	else {
                                    		cdtSCAP.setDefault(false);
                                    	}
                                    }
                                    else {
                                    	cdtSCAP.setDefault(cdtAP.isDefault());
                                    }

                                    if (cdtSCAP.isDefault()) {
                                        logger.debug(" and it's Default!");
                                    } else {
                                        logger.debug("");
                                    }

                                    cdtScAwdPriRepository.save(cdtSCAP);

                                    CoreDataTypeSupplementaryComponentAllowedPrimitive insertedCDTSCAP =
                                            cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(cdtSCAP.getCdtScId(), cdtSCAP.getCdtPriId());

                                    List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAPXTMs =
                                            cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId(cdtAP.getCdtAwdPriId());
                                    for (int m = 0; m < cdtAPXTMs.size(); m++) {
                                        CoreDataTypeAllowedPrimitiveExpressionTypeMap thisAPXTmap = cdtAPXTMs.get(m);
                                        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap tmp = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                                        tmp.setXbtId(thisAPXTmap.getXbtId());
                                        tmp.setCdtScAwdPri(insertedCDTSCAP.getCdtScAwdPriId());
                                        cdtScAwdPriXpsTypeMapRepository.save(tmp);
                                    }
                                }
                                break;//if this is hit, that means dt sc is mapped to cdt sc
                            }
                        }
                    }
                }

                //Inherit From the remain SCs based on default bdt because they don't have corresponding attributes
                //Just copy and get the values from remain baseDefaultDTSCs
                for (int i = 0; i < baseDefaultDTSCs.size(); i++) {
                    DataTypeSupplementaryComponent baseDefaultBDTSC = (DataTypeSupplementaryComponent) baseDefaultDTSCs.get(i);
                    DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
                    vo.setOwnerDtId(dt.getDtId());
                    vo.setGuid(Utility.generateGUID());
                    vo.setPropertyTerm(baseDefaultBDTSC.getPropertyTerm());
                    vo.setRepresentationTerm(baseDefaultBDTSC.getRepresentationTerm());
                    vo.setDefinition(baseDefaultBDTSC.getDefinition());

                    //we already know it doesn't have attributes
                    //so according to design doc,
                    //inherit the values of default BDT sc's min_cardinality, max_cardinality
                    vo.setMinCardinality(baseDefaultBDTSC.getMinCardinality());
                    vo.setMaxCardinality(baseDefaultBDTSC.getMaxCardinality());
                    vo.setBasedDtScId(baseDefaultBDTSC.getDtScId());
                    logger.debug("~~~" + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by unqualified BDT is inherited from Base!");
                    dtScRepository.save(vo);
                }
            }
        }
    }


    public void validatePopulateDTSCforUnqualifiedBDT(XPathHandler xh, XPathHandler xh2, boolean is_fields_xsd) throws Exception {
        List<DataType> srtObjects = new ArrayList();
        if (is_fields_xsd) {
            srtObjects = dataTypeRepository.findByType(1);
        } else {
            String metalist[] = {"ExpressionType", "ActionExpressionType", "ResponseExpressionType"};
            for (int k = 0; k < metalist.length; k++) {
                srtObjects.add(k, dataTypeRepository.findOneByTypeAndDen(1, Utility.typeToDen(metalist[k])));
            }
        }

        for (DataType dt : srtObjects) {
            DataType dt2 = dataTypeRepository.findOne(dt.getBasedDtId());
            // unqualified BDT
            if (dt2.getType() != 0) {
                String denType = Utility.denToName(dt.getDen());
                logger.debug("Validating SCs for unqualified bdt with type = " + denType);
                Node extensionNode = xh2.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension");
                if (extensionNode == null)
                    extensionNode = xh2.getNode("//xsd:simpleType[@name = '" + denType + "']/xsd:restriction");
                String base = ((Element) extensionNode).getAttribute("base");
                Node baseNode = xh2.getNode("//xsd:complexType[@name = '" + base + "']");
                if (baseNode == null)
                    baseNode = xh.getNode("//xsd:complexType[@name = '" + base + "']");
                if (baseNode == null)
                    baseNode = xh2.getNode("//xsd:simpleType[@name = '" + base + "']");
                if (baseNode == null)
                    baseNode = xh.getNode("//xsd:simpleType[@name = '" + base + "']");

                DataType basedDt = dataTypeRepository.findOneByGuid(((Element) baseNode).getAttribute("id"));
                int based_dt_id = basedDt.getDtId();
                List<DataTypeSupplementaryComponent> baseDefaultDTSCs = dtScRepository.findByOwnerDtId(based_dt_id);

                //adding additional SCs for attributes
                NodeList attributeList = xh2.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                if (attributeList == null) {
                    attributeList = xh.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
                }
                int min_cardinality = 0;
                int max_cardinality = 1;

                for (int i = 0; i < attributeList.getLength(); i++) {
                    Element attrElement = (Element) attributeList.item(i);
                    String attribute_name = attrElement.getAttribute("name");
                    String attribute_id = attrElement.getAttribute("id");

                    String fromXSD = "";
                    String fromDB = "";

                    DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
                    vo.setOwnerDtId(dt.getDtId());
                    vo.setGuid(attribute_id);
                    if (attrElement.getAttribute("use") == null) {
                        min_cardinality = 0;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("optional")) {
                        min_cardinality = 0;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("required")) {
                        min_cardinality = 1;
                        max_cardinality = 1;
                    } else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited")) {
                        min_cardinality = 0;
                        max_cardinality = 0;
                    }

                    String propertyTerm = "null";
                    String representationTerm = "null";
                    String definition = "null";

                    propertyTerm = Utility.spaceSeparator(attribute_name);
                    if (!is_fields_xsd) {
                        propertyTerm = Utility.spaceSeparatorBeforeStr(attribute_name, "Code");
                    }

                    representationTerm = Utility.getRepresentationTerm(attribute_name);

                    Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation/ccts_Definition");
                    if (defNode == null) {
                        defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation");
                    }
                    if (defNode != null) {
                        definition = defNode.getTextContent();
                    }

                    fromXSD = fromXSD + attribute_id;
                    fromXSD = fromXSD + propertyTerm;
                    fromXSD = fromXSD + representationTerm;
                    fromXSD = fromXSD + definition;
                    fromXSD = fromXSD + min_cardinality;
                    fromXSD = fromXSD + max_cardinality;

                    DataTypeSupplementaryComponent dtsc = dtScRepository.findOneByGuid(attribute_id);

                    if (dtsc == null) {
                        logger.debug("@@@@ The Attribute is not imported! Check DT: " + dt.getGuid());
                    } else {
                        fromDB = fromDB + dtsc.getGuid();
                        fromDB = fromDB + dtsc.getPropertyTerm();
                        fromDB = fromDB + dtsc.getRepresentationTerm();
                        fromDB = fromDB + dtsc.getDefinition();
                        fromDB = fromDB + dtsc.getMinCardinality();
                        fromDB = fromDB + dtsc.getMaxCardinality();

                        if (!fromXSD.equals(fromDB)) {
                            logger.debug("@@@@ DTSC from Attributes has different values! Check DTSC (guid=" + dtsc.getGuid() + ")");
                            logger.debug("     FromXSD: " + fromXSD);
                            logger.debug("      FromDB: " + fromDB);
                        }
                    }


                    //if it has new attribute extension,
                    //it needs to have records in cdt_sc_awd_pri and cdt_sc_awd_pri_xps_type_map
                    DataTypeSupplementaryComponent insertedSC = dtScRepository.findOneByGuid(vo.getGuid());

                    List DataTypewDataTypeTerm = dataTypeRepository.findByDataTypeTerm(insertedSC.getRepresentationTerm());

                    for (int j = 0; j < DataTypewDataTypeTerm.size(); j++) {
                        DataType aDataType = (DataType) DataTypewDataTypeTerm.get(j);
                        List<CoreDataTypeAllowedPrimitive> CDTAwdPris = cdtAwdPriRepository.findByCdtId(aDataType.getDtId());

                        if (CDTAwdPris.size() > 0 && !CDTAwdPris.isEmpty()) {
                            for (int k = 0; k < CDTAwdPris.size(); k++) {
                                CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAP = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                                CoreDataTypeAllowedPrimitive cdtAP = CDTAwdPris.get(k);
                                cdtSCAP.setCdtScId(insertedSC.getDtScId());
                                cdtSCAP.setCdtPriId(cdtAP.getCdtPriId());
                                CoreDataTypePrimitive tmpPri = cdtPriRepository.findOne(cdtAP.getCdtPriId());
                                if(!is_fields_xsd){
                                	if(tmpPri.equals("Token")){
                                		cdtSCAP.setDefault(true);
                                	}
                                	else {
                                		cdtSCAP.setDefault(false);
                                	}
                                }
                                else {
                                	cdtSCAP.setDefault(cdtAP.isDefault());
                                }


                                String expressionLanguageOrActionCode = "";
                                expressionLanguageOrActionCode = insertedSC.getPropertyTerm();

//								System.out.print("   ~~~"+insertedSC.getPropertyTerm()+" "+insertedSC.getRepresentationTerm()+" is "+tmpPri.getName());
//								if(cdtSCAP.getisDefault()){
//									logger.debug(" and it's Default!");
//								}
//								else{
//									logger.debug("");
//								}

                                //cdtScAwdPriRepository.insertObject(cdtSCAP);


                                logger.debug("        ** Pri:" + tmpPri.getName() + " Default:" + cdtSCAP.isDefault());

                                CoreDataTypeSupplementaryComponentAllowedPrimitive insertedCDTSCAP =
                                        cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(cdtSCAP.getCdtScId(), cdtSCAP.getCdtPriId());

                                List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAPXTMs = cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId(cdtAP.getCdtAwdPriId());
                                for (int m = 0; m < cdtAPXTMs.size(); m++) {
                                    CoreDataTypeAllowedPrimitiveExpressionTypeMap thisAPXTmap = cdtAPXTMs.get(m);
                                    CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap tmp = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                                    tmp.setXbtId(thisAPXTmap.getXbtId());
                                    tmp.setCdtScAwdPri(insertedCDTSCAP.getCdtScAwdPriId());

                                    XSDBuiltInType xbt = xbtRepository.findOne(tmp.getXbtId());

                                    //cdtScAwdPriXpsTypeMapRepository.insertObject(tmp);
                                    logger.debug("          ** XBT:" + xbt.getBuiltInType());

                                }
                            }
                            break;//if this is hit, that means dt sc is mapped to cdt sc
                        }
                    }
                }

                //Inherit From the remain SCs based on default bdt because they don't have corresponding attributes
                //Just copy and get the values from remain baseDefaultDTSCs

                List<DataTypeSupplementaryComponent> unqaulifiedDTSCs = dtScRepository.findByOwnerDtId(dt.getDtId());

                for (int i = baseDefaultDTSCs.size() - 1; i > -1; i--) {
                    DataTypeSupplementaryComponent baseDefaultDTSC = baseDefaultDTSCs.get(i);
                    System.out.print("    Unqualified SC:" + baseDefaultDTSC.getPropertyTerm() + " " + baseDefaultDTSC.getRepresentationTerm());

                    for (int j = 0; j < unqaulifiedDTSCs.size(); j++) {

                        DataTypeSupplementaryComponent unqualifiedDTSC = unqaulifiedDTSCs.get(j);

                        if (baseDefaultDTSC.getPropertyTerm().equals(unqualifiedDTSC.getPropertyTerm())
                                && baseDefaultDTSC.getRepresentationTerm().equals(unqualifiedDTSC.getRepresentationTerm())
                                && baseDefaultDTSC.getDefinition().equals(unqualifiedDTSC.getDefinition())
                                && baseDefaultDTSC.getDtScId() == unqualifiedDTSC.getBasedDtScId()) {
                            System.out.print(" has corresponding attr=" + baseDefaultDTSC.getPropertyTerm() + " " + baseDefaultDTSC.getRepresentationTerm());

                            Node attribute = xh2.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + unqualifiedDTSC.getGuid() + "']");
                            if (attribute == null) {
                                attribute = xh.getNode("//xsd:complexType/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + unqualifiedDTSC.getGuid() + "']");
                            }

                            if (attribute != null) {
                                Element ele = (Element) attribute;

                                if (ele.getAttribute("use").equalsIgnoreCase("required")) {
                                    if (unqualifiedDTSC.getMinCardinality() == 1 && unqualifiedDTSC.getMaxCardinality() == 1) {
                                        baseDefaultDTSCs.remove(i);
                                        break;
                                    }
                                } else if (ele.getAttribute("use").equalsIgnoreCase("optional")) {
                                    if (unqualifiedDTSC.getMinCardinality() == 0 && unqualifiedDTSC.getMaxCardinality() == 1) {
                                        baseDefaultDTSCs.remove(i);
                                        break;
                                    }
                                } else if (ele.getAttribute("use").equalsIgnoreCase("prohibited")) {
                                    if (unqualifiedDTSC.getMinCardinality() == 0 && unqualifiedDTSC.getMaxCardinality() == 0) {
                                        baseDefaultDTSCs.remove(i);
                                        break;
                                    }
                                }
                            } else {
                                if (baseDefaultDTSC.getMinCardinality() == unqualifiedDTSC.getMinCardinality()
                                        && baseDefaultDTSC.getMaxCardinality() == unqualifiedDTSC.getMaxCardinality()) {
                                    baseDefaultDTSCs.remove(i);
                                    break;
                                }
                            }
                        }
                    }
                    logger.debug("");
                }
                for (int i = baseDefaultDTSCs.size() - 1; i > -1; i--) {
                    DataTypeSupplementaryComponent bdtsc = baseDefaultDTSCs.get(i);
                    logger.debug("@@@@ " + bdtsc.getPropertyTerm() + " " + bdtsc.getRepresentationTerm() + " is not inherited! Check Unqualified BDT: " + dt.getGuid());
                }
            }
        }
    }

    private void populateDTSC() throws Exception {
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        populateDTSCforDefaultBDT(businessDataType_xsd, fields_xsd);
        populateDTSCforUnqualifiedBDT(businessDataType_xsd, fields_xsd, true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.5.3-5 Start");

        populateDTSC();

        logger.info("### 1.5.3-5 End");

    }

    public void validate() throws Exception {
        logger.info("### 1.5.3-5 Start Validation");

        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        validatePopulateDTSCforDefaultBDT(businessDataType_xsd, fields_xsd);
        validatePopulateDTSCforUnqualifiedBDT(businessDataType_xsd, fields_xsd, true);

        logger.info("### 1.5.3-5 Validation End");

    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_5_3_to_5_PopulateSCInDTSC populateSCInDTSC = ctx.getBean(P_1_5_3_to_5_PopulateSCInDTSC.class);
            populateSCInDTSC.run(ctx);
        }
    }
}
