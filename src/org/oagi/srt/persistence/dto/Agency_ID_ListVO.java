package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class Agency_ID_ListVO extends SRTObject {

	private int AgencyIDListID;
	private String AgencyIDListGUID;
	private String EnumerationTypeGUID;
	private String Name;
	private String ListID;
	private int AgencyID;
	private String VersionID;
	private String Definition;


	public int getAgencyIDListID() {
		return AgencyIDListID;
	}
	
	public void setAgencyIDListID(int agencyIDListID) {
		AgencyIDListID = agencyIDListID;
	}
	
	public String getAgencyIDListGUID() {
		return AgencyIDListGUID;
	}
	
	public void setAgencyIDListGUID(String agencyIDListGUID) {
		AgencyIDListGUID = agencyIDListGUID;
	}
	
	public String getEnumerationTypeGUID() {
		return EnumerationTypeGUID;
	}
	
	public void setEnumerationTypeGUID(String enumerationTypeGUID) {
		EnumerationTypeGUID = enumerationTypeGUID;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
	public String getListID() {
		return ListID;
	}
	
	public void setListID(String listID) {
		ListID = listID;
	}
	
	public int getAgencyID() {
		return AgencyID;
	}
	
	public void setAgencyID(int agencyID) {
		AgencyID = agencyID;
	}
	
	public String getVersionID() {
		return VersionID;
	}
	
	public void setVersionID(String versionID) {
		VersionID = versionID;
	}
	
	public String getDefinition() {
		return Definition;
	}
	
	public void setDefinition(String definition) {
		Definition = definition;
	}

}
