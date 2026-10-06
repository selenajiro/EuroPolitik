package io.github.selenajiro.europolitik.news;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class EuRssParser {

    private static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    private EuRssParser() {
    }

    public record RssItem(String title, String link, String description, LocalDate publishedDate) {}

    public static List<RssItem> parse(String rawXml) {
        List<RssItem> result = new ArrayList<>();
        if (rawXml == null || rawXml.isBlank()) {
            return result;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(rawXml)));

            NodeList items = doc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);

                String title = textOf(item, "title");
                String link = textOf(item, "link");
                String description = textOf(item, "description");
                String updated = textOfNS(item, ATOM_NS, "updated");

                if (title == null || title.isBlank() || link == null || link.isBlank()) {
                    continue;
                }

                LocalDate publishedDate = parseUpdated(updated);
                result.add(new RssItem(title.trim(), link.trim(),
                        description != null && !description.isBlank() ? description.trim() : null,
                        publishedDate != null ? publishedDate : LocalDate.now()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse EU RSS feed: " + e.getMessage(), e);
        }

        return result;
    }

    private static String textOf(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }

    private static String textOfNS(Element parent, String namespaceUri, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(namespaceUri, localName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }

    private static LocalDate parseUpdated(String updated) {
        if (updated == null || updated.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(updated).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }
}
