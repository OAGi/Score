package org.oagi.srt.export.model;

import org.jdom2.Document;
import org.jdom2.input.DOMBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class BdtsBlob {

    private Document document;

    public BdtsBlob(byte[] content) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        org.w3c.dom.Document document;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            try (InputStream inputStream = new ByteArrayInputStream(content)) {
                document = documentBuilder.parse(inputStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        DOMBuilder jdomBuilder = new DOMBuilder();
        this.document = jdomBuilder.build(document);
    }

    public boolean exists(String guid) {
        return false;
    }

}
