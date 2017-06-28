package org.oagi.srt.persistence.populate;

import org.apache.commons.lang.StringUtils;
import org.oagi.srt.repository.NamespaceRepository;
import org.oagi.srt.repository.ReleaseRepository;
import org.oagi.srt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static org.oagi.srt.common.SRTConstants.OAGIS_VERSION;

@Component
public class ImportUtil {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private NamespaceRepository namespaceRepository;

    private long userId;
    private long releaseId;
    private long namespaceId;

    public long getUserId() {
        if (userId == 0L) {
            userId = userRepository.findAppUserIdByLoginId("oagis");
        }
        return userId;
    }

    public long getReleaseId() {
        if (releaseId == 0L) {
            releaseId = releaseRepository.findReleaseIdByReleaseNum(Double.toString(OAGIS_VERSION));
        }
        return releaseId;
    }

    public long getNamespaceId() {
        if (namespaceId == 0L) {
            namespaceId = namespaceRepository.findNamespaceIdByUri("http://www.openapplications.org/oagis/10");
        }
        return namespaceId;
    }

    public String toString(NodeList nodeList) {
        if (nodeList.getLength() == 1 && nodeList.item(0) instanceof Text) {
            return ((Text) nodeList.item(0)).getWholeText();
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = nodeList.getLength(); i < len; ++i) {
            Node node = nodeList.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            DOMSource domSource = new DOMSource(node);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            try {
                transformer.transform(domSource, result);
            } catch (TransformerException e) {
                throw new IllegalStateException(e);
            }

            writer.flush();

            String str = StringUtils.trim(writer.toString());
            // Eliminate definitions of the namespace
            str = str.replaceAll("[\\W]+xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"", "")
                    .replaceAll("[\\W]+xmlns=\"http://www.openapplications.org/oagis/10\"", "")
                    .replaceAll("([\t]+)", "\t")
                    .replaceAll("[\t]</", "</");
            sb.append(str);

            if ((i + 1) != len) {
                sb.append("\n");
            }
        }

        return StringUtils.trim(sb.toString());
    }

}
