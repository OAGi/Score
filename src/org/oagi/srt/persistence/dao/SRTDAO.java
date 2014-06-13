package org.oagi.srt.persistence.dao;

import java.util.ArrayList;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 *
 */
public abstract class SRTDAO {

	public abstract boolean insertObject(SRTObject obj) throws SRTDAOException;
	
	public abstract SRTObject findObject(QueryCondition qc)	throws SRTDAOException;

	public abstract ArrayList<?> findObjects() throws SRTDAOException;
	
	public abstract boolean updateObject(SRTObject obj) throws SRTDAOException;

	public abstract boolean deleteObject(SRTObject obj) throws SRTDAOException;

}