package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.impl;


import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelFileReaderImplTest {

    private final ExcelFileReaderImpl reader = new ExcelFileReaderImpl();

    @Test
    void readFile_validExcel_returnsParsedSwiftCodes() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(0).setCellValue("HEADER");
        Row data = sheet.createRow(1);
        data.createCell(0).setCellValue(" us ");
        data.createCell(1).setCellValue(" aaabbbccxxx ");
        data.createCell(3).setCellValue(" bank name ");
        data.createCell(4).setCellValue(" address 123 ");
        data.createCell(6).setCellValue(" country name ");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        byte[] excelBytes = baos.toByteArray();

        Resource resource = new ByteArrayResource(excelBytes);

        List<SwiftCode> codes = reader.readFile(resource);

        assertThat(codes).hasSize(1);
        SwiftCode swiftCode = codes.get(0);
        assertThat(swiftCode.swiftCode()).isEqualTo("AAABBBCCXXX");
        assertThat(swiftCode.bankName()).isEqualTo("BANK NAME");
        assertThat(swiftCode.address()).isEqualTo("ADDRESS 123");
        assertThat(swiftCode.countryISO2Code()).isEqualTo("US");
        assertThat(swiftCode.countryName()).isEqualTo("COUNTRY NAME");
    }

    @Test
    void readFile_ioException_throwsRuntimeException() throws Exception {
        Resource badResource = mock(Resource.class);
        when(badResource.getInputStream()).thenThrow(new IOException("disk gone"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reader.readFile(badResource)
        );
        assertThat(ex).hasMessageContaining("Failed to read Excel file");
        assertThat(ex.getCause()).isInstanceOf(IOException.class)
                .hasMessage("disk gone");
    }
}