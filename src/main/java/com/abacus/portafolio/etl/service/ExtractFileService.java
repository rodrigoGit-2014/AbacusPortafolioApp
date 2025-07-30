package com.abacus.portafolio.etl.service;

import com.abacus.portafolio.etl.model.EtlContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractFileService {
    private final List<FileExtractionStep> extractionSteps;

    public void importFromExcel(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            EtlContext context = EtlContext.builder().workbook(workbook).build();
            for (FileExtractionStep step : extractionSteps) {
                step.execute(context);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file", e);
        }
    }


}
