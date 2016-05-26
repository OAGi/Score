package org.oagi.srt.persistence.populate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository;
import org.oagi.srt.repository.CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository;
import org.oagi.srt.repository.DataTypeRepository;
import org.oagi.srt.repository.DataTypeSupplementaryComponentRepository;
import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitive;
import org.oagi.srt.repository.entity.CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.DataTypeSupplementaryComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest

public class P_1_5_3_to_5_PopulateSCInDTSCTestCase {
	 @Autowired
	 private DataTypeSupplementaryComponentRepository dataTypeSupplementaryComponentRepository;
	
	 @Autowired	
	 private DataTypeRepository dataTypeRepository;
	 
	 @Autowired
	 private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtSCAllowedPrimitiveRepository;
	 
	 @Autowired
	 private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtSCAllowedPrimitiveExpressionTypeMapRepository;
	 
	 private class ExpectedNewDataTypeSupplementaryComponent {
		 private String guid;
		 private String attributeName;
		 private String type;
		 private String definition;
		 private String use;
		 XPathHandler fieldsXP = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
         XPathHandler bizDTXP = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		 
		 public ExpectedNewDataTypeSupplementaryComponent(String guid, String attributeName) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
			 this.guid=guid;
			 this.attributeName=attributeName;
			 
			 boolean isDefaultBDTSC=true;
			 
             XPathHandler xbtXP = new XPathHandler(SRTConstants.XBT_FILE_PATH);
			 
             Node aDTSCNode = bizDTXP.getNode("//xsd:complexType//xsd:attribute[@id='"+guid+"']");
             if(aDTSCNode==null){
             	aDTSCNode = fieldsXP.getNode("//xsd:complexType//xsd:attribute[@id='"+guid+"']");
             	isDefaultBDTSC=false;
             }
			 
             Element aDTSCElem = (Element) aDTSCNode;
             type = aDTSCElem.getAttribute("type");
             
             if(isDefaultBDTSC){
            	Node defNode =  bizDTXP.getNode("//xsd:complexType//xsd:attribute[@id='"+guid+"']//xsd:documentation//*[local-name()=\"ccts_Definition\"]");
            	definition = defNode.getTextContent();
             }
             else {
            	 Node defNode =  fieldsXP.getNode("//xsd:complexType//xsd:attribute[@id='"+guid+"']//xsd:documentation");
            	 definition = defNode.getTextContent();
             }
             use = aDTSCElem.getAttribute("use");
		 }
	 }
	 
	 @Test
	 public void testPopulateNewDTSCsFromAttribute() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
	        Map<String, ExpectedNewDataTypeSupplementaryComponent> expectedDTSCs = new HashMap();
	        
	        expectedDTSCs.put("oagis-id-0dd62519460d4a91bfdbc1f7778befac", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-0dd62519460d4a91bfdbc1f7778befac","currencyCode"));
	        expectedDTSCs.put("oagis-id-4b87790af2ea49a7a5d1f92dec29335f", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-4b87790af2ea49a7a5d1f92dec29335f","mimeCode"));
	        expectedDTSCs.put("oagis-id-88d1b6e5f2d94dbda4d08793249cd878", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-88d1b6e5f2d94dbda4d08793249cd878","characterSetCode"));
	        expectedDTSCs.put("oagis-id-8ca03283b2ec469598af6bb115cf0bf2", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-8ca03283b2ec469598af6bb115cf0bf2","filenameName"));
	        expectedDTSCs.put("oagis-id-a269304987de4f3a845be02a78df41ae", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-a269304987de4f3a845be02a78df41ae","listID"));
	        expectedDTSCs.put("oagis-id-6521de84253e4428adfd742cc8cd4603", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-6521de84253e4428adfd742cc8cd4603","listAgencyID"));
	        expectedDTSCs.put("oagis-id-118b994c3a7b45f3b0f4f37d184cf0a6", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-118b994c3a7b45f3b0f4f37d184cf0a6","listVersionID"));
	        expectedDTSCs.put("oagis-id-deb5c8ba87004cd49e57010ad3ece3af", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-deb5c8ba87004cd49e57010ad3ece3af","mimeCode"));
	        expectedDTSCs.put("oagis-id-318dae29eff94cbc9c172448ce54fb34", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-318dae29eff94cbc9c172448ce54fb34","characterSetCode"));
	        expectedDTSCs.put("oagis-id-600fb9b394aa44c2ad17be7ce8aa2396", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-600fb9b394aa44c2ad17be7ce8aa2396","filenameName"));
	        expectedDTSCs.put("oagis-id-3233f3fb57c9482fb39255be78c495af", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-3233f3fb57c9482fb39255be78c495af","schemeID"));
	        expectedDTSCs.put("oagis-id-59ee9c5aa80641d7a0d78f5418ebcfa4", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-59ee9c5aa80641d7a0d78f5418ebcfa4","schemeVersionID"));
	        expectedDTSCs.put("oagis-id-cb2ac5b98b0847e785de08756c29de85", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-cb2ac5b98b0847e785de08756c29de85","schemeAgencyID"));
	        expectedDTSCs.put("oagis-id-d83591f0ee35430f95172883718499ff", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-d83591f0ee35430f95172883718499ff","unitCode"));
	        expectedDTSCs.put("oagis-id-42e59d799de147b8ab49c8a27ec85ff1", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-42e59d799de147b8ab49c8a27ec85ff1","languageCode"));
	        expectedDTSCs.put("oagis-id-9b0fbfcf9ac244b29dc8c7281607dc90", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-9b0fbfcf9ac244b29dc8c7281607dc90","unitCode"));
	        expectedDTSCs.put("oagis-id-7b304c647a924ce7973753eecdcb5d79", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-7b304c647a924ce7973753eecdcb5d79","mimeCode"));
	        expectedDTSCs.put("oagis-id-8bbefdeaf55d4daab08a48f32e0f4796", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-8bbefdeaf55d4daab08a48f32e0f4796","characterSetCode"));
	        expectedDTSCs.put("oagis-id-d6688838cedb4fd7aae20626c2ef27b0", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-d6688838cedb4fd7aae20626c2ef27b0","filenameName"));
	        expectedDTSCs.put("oagis-id-c8d0c7094d7d4fbeb7e50fd20a17c1b3", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-c8d0c7094d7d4fbeb7e50fd20a17c1b3","languageCode"));
	        expectedDTSCs.put("oagis-id-2c6f9ff650c24bfdbd0c49ac67de13bd", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-2c6f9ff650c24bfdbd0c49ac67de13bd","mimeCode"));
	        expectedDTSCs.put("oagis-id-b558001026164520b5ddda71d6360b09", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-b558001026164520b5ddda71d6360b09","characterSetCode"));
	        expectedDTSCs.put("oagis-id-3a765939c131448da1858fd6f1c339db", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-3a765939c131448da1858fd6f1c339db","filenameName"));
	        expectedDTSCs.put("oagis-id-84fa20db74b942449e1885cff79b24df", new ExpectedNewDataTypeSupplementaryComponent("oagis-id-84fa20db74b942449e1885cff79b24df","sequenceNumber"));

	        /*
		        //xsd:complexType[@id='oagis-id-109055a967bd4cf19ee3320755b01f8d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-f2c5dcba0088440d866ea23a81876280']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-9ec6be30dabf45d5b53b765634be2412']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-dc994532fe464847acf84a54548276ff']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-83d4cc94be8249a3b8cbe7e1c2ecb417']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3318aed9165847e3afb907724db2b65c']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-bea4dcd433d54aa698db2176cab33c19']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3cbb2f0b87254ff696e9315cd863f613']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-bf66e0afea2c4c2da7bc69af14ca23c9']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-5212437eb6f045f98b072db0ce971409']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-d97b8cf6a26f408db148163485796d15']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-dd0c8f86b160428da3a82d2866a5b48d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-ee2b3bf53bd44b21960ff8575891c638']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-ef32205ede95407f981064a45ffa652c']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-06083bfba01d4213a852830000e939b9']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-06083bfba01d4213a852830000e12051']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-57efecfe17e64e20b83adccce3159a9e']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-83ea28a4218e447ebe99113901b2c70f']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-d006eb3550364c61bc10cac70763e677']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3ecb2fb750c144c58c1b68d2fa2a36ae']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-e6f93bd0dc934ab2af11bd46888c1233']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-6eae247688734e6da1dfdadb89c3e43a']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-0c482df00c2343cd99b1b5c1637a199d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-f074a322acff4705bbef417505f9bf11']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-a5cfd20385314a63afc1ffcf6357a08b']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-f16cdbda66d2441cac9e99615c99e70e']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-a3101ac1f4734f408d0b01e2ba182648']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-6e141689c22944a083798b9dbba8b47f']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-c62bf0f41c964349b874b8f397f673ec']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-bf1305892fcb4e2e9eccec5ae693d73d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-8ef2aeaecfa645088c4bf4b424905596']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-e57e1a2b7be44356a2256bc46f61ec36']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-23a9c386c4304400a8f1dd620b26035b']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-af8d69bed6454c0eb9cc494977993b03']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-6d24dd8e55414acc915b1a2c7e358552']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-7242ea809d804f9ab4e08766a6117710']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-89be97039be04d6f9cfda107d75926b4']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-6659e405ac8d43268d2a10d451eea261']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-641fdee1d3114629a15902311d895ca2']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3292eaa5630b48ecb7c4249b0ddc760e']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-d26b22f9103744edb0a4d3728aefc26e']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-e28a3e09aa6e42339e11a0c740362ca9']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-310d5bf351c143ed80c16ee9ef837271']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3ecb2fb750c144c58c1b68d2fa2a36ae']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-dd0c8f86b160428da3a82d2866a5b48d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-ee2b3bf53bd44b21960ff8575891c638']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-ef32205ede95407f981064a45ffa652c']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-06083bfba01d4213a852830000e939b9']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-06083bfba01d4213a852830000e12051']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-57efecfe17e64e20b83adccce3159a9e']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-83ea28a4218e447ebe99113901b2c70f']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-d006eb3550364c61bc10cac70763e677']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-5a2ed18041c04f3e995c773480b0076d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-af5bfda6510443a5a468bd1df713ff4c']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-86ce40c683224461a3d4747410c551c3']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-a83c14386c7547669c8c9a516ff4c54e']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-004a7da25119417ba44a1f43a2585d0d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3d47ac271e344e6094791b6e93fbce26']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-10ef9f34e0504a71880c967c82ac039f']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-9b8fed621a7148a5b7fb2f04b80381be']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-ec9821a975e84ad9804265b0f082a36b']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-5a2ed18041c04f3e995c7734386ae380']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3049eed90b924d699f1102b946843725']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-3bc40b222d994d9b9fb5d4a33319a146']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-52df32ab374440f0ac456a1abe66cb94']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-6712fbca652e49ac9739396377b090cb']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-0a7f3544ea954099aa06afe488417136']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-f074a322acff4705bbef417505f9bf11']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-a5cfd20385314a63afc1ffcf6357a08b']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-f16cdbda66d2441cac9e99615c99e70e']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-ff84535456d44233b6f0976d993b442d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-c62bf0f41c964349b874b8f397f673ec']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-e57e1a2b7be44356a2256bc46f61ec36']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-d614ed8726ff482c9c5a8183d735d9ed']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-6b81b03c96cc47f08ccb26838853012d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-23a9c386c4304400a8f1dd620b26035b']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-af8d69bed6454c0eb9cc494977993b03']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-89be97039be04d6f9cfda107d75926b5']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-42a03ed19450453da6c87fe8eadabfa4']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-d5cb8551edf041389893fee25a496395']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-6659e405ac8d43268d2a10d451eea261']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-641fdee1d3114629a15902311d895ca2']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-d2f721a297684b538e7dbb88cf5526bc']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-ff84535456d44233b6f0976d993b442d']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-0fb76e8565244977b1239327ca436f76']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-5646bf52a97b48adb50ded6ff8c38354']//xsd:attribute | 
				//xsd:complexType[@id='oagis-id-08d6ade226fd42488b53c0815664e246']//xsd:attribute 
			*/
	        
	        for(Map.Entry<String, ExpectedNewDataTypeSupplementaryComponent> entry: expectedDTSCs.entrySet()){
	        	String key = entry.getKey();
	        	ExpectedNewDataTypeSupplementaryComponent value = entry.getValue();
	        	boolean isDefaultBDT = false;
	        	DataTypeSupplementaryComponent dtsc = dataTypeSupplementaryComponentRepository.findOneByGuid(key);
	        	DataType baseDT = dataTypeRepository.findOne(dtsc.getOwnerDtId());
	        	if(baseDT.getDen().contains("_")){
	        		isDefaultBDT = true;
	        	}
	        	
	        	assertTrue(dtsc!=null);
	        	
	        	int dtscMinCard = dtsc.getMinCardinality();
	        	int dtscMaxCard = dtsc.getMaxCardinality();
	        	String dtscDefinition = dtsc.getDefinition();
	        	String dtscPropertyTerm = dtsc.getPropertyTerm();
	        	String dtscRepresentationTerm = dtsc.getRepresentationTerm();
	        	
	        	String expectedPropertyTerm = Utility.getPropertyTerm(value.attributeName);
	        	String expectedRepresentationTerm = Utility.getRepresentationTerm(value.attributeName);
	        	int expectedMinCard=-1;
	        	int expectedMaxCard=-1;
	        	
	        	if(value.use.equalsIgnoreCase("optional")){
	        		expectedMinCard = 0;
	        		expectedMaxCard = 1;
	        	}
	        	else if (value.use.equalsIgnoreCase("required")){
	        		expectedMinCard = 1;
	        		expectedMaxCard = 1;
	        	}
	        	else if (value.use.equalsIgnoreCase("prohibited")){
	        		expectedMinCard = 0;
	        		expectedMaxCard = 0;
	        	}
	        	else if(value.use==null || value.use.equals("")){
	        		expectedMinCard = 0;
	        		expectedMaxCard = 1;
	        	}
	        	assertTrue(expectedMinCard!=-1);
	        	assertTrue(expectedMaxCard!=-1);
        	
	        	if(isDefaultBDT){
	        		DataTypeSupplementaryComponent baseDTSC = dataTypeSupplementaryComponentRepository.findOne(dtsc.getBasedDtScId());
	        		expectedPropertyTerm = baseDTSC.getPropertyTerm();
	        		expectedRepresentationTerm = baseDTSC.getRepresentationTerm();
	        	}
	        	
	        	assertEquals(expectedPropertyTerm, dtscPropertyTerm);
	        	assertEquals(expectedRepresentationTerm, dtscRepresentationTerm);
	        	assertEquals(expectedMinCard, dtscMinCard);
	        	assertEquals(expectedMaxCard, dtscMaxCard);
	        }
	 }
	 
	 
	 
	 @Test
	 public void testPopulateDTSCsFromBase() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException{
		 XPathHandler fieldsXP = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
         XPathHandler bizDTXP = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);   
		 
	       List<DataTypeSupplementaryComponent> dtscList = dataTypeSupplementaryComponentRepository.findAll();
	       
	       for(int i=0; i<dtscList.size(); i++){
			   DataTypeSupplementaryComponent aDTSC= dtscList.get(i);
			   
	    	   if(aDTSC.getOwnerDtId() > 23){ // There are 23 CDT
	    		   boolean isDefaultBDTSC = false;
	    		   DataType ownerDT = dataTypeRepository.findOne(aDTSC.getOwnerDtId());
	    		   if(ownerDT.getBasedDtId() < 24) {
	    			   isDefaultBDTSC = true;
	    		   }
	    		   //check whether it is inherited or new(redefined)
	    		   Node exist = bizDTXP.getNode("//xsd:attribute[@id='"+aDTSC.getGuid()+"']");
	    		   if(exist==null){
	    			   exist=fieldsXP.getNode("//xsd:attribute[@id='"+aDTSC.getGuid()+"']");
	    		   }
	    		   
	    		   if(exist==null){// if it exists, we already check it in testPopulateNewDTSCsFromAttribute()
	    			   DataTypeSupplementaryComponent baseDTSC  = dataTypeSupplementaryComponentRepository.findOne(aDTSC.getBasedDtScId());
	    			   
	    			   assertTrue(baseDTSC!=null);
	    			   assertEquals(baseDTSC.getPropertyTerm(), aDTSC.getPropertyTerm());
	    			   assertEquals(baseDTSC.getRepresentationTerm(), aDTSC.getRepresentationTerm());
	    			   assertEquals(baseDTSC.getDefinition(), aDTSC.getDefinition());
	    			   
	    			   if(isDefaultBDTSC) {
		    			   assertEquals(0, aDTSC.getMinCardinality());
		    			   assertEquals(0, aDTSC.getMaxCardinality());
	    			   }
	    			   else {
	    				   assertEquals(baseDTSC.getMinCardinality(), aDTSC.getMinCardinality());
		    			   assertEquals(baseDTSC.getMaxCardinality(), aDTSC.getMaxCardinality());
	    			   }
	    		   }
	    	   }
	       }
 
	 }
	 
	 private class ExpectedInheritedCDTSCAllowedPrimitiveAndXpsTypeMap {
		 
		 private int cdtPriId;
		 private int xbtId;
		 private boolean isDefault;
		 
		 public ExpectedInheritedCDTSCAllowedPrimitiveAndXpsTypeMap(int cdtPriId, int xbtId, boolean isDefault){
			 this.xbtId = xbtId;
			 this.cdtPriId = cdtPriId;
			 this.isDefault= isDefault;
		 }
	 }
	
	 @Test
  	 public void testPopulateCDTSCAwdPriAndXPSTypeMap(){
  	   
		//In OAGIS 10.1, there is only one unqualified BDT that has an additional SC to its based default BDT – the NameType.		
		 DataTypeSupplementaryComponent dtsc = dataTypeSupplementaryComponentRepository.findOneByGuid("oagis-id-84fa20db74b942449e1885cff79b24df");
		 
		 assertTrue(dtsc!=null);
		 
		 List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtSCAwdPris = cdtSCAllowedPrimitiveRepository.findByCdtScId(dtsc.getDtScId());
		 assertTrue(cdtSCAwdPris.size()==4);
		 
		 for(int i=0; i<cdtSCAwdPris.size(); i++){
			 
			 int cdtPriId = cdtSCAwdPris.get(i).getCdtPriId();
			 boolean isDefault = cdtSCAwdPris.get(i).isDefault();
			 List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtSCAwdPriXpsTypeMaps = cdtSCAllowedPrimitiveExpressionTypeMapRepository.findByCdtScAwdPri(cdtSCAwdPris.get(i).getCdtScAwdPriId());
			 
			 if(cdtPriId==3){ //CDTPrimitive = Decimal
				 assertTrue(isDefault);
				 assertTrue(cdtSCAwdPriXpsTypeMaps.size()==1);
				 assertEquals(19, cdtSCAwdPriXpsTypeMaps.get(0).getXbtId());//xsd:decimal
			 }
			 
			 else if(cdtPriId==4) { //CDTPrimitive = Double
				 assertTrue(!isDefault);
				 assertTrue(cdtSCAwdPriXpsTypeMaps.size()==2);
				 assertEquals(23, cdtSCAwdPriXpsTypeMaps.get(0).getXbtId());//xsd:double
				 assertEquals(18, cdtSCAwdPriXpsTypeMaps.get(1).getXbtId());//xsd:float
			 }
			 else if (cdtPriId==5){//CDTPrimitive = Float
				 assertTrue(!isDefault);
				 assertTrue(cdtSCAwdPriXpsTypeMaps.size()==1);
				 assertEquals(18, cdtSCAwdPriXpsTypeMaps.get(0).getXbtId());//xsd:float
			 }
			 else if (cdtPriId ==6) { //CDTPrimitive = Integer
				 assertTrue(!isDefault);
				 assertTrue(cdtSCAwdPriXpsTypeMaps.size()==3);
				 assertEquals(20, cdtSCAwdPriXpsTypeMaps.get(0).getXbtId());//xsd:integer
				 assertEquals(22, cdtSCAwdPriXpsTypeMaps.get(1).getXbtId());//xsd:positiveInteger
				 assertEquals(21, cdtSCAwdPriXpsTypeMaps.get(2).getXbtId());//xsd:nonNegativeInteger

			 }
			 else {
				 assertTrue(false);
			 }
		 }
     }
}
