package org.oagi.srt.common;

import java.util.Vector;
import java.util.Iterator;
//import java.sql.PreparedStatement;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 */
public class QueryCondition {
	//public static final int STRING = 1;
	//public static final int INTEGER = 2;

	private Vector _fields;
	private Vector _values;
	private String _orderBy;
	private boolean _hasOrderByClause = false;
	//private Vector _types;
	private String _inField;
	private Vector _inValues;
	private boolean _hasInClause = false;

	private Vector _likeValues;
	private Vector _likeFields;

	private boolean _queryLargeData = false;

	public boolean isQueryLargeData() {
		return _queryLargeData;
	}

	public void setQueryLargeData(boolean query) {
		_queryLargeData = query;
	}


	public QueryCondition() {
		_fields = new Vector();
		_values = new Vector();
	}

	public void add(String field, Object value) {
		_fields.add(field);
		_values.add(value);
	}

	public String getField(int n) {
		return (String)_fields.get(n);
	}

	public Object getValue(int n) {
		return _values.get(n);
	}

	public String getStringValue(int n) {
		Object val = _values.get(n);
		if (val instanceof String) {
			return (String)val;
		}
		return val.toString();
	}

	public int getIntValue(int n) {
		Object val = _values.get(n);
		if (val instanceof Integer) {
			return ((Integer)val).intValue();
		}
		return -1;
	}

	public int getSize() {
		return _fields.size();
	}

	public void setOrderByClause(String s) {
		if (s == null || s.length() == 0) {
			_hasOrderByClause = false;
		} else {
			_orderBy = s;
			_hasOrderByClause = true;
		}
	}

	public String getOrderByClause() {
		if (_hasOrderByClause) {
			return _orderBy;
		} else {
			return "";
		}
	}

	public void setInClause(String field, Vector values) {
		_inField = field;
		_inValues = values;
		_hasInClause = true;
	}

	public boolean hasInClause() {
		return _hasInClause;
	}

	public boolean hasOrderByClause() {
		return _hasOrderByClause;
	}

	public String getInClause() {
		String ret = _inField + " IN (";
		Iterator i = _inValues.iterator();
		while (i.hasNext()) {
			String cur = (String)i.next();
			ret += "'" + cur + "',";
		}
		ret = ret.substring(0, ret.length() - 1);
		ret += ")";
		return ret;
	}

	public void addLikeClause(String field, String s) {
		if (_likeFields == null) {
			_likeFields = new Vector();
			_likeValues = new Vector();
		}
		_likeValues.add(s);
		_likeFields.add(field);
	}

	public String getLikeField(int n) {
		return (String)_likeFields.get(n);
	}

	public String getLikeValue(int n) {
		Object val = _likeValues.get(n);
		if (val instanceof String) {
			return (String)val;
		}
		return val.toString();
	}

	public int getLikeSize() {
		if (_likeFields != null) {
			return _likeFields.size();
		}
		return 0;
	}

	public Vector getInValues() {
		return _inValues;
	}

	public String getInField() {
		return _inField;
	}
	/*
	public void setParameters(PreparedStatement ps) {
		int size = _fields.size();
		if (size == 0) {
			return;
		}
		for (int n = 1; n <= size; n++) {
			ps.setString(n,
		}
	}
	*/
}
