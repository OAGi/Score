package org.oagi.srt.web.handler;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dto.UserVO;
import org.primefaces.context.RequestContext;

public class UIHandler {

	protected DAOFactory df;
	protected SRTDAO daoBC;
	protected SRTDAO daoBCV;
	protected SRTDAO daoCC;
	protected SRTDAO daoCS;
	protected SRTDAO daoCV;
	protected SRTDAO daoCL;
	protected SRTDAO daoCLV;
	protected SRTDAO daoUser;
	
	protected int userId;

	public UIHandler() {
		try {
			df = DAOFactory.getDAOFactory();
			daoBC = df.getDAO("BusinessContext");
			daoBCV = df.getDAO("BusinessContextValue");
			daoCC = df.getDAO("ContextCategory");
			daoCS = df.getDAO("ContextScheme");
			daoCV = df.getDAO("ContextSchemeValue");
			daoCL = df.getDAO("CodeList");
			daoCLV = df.getDAO("CodeListValue");
			daoUser = df.getDAO("User");
			
			QueryCondition qc = new QueryCondition();
			qc.add("name", "oagis");
			userId = ((UserVO)daoUser.findObject(qc)).getUserID();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }
}
