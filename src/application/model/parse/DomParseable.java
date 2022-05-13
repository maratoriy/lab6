package application.model.parse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface DomParseable {
    static Element createTextNode(Document document, String nodeName, String text) {
        Element element = document.createElement(nodeName);
        element.appendChild(document.createTextNode(text));
        return element;
    }

    Element parse(Document document);

    void parse(Element element);
//    void parse(Element element);
}
