package org.oagi.srt.common;

import java.sql.Timestamp;
import java.util.HashMap;

import org.chanchan.common.system.BfObject;

/**
 * @author Yunsu Lee
 * @version 1.1
 */
public class SRTObject extends BfObject {
	protected String _createdBy = "";
	protected Timestamp _createdOn;
	protected String _modifiedBy = "";
	protected Timestamp _modifiedOn;

	private HashMap _extension;

	public SRTObject() {
		super();
	}

	public SRTObject(String obid) {
		super(obid);
	}

	public String getCreatedBy() {
		return _createdBy;
	}

	public void setCreatedBy(String creator) {
		_createdBy = creator;
	}

	public Timestamp getCreatedOn() {
		return _createdOn;
	}

	public void setCreatedOn(Timestamp time) {
		_createdOn = time;
	}

	public String getModifiedBy() {
		return _modifiedBy;
	}

	public void setModifiedBy(String modifier) {
		_modifiedBy = modifier;
	}

	public Timestamp getModifiedOn() {
		return _modifiedOn;
	}

	public void setModifiedOn(Timestamp time) {
		_modifiedOn = time;
	}

	public void putExtension(String key, Object value) {
		if (_extension == null) {
			_extension = new HashMap();
		}
		_extension.put(key, value);
	}

	public Object getExtension(String key) {
		if (_extension == null) {
			_extension = new HashMap();
		}
		return _extension.get(key);
	}

	public HashMap getExtensions() {
		if (_extension == null) {
			_extension = new HashMap();
		}
		return _extension;
	}

	public void setExtensions(HashMap extension) {
		if (extension == null) {
			return;
		}
		_extension = extension;
	}


	public Object removeExtension(String key) {
		if (_extension == null) {
			_extension = new HashMap();
		}
		return _extension.remove(key);
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		/*
		ret.append("\n[BfObject Dump Start]--------------------------------\n");
		Class c = getClass();
		System.out.println("MxsObject c = " + c.getName());
		Field[] fields = c.getDeclaredFields();
		System.out.println("len fields = " + fields.length);
		for (int n = 0; n < fields.length; n++) {
			ret.append(fields[n].getName());
			ret.append(" (");
			ret.append(fields[n].getType().getName());
			ret.append(") = [");
			ret.append(fields[n].get(this));
			ret.append("]\n");
		}
		*/
		ret.append("\n_obid = [" + _obid);
		ret.append("]\n_createdBy = [" + _createdBy);
		ret.append("]\n_createdOn = [" + _createdOn);
		ret.append("]\n_modifiedBy = [" + _modifiedBy);
		ret.append("]\n_modifiedOn = [" + _modifiedOn);
		ret.append("]\n");
		return ret.toString();
	}
}
