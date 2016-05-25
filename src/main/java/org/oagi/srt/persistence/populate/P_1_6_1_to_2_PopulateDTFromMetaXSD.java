package org.oagi.srt.persistence.populate;

import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.BusinessDataTypePrimitiveRestrictionRepository;
import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

/**
*
* @author Jaehun Lee
* @author Yunsu Lee
* @version 1.0
*
*/
@Component
public class P_1_6_1_to_2_PopulateDTFromMetaXSD {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DataTypeRepository dataTypeRepository;

	@Autowired
	private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;
	
	public void importAdditionalBDT(XPathHandler xh) throws Exception {
		DataType dtVO = new DataType();

		NodeList result = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");
	    
		for(int i = 0; i < result.getLength(); i++) {
		    Element ele = (Element)result.item(i);
		    String name = ele.getAttribute("name");
		    
		    dtVO.setGuid(ele.getAttribute("id"));
		    dtVO.setType(1);
		    dtVO.setVersionNum("1.0");
		    //dtVO.setRevisionType(0);
		    
		    Node extension = xh.getNode("//xsd:complexType[@name = '" + name + "']/xsd:simpleContent/xsd:extension");
		    String base = Utility.typeToDen(((Element)extension).getAttribute("base"));
			DataType dtVO_01 = dataTypeRepository.findOneByDen(base);
		    
		    
		    dtVO.setBasedDtId(dtVO_01.getDtId());
		    dtVO.setDataTypeTerm(dtVO_01.getDataTypeTerm());
		    
		    dtVO.setDen(Utility.typeToDen(name));
		    dtVO.setContentComponentDen(Utility.typeToContent(name));
		    
		    Element definition = (Element)ele.getElementsByTagName("xsd:documentation").item(0);
		    if(definition != null) 
			    dtVO.setDefinition(definition.getTextContent());
		    else 
		    	dtVO.setDefinition(null);
		    
		    dtVO.setContentComponentDefinition(null);
		    dtVO.setRevisionDoc(null);
		    dtVO.setState(3);

			int userId = userRepository.findAppUserIdByLoginId("oagis");
			dtVO.setCreatedBy(userId);
			dtVO.setLastUpdatedBy(userId);
			dtVO.setOwnerUserId(userId);
			dtVO.setRevisionDoc(null);
			dtVO.setRevisionNum(0);
			dtVO.setRevisionTrackingNum(0);
			dtVO.setDeprecated(false);
			System.out.println("Populating additonal BDTs from meta whose name is "+ name);
			dataTypeRepository.save(dtVO);
		    
		    // BDT_Primitive_Restriction
			insertBDTPrimitiveRestriction(dtVO_01.getDtId(), dataTypeRepository.findOneByGuid(dtVO.getGuid()).getDtId());
	    }
	}
	
	private void insertBDTPrimitiveRestriction(int basedBdtId, int bdtId) throws Exception {
		List<BusinessDataTypePrimitiveRestriction> al = bdtPriRestriRepository.findByBdtId(basedBdtId);
		
		for(BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
			BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
			theBDT_Primitive_RestrictionVO.setBdtId(bdtId);
			theBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
			theBDT_Primitive_RestrictionVO.setDefault(aBusinessDataTypePrimitiveRestriction.isDefault());
			System.out.println("Populating BDT Primitive Restriction for bdt id = " + bdtId+ " cdt primitive expression type map = "+theBDT_Primitive_RestrictionVO.getCdtAwdPriXpsTypeMapId()+" is_default = " + theBDT_Primitive_RestrictionVO.isDefault());
			bdtPriRestriRepository.save(theBDT_Primitive_RestrictionVO);
		}
	}

	@Transactional(rollbackFor = Throwable.class)
	public void run(ApplicationContext applicationContext) throws Exception {
		System.out.println("### 1.6. Start");
		
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		importAdditionalBDT(meta_xsd);
		
		P_1_5_3_to_5_PopulateSCInDTSC dtsc = applicationContext.getBean(P_1_5_3_to_5_PopulateSCInDTSC.class);
		dtsc.populateDTSCforUnqualifiedBDT(businessDataType_xsd, meta_xsd, false);
		
		P_1_5_6_PopulateBDTSCPrimitiveRestriction bdtscpri = applicationContext.getBean(P_1_5_6_PopulateBDTSCPrimitiveRestriction.class);
		bdtscpri.populateBDTSCPrimitiveRestriction(businessDataType_xsd, meta_xsd, false);

		System.out.println("### 1.6. End");
	}
	
	public static void main (String args[]) throws Exception {
		try (AbstractApplicationContext ctx = (AbstractApplicationContext)
				SpringApplication.run(Application.class, args);) {
			P_1_6_1_to_2_PopulateDTFromMetaXSD populateDTFromMetaXSD = ctx.getBean(P_1_6_1_to_2_PopulateDTFromMetaXSD.class);
			populateDTFromMetaXSD.run(ctx);
		}
	}
}
