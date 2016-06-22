package org.oagi.srt.persistence.populate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImportApplication.class)
public class P_1_8_PopulateAccAsccpBccAsccTestCase extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private DataTypeRepository dtRepository;
	@Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;
	@Autowired
	private BasicCoreComponentPropertyRepository bccpRepository;
	@Autowired
	private AssociationCoreComponentPropertyRepository asccpRepository;
	@Autowired
	private AggregateCoreComponentRepository accRepository;
	@Autowired
	private AssociationCoreComponentRepository asccRepository;
	@Autowired
	private BasicCoreComponentRepository bccRepository;
	@Test
	public void test_ID_string_included(){
		
		//From dt table
		List<DataType> dtList = dtRepository.findAll();
		for(int i=0; i<dtList.size(); i++){
			DataType aDT=dtList.get(i);
			if(aDT.getDen().contains("ID")){
				System.out.println("DT: "+aDT.getDtId());
				assertTrue(false);
			}
		}
		
		//From dt_sc table
		List<DataTypeSupplementaryComponent> dtscList = dtScRepository.findAll();
		for(int i=0; i<dtscList.size(); i++){
			DataTypeSupplementaryComponent aDTSC = dtscList.get(i);
			if(aDTSC.getPropertyTerm().contains("ID")
			|| aDTSC.getRepresentationTerm().contains("ID")){
				System.out.println("DTSC: "+aDTSC.getDtScId());
				assertTrue(false);
			}
		}
		
		//From bccp table
		List<BasicCoreComponentProperty> bccpList = bccpRepository.findAll();
		for(int i=0; i<bccpList.size(); i++){
			BasicCoreComponentProperty aBCCP = bccpList.get(i);
			if(aBCCP.getPropertyTerm().contains("ID")
			|| aBCCP.getRepresentationTerm().contains("ID")
			|| aBCCP.getDen().contains("ID")){
				System.out.println("BCCP: "+aBCCP.getBccpId());
				assertTrue(false);
			}
		}
		
		//From asccp table
		List<AssociationCoreComponentProperty> asccpList = asccpRepository.findAll();
		for(int i=0; i<asccpList.size(); i++){
			AssociationCoreComponentProperty anASCCP = asccpList.get(i);
			if(anASCCP.getPropertyTerm().contains("ID")
			|| anASCCP.getDen().contains("ID")){
				System.out.println("ASCCP: "+anASCCP.getAsccpId());
				assertTrue(false);
			}
		}
		
		//From acc table
		List<AggregateCoreComponent> accList = accRepository.findAll();
		for(int i=0; i<accList.size(); i++){
			AggregateCoreComponent anACC = accList.get(i);
			if(anACC.getObjectClassTerm().contains("ID")
			|| anACC.getDen().contains("ID")){
				System.out.println("ACC: "+anACC.getAccId());
				assertTrue(false);
			}
		}
		
		//From ascc table
		List<AssociationCoreComponent> asccList = asccRepository.findAll();
		for(int i=0; i<asccList.size(); i++){
			AssociationCoreComponent anASCC = asccList.get(i);
			if(anASCC.getDen().contains("ID")){
				System.out.println("ASCC: "+anASCC.getAsccId());
				assertTrue(false);
			}
		}
		
		//From bcc table
		List<BasicCoreComponent> bccList = bccRepository.findAll();
		for(int i=0; i<bccList.size(); i++){
			BasicCoreComponent aBCC = bccList.get(i);
			if(aBCC.getDen().contains("ID")){
				System.out.println("BCC: "+aBCC.getBccId());
				assertTrue(false);
			}
		}
	}
}
