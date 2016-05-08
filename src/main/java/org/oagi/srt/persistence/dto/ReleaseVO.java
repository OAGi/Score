package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
 *
 * @version 1.0
 * @author Jaehun Lee
 */
public class ReleaseVO extends SRTObject {

	private int ReleaseID;
	private String ReleaseNum;
	private String ReleaseNote;
	private int NamespaceID;
	
	public int getReleaseID() {
		return ReleaseID;
	}
	
	public void setReleaseID(int releaseID) {
		ReleaseID = releaseID;
	}
	
	public String getReleaseNum() {
		return ReleaseNum;
	}
	
	public void setReleaseNum(String releaseNum) {
		ReleaseNum = releaseNum;
	}
	
	public String getReleaseNote() {
		return ReleaseNote;
	}
	
	public void setReleaseNote(String releaseNote) {
		ReleaseNote = releaseNote;
	}
	public int getNamespaceID() {
		return NamespaceID;
	}
	
	public void setNamespaceID(int namespaceID) {
		NamespaceID = namespaceID;
	}
}
