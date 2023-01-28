package com.example.pubmed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.pubmed.Publication;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, String> {

}
