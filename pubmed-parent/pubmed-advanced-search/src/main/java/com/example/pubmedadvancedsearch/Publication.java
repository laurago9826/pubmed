package com.example.pubmedadvancedsearch;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "publication")
@Getter @Setter
public class Publication {

	@Id
	private String pmid;
	
	@Column(length = 1500)
	private String title;
	
	@Column(columnDefinition = "TEXT")
	private String keywords;
	
	@Column(columnDefinition = "TEXT")
	private String abstractText;
	
	private String publicationType;
	
	private String doi;
	
	private Date publicationDate;
}

