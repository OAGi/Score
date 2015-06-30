package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class CodeListValueVO extends SRTObject {

	private int CodeListValueID;
	private int OwnerCodeListID;
	private String Value;
	private String Name;
	private String Definition;
	private String DefinitionSource;
	private boolean UsedIndicator;
	private boolean LockedIndicator;
	private boolean extensionIndicator;
	private String color;
	private boolean disabled = false;
	
	public boolean isDisabled() {
		if(color != null && (color.equals("red") || color.equals("green")))
			return true;
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public boolean isExtensionIndicator() {
		return extensionIndicator;
	}

	public void setExtensionIndicator(boolean extensionIndicator) {
		this.extensionIndicator = extensionIndicator;
	}

	public int getCodeListValueID() {
		return CodeListValueID;
	}
	
	public void setCodeListValueID(int codeListValueID) {
		CodeListValueID = codeListValueID;
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
	
	public String getName(){
		return Name;
	}
	
	public void setName(String name){
		Name = name;
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
	
	public String getDefinitionSource(){
		return DefinitionSource;
	}
	
	public void setDefinitionSource(String definitionSource){
		DefinitionSource = definitionSource;
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
