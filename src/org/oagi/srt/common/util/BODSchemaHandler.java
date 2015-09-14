package org.oagi.srt.common.util;

import java.io.File;
import java.util.ArrayList;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.oagi.srt.common.SRTConstants;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

public class BODSchemaHandler {

	private static XSModel model;
	private String rootName;
	private Document doc;

	public BODSchemaHandler(String schemaPath) {
		try {
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
			XSLoader schemaLoader = impl.createXSLoader(null);
			//DOMConfiguration config = schemaLoader.getConfig();
			//config.setParameter("validate", Boolean.FALSE);
			model = schemaLoader.loadURI(schemaPath);
			rootName = schemaPath.substring(schemaPath.lastIndexOf(File.separator) + 1, schemaPath.lastIndexOf("."));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}

	}

	public XSElementDecl getGlobalElementDeclaration() {
		return (XSElementDecl)model.getElementDeclaration(rootName, SRTConstants.OAGI_NS);
	}

	public XSComplexTypeDecl getComplexTypeDefinition(XSElementDecl xsed) {
		return (XSComplexTypeDecl)xsed.getTypeDefinition(); 
	}

	public XSComplexTypeDecl getComplexTypeDefinition(String type) {
		return (XSComplexTypeDecl)model.getTypeDefinition(type, SRTConstants.OAGI_NS);
	}
	
	public boolean isComplexWithoutSimpleContent(String type) {
		if(model.getTypeDefinition(type, SRTConstants.OAGI_NS) instanceof XSComplexTypeDecl) {
			if(((XSComplexTypeDecl)model.getTypeDefinition(type, SRTConstants.OAGI_NS)).getSimpleType() == null) {
				return true;
			} else {
				return ((XSComplexTypeDecl)model.getTypeDefinition(type, SRTConstants.OAGI_NS)).isComplexContent();
			}
		} else {
			return false;
		}
	}

	public ArrayList<BODElementVO> processParticle(XSParticle theXSParticle, int order) {
		ArrayList<BODElementVO> al = new ArrayList<BODElementVO>();
		XSTerm xsTerm = theXSParticle.getTerm();
		switch (xsTerm.getType()) {
		
		case XSConstants.ELEMENT_DECLARATION:
			BODElementVO bodVO = new BODElementVO();
			bodVO.setMaxOccur(theXSParticle.getMaxOccurs());
			bodVO.setMinOccur(theXSParticle.getMinOccurs());
			bodVO.setName(xsTerm.getName());
			bodVO.setOrder(order);
			
			XSElementDecl e = (XSElementDecl)xsTerm;
			bodVO.setTypeName(e.getTypeDefinition().getName());
			bodVO.setId(e.getFId());
			bodVO.setElement(e);
			bodVO.setRef((theXSParticle.getFRef() != null) ? theXSParticle.getFId() : null);
			bodVO.setGroup(theXSParticle.isGroup());
			bodVO.setGroupId(theXSParticle.getFGroupId());
			bodVO.setGroupRef(theXSParticle.getFGroupRef());
			bodVO.setGroupParentf(theXSParticle.getFGroupParent());
			bodVO.setGroupNamef(theXSParticle.getFGroupName());
			
			al.add(bodVO);
			
			return al;

		case XSConstants.MODEL_GROUP:
			XSModelGroup xsGroup = (XSModelGroup) xsTerm;
			XSObjectList xsParticleList = xsGroup.getParticles();
			for (int i = 0; i < xsParticleList.getLength(); i ++) {
				ArrayList<BODElementVO> al2 = processParticle((XSParticleDecl)xsParticleList.item(i), i + 1);
				if(al2 != null && al2.size() > 0)
					al.addAll(al2);
			}
			return al;
		default:
			System.out.println("### default: " + xsTerm);
		}
		return null;
	}

	public String getAnnotation(XSElementDecl element) {
		XSAnnotation anno = element.getAnnotation();
		if(anno != null) {
			String annoStr = anno.getAnnotationString();
			return annoSubString(annoStr);
		}
		return null;
	}
	
	public String getAnnotation(XSComplexTypeDecl complexType) {
		XSAnnotation anno = (XSAnnotation)complexType.getAnnotations().item(0);
		if(anno != null) {
			String annoStr = anno.getAnnotationString();
			return annoSubString(annoStr);
		}
		return null;
	}
	
	private String annoSubString(String str) {
		String s = str.substring(str.indexOf(">") + 1, str.lastIndexOf("<"));
		return s.substring(s.indexOf(">") + 1, s.lastIndexOf("<"));
	}
	
	public static void main(String args[]) throws Exception {

		try {
			File f = new File(SRTConstants.BOD_FILE_PATH_02 + "AcknowledgeAllocateResource.xsd");
			BODSchemaHandler bs = new BODSchemaHandler(f.getAbsolutePath());
			//System.out.println(bs.getAnnotation(bs.getGlobalElementDeclaration()));
			//System.out.println(bs.getComplexTypeDefinition(bs.getGlobalElementDeclaration()).getBaseType().getName() + " - " + bs.getComplexTypeDefinition(bs.getGlobalElementDeclaration()).getFId());

			XSComplexTypeDecl x = bs.getComplexTypeDefinition("DummyType");
			//System.out.println("###11 " + bs.isComplexWithoutSimpleContent("AcknowledgeAllocateResourceType"));
			ArrayList<BODElementVO> al = bs.processParticle(x.getParticle(), 1);
			for(BODElementVO e : al) {
				System.out.println("### type: " + e.getOrder() + " | name: " + e.getName() + " | id: " + e.getId() + " | ref: " + e.getRef() + " | group?: " + e.isGroup() + " | groupid: " + e.getGroupId() + " | groupref: " + e.getGroupRef() + " | grouparent: " + e.getGroupParent());
				//System.out.println("### " + e.getMaxOccur());
				//System.out.println("### " + e.getMinOccur());
				//System.out.println("### " + e.getId());
			}
			
//
//			System.out.println(bs.isComplexWithoutSimpleContent("AcknowledgeFieldDataAreaType"));
//			System.out.println(bs.getComplexTypeDefinition(bs.getGlobalElementDeclaration()).getSimpleType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//        	System.out.println("###" + ((XSComplexTypeDefinition)model.getElementDeclaration("AcknowledgeField", "http://www.openapplications.org/oagis/10").getTypeDefinition()).getAttributeUses().size());
		//        	System.out.println("###" + ((XSComplexTypeDefinition)model.getTypeDefinition("AcknowledgeFieldDataAreaType", "http://www.openapplications.org/oagis/10")));
		//
		//        	XSObjectList xo = ((XSComplexTypeDefinition)model.getElementDeclaration("AcknowledgeField", "http://www.openapplications.org/oagis/10").getTypeDefinition()).getAttributeUses();
		//        	
		//        	System.out.println(((XSAttributeUse)xo.item(0)).getAttrDeclaration().getName());
		//        	System.out.println(((XSAttributeUse)xo.item(1)).getAttrDeclaration().getName());
		//        	System.out.println(((XSAttributeUse)xo.item(2)).getAttrDeclaration().getName());
		//        	System.out.println(((XSAttributeUse)xo.item(3)).getAttrDeclaration().getName());


		// element declarations
		//            XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
		//            if (map.getLength() != 0) {
		//				System.out.println("*************************************************");
		//				System.out.println("* Global element declarations: {namespace} name ");
		//				System.out.println("*************************************************");
		//                for (int i = 0; i < map.getLength(); i++) {
		//                    XSObject item = map.item(i);
		//                    
		//                    if(item.getName().equals("AcknowledgeField"))
		//                    	System.out.println("{" + item.getNamespace() + "}" + item.getName() + " " + item.getType());
		//                }
		//            }
		/*
            // attribute declarations
            map = model.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
            if (map.getLength() != 0) {
				System.out.println("*************************************************");
                System.out.println("* Global attribute declarations: {namespace} name");
				System.out.println("*************************************************");
                for (int i = 0; i < map.getLength(); i++) {
                    XSObject item = map.item(i);
                    System.out.println("{" + item.getNamespace() + "}" + item.getName());
                }
            }
			// notation declarations
			map = model.getComponents(XSConstants.TYPE_DEFINITION);
			if (map.getLength() != 0) {
				System.out.println("*************************************************");
				System.out.println("* Global type declarations: {namespace} name");
				System.out.println("*************************************************");
				for (int i = 0; i < map.getLength(); i++) {
					XSObject item = map.item(i);
					System.out.println("{" + item.getNamespace() + "}" + item.getName());
				}
			}

			// notation declarations
			map = model.getComponents(XSConstants.NOTATION_DECLARATION);
			if (map.getLength() != 0) {
				System.out.println("*************************************************");
				System.out.println("* Global notation declarations: {namespace} name");
				System.out.println("*************************************************");
				for (int i = 0; i < map.getLength(); i++) {
					XSObject item = map.item(i);
					System.out.println("{" + item.getNamespace() + "}" + item.getName());
				}
			}
		 */

	}

}
