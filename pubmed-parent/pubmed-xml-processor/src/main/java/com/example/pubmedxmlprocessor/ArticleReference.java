package com.example.pubmedxmlprocessor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "article_reference")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class ArticleReference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String articlePmid;
	
	private String citedArticlePmid;
}
