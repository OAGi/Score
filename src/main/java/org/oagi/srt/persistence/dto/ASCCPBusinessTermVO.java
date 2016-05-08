package org.oagi.srt.persistence.dto;

import java.sql.Timestamp;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class ASCCPBusinessTermVO extends SRTObject {

	private int IdASCCPBusinessTerm;
	
	public int getidASCCPBusinessTerm(){
		return IdASCCPBusinessTerm;
	}
	
	public void setidASCCPBusinessTerm(int idASCCPBusinessTerm){
		IdASCCPBusinessTerm = idASCCPBusinessTerm;
	}
		
}