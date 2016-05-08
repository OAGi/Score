package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class CodeValueVO extends SRTObject {

	private int CodeValueID;
	private int OwnerCodeListID;
	private String Value;
	private String Definition;
	private boolean UsedIndicator;
	private boolean LockedIndicator;
	
	public int getCodeValueID() {
		return CodeValueID;
	}
	
	public void setCodeValueID(int codeValueID) {
		CodeValueID = codeValueID;
	}
	
	public int getOwnerCodeListID() {
		return OwnerCodeListID;
	}
	
	public void setOwnerCodeListID(int ownerCodeListID) {
		OwnerCodeListID = ownerCodeListID;
	}
	
	public String getValue() {
		return Value;
	}
	
	public void setValue(String value) {
		Value = value;
	}
	
	public String getDefinition() {
		return Definition;
	}
	
	public void setDefinition(String definition) {
		Definition = definition;
	}
	
	public boolean getUsedIndicator() {
		return UsedIndicator;
	}
	
	public void setUsedIndicator(boolean usedIndicator) {
		UsedIndicator = usedIndicator;
	}
	
	public boolean getLockedIndicator() {
		return LockedIndicator;
	}
	
	public void setLockedIndicator(boolean lockedIndicator) {
		LockedIndicator = lockedIndicator;
	}
	
	
}
