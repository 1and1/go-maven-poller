package com.oneandone.go.plugin.maven.util;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** Factory method for document builders. */
public class DocumentBuilders {

    private DocumentBuilders() {
        // no instance allowed
    }

    /** Creates a securely configured document builder.
     * @see <a href="https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing">XML External Entity (XXE) Processing</a>
     * */
    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); //NOSONAR
        // deny all external entity processing (XXE)
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setAttribute(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder();
    }
}
