package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;


/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class ClientVO extends SRTObject {
	
	private int Client_id;
	private String Name;
	
	public int getClient_id() {
		return Client_id;
	}
	
	public void setClient_id(int client_id) {
		Client_id = client_id;
	}
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
}
