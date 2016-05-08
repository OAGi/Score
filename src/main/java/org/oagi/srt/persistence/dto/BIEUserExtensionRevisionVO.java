package org.oagi.srt.persistence.dto;

import org.oagi.srt.common.SRTObject;

/**
*
* @version 1.0
* @author Jaehun Lee
*/

public class BIEUserExtensionRevisionVO extends SRTObject {

	private int Bie_user_ext_revision_id;
	private int Top_level_abie_id;
	private int Ext_abie_id;
	private int Ext_acc_id;
	private int User_ext_acc_id;
	private boolean Revised_indicator;
	
	public int getBie_user_ext_revision_id() {
		return Bie_user_ext_revision_id;
	}

	public void setBie_user_ext_revision_id(int bie_user_ext_revision_id) {
		Bie_user_ext_revision_id = bie_user_ext_revision_id;
	}

	public int getTop_level_abie_id() {
		return Top_level_abie_id;
	}

	public void setTop_level_abie_id(int top_level_abie_id) {
		Top_level_abie_id = top_level_abie_id;
	}

	public int getExt_abie_id() {
		return Ext_abie_id;
	}
	
	public void setExt_abie_id(int ext_abie_id) {
		Ext_abie_id = ext_abie_id;
	}
	
	public int getExt_acc_id() {
		return Ext_acc_id;
	}
	
	public void setExt_acc_id (int ext_acc_id) {
		Ext_acc_id = ext_acc_id;
	}

	public int getUser_ext_acc_id() {
		return User_ext_acc_id;
	}
	
	public void setUser_ext_acc_id(int user_ext_acc_id) {
		User_ext_acc_id = user_ext_acc_id;
	}
	
	public boolean getRevised_indicator() {
		return Revised_indicator;
	}
	
	public void setRevised_indicator(boolean revised_indicator) {
		Revised_indicator = revised_indicator;
	}

		
}