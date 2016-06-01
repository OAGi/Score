package org.oagi.srt.common.util;

import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.xs.*;
import org.oagi.srt.common.SRTConstants;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BODSchemaHandler {

    private XSModel model;
    private String rootName;
    private Document doc;

    public BODSchemaHandler(String schemaPath) throws Exception {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
        XSLoader schemaLoader = impl.createXSLoader(null);
        //DOMConfiguration config = schemaLoader.getConfig();
        //config.setParameter("validate", Boolean.FALSE);
        model = schemaLoader.loadURI(schemaPath);
        rootName = schemaPath.substring(schemaPath.lastIndexOf(File.separator) + 1, schemaPath.lastIndexOf("."));
    }

    public XSElementDecl getGlobalElementDeclaration() {
        return (XSElementDecl) model.getElementDeclaration(rootName, SRTConstants.OAGI_NS);
    }

    public XSComplexTypeDecl getComplexTypeDefinition(XSElementDecl xsed) {
        return (XSComplexTypeDecl) xsed.getTypeDefinition();
    }

    public XSComplexTypeDecl getComplexTypeDefinition(String type) {
        return (XSComplexTypeDecl) model.getTypeDefinition(type, SRTConstants.OAGI_NS);
    }

    public boolean isComplexWithoutSimpleContent(String type) {
        if (model.getTypeDefinition(type, SRTConstants.OAGI_NS) instanceof XSComplexTypeDecl) {
            if (((XSComplexTypeDecl) model.getTypeDefinition(type, SRTConstants.OAGI_NS)).getSimpleType() == null) {
                return true;
            } else {
                return ((XSComplexTypeDecl) model.getTypeDefinition(type, SRTConstants.OAGI_NS)).isComplexContent();
            }
        } else {
            return false;
        }
    }

    public boolean isComplexWithSimpleContent(String type) {
        if (model.getTypeDefinition(type, SRTConstants.OAGI_NS) instanceof XSComplexTypeDecl) {
            if (((XSComplexTypeDecl) model.getTypeDefinition(type, SRTConstants.OAGI_NS)).getSimpleType() != null)
                return true;
            else
                return false;
        } else {
            return true;
        }
    }

    public List<BODElementVO> processParticle(XSParticle theXSParticle, int order) {
        List<BODElementVO> al = new ArrayList<BODElementVO>();
        XSTerm xsTerm = theXSParticle.getTerm();
        switch (xsTerm.getType()) {

            case XSConstants.ELEMENT_DECLARATION:
                BODElementVO bodVO = new BODElementVO();
                bodVO.setMaxOccur(theXSParticle.getMaxOccurs());
                bodVO.setMinOccur(theXSParticle.getMinOccurs());
                bodVO.setName(xsTerm.getName());
                bodVO.setOrder(order);

                XSElementDecl e = (XSElementDecl) xsTerm;
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
                for (int i = 0; i < xsParticleList.getLength(); i++) {
                    List<BODElementVO> al2 = processParticle((XSParticleDecl) xsParticleList.item(i), i + 1);
                    if (al2 != null && al2.size() > 0)
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
        if (anno != null) {
            String annoStr = anno.getAnnotationString();
            return annoSubString(annoStr);
        }
        return null;
    }

    public String getAnnotation(XSComplexTypeDecl complexType) {
        XSAnnotation anno = (XSAnnotation) complexType.getAnnotations().item(0);
        if (anno != null) {
            String annoStr = anno.getAnnotationString();
            return annoSubString(annoStr);
        }
        return null;
    }

    private String annoSubString(String str) {
        String s = str.substring(str.indexOf(">") + 1, str.lastIndexOf("<"));
        return s.substring(s.indexOf(">") + 1, s.lastIndexOf("<"));
    }

}
