package com.example.pubmedxmlprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class PubmedFileParserService {

	@Value("${filesPath}")
	private String pubmedFilePath;
	
	@Autowired
	private PublicationRepository publicationRepository;
	
	@Autowired
	private ArticleReferenceRepository referenceRepository;
	
	@Autowired
	private KeywordRepository keywordRepository;
	
	public void processFiles() {
		File rootDir = new File("");
		Path appDir = Path.of(rootDir.getAbsoluteFile().getPath()).getParent().getParent();
		File pubmedDataDir = new File(Paths.get(appDir.toString(), pubmedFilePath).toString());
		File[] filesToProcess = pubmedDataDir.listFiles();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			for (File file : filesToProcess) {
				Document document = builder.parse(file);
				List<XmlPublication> publications = parseXmlDocument(document);
				publicationRepository.saveAll(publications);
				List<Keywords> keywords = createKeywords(publications);
				keywordRepository.saveAll(keywords);
				List<ArticleReference> references = createReferences(publications);
				referenceRepository.saveAll(references);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private static List<Keywords> createKeywords(List<XmlPublication> articles) {
		List<Keywords> keywords = new ArrayList<Keywords>();
		for (XmlPublication article : articles) {
			for (String k : article.getKeywords()) {
				Keywords keyword = Keywords.builder().articlePmid(article.getPmid())
						.keyword(k).build();
				keywords.add(keyword);
			}
		}
		return keywords;
	}
	
	private static final List<ArticleReference> createReferences(List<XmlPublication> articles) {
		List<ArticleReference> references = new ArrayList<ArticleReference>();
		for (XmlPublication article : articles) {
			for (String r : article.getReferences()) {
				ArticleReference reference = ArticleReference.builder().articlePmid(article.getPmid())
						.citedArticlePmid(r).build();
				references.add(reference);
			}
		}
		return references;
	}
	
	private List<XmlPublication> parseXmlDocument(Document doc) {
		NodeList articleTags = doc.getElementsByTagName("PubmedArticle");
		List<XmlPublication> processedPublications = new ArrayList<>();
		for (int i = 0; i < articleTags.getLength(); i++) {
			XmlPublication pub = processArticleNode(articleTags.item(i));
			processedPublications.add(pub);
		}
		return processedPublications;
	}
	
	private XmlPublication processArticleNode(Node article) {
		XmlPublication publication = new XmlPublication();
		
		Node pubDateElement = getOneDescendantByTag(article, "PubDate");
		Date publicationDate = extractPublicationDate(pubDateElement);
		publication.setPublicationDate(publicationDate);
		
		String pmid = getTextOfDescendant(article, "PMID");
		if (pmid == null) {
			pmid = getTextOfDescendant(article, "IdType", "pubmed", "ArticleId");
		}
		publication.setPmid(pmid);
		
		String title = getTextOfDescendant(article, "ArticleTitle");
		publication.setTitle(title);
		
		String abstractText = getTextOfDescendant(article, "AbstractText");
		publication.setAbstractText(abstractText);
		
		Node authorElement = getOneDescendantByTag(article, "Author");
		String lastName = getTextOfDescendant(authorElement, "LastName");
		String firstName = getTextOfDescendant(authorElement, "FirstName");
		String initials = getTextOfDescendant(authorElement, "Initials");
		String authorName = lastName;
		if (firstName != null) {
			authorName = authorName + "," + firstName;
		} else {
			authorName = authorName + "," + initials;
		}
		publication.setFirstAuthorName(authorName);
		
		String publicationType = getTextOfDescendant(article, "PublicationType");
		publication.setPublicationType(publicationType);
		
		String doi = getTextOfDescendant(article, "EIdType", "doi", "ELocationID");
		if (doi == null) {
			doi = getTextOfDescendant(article, "IdType", "doi", "ArticleId");
		}
		publication.setDoi(doi);
		
		publication.setKeywords(extractKeywords(article));
		
		publication.setReferences(extractReferences(article));
		
		return publication;
	}
	
	
	private Date extractPublicationDate(Node pubDateElement) {
		String year = getTextOfDescendant(pubDateElement, "Year");
		String month = getTextOfDescendant(pubDateElement, "Month");
		String day = getTextOfDescendant(pubDateElement, "Day");
		if (day == null) {
			day = "1";
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd");
		try {
			return format.parse(year + " " + month + " " + day);
		} catch (ParseException e) {
			
			e.printStackTrace();
			
		}
		
		return null;
	}
	
	private List<String> extractReferences(Node article) {
		List<String> references = new ArrayList<String>();
		List<Element> elements = getDescendantsByTag(article, "Reference");
		elements.forEach(e -> references.add(getTextOfDescendant(e, "IdType", "pubmed", "ArticleId")));
		return references;
	}
	
	private List<String> extractKeywords(Node article) {
		List<String> keywords = new ArrayList<String>();
		List<Element> keywordElements = getDescendantsByTag(article, "Keyword");
		keywordElements.forEach(e -> keywords.add(getTextValue(e)));
		return keywords;
	}
	
	
	private Node getOneDescendantByTag(Node node, String tagName) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			NodeList list = element.getElementsByTagName(tagName);
			if (list.getLength() > 0) {
				return list.item(0);
			}
		}
		return null;
	}
	
	private String getTextOfDescendant(Node node, String tagName) {
		Node textNode = getOneDescendantByTag(node, tagName);
		if (node.getNodeType() == Node.ELEMENT_NODE && ((Element)node).getTagName().equals(tagName)) {
			return getTextValue(node);
		}
		if (textNode != null) {
			return getTextValue(textNode);
		}
		return null;
	}
	
	private String getTextValue(Node node) {
		Node textNode = node.getFirstChild();
		if (textNode.getNodeType() == Node.TEXT_NODE) {
			return textNode.getTextContent();
		}
		return null;
	}
	
	private String getTextOfDescendant(Node node, String attributeName, String attributeValue, String tag) {
		List<Element> elements = getDescendantsByTag(node, tag);
		for (Element el : elements) {
			if (el.hasAttribute(attributeName) &&
				el.getAttribute(attributeName).equals(attributeValue)) {
					return getTextOfDescendant(el, tag);
				}
		}
		return null;
	}
	
	private List<Element> getDescendantsByTag(Node node, String tagName) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			return convertNodeListToElements(element.getElementsByTagName(tagName));
		}
		return null;
	}
	
	private List<Element> convertNodeListToElements(NodeList nodeList) {
		List<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				elements.add((Element)nodeList.item(i));
			}
		}
		return elements;
	}
	
	
	
	
	
	
	
	
	
	
}
