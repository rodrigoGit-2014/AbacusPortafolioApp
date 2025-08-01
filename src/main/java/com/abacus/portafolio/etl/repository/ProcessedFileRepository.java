package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, String> {
}

