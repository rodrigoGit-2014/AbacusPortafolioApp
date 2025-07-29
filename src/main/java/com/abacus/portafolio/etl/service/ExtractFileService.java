package com.abacus.portafolio.etl.service;

import com.abacus.portafolio.etl.model.Asset;
import com.abacus.portafolio.etl.model.Price;
import com.abacus.portafolio.etl.repository.AssetRepository;
import com.abacus.portafolio.etl.repository.PriceRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ExtractFileService {
    @Autowired
    private PriceRepository priceRepository;
    @Autowired
    private AssetRepository assetRepository;

    public void importarDesdeExcel(MultipartFile archivoExcel) {
        try (Workbook workbook = WorkbookFactory.create(archivoExcel.getInputStream())) {
             importPrices(workbook);
            //importarPrecios(workbook);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo Excel", e);
        }
    }

    private void importPrices(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Precios");
        if (sheet == null) throw new RuntimeException("Hoja 'Precios' no encontrada");

        Row header = sheet.getRow(0);
        int numActivos = header.getLastCellNum();

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            LocalDate fecha = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();

            for (int colIndex = 1; colIndex < numActivos; colIndex++) {
                String activoNombre = header.getCell(colIndex).getStringCellValue().trim();
                double precioValue = row.getCell(colIndex).getNumericCellValue();

                Asset activo = assetRepository.findByNameIgnoreCase(activoNombre)
                        .orElseGet(() -> assetRepository.save(new Asset(activoNombre)));

                Price price = new Price();
                price.setAsset(activo);
                price.setDate(fecha);
                price.setValue(BigDecimal.valueOf(precioValue));
                priceRepository.save(price);
            }
        }
    }

    private void importPricesu(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Weights");
        if (sheet == null) throw new RuntimeException("Hoja 'Weights' no encontrada");

        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            String activoNombre = row.getCell(1).getStringCellValue().trim();
            BigDecimal pesoP1 = new BigDecimal(row.getCell(2).getNumericCellValue()); // Portafolio 1

       /*     Asset activo = activoRepository.findByNombreIgnoreCase(activoNombre)
                    .orElseGet(() -> activoRepository.save(new Activo(activoNombre)));

            Portafolio portafolio1 = portafolioRepository.findByNombre("Portafolio 1")
                    .orElseGet(() -> portafolioRepository.save(new Portafolio("Portafolio 1")));

            PesoInicial peso = new PesoInicial();
            peso.setActivo(activo);
            peso.setPortafolio(portafolio1);
            peso.setPeso(pesoP1);

            pesoInicialRepository.save(peso);*/
        }
    }

}
