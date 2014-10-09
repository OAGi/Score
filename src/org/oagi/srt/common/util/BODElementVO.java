package org.oagi.srt.common.util;

import org.apache.xerces.impl.xs.XSElementDecl;

public class BODElementVO {

	private String name;
	private String id;
	private int maxOccur;
	private int minOccur;
	private int order;
	private String typeName;
	private XSElementDecl element;
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public XSElementDecl getElement() {
		return element;
	}

	public void setElement(XSElementDecl element) {
		this.element = element;
	}

	public String getTypeName() {
		return typeName;
	}
	
	public void setTypeName(String name) {
		typeName = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public int getMaxOccur() {
		return maxOccur;
	}
	
	public void setMaxOccur(int maxOccur) {
		this.maxOccur = maxOccur;
	}
	
	public int getMinOccur() {
		return minOccur;
	}
	
	public void setMinOccur(int minOccur) {
		this.minOccur = minOccur;
	}
}
