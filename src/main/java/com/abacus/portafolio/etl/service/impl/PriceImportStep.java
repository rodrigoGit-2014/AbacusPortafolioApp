package com.abacus.portafolio.etl.service.impl;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.model.EtlContext;
import com.abacus.portafolio.etl.repository.AssetRepository;
import com.abacus.portafolio.etl.repository.PriceRepository;
import com.abacus.portafolio.etl.service.FileExtractionStep;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Order(1)
public class PriceImportStep implements FileExtractionStep {
     private final PriceRepository priceRepository;
     private final AssetRepository assetRepository;

    @Override
    public void execute(EtlContext context) {
        Workbook workbook = context.getWorkbook();
        Sheet sheet = workbook.getSheet("Precios");
        if (sheet == null) throw new RuntimeException("Sheet 'Precios' not found");

        Row header = sheet.getRow(0);
        int assetTotal = header.getLastCellNum();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            LocalDate dateColumn = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();

            for (int colIndex = 1; colIndex < assetTotal; colIndex++) {
                String assetName = header.getCell(colIndex).getStringCellValue().trim();
                double priceAmount = row.getCell(colIndex).getNumericCellValue();

                Asset assetEntity = assetRepository.findByNameIgnoreCase(assetName)
                        .orElseGet(() -> assetRepository.save(new Asset(assetName)));

                Price priceEntity = new Price();
                priceEntity.setAsset(assetEntity);
                priceEntity.setDate(dateColumn);
                priceEntity.setPriceAmount(BigDecimal.valueOf(priceAmount));
                priceRepository.save(priceEntity);
            }
        }
    }
}
