package org.oagi.srt.crossvalidate;

/**
 * Created by tnk11 on 7/29/2016.
 */

import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;
import org.apache.xerces.xs.XSModel;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ValidateXML {

    public static void generateXML(String xsdfilePath, String xsdfilename, String xmlfilePath, String xmlfilename, String rootElementname, String prefix) throws Exception, FileNotFoundException {
        String oagis = "http://www.openapplications.org/oagis/10";
        XSModel xsModel = new XSParser().parse(xsdfilePath + "\\" + xsdfilename);
        XMLDocument sampleXML = new XMLDocument(new StreamResult(new FileOutputStream(xmlfilePath + "\\" + xmlfilename)), false, 4, null);
        XSInstance xsInstance = new XSInstance();
        QName rootElement = new QName(oagis, rootElementname, prefix);
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumElementsGenerated = 1;
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumListItemsGenerated = 1;
        xsInstance.generateAllChoices = true;
        xsInstance.generateOptionalElements = true;
        xsInstance.generateDefaultAttributes = true;
        xsInstance.generateOptionalAttributes = false;

        xsInstance.showContentModel = false;
        String schemalocation = oagis + " " + xsdfilename;
        xsInstance.generate(xsModel, rootElement, sampleXML, schemalocation, null);

        System.out.println(xmlfilename + " is generated from " + xsdfilename);
    }

    public static String replace_any(String filePath, String xsdfilename) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = f.newDocumentBuilder();
        File XSD = new File(filePath + "\\" + xsdfilename);
        Document doc = db.parse(XSD);
        NodeList any = doc.getElementsByTagName("xsd:any");
        NodeList replaced = doc.getElementsByTagName("xsd:element");
        Node replacedNode = replaced.item(0);
        Element aa = doc.createElement("xsd:element");
        aa.setAttribute("id", Utility.generateGUID());
        aa.setAttribute("name", ((Element) replacedNode).getAttribute("name"));

        for (int i = any.getLength() - 1; i >= 0; i--) {
            Node bb = any.item(i);
            //bb.getParentNode().removeChild(any.item(i));
            bb.getParentNode().replaceChild(aa, any.item(i));
        }

        NodeList includenodelist = doc.getElementsByTagName("xsd:include");
        if (includenodelist.getLength() > 0) {
            Node include = includenodelist.item(0);
            ((Element) include).setAttribute("schemaLocation", SRTConstants.COMPONENTS_XSD_FILE_PATH);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        String newxsdfilname = xsdfilename.replace(".xsd", "_repaced_any.xsd");
        StreamResult result = new StreamResult(filePath + "\\" + newxsdfilname);
        t.transform(source, result);
        return newxsdfilname;
    }

    public static void validate(String xsdPath, String xsdName, String xmlPath, String xmlName) throws FileNotFoundException {
        InputStream xsdStream = new FileInputStream(xsdPath + "\\" + xsdName);
        File xml = new File(xmlPath + "\\" + xmlName);
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

            Schema schema = factory.newSchema(new StreamSource(xsdStream));

            Validator validator = schema.newValidator();
            final List<SAXParseException> exceptions = new LinkedList<SAXParseException>();
            validator.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    exceptions.add(exception);
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    if (exception.getMessage().startsWith("cvc-complex-type.2.4.a") && !exception.getMessage().contains("ns:anyElement"))
                        System.out.println(exception);
                    exceptions.add(exception);
                }

                public void error(SAXParseException exception) throws SAXException {
                    if (exception.getMessage().startsWith("cvc-complex-type.2.4.a") && !exception.getMessage().contains("ns:anyElement"))
                        System.out.println(exception);
                    exceptions.add(exception);
                }
            });
            StreamSource xmlFile = new StreamSource(xml);
            validator.validate(xmlFile);
            System.out.println(xml.getName() + " is valid against given " + xsdName);
            System.out.println("");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void validate(File generatedXsdFile, File targetXsdFile, File targetXmlFile) throws Exception {
        String prefix = "xs";

        String generatedXsdFilePath = generatedXsdFile.getCanonicalPath().substring(0, generatedXsdFile.getCanonicalPath().lastIndexOf("\\"));
        String generatedXsdFileName = generatedXsdFile.getName();
        String rootElement = generatedXsdFileName.replace(".xsd","");   //Assume that xsd file name is BOD's name

        String targetXsdPath = "";
        String targetXsdName = "";
        boolean oagStandaloneReady = false;
        if(targetXsdFile!=null){
            targetXsdPath = targetXsdFile.getCanonicalPath().substring(0, targetXsdFile.getCanonicalPath().lastIndexOf("\\"));
            targetXsdName = targetXsdFile.getName();
            oagStandaloneReady = true;
        }
        String targetXmlPath = targetXmlFile.getCanonicalPath().substring(0, targetXmlFile.getCanonicalPath().lastIndexOf("\\"));
        String targetXmlName = targetXmlFile.getName();

        String generatedAnyReplacedXsdFileName = replace_any(generatedXsdFilePath, generatedXsdFileName);
        String generatedXmlFilePath = generatedXsdFilePath;                                       //will be Generated from xsd
        String generatedXmlFileName = generatedAnyReplacedXsdFileName.replace(".xsd",".xml");                //will be Generated from xsd

        generateXML(generatedXsdFilePath, generatedAnyReplacedXsdFileName, generatedXmlFilePath, generatedXmlFileName, rootElement, prefix);

        validate(generatedXsdFilePath, generatedAnyReplacedXsdFileName, targetXmlPath, targetXmlName);

        if(oagStandaloneReady) {
            validate(targetXsdPath, targetXsdName, generatedXmlFilePath, generatedXmlFileName);
        }
    }

    public static void main(String args[]) throws Exception {

        File generatedXsdFile = new File("C:\\Users\\tnk11\\Desktop\\AcknowledgeField.xsd");
        File oagStandalone = new File("C:\\Users\\tnk11\\Desktop\\AcknowledgeField_standalone.xsd");
        File targetXmlFile = new File(SRTConstants.BASE_DATA_PATH+"/Instances/BODs/AcknowledgeField.xml");
        validate(generatedXsdFile, oagStandalone, targetXmlFile);

    }
}
