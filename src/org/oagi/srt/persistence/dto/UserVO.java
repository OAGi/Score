package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Nasif Sikder
 */
public class UserVO extends SRTObject {

	private int UserID;
	private String UserName;
	private String Password;
	private String Name;
	private String Organization;
	private boolean Oagis_developer_indicator;
	
	public int getUserID() {
		return UserID;
	}
	public void setUserID(int userID) {
		UserID = userID;
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public String getPassword() {
		return Password;
	}
	public void setPassword(String password) {
		Password = password;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getOrganization() {
		return Organization;
	}
	public void setOrganization(String organization) {
		Organization = organization;
	}
	
	public boolean getOagis_developer_indicator() {
		return Oagis_developer_indicator;
	}
	public void setOagis_developer_indicator(boolean oagis_developer_indicator) {
		Oagis_developer_indicator = oagis_developer_indicator;
	}

}
