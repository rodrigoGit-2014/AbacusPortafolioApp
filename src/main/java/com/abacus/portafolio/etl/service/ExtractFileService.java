package com.abacus.portafolio.etl.service;

import com.abacus.portafolio.etl.entities.ProcessedFile;
import com.abacus.portafolio.etl.exceptions.DuplicateFileException;
import com.abacus.portafolio.etl.exceptions.InvalidFileFormatException;
import com.abacus.portafolio.etl.model.EtlContext;
import com.abacus.portafolio.etl.repository.ProcessedFileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractFileService {
    private final List<FileExtractionStep> extractionSteps;
    private final ProcessedFileRepository processedFileRepository;

    public void importFromExcel(MultipartFile file) {
        String checksum = extractChecksum(file);
        validateFileIsNew(checksum);
        Workbook workbook = loadWorkbook(file);
        processWorkbook(workbook);
        saveChecksum(checksum);
    }

    private String extractChecksum(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return DigestUtils.sha256Hex(is);
        } catch (IOException e) {
            throw new InvalidFileFormatException("Error calculating checksum");
        }
    }

    private void validateFileIsNew(String checksum) {
        if (processedFileRepository.existsById(checksum)) {
            throw new DuplicateFileException("Archivo ya procesado previamente.");
        }
    }

    private Workbook loadWorkbook(MultipartFile file) {
        try {
            return WorkbookFactory.create(file.getInputStream());
        } catch (IOException e) {
            throw new InvalidFileFormatException("No se pudo leer el archivo Excel.");
        }
    }

    private void processWorkbook(Workbook workbook) {
        EtlContext context = EtlContext.builder().workbook(workbook).build();
        extractionSteps.forEach(step -> step.execute(context));
    }

    private void saveChecksum(String checksum) {
        processedFileRepository.save(new ProcessedFile(checksum, LocalDateTime.now()));
    }
}
