package com.example.pubmedmethoddownloader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PubmedMethodDownloaderApplication implements CommandLineRunner {

	@Autowired
	private PubmedMethodDownloaderService pubmedMethodDownlaoderService;
	
	@Autowired
	private MethodCounterService counterService;
	
	public static void main(String[] args) {
		SpringApplication.run(PubmedMethodDownloaderApplication.class, args);
	}

	@Override
	public void run(String... args) {
		//pubmedMethodDownlaoderService.downloadPubmedMethods();
		counterService.countData();
	}
}
