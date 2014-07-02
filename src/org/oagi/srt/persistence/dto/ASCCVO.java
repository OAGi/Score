package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Nasif Sikder
*/
public class ASCCVO extends SRTObject {
	private int ASCCID;
	private int CardinalityMin;
	private int CardinalityMax;
	private int SequencingKey;
	private int AssocFromACCID;
	private int AssocToASCCPID;
	private String Definition;
	
	public int getASCCID(){
		return ASCCID;
	}
	
	public void setASCCID(int aSCCID){
		ASCCID = aSCCID;
	}
	
	public int getCardinalityMin(){
		return CardinalityMin;
	}
	
	public void setCardinalityMin(int cardinalityMin){
		CardinalityMin = cardinalityMin;
	}
	
	public int getCardinalityMax(){
		return CardinalityMax;
	}
	
	public void setCardinalityMax(int cardinalityMax){
		CardinalityMax = cardinalityMax;
	}
	
	public int getSequencingKey(){
		return SequencingKey;
	}
	
	public void setSequencingKey(int sequencingKey){
		SequencingKey = sequencingKey;
	}

	public int getAssocFromACCID(){
		return AssocFromACCID;
	}
	
	public void setAssocFromACCID(int assocFromACCID){
		AssocFromACCID = assocFromACCID;
	}
	
	public int getAssocToASCCPID(){
		return AssocToASCCPID;
	}
	
	public void setAssocToASCCPID(int assocToASCCPID){
		AssocToASCCPID = assocToASCCPID;
	}
	
	public String getDefinition(){
		return Definition;
	}
	
	public void setDefinition(String definition){
		Definition = definition;
	}
}
