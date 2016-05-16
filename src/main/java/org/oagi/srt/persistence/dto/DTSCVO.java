package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class DTSCVO extends SRTObject {

	private int DTSCID;
	private String DTSCGUID;
	private String PropertyTerm;
	private String RepresentationTerm;
	private String Definition;
	private int OwnerDTID;
	private int MinCardinality;
	private int MaxCardinality;
	private int BasedDTSCID ;
	
	public int getDTSCID() {
		return DTSCID;
	}
	
	public void setDTSCID(int dTSCID) {
		DTSCID = dTSCID;
	}
	
	public String getDTSCGUID() {
		return DTSCGUID;
	}
	
	public void setDTSCGUID(String dTSCGUID) {
		DTSCGUID = dTSCGUID;
	}
	
	public String getPropertyTerm() {
		return PropertyTerm;
	}
	
	public void setPropertyTerm(String propertyTerm) {
		PropertyTerm = propertyTerm;
	}
	
	public String getRepresentationTerm() {
		return RepresentationTerm;
	}
	
	public void setRepresentationTerm(String representationTerm) {
		RepresentationTerm = representationTerm;
	}
	
	public String getDefinition() {
		return Definition;
	}
	
	public void setDefinition(String definition) {
		Definition = definition;
	}
	
	public int getOwnerDTID() {
		return OwnerDTID;
	}
	
	public void setOwnerDTID(int ownerDTID) {
		OwnerDTID = ownerDTID;
	}
	
	public int getMinCardinality() {
		return MinCardinality;
	}
	
	public void setMinCardinality(int minCardinality) {
		MinCardinality = minCardinality;
	}
	
	public int getMaxCardinality() {
		return MaxCardinality;
	}
	
	public void setMaxCardinality(int maxCardinality) {
		MaxCardinality = maxCardinality;
	}
	
	public int getBasedDTSCID() {
		return BasedDTSCID;
	}
	
	public void setBasedDTSCID(int basedDTSCID) {
		BasedDTSCID = basedDTSCID;
	}
	
}
