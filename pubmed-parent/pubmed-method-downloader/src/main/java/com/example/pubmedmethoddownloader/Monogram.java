package com.example.pubmedmethoddownloader;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Monogram {

	@Id
	@GeneratedValue
	private Integer id;
	
	@Column(columnDefinition = "TEXT")
	private String text;
	private Long count;
}
