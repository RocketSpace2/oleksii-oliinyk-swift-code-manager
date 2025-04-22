package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.impl;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.contract.FileReaderContract;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


import org.apache.poi.ss.usermodel.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Component
public class ExcelFileReaderImpl implements FileReaderContract {
    @Override
    public List<SwiftCode> readFile(Resource file) {
        List<SwiftCode> swiftCodes = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (
                InputStream inputStream = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            //Read each sheet
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);

                boolean firstRow = true;
                //Read each row
                for (Row row : sheet) {
                    //Skip header
                    if (firstRow) {
                        firstRow = false;
                        continue;
                    }
                    //Read each column as String
                    String countryIso2Code = formatter.formatCellValue(row.getCell(0)).trim().toUpperCase();
                    String swiftCode = formatter.formatCellValue(row.getCell(1)).trim().toUpperCase();
                    String bankName = formatter.formatCellValue(row.getCell(3)).trim().toUpperCase();
                    String address = formatter.formatCellValue(row.getCell(4)).trim().toUpperCase();
                    String countryName = formatter.formatCellValue(row.getCell(6)).trim().toUpperCase();

                    swiftCodes.add(new SwiftCode(
                            null,
                            swiftCode,
                            bankName,
                            address,
                            countryIso2Code,
                            countryName,
                            null
                    ));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return swiftCodes;
    }
}
