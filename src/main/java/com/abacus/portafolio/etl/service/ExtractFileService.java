package com.abacus.portafolio.etl.service;

import com.abacus.portafolio.etl.config.InitialPortfolioValuesConfig;
import com.abacus.portafolio.etl.model.*;
import com.abacus.portafolio.etl.repository.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExtractFileService {
    @Autowired
    private PriceRepository priceRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private InitialWeightRepository initialWeightRepository;
    @Autowired
    private AssetQuantityRepository assetQuantityRepository;
    @Autowired
    private InitialPortfolioValuesConfig initialPortfolioValuesConfig;

    public void importarDesdeExcel(MultipartFile archivoExcel) {
        try (Workbook workbook = WorkbookFactory.create(archivoExcel.getInputStream())) {
            importWeight(workbook);
            importPrices(workbook);
            handleInitializationOfQuantities();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo Excel", e);
        }
    }

    private void importPrices(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Precios");
        if (sheet == null) throw new RuntimeException("Hoja 'Precios' no encontrada");

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

    private void importWeight(Workbook workbook) {
        Sheet sheet = workbook.getSheet("Weights");
        if (sheet == null) throw new RuntimeException("Hoja 'Weights' no encontrada");

        Row header = sheet.getRow(0);
        int portFolioTotal = header.getLastCellNum() - 2;

        for (int indexPortfolio = 0; indexPortfolio < portFolioTotal; indexPortfolio++) {
            String portfolioName = header.getCell(2 + indexPortfolio).getStringCellValue().trim();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row.getCell(0).getLocalDateTimeCellValue() == null) continue;

                String assetName = row.getCell(1).getStringCellValue().trim();
                BigDecimal pesoP1 = new BigDecimal(row.getCell(2).getNumericCellValue());

                Asset assetEntity = assetRepository.findByNameIgnoreCase(assetName)
                        .orElseGet(() -> assetRepository.save(new Asset(assetName)));

                Portfolio portfolio = portfolioRepository.findByNameIgnoreCase(portfolioName)
                        .orElseGet(() -> portfolioRepository.save(new Portfolio(portfolioName)));

                InitialWeight peso = new InitialWeight();
                peso.setAsset(assetEntity);
                peso.setPortfolio(portfolio);
                peso.setWeight(pesoP1);

                initialWeightRepository.save(peso);
            }
        }
    }

    private void handleInitializationOfQuantities() {
        Price price = priceRepository.findFirstByOrderByDateAsc();
        List<Portfolio> portfolios = portfolioRepository.findAll();
        for (Portfolio portfolio : portfolios) {
            BigDecimal initialValue = initialPortfolioValuesConfig.findValue(portfolio.getName());
            initializeQuantities(portfolio.getId(), initialValue.doubleValue(), price.getDate());
        }

    }

    @Transactional
    public void initializeQuantities(Long portafolioId, double valorInicial, LocalDate fechaInicial) {
        Portfolio portafolio = portfolioRepository.findById(portafolioId)
                .orElseThrow(() -> new RuntimeException("Portafolio no encontrado"));

        List<InitialWeight> pesos = initialWeightRepository.findByPortfolio(portafolio);

        for (InitialWeight pesoInicial : pesos) {
            Asset activo = pesoInicial.getAsset();
            double peso = pesoInicial.getWeight().doubleValue();

            Price precio = priceRepository.findByAssetAndDate(activo, fechaInicial)
                    .orElseThrow(() -> new RuntimeException("No hay precio para " + activo.getName() + " en " + fechaInicial));

            double cantidadCalculada = (peso * valorInicial) / precio.getPriceAmount().doubleValue();

            AssetQuantity cantidad = new AssetQuantity();
            cantidad.setPortfolio(portafolio);
            cantidad.setAsset(activo);
            cantidad.setAmount(BigDecimal.valueOf(cantidadCalculada));

            assetQuantityRepository.save(cantidad);
        }
    }


}
