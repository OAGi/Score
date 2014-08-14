package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class Agency_ID_List_ValueVO extends SRTObject {

	private int AgencyIDListValueID;
	private String Value;
	private String Name;
	private String Definition;
	private int OwnerAgencyIDListID;

	public int getAgencyIDListValueID() {
		return AgencyIDListValueID;
	}
	
	public void setAgencyIDListValueID(int agencyIDListValueID) {
		AgencyIDListValueID = agencyIDListValueID;
	}
	
	public String getValue() {
		return Value;
	}
	
	public void setValue(String value) {
		Value = value;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
	public String getDefinition() {
		return Definition;
	}
	
	public void setDefinition(String definition) {
		Definition = definition;
	}
	
	public int getOwnerAgencyIDListID() {
		return OwnerAgencyIDListID;
	}
	
	public void setOwnerAgencyIDListID(int ownerAgencyIDListID) {
		OwnerAgencyIDListID = ownerAgencyIDListID;
	}
	
}
