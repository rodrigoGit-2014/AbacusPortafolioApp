package com.abacus.portafolio.etl.service.impl;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetWeight;
import com.abacus.portafolio.etl.entities.Portfolio;
import com.abacus.portafolio.etl.model.EtlContext;
import com.abacus.portafolio.etl.repository.AssetRepository;
import com.abacus.portafolio.etl.repository.AssetWeightRepository;
import com.abacus.portafolio.etl.repository.PortfolioRepository;
import com.abacus.portafolio.etl.service.FileExtractionStep;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Order(0)
public class WeightImportStep implements FileExtractionStep {

    private final AssetRepository assetRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetWeightRepository assetWeightRepository;

    @Override
    public void execute(EtlContext context) {
        Workbook workbook = context.getWorkbook();
        Sheet sheet = workbook.getSheet("Weights");
        if (sheet == null) {
            throw new RuntimeException("Sheet 'Weights' not found");
        }

        Row header = sheet.getRow(0);
        int totalPortfolios = header.getLastCellNum() - 2;
        Row secondRow = sheet.getRow(1);
        Cell dateCell = secondRow.getCell(0);


        for (int portfolioIndex = 0; portfolioIndex < totalPortfolios; portfolioIndex++) {
            String portfolioName = getCellValueAsString(header.getCell(2 + portfolioIndex));
            Portfolio portfolio = findOrCreatePortfolio(portfolioName);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || row.getCell(0) == null || row.getCell(0).getLocalDateTimeCellValue() == null)
                    continue;

                String assetName = getCellValueAsString(row.getCell(1));
                BigDecimal weightValue = getCellValueAsBigDecimal(row.getCell(2 + portfolioIndex));

                Asset asset = findOrCreateAsset(assetName);

                AssetWeight weight = AssetWeight.builder()
                        .asset(asset)
                        .portfolio(portfolio)
                        .weight(weightValue)
                        .validFrom(dateCell.getLocalDateTimeCellValue().toLocalDate())
                        .validTo(LocalDate.of(9999, 12, 31))
                        .build();

                assetWeightRepository.save(weight);
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        return cell != null ? cell.getStringCellValue().trim() : "";
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        return cell != null ? BigDecimal.valueOf(cell.getNumericCellValue()) : BigDecimal.ZERO;
    }

    private Asset findOrCreateAsset(String name) {
        return assetRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> assetRepository.save(new Asset(name)));
    }

    private Portfolio findOrCreatePortfolio(String name) {
        return portfolioRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> portfolioRepository.save(new Portfolio(name)));
    }
}
