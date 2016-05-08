package org.oagi.srt.web.handler;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.ContextCategoryVO;

public class Handler {
	
	private DAOFactory df;
	private SRTDAO dao;
	
	private List<SRTObject> objects;
	
	public Handler(String daoName) {
		try {
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO(daoName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> completeInput(String query) {
		List<String> results = new ArrayList<String>();

		try {
			objects = dao.findObjects();
			for(SRTObject obj : objects) {
				ContextCategoryVO ccVO = (ContextCategoryVO)obj;
				if(ccVO.getName().contains(query)) {
					results.add(ccVO.getName());
				}
			}
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	public List<SRTObject> getObjects() {

		try {
			objects = dao.findObjects();
		} catch (SRTDAOException e) {
			e.printStackTrace();
		}

		return objects;
	}

}
