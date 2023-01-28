package com.example.pubmed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.pubmed.repository.PublicationRepository;

@Service
public class PubmedFileParserService {

	@Value("${filesPath}")
	private String pubmedFilePath;
	
	@Autowired
	private PublicationRepository publicationRepository;
	
	public void processFiles() {
		
		File appDir = new File("").getAbsoluteFile();
		File pubmedDataDir = new File(Paths.get(appDir.getParent(), pubmedFilePath).toString());
		File[] filesToProcess = pubmedDataDir.listFiles();
		for (File file : filesToProcess) {
			List<Publication> publications = parseFile(file);
			publicationRepository.saveAll(publications);
		}
		
		List<Publication> pbs = publicationRepository.findAll();
		int a = 0;
	}
	
	private List<Publication> parseFile(File file) {
		try {
			List<String> lines = FileUtils.readLines(file, "utf-8");
			List<List<String>> linesByPub = new ArrayList<>();
			linesByPub.add(new ArrayList<String>());
			for (String l : lines) {
				if (l.isBlank()) {
					linesByPub.add(new ArrayList<String>());
				} else {
					linesByPub.get(linesByPub.size() - 1).add(l);
				}
			}
			List<Publication> publications = linesByPub.stream()
					.map(this::createPublicationFromPubString).collect(Collectors.toList());
			return publications;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	private Publication createPublicationFromPubString(List<String> pubStringRows) {
		List<Pairs> pairs = createKeyValuePairs(pubStringRows);
		Publication publication = new Publication();
		publication.setPmid(getFirstValueByPubmedTag("PMID", pairs));
		publication.setTitle(getFirstValueByPubmedTag("TT", pairs));
		publication.setAbstractText(getFirstValueByPubmedTag("AB", pairs));
		publication.setPublicationType(
				StringUtils.collectionToCommaDelimitedString(getAllValuesByPubmedTag("PT", pairs)));
		publication.setDoi(getDoi(pairs));
		publication.setKeywords(
				StringUtils.collectionToCommaDelimitedString(getAllValuesByPubmedTag("OT", pairs)));
		
		Date publicationDate = getPublicationDate(pairs);
		if (publicationDate != null) {
			publication.setPublicationDate(publicationDate);
		}
		return publication;
	}
	
	private String getDoi(List<Pairs> pairs) {
		String doi = "";
		String lid = getAllValuesByPubmedTag("LID", pairs).stream()
				.filter(v -> v.contains("doi")).findAny().orElse(null);
		if (lid != null && lid.contains("doi")) {
			doi = lid.replace("[doi]", "");
		}
		if (doi == null) {
			String SOstring = getFirstValueByPubmedTag("SO", pairs);
			if (SOstring == null) {
				int a = 0;
			}
			int doiStringIndex = SOstring.lastIndexOf("doi:");
			if (doiStringIndex != -1) {
				doi = SOstring.substring(doiStringIndex);
			}
		}
		return doi.trim();
	}
	
	private Date getPublicationDate(List<Pairs> pairs) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy MMM dd", Locale.ENGLISH);
			return formatter.parse(getFirstValueByPubmedTag("DP", pairs));
		} catch (ParseException e) {}
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy MMM", Locale.ENGLISH);
			return formatter.parse(getFirstValueByPubmedTag("DP", pairs));
		} catch (ParseException e) {}
		return null;
	}
	
	private String getFirstValueByPubmedTag(String tag, List<Pairs> tags) {
		Pairs pair = tags.stream().filter(t -> t.val1.equalsIgnoreCase(tag)).findFirst().orElse(null);
		return pair != null ? pair.val2 : null;
	}
	
	private List<String> getAllValuesByPubmedTag(String tag, List<Pairs> tags) {
		List<String> values = tags.stream().filter(t -> t.val1.equalsIgnoreCase(tag))
				.map(t -> t.val2).collect(Collectors.toList());
		return values;
	}
	
	private List<Pairs> createKeyValuePairs(List<String> pubRows) {
		List<String> rowsProcessed = new ArrayList<String>();
		//join multiple broken lines into one
		for (String line : pubRows) {
			//there are empty characters on the beginning --> join them to the previous row
			if (!line.stripLeading().equals(line)) {
				int lastIdx = rowsProcessed.size() - 1;
				String newValue = rowsProcessed.get(lastIdx) + ' ' + line.trim();
				rowsProcessed.remove(lastIdx);
				rowsProcessed.add(newValue);
			} else if (!line.trim().isBlank()) {
				rowsProcessed.add(line.trim());
			}
		}
		
		List<Pairs> pairs = new ArrayList<>();
		for (String row : rowsProcessed) {
			String[] keyValues = row.split("-");
			String prefix = keyValues[0] + '-';
			String value = row.replace(prefix, "");
			pairs.add(new Pairs(keyValues[0].trim(), value.trim()));
		}
		
		return pairs;
	}
	
	private class Pairs {
		
		private String val1;
		private String val2;
		
		private Pairs(String val1, String val2) {
			this.val1 = val1;
			this.val2 = val2;
		}
	}
}
