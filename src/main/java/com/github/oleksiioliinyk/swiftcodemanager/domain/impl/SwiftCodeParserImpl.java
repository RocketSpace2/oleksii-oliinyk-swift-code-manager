package com.github.oleksiioliinyk.swiftcodemanager.domain.impl;

import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.contract.FileReaderContract;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeParser;
import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.mapper.contract.SwiftCodeEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SwiftCodeParserImpl implements SwiftCodeParser {
    @Autowired
    private SwiftCodeEntityRepository swiftCodeEntityRepository;
    @Autowired
    private FileReaderContract fileReader;
    @Autowired
    private SwiftCodeEntityMapper swiftCodeEntityMapper;

    @Override
    public boolean isInitialized() {
        return swiftCodeEntityRepository.count() != 0;
    }

    @Override
    @Transactional
    public void parseAndSave(Resource file) {
        //Read everything from the file
        List<SwiftCode> swiftCodes = fileReader.readFile(file);

        //Build a map of HQ codes (first 8 chars -> headquarter SwiftCode)
        Map<String, SwiftCode> headquarterMap = new HashMap<>();
        for (SwiftCode swiftCode : swiftCodes) {
            if (swiftCode.swiftCode().endsWith("XXX")) {
                headquarterMap.put(
                        swiftCode.swiftCode().substring(0, 8),
                        swiftCode
                );
            }
        }

        //Save all headquarters
        swiftCodeEntityRepository.saveAll(new ArrayList<>(headquarterMap.values()));

        List<SwiftCode> branches = new ArrayList<>();
        //Link each branch to its headquarter (if one exists)
        for (SwiftCode current : swiftCodes) {
            if (!current.swiftCode().endsWith("XXX")) {
                String prefix = current.swiftCode().substring(0, 8);
                SwiftCode parent = headquarterMap.get(prefix);

                //Replace branch entry with a new instance that holds its parent
                branches.add(new SwiftCode(
                        null,
                        current.swiftCode(),
                        current.bankName(),
                        current.address(),
                        current.countryISO2Code(),
                        current.countryName(),
                        parent != null ? swiftCodeEntityRepository.findSwiftCodeBySwiftCode(parent.swiftCode()) : null
                ));
            }
        }

        //Save all branches
        swiftCodeEntityRepository.saveAll(branches);
    }
}
