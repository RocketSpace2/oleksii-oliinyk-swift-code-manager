package com.github.oleksiioliinyk.swiftcodemanager.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;


@SpringBootTest
public class SwiftCodeInitializationServiceIntegrationTest {
    @Autowired
    private SwiftCodeInitializationService initializationService;

    @MockitoBean
    private SwiftCodeParser swiftCodeParser;

    @Value("classpath:swift-codes.xlsx")
    private Resource file;

    @Autowired
    private ApplicationArguments args;

    @Test
    void run_notInitialized_invokesParseAndSave() throws Exception {
        when(swiftCodeParser.isInitialized()).thenReturn(false);

        clearInvocations(swiftCodeParser);

        initializationService.run(args);

        verify(swiftCodeParser).isInitialized();
        verify(swiftCodeParser).parseAndSave(file);
        verifyNoMoreInteractions(swiftCodeParser);
    }

    @Test
    void run_alreadyInitialized_doesNotParseOrSave() throws Exception {
        when(swiftCodeParser.isInitialized()).thenReturn(true);

        initializationService.run(args);

        verify(swiftCodeParser).isInitialized();
        verify(swiftCodeParser, never()).parseAndSave(any(Resource.class));
        verifyNoMoreInteractions(swiftCodeParser);
    }
}
