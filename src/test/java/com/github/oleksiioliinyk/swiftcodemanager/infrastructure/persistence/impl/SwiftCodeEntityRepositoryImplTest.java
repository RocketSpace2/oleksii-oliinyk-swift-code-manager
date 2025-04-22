package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.impl;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.mapper.contract.SwiftCodeEntityMapper;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.repository.SwiftCodeEntityJpaRepository;
import jakarta.validation.constraints.Null;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeEntityRepositoryImplTest {

    @InjectMocks
    private SwiftCodeEntityRepositoryImpl repoImpl;

    @Mock
    private SwiftCodeEntityJpaRepository jpa;

    @Mock
    private SwiftCodeEntityMapper mapper;

    private SwiftCode domain;
    private SwiftCodeEntity entity;

    @BeforeEach
    void setUp() {
        domain = new SwiftCode(
                null,
                "ABCDEF12XXX",
                "Bank",
                "Address",
                "US",
                "United States",
                null
        );
        entity = new SwiftCodeEntity();
        entity.setSwiftCode("ABCDEF12XXX");
    }

    @Test
    void count_delegates() {
        when(jpa.count()).thenReturn(42L);
        assertThat(repoImpl.count()).isEqualTo(42L);
        verify(jpa).count();
    }

    @Test
    void saveAll_mapsAndSaves() {
        List<SwiftCode> inputs = List.of(domain, domain);
        SwiftCodeEntity e1 = new SwiftCodeEntity();
        SwiftCodeEntity e2 = new SwiftCodeEntity();
        when(mapper.toEntityForFile(domain, repoImpl)).thenReturn(e1, e2);

        repoImpl.saveAll(inputs);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SwiftCodeEntity>> cap =
                ArgumentCaptor.forClass(List.class);
        verify(jpa).saveAll(cap.capture());
        assertThat(cap.getValue()).containsExactly(e1, e2);
    }

    @Test
    void deleteSwiftCode_success() {
        when(mapper.toEntity(domain)).thenReturn(entity);
        // no exception
        repoImpl.deleteSwiftCode(domain);
        verify(jpa).delete(entity);
    }

    @Test
    void deleteSwiftCode_notFound_throwsBadRequest() {
        when(mapper.toEntity(domain)).thenReturn(entity);
        doThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "SWIFT code was not deleted. Provided SWIFT code does not exists."
        )).when(jpa).delete(entity);

        ResponseStatusException ex = catchThrowableOfType(
                () -> repoImpl.deleteSwiftCode(domain),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason())
                .isEqualTo("SWIFT code was not deleted. Provided SWIFT code does not exists.");
    }

    @Test
    void findSwiftCodeBySwiftCode_mapsThrough() {
        when(repoImpl.findSwiftCodeWithoutMappingBySwiftCode("ABCDEF12XXX"))
                .thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        SwiftCode out = repoImpl.findSwiftCodeBySwiftCode("ABCDEF12XXX");
        assertThat(out).isSameAs(domain);
    }

    @Test
    void findSwiftCodeWithoutMapping_delegatesIgnoreCase() {
        when(jpa.findSwiftCodeEntityBySwiftCodeIgnoreCase("abcdef12xxx"))
                .thenReturn(entity);
        assertThat(repoImpl.findSwiftCodeWithoutMappingBySwiftCode("abcdef12xxx"))
                .isSameAs(entity);
    }

    @Test
    void findBranches_delegatesAndMaps() {
        SwiftCodeEntity e1 = new SwiftCodeEntity(); e1.setSwiftCode("ABCDEF12001");
        SwiftCodeEntity e2 = new SwiftCodeEntity(); e2.setSwiftCode("ABCDEF12002");
        when(jpa.findAllBySwiftCodeStartingWithAndSwiftCodeNotLikeAllIgnoreCase("ABCDEF12", "%XXX"))
                .thenReturn(List.of(e1, e2));
        SwiftCode d1 = new SwiftCode(
                null,
                "ABCDEF12001",
                "Bank 1",
                "Address1",
                "US",
                "United States",
                null
        );
        SwiftCode d2 = new SwiftCode(
                null,
                "ABCDEF12002",
                "Bank 2",
                "Address2",
                "US",
                "United States",
                null
        );
        when(mapper.toDomain(e1)).thenReturn(d1);
        when(mapper.toDomain(e2)).thenReturn(d2);

        List<SwiftCode> branches = repoImpl.findBranches("ABCDEF12","%XXX");
        assertThat(branches).containsExactly(d1, d2);
    }

    @Test
    void findSwiftCodeByCountry_nonEmpty_returnsList() {
        SwiftCodeEntity e = new SwiftCodeEntity(
                1L,
                "ZZZ000000XXX",
                "",
                "",
                "US",
                "United States",
                null
        );

        when(jpa.findSwiftCodeByCountryISO2Code("US"))
                .thenReturn(List.of(e));
        SwiftCode d = new SwiftCode(
                1L,
                "ZZZ000000XXX",
                "",
                "",
                "US",
                "",
                null
        );
        when(mapper.toDomain(e)).thenReturn(d);

        List<SwiftCode> result = repoImpl.findSwiftCodeByCountryISO2Code("US");
        assertThat(result).containsExactly(d);
    }

    @Test
    void findSwiftCodeByCountry_empty_throwsBadRequest() {
        when(jpa.findSwiftCodeByCountryISO2Code("XX"))
                .thenReturn(List.of());
        ResponseStatusException ex = catchThrowableOfType(
                () -> repoImpl.findSwiftCodeByCountryISO2Code("XX"),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason())
                .isEqualTo("SWIFT codes for provided country ISO2 code were not found.");
    }

    @Test
    void createSwiftCode_success() {
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpa.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domain);

        SwiftCode out = repoImpl.createSwiftCode(domain);
        assertThat(out).isSameAs(domain);
    }

    @Test
    void createSwiftCode_duplicate_throwsBadRequest() {
        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpa.save(entity)).thenThrow(new DataIntegrityViolationException("dup"));

        ResponseStatusException ex = catchThrowableOfType(
                () -> repoImpl.createSwiftCode(domain),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason())
                .isEqualTo("SWIFT code was not created. ABCDEF12XXX already exists. Please provide correct data.");
    }
}