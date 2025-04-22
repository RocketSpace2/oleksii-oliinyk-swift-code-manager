package com.github.oleksiioliinyk.swiftcodemanager.application.service;

import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SwiftCodeInitializationService implements ApplicationRunner {
    @Autowired
    private SwiftCodeParser swiftCodeParser;

    @Value("classpath:swift-codes.xlsx")
    private Resource file;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!swiftCodeParser.isInitialized()) {
            swiftCodeParser.parseAndSave(file);
        }
    }
}
