package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class BusinessContextValueVO extends SRTObject {

	private int BusinessContextValueID;
	private int BusinessContextID;
	private int ContextSchemeValueID;
	
	public int getBusinessContextValueID() {
		return BusinessContextValueID;
	}
	public void setBusinessContextValueID(int businessContextValueID) {
		BusinessContextValueID = businessContextValueID;
	}
	public int getBusinessContextID() {
		return BusinessContextID;
	}
	public void setBusinessContextID(int businessContextID) {
		BusinessContextID = businessContextID;
	}
	public int getContextSchemeValueID() {
		return ContextSchemeValueID;
	}
	public void setContextSchemeValueID(int contextSchemeValueID) {
		ContextSchemeValueID = contextSchemeValueID;
	}

}
