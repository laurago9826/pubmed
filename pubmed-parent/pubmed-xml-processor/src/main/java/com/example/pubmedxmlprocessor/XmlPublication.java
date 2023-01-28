package com.example.pubmedxmlprocessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "xml_publication")
@Getter @Setter
public class XmlPublication {

	@Id
	private String pmid;//done
	
	@Column(length = 1500)
	private String title;//done
	
	@Column(columnDefinition = "TEXT")
	private String abstractText;//done
	
	private String publicationType;//done
	
	private String doi; //done
	
	private Date publicationDate;//done
	
	private String firstAuthorName; //done
	
	@Transient
	private List<String> keywords = new ArrayList<>(); //done
	
	@Transient
	private List<String> references = new ArrayList<>(); //done
}
