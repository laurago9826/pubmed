package com.example.pubmedmethoddownloader;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "method")
@Getter @Setter
public class Method {

	@Id
	private String doi;
	
	private String downloadHref;

	@Column(columnDefinition = "TEXT")
	private String method;
	
	@Column(columnDefinition = "TEXT")
	private String fullText;
}
