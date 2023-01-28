package com.example.pubmedxmlprocessor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleReferenceRepository extends JpaRepository<ArticleReference, Long> {

}
