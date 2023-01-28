package com.example.pubmedmethoddownloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

//PUBMED FORMAT
@Service
public class PubmedMethodDownloaderService {

	private final static List<String> METHODS_LIST = Arrays.asList("Methods", "METHODS");
	private final static List<String> RESULTS_LIST = Arrays.asList("Results", "RESULTS", "Discussion", "DISCUSSION");
	
	@Autowired
	private PublicationRepository publicationRepository;
	
	@Autowired
	private MethodRepository methodRepository;
	
	private static final String SCIHUB_BASE_URL = "https://sci-hub.se/";
//	private static final String SCIHUB_BASE_URL = "https://sci-hub.hkvisa.net/";
//	private static final String SCIHUB_BASE_URL = "https://sci-hub.st/";

	public void downloadPubmedMethods() {
		List<Publication> publications = publicationRepository.findAll();
		List<Method> downloadedMethods = methodRepository.findAll();
		for (Publication pub : publications) {
//			if (!publicationAlreadyProcessed(pub.getDoi(), downloadedMethods)) {
//				continue;
//			}
			String htmlResponse = extractPublicationHtmlResponse(pub.getDoi());
			if (!ObjectUtils.isEmpty(htmlResponse)) {
				String downloadHref = extractDownloadPdfLinkFromResponse(htmlResponse);
				if (downloadHref == null) {
					printError(pub.getDoi());
					continue;
				}
				String pdfContent = extractPdfTextFromUrl(SCIHUB_BASE_URL + downloadHref);
				if (pdfContent == null) {
					printError(pub.getDoi());
					continue;
				}
				String methods = extractMethodsPart(pdfContent);
				if (methods == null) {
					methods = "";
				}
				Method method = new Method(pub.getDoi(), downloadHref, methods, pdfContent);
				try {
					methodRepository.save(method);
					System.err.println("Done: " + pub.getDoi());
				} catch(Exception e) {}
			} else {
				Method method = new Method(pub.getDoi(), "", "", "");
				methodRepository.save(method);
				printError(pub.getDoi());
			}
			
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void printError(String doi) {
		System.err.println("	Error: " + doi);
	}
	
	private boolean publicationAlreadyProcessed(String doi, List<Method> methods) {
		return methods.stream().filter(p -> p.getDoi().equals(doi)).count() > 0;
	}
	
	private String extractPdfTextFromUrl(String href) {
		try {
			PDFParser parser = new PDFParser(
					new RandomAccessBuffer( new BufferedInputStream(new URL(href).openStream()) ) );
			parser.parse();
			PDFTextStripper textStripper = new PDFTextStripper();
			
			String text = textStripper.getText(parser.getPDDocument());
			parser.getPDDocument().close();
			return text;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String extractMethodsPart(String fullPdfText) {
		String methodsChapterTitle = METHODS_LIST.stream()
				.filter(m -> fullPdfText.contains(m + System.lineSeparator()))
				.map(m -> m + System.lineSeparator())
				.findAny()
				.orElse(null);
		String resultsChapterTitle = RESULTS_LIST.stream()
				.filter(m -> fullPdfText.contains(m + System.lineSeparator()))
				.map(m -> m + System.lineSeparator())
				.findAny()
				.orElse(null);
		if (methodsChapterTitle != null && resultsChapterTitle != null) {
			Integer startIndex = fullPdfText.indexOf(methodsChapterTitle) + methodsChapterTitle.length();
			Integer endIndex = fullPdfText.indexOf(resultsChapterTitle);
			if (startIndex >= endIndex || startIndex > fullPdfText.length() || endIndex > fullPdfText.length()) {
				return null;
			}
			String methodsPart = fullPdfText.substring(startIndex, endIndex);
			return methodsPart/*.replace(System.lineSeparator(), " ")*/;
		}
		return null;
	}

	private String extractPublicationHtmlResponse(String doi) {
		String url = SCIHUB_BASE_URL + doi + "/";
		String htmlText = readContentFromUrl(url);
		if (htmlText != null && !htmlText.contains("Sci-Hub: article not found")) {
			return htmlText;
		}
		
		if (htmlText.contains("Sci-Hub: article not found")) {
			System.out.println("*Article not available on sci-hub");
		}

		return null;
	}
	
	private String extractDownloadPdfLinkFromResponse(String htmlResponse) {
		final String hrefPrefix = "location.href='";
		Integer indexOfLocationHref = htmlResponse.lastIndexOf(hrefPrefix);
		String startHref = htmlResponse.substring(indexOfLocationHref + hrefPrefix.length());
		Integer endIndex = startHref.indexOf("'\">");
		if (endIndex == -1) {
			return null;
		}
		String finalHref = startHref.substring(0, startHref.indexOf("'\">"));
		return finalHref;
	}
	
	private String readContentFromUrl(String url) {
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			InputStreamReader isReader = new InputStreamReader(in);
			// Creating a BufferedReader object
			BufferedReader reader = new BufferedReader(isReader);
			StringBuffer sb = new StringBuffer();
			String str;
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
