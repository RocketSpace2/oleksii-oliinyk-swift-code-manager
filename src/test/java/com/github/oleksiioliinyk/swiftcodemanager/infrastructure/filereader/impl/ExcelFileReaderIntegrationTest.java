package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.impl;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.contract.FileReaderContract;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
public class ExcelFileReaderIntegrationTest {
    @Autowired
    private FileReaderContract reader;

    @TempDir
    private Path tempDir;

    @Test
    void readFile_readRealExcel_ok() throws Exception {
        Path xlsx = tempDir.resolve("swiftcodes.xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook();
             OutputStream os = java.nio.file.Files.newOutputStream(xlsx)) {

            XSSFSheet sheet = wb.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("HEADER");
            Row r = sheet.createRow(1);
            r.createCell(0).setCellValue(" us ");
            r.createCell(1).setCellValue(" aaabbbccxxx ");
            r.createCell(3).setCellValue(" bank name ");
            r.createCell(4).setCellValue(" address 123 ");
            r.createCell(6).setCellValue(" country name ");

            wb.write(os);
        }

        Resource resource = new FileSystemResource(xlsx);
        List<SwiftCode> list = reader.readFile(resource);

        assertThat(list).hasSize(1);

        SwiftCode sc = list.get(0);
        assertThat(sc.swiftCode()).isEqualTo("AAABBBCCXXX");
        assertThat(sc.bankName()).isEqualTo("BANK NAME");
        assertThat(sc.address()).isEqualTo("ADDRESS 123");
        assertThat(sc.countryISO2Code()).isEqualTo("US");
        assertThat(sc.countryName()).isEqualTo("COUNTRY NAME");
    }


    @Test
    void readFile_ioProblem_throwsRuntimeException() {
        Resource bad = new FileSystemResource(tempDir.resolve("missing.xlsx"));

        RuntimeException ex = catchThrowableOfType(
                () -> reader.readFile(bad),
                RuntimeException.class);

        assertThat(ex).hasMessageContaining("Failed to read Excel file");
        assertThat(ex.getCause())
                .isInstanceOf(IOException.class);
    }
}
