package com.github.oleksiioliinyk.swiftcodemanager.domain.impl;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.contract.FileReaderContract;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeParserImplTest {

    @InjectMocks
    private SwiftCodeParserImpl parser;

    @Mock
    private SwiftCodeEntityRepository repo;

    @Mock
    private FileReaderContract fileReader;

    @Mock
    private Resource resource;

    private SwiftCode hq1, hq2, branch1, branch2;

    @BeforeEach
    void setUp() {
        // headquarters
        hq1 = new SwiftCode(
                null,
                "AAAABBBBXXX",
                "Bank A",
                "Addr A",
                "US",
                "United States",
                null
        );
        hq2 = new SwiftCode(
                null,
                "CCCCDDDDXXX",
                "Bank C",
                "Addr C",
                "FR",
                "France",
                null
        );
        // branches
        branch1 = new SwiftCode(
                null,
                "AAAABBBB001",
                "Bank A",
                "Addr A1",
                "US",
                "United States",
                null
        );
        branch2 = new SwiftCode(
                null,
                "EEEEFFFF001",
                "Bank E",
                "Addr E1",
                "DE",
                "Germany",
                null
        );
    }

    @Test
    void isInitialized_whenCountZero_returnsFalse() {
        when(repo.count()).thenReturn(0L);
        assertThat(parser.isInitialized()).isFalse();
    }

    @Test
    void isInitialized_whenCountNonZero_returnsTrue() {
        when(repo.count()).thenReturn(5L);
        assertThat(parser.isInitialized()).isTrue();
    }

    @Test
    void parseAndSave_emptyFile_doesNothing() {
        when(fileReader.readFile(resource)).thenReturn(List.of());

        parser.parseAndSave(resource);

        verify(fileReader).readFile(resource);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SwiftCode>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo, times(2)).saveAll(captor.capture());

        List<List<SwiftCode>> allLists = captor.getAllValues();
        assertThat(allLists).hasSize(2);
        assertThat(allLists.get(0)).isEmpty();
        assertThat(allLists.get(1)).isEmpty();

        verify(repo, never()).findSwiftCodeBySwiftCode(anyString());
    }

    @Test
    void parseAndSave_onlyHeadquarters_savesAllAsOneBatch() {
        when(fileReader.readFile(resource)).thenReturn(List.of(hq1, hq2));

        parser.parseAndSave(resource);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SwiftCode>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo, times(2)).saveAll(captor.capture());

        List<List<SwiftCode>> allLists = captor.getAllValues();
        assertThat(allLists.get(0))
                .containsExactlyInAnyOrder(hq1, hq2);
        assertThat(allLists.get(1)).isEmpty();

        verify(repo, never()).findSwiftCodeBySwiftCode(anyString());
    }

    @Test
    void parseAndSave_onlyBranches_savesAllBranchesWithNullParent() {
        when(fileReader.readFile(resource)).thenReturn(List.of(branch1, branch2));

        parser.parseAndSave(resource);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SwiftCode>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo, times(2)).saveAll(captor.capture());

        List<List<SwiftCode>> allLists = captor.getAllValues();
        assertThat(allLists.get(0)).isEmpty();

        List<SwiftCode> savedBranches = allLists.get(1);
        assertThat(savedBranches)
                .hasSize(2)
                .extracting(SwiftCode::swiftCode)
                .containsExactlyInAnyOrder("AAAABBBB001", "EEEEFFFF001");

        verify(repo, never()).findSwiftCodeBySwiftCode(anyString());
    }

    @Test
    void parseAndSave_mixed_entries_savesHQthenBranches_andLooksUpParents() {
        when(fileReader.readFile(resource))
                .thenReturn(List.of(hq1, branch1, branch2));

        when(repo.findSwiftCodeBySwiftCode("AAAABBBBXXX")).thenReturn(hq1);

        parser.parseAndSave(resource);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SwiftCode>> captor =
                ArgumentCaptor.forClass((Class) List.class);

        verify(repo, times(2)).saveAll(captor.capture());
        List<List<SwiftCode>> allSaved = captor.getAllValues();

        List<SwiftCode> hqSaved = allSaved.get(0);
        assertThat(hqSaved).containsExactly(hq1);

        List<SwiftCode> branchSaved = allSaved.get(1);
        assertThat(branchSaved).hasSize(2);

        assertThat(branchSaved).filteredOn(s -> s.swiftCode().equals("AAAABBBB001"))
                .singleElement()
                .extracting(SwiftCode::headquarter)
                .isEqualTo(hq1);

        assertThat(branchSaved).filteredOn(s -> s.swiftCode().equals("EEEEFFFF001"))
                .singleElement()
                .extracting(SwiftCode::headquarter)
                .isNull();

        verify(repo).findSwiftCodeBySwiftCode("AAAABBBBXXX");
        verify(repo, never()).findSwiftCodeBySwiftCode("EEEEFFFFXXX");
    }
}