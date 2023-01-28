package com.example.pubmedmethoddownloader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MethodCounterService {

	@Autowired
	private MethodRepository methodRepository;
	
	@Autowired
	private MonogramRepository monogramRepository;
	
	@Autowired
	private BigramRepository bigramRepository;
	
	@Autowired
	private TrigramRepository trigramRepository;

	public void countData() {
		List<String> methods = methodRepository.findAll().stream().map(Method::getMethod).collect(Collectors.toList());
		countMonograms(methods);
		countBigrams(methods);
		countTrigrams(methods);
	}

	public void countMonograms(List<String> methods) {
		Map<String, Long> counts = new HashMap<>();
		for (String method : methods) {
			for (String word : splitString(method)) {
				if (!counts.containsKey(word)) {
					counts.put(word, 1L);
				} else {
					counts.compute(word, (k, v) -> v + 1);
				}
			}
		}
		List<Monogram> monograms = counts.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		.map(e -> Monogram.builder().text(e.getKey()).count(e.getValue()).build())
		.collect(Collectors.toList());
		monogramRepository.saveAll(monograms);
	}
	
	public void countBigrams(List<String> methods) {
		Map<String, Long> counts = new HashMap<>();
		for (String method : methods) {
			List<String> words = splitString(method);
			for(int i = 0; i < words.size() - 1; i++) {
				String bigram = words.get(i) + " " + words.get(i + 1);
				if (!counts.containsKey(bigram)) {
					counts.put(bigram, 1L);
				} else {
					counts.compute(bigram, (k, v) -> v + 1);
				}
			}
		}
		List<Bigram> bigrams = counts.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		.map(e -> Bigram.builder().text(e.getKey()).count(e.getValue()).build())
		.collect(Collectors.toList());
		bigramRepository.saveAll(bigrams);
	}
	
	public void countTrigrams(List<String> methods) {
		Map<String, Long> counts = new HashMap<>();
		for (String method : methods) {
			List<String> words = splitString(method);
			for(int i = 0; i < words.size() - 2; i++) {
				String trigram = words.get(i) + " " + words.get(i + 1) + " " + words.get(i + 2);
				if (!counts.containsKey(trigram)) {
					counts.put(trigram, 1L);
				} else {
					counts.compute(trigram, (k, v) -> v + 1);
				}
			}
		}
		List<Trigram> trigrams = counts.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		.map(e -> Trigram.builder().text(e.getKey()).count(e.getValue()).build())
		.collect(Collectors.toList());
		trigramRepository.saveAll(trigrams);
	}
	
	private List<String> splitString(String text) {
		String filtered = text.replace("\t", " ").
				replace("\n", " ")
				.replace("(", " ")
				.replace(")", " ")
				.replace(",", " ")
				.replace(".", " ")
				.replace("!", " ")
				.replace("?", " ")
				.replace(";", " ")
				.replace(":", " ")
				.replace("-", " ")
				.replace("'", " ")
				.replace("\"", " ")
				.replace("*", " ")
				.replace("/", " ")
				.toLowerCase();
		List<String> list = Arrays.asList(filtered.split(" "))
				.stream().filter(StringUtils::hasText)
				.collect(Collectors.toList());
		return list;
	}
}
