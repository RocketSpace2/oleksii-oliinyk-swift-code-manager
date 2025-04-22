package com.github.oleksiioliinyk.swiftcodemanager.application.service;

import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SwiftCodeInitializationServiceTest {

    @InjectMocks
    private SwiftCodeInitializationService initializationService;

    @Mock
    private SwiftCodeParser swiftCodeParser;

    @Mock
    private Resource file;

    private ApplicationArguments args;

    @BeforeEach
    void setUp() {
        args = mock(ApplicationArguments.class);
        ReflectionTestUtils.setField(
                initializationService,
                "file",
                file
        );
    }

    @Test
    void run_alreadyInitialized_doesNotParseOrSave() throws Exception {
        when(swiftCodeParser.isInitialized()).thenReturn(true);

        initializationService.run(args);

        verify(swiftCodeParser).isInitialized();
        verify(swiftCodeParser, never()).parseAndSave(any());
        verifyNoMoreInteractions(swiftCodeParser);
    }

    @Test
    void run_notInitialized_invokesParseAndSave() throws Exception {
        when(swiftCodeParser.isInitialized()).thenReturn(false);

        initializationService.run(args);

        verify(swiftCodeParser).isInitialized();
        verify(swiftCodeParser).parseAndSave(file);
        verifyNoMoreInteractions(swiftCodeParser);
    }
}