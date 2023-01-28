package com.example.pubmedadvancedsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PubmedAdvancedSearchApplication implements CommandLineRunner {

	@Autowired
	private PubmedFileParserService pubmedFileParserService;
	
	public static void main(String[] args) {
		SpringApplication.run(PubmedAdvancedSearchApplication.class, args);
	}

	@Override
	public void run(String... args) {
		pubmedFileParserService.processFiles();
	}
}
