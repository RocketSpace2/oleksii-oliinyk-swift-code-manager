package com.github.oleksiioliinyk.swiftcodemanager.domain.impl;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeManagerImplTest {

    @InjectMocks
    private SwiftCodeManagerImpl manager;

    @Mock
    private SwiftCodeEntityRepository repo;

    private SwiftCode sample, branch, headquarter;

    @BeforeEach
    void setUp() {
        sample = new SwiftCode(
                1L,
                "ABCDEFGHXXX",
                "HQ Bank",
                "HQ Address",
                "US",
                "United States",
                null
        );
        branch = new SwiftCode(
                2L,
                "ABCDEFGH001",
                "Branch Bank",
                "Branch Address",
                "US",
                "United States",
                null
        );
        headquarter = sample;
    }

    @Test
    void findSwiftCodesForCountry_delegatesAndReturns() {
        when(repo.findSwiftCodeByCountryISO2Code("US"))
                .thenReturn(List.of(sample, branch));

        List<SwiftCode> result = manager.findSwiftCodesForCountry("US");

        assertThat(result).containsExactly(sample, branch);
        verify(repo).findSwiftCodeByCountryISO2Code("US");
    }

    @Test
    void findHeadquarterForBranch_buildsXxxSuffixAndDelegates() {
        // branch code → headquarter code
        when(repo.findSwiftCodeBySwiftCode("ABCDEFGHXXX")).thenReturn(headquarter);

        SwiftCode found = manager.findHeadquarterForBranch("ABCDEFGH001");

        assertThat(found).isSameAs(headquarter);
        verify(repo).findSwiftCodeBySwiftCode("ABCDEFGHXXX");
    }

    @Test
    void findSwiftCode_delegatesByExactCode() {
        when(repo.findSwiftCodeBySwiftCode("SOMECODEXXX")).thenReturn(sample);

        SwiftCode found = manager.findSwiftCode("SOMECODEXXX");

        assertThat(found).isSameAs(sample);
        verify(repo).findSwiftCodeBySwiftCode("SOMECODEXXX");
    }

    @Test
    void createSwiftCode_delegatesToRepoCreate() {
        SwiftCode toCreate = new SwiftCode(
                null,
                "NEWCODEXXX",
                "Bank",
                "Addr",
                "CA",
                "Canada",
                null
        );
        when(repo.createSwiftCode(toCreate)).thenReturn(toCreate);

        SwiftCode created = manager.createSwiftCode(toCreate);

        assertThat(created).isSameAs(toCreate);
        verify(repo).createSwiftCode(toCreate);
    }

    @Test
    void findBranchesForHeadquarter_usesPrefixAndWildcard() {
        when(repo.findBranches("ABCDEFGH", "%XXX"))
                .thenReturn(List.of(branch));

        List<SwiftCode> branches = manager.findBranchesForHeadquarter("ABCDEFGHXXX");

        assertThat(branches).containsExactly(branch);
        verify(repo).findBranches("ABCDEFGH", "%XXX");
    }

    @Test
    void deleteSwiftCode_delegatesToRepoDelete() {
        manager.deleteSwiftCode(sample);
        verify(repo).deleteSwiftCode(sample);
    }
}