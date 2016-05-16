package org.oagi.srt.common.util;

import org.apache.xerces.impl.xs.XSElementDecl;

/**
 * @author yslee
 *
 */
public class BODElementVO {

	private String name;
	private String id;
	private String ref;
	private int maxOccur;
	private int minOccur;
	private int order;
	private String typeName;
	private XSElementDecl element;
	private boolean isGroup;
	private String groupId;
	private String groupRef;
	private String groupParent;
	private String groupName;
	
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

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getGroupRef() {
		return groupRef;
	}

	public void setGroupRef(String groupRef) {
		this.groupRef = groupRef;
	}
	
	public String getGroupParent() {
		return groupParent;
	}

	public void setGroupParentf(String groupParent) {
		this.groupParent = groupParent;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupNamef(String groupName) {
		this.groupName = groupName;
	}
}
