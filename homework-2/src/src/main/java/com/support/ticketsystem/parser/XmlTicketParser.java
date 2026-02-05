package com.support.ticketsystem.parser;

import com.support.ticketsystem.domain.dto.TicketImportDto;
import com.support.ticketsystem.exception.ImportParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Parser for XML ticket import files.
 * Expected format: XML with root element containing ticket elements.
 */
@Component
public class XmlTicketParser implements TicketFileParser {

    private static final Logger log = LoggerFactory.getLogger(XmlTicketParser.class);

    private static final String FILE_TYPE = "XML";
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
        "application/xml",
        "text/xml"
    );

    private final DocumentBuilderFactory documentBuilderFactory;

    public XmlTicketParser() {
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // Security: Disable external entities to prevent XXE attacks
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException e) {
            log.warn("Could not configure XML parser security features", e);
        }
    }

    @Override
    public List<TicketImportDto> parse(MultipartFile file) throws ImportParseException {
        log.info("Parsing XML file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            // Find ticket elements (try common element names)
            List<Element> ticketElements = findTicketElements(document);

            if (ticketElements.isEmpty()) {
                log.info("No ticket elements found in XML");
                return Collections.emptyList();
            }

            List<TicketImportDto> tickets = new ArrayList<>();

            for (int i = 0; i < ticketElements.size(); i++) {
                Element ticketElement = ticketElements.get(i);
                try {
                    TicketImportDto dto = parseTicketElement(ticketElement);
                    tickets.add(dto);
                } catch (Exception e) {
                    log.warn("Error parsing ticket element at index {}: {}", i, e.getMessage());
                    throw new ImportParseException(FILE_TYPE,
                        "Error parsing ticket at index " + i + ": " + e.getMessage(), e);
                }
            }

            log.info("Successfully parsed {} tickets from XML", tickets.size());
            return tickets;

        } catch (ParserConfigurationException e) {
            log.error("XML parser configuration error", e);
            throw new ImportParseException(FILE_TYPE, "XML parser configuration error: " + e.getMessage(), e);
        } catch (SAXException e) {
            log.error("XML parsing error", e);
            throw new ImportParseException(FILE_TYPE, "Invalid XML format: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error reading XML file", e);
            throw new ImportParseException(FILE_TYPE, "Failed to read file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String contentType, String filename) {
        if (contentType != null && SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }
        return filename != null && filename.toLowerCase().endsWith(".xml");
    }

    @Override
    public String getFileTypeName() {
        return FILE_TYPE;
    }

    private List<Element> findTicketElements(Document document) {
        List<Element> elements = new ArrayList<>();

        // Try common ticket element names
        String[] possibleNames = {"ticket", "Ticket", "TICKET", "record", "Record", "item", "Item"};

        for (String name : possibleNames) {
            NodeList nodeList = document.getElementsByTagName(name);
            if (nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        elements.add((Element) node);
                    }
                }
                break;
            }
        }

        // If no specific ticket elements found, try to get direct children of root
        if (elements.isEmpty()) {
            Element root = document.getDocumentElement();
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    elements.add((Element) node);
                }
            }
        }

        return elements;
    }

    private TicketImportDto parseTicketElement(Element element) {
        TicketImportDto dto = new TicketImportDto();

        dto.setCustomerId(getElementText(element, "customer_id", "customerId", "customer-id"));
        dto.setCustomerEmail(getElementText(element, "customer_email", "customerEmail", "customer-email", "email"));
        dto.setCustomerName(getElementText(element, "customer_name", "customerName", "customer-name", "name"));
        dto.setSubject(getElementText(element, "subject", "title"));
        dto.setDescription(getElementText(element, "description", "content", "body"));
        dto.setCategory(getElementText(element, "category"));
        dto.setPriority(getElementText(element, "priority"));
        dto.setStatus(getElementText(element, "status"));
        dto.setAssignedTo(getElementText(element, "assigned_to", "assignedTo", "assigned-to", "assignee"));

        // Parse tags
        List<String> tags = parseTagsElement(element);
        if (!tags.isEmpty()) {
            dto.setTags(tags);
        }

        // Parse metadata (can be nested or flat)
        Element metadataElement = getChildElement(element, "metadata");
        if (metadataElement != null) {
            dto.setMetadataSource(getElementText(metadataElement, "source"));
            dto.setMetadataBrowser(getElementText(metadataElement, "browser"));
            dto.setMetadataDeviceType(getElementText(metadataElement, "device_type", "deviceType", "device-type"));
        } else {
            dto.setMetadataSource(getElementText(element, "metadata_source", "metadataSource", "source"));
            dto.setMetadataBrowser(getElementText(element, "metadata_browser", "metadataBrowser", "browser"));
            dto.setMetadataDeviceType(getElementText(element, "metadata_device_type", "metadataDeviceType", "device_type", "deviceType"));
        }

        return dto;
    }

    private String getElementText(Element parent, String... elementNames) {
        for (String name : elementNames) {
            NodeList nodeList = parent.getElementsByTagName(name);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                String text = node.getTextContent();
                if (text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    private Element getChildElement(Element parent, String... elementNames) {
        for (String name : elementNames) {
            NodeList nodeList = parent.getElementsByTagName(name);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    return (Element) node;
                }
            }
        }
        return null;
    }

    private List<String> parseTagsElement(Element parent) {
        List<String> tags = new ArrayList<>();

        // Try to find tags element
        Element tagsElement = getChildElement(parent, "tags");

        if (tagsElement != null) {
            // Check for individual tag elements
            NodeList tagNodes = tagsElement.getElementsByTagName("tag");
            if (tagNodes.getLength() > 0) {
                for (int i = 0; i < tagNodes.getLength(); i++) {
                    String tagText = tagNodes.item(i).getTextContent();
                    if (tagText != null && !tagText.trim().isEmpty()) {
                        tags.add(tagText.trim());
                    }
                }
            } else {
                // Tags might be comma-separated in text content
                String tagsText = tagsElement.getTextContent();
                if (tagsText != null && !tagsText.trim().isEmpty()) {
                    Arrays.stream(tagsText.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(tags::add);
                }
            }
        }

        return tags;
    }
}
