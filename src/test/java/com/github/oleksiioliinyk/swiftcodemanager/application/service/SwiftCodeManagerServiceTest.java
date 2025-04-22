package com.github.oleksiioliinyk.swiftcodemanager.application.service;

import com.github.oleksiioliinyk.swiftcodemanager.application.mapper.SwiftCodeDTOMapper;
import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeManager;
import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.presentation.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeManagerServiceTest {

    @InjectMocks
    private SwiftCodeManagerService service;

    @Mock
    private SwiftCodeManager swiftCodeManager;

    @Mock
    private SwiftCodeDTOMapper swiftCodeDTOMapper;

    private SwiftCodeDTO headquarterDto;
    private SwiftCode headquarterDomain;
    private SwiftCode branchDomain;
    private SwiftCodeDTO branchDto;

    @BeforeEach
    void init() {
        branchDomain = new SwiftCode(
                null,
                "AAAABBCC001",
                "Branch Bank",
                "Branch Address",
                "US",
                "United States",
                null
        );
        branchDto = new SwiftCodeDTO(
                null,
                "AAAABBCC001",
                "Branch Bank",
                "Branch Address",
                "US",
                "United States",
                null
        );
        headquarterDto = new SwiftCodeDTO(
                null,
                "AAAABBCCXXX",
                "Some Bank",
                "123 Main St",
                "US",
                "United States",
                null
        );
        headquarterDomain = new SwiftCode(
                null,
                "AAAABBCCXXX",
                "Some Bank",
                "123 Main St",
                "US",
                "United States",
                null
        );
    }

    @Test
    void findSwiftCode_headquarterWithBranches() {
        when(swiftCodeManager.findSwiftCode("AAAABBCCXXX"))
                .thenReturn(headquarterDomain);

        when(swiftCodeDTOMapper.toDto(headquarterDomain))
                .thenReturn(headquarterDto);

        SwiftCode branchDomain = new SwiftCode(
                null,
                "AAAABBCC001",
                "Some Bank",
                "456 Side St",
                "US",
                "United States",
                null
        );
        SwiftCodeDTO branchDto = new SwiftCodeDTO(
                null,
                "AAAABBCC001",
                "Some Bank",
                "456 Side St",
                "US",
                "United States",
                null
        );

        when(swiftCodeManager.findBranchesForHeadquarter("AAAABBCCXXX"))
                .thenReturn(List.of(branchDomain));
        when(swiftCodeDTOMapper.toDto(branchDomain))
                .thenReturn(branchDto);

        SwiftCodeWithBranchesResponse response =
                service.findSwiftCode("AAAABBCCXXX");

        assertThat(response.getAddress())
                .isEqualTo(headquarterDto.getAddress());
        assertThat(response.getBankName())
                .isEqualTo(headquarterDto.getBankName());
        assertThat(response.getCountryISO2())
                .isEqualTo(headquarterDto.getCountryISO2Code());
        assertThat(response.getCountryName())
                .isEqualTo(headquarterDto.getCountryName());
        assertThat(response.getIsHeadquarter())
                .isTrue();
        assertThat(response.getSwiftCode())
                .isEqualTo(headquarterDto.getSwiftCode());

        assertThat(response.getBranches())
                .hasSize(1);
        SwiftCodeBranchResponse br = response.getBranches().get(0);
        assertThat(br.getAddress()).isEqualTo(branchDto.getAddress());
        assertThat(br.getBankName()).isEqualTo(branchDto.getBankName());
        assertThat(br.getCountryISO2()).isEqualTo(branchDto.getCountryISO2Code());
        assertThat(br.getCountryName()).isEqualTo(branchDto.getCountryName());
        assertThat(br.getIsHeadquarter()).isFalse();
        assertThat(br.getSwiftCode()).isEqualTo(branchDto.getSwiftCode());

        verify(swiftCodeManager).findSwiftCode("AAAABBCCXXX");
        verify(swiftCodeDTOMapper).toDto(headquarterDomain);
        verify(swiftCodeManager).findBranchesForHeadquarter("AAAABBCCXXX");
        verify(swiftCodeDTOMapper).toDto(branchDomain);
    }

    @Test
    void findSwiftCode_notFound_throws() {
        when(swiftCodeManager.findSwiftCode("NONEXISTENT"))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Provided SWIFT code was not found."
                ));

        assertThatThrownBy(() -> service.findSwiftCode("NONEXISTENT"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException rse = (ResponseStatusException) exception;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).isEqualTo("Provided SWIFT code was not found.");
                });
    }

    @Test
    void findByCountry_happyPath() {
        when(swiftCodeManager.findSwiftCodesForCountry("US"))
                .thenReturn(List.of(headquarterDomain));
        when(swiftCodeDTOMapper.toDto(headquarterDomain))
                .thenReturn(headquarterDto);

        SwiftCodesByCountryResponse response =
                service.findSwiftCodeByCountryISO2Code("US");

        assertThat(response.getCountryISO2()).isEqualTo("US");
        assertThat(response.getCountryName()).isEqualTo(headquarterDto.getCountryName());

        assertThat(response.getSwiftCodes()).hasSize(1);

        SwiftCodeCountryResponse swiftCodeCountryResponse = response.getSwiftCodes().get(0);
        assertThat(swiftCodeCountryResponse.getSwiftCode()).isEqualTo(headquarterDto.getSwiftCode());
        assertThat(swiftCodeCountryResponse.getAddress()).isEqualTo(headquarterDto.getAddress());
        assertThat(swiftCodeCountryResponse.getBankName()).isEqualTo(headquarterDto.getBankName());
        assertThat(swiftCodeCountryResponse.getCountryISO2()).isEqualTo(headquarterDto.getCountryISO2Code());
        assertThat(swiftCodeCountryResponse.getIsHeadquarter()).isTrue();
    }

    @Test
    void findByCountry_notFound_throws() {
        when(swiftCodeManager.findSwiftCodesForCountry("LL"))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "SWIFT codes for provided country ISO2 code were not found."
                ));

        assertThatThrownBy(() -> service.findSwiftCodeByCountryISO2Code("LL"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException rse = (ResponseStatusException) exception;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(rse.getReason()).isEqualTo("SWIFT codes for provided country ISO2 code were not found.");
                });
    }

    @Test
    void createSwiftCode_branch_invokesManager() {
        var request = new CreateSwiftCodeRequest(
                "456 Avenue des Champs‑Élysées, 75008 Paris",
                "BANQUE PARISIENNE DE CRÉDIT",
                "US",
                "United States",
                "AAAABBCC001",
                false
        );

        when(swiftCodeManager.findHeadquarterForBranch("AAAABBCC001"))
                .thenReturn(headquarterDomain);

        when(swiftCodeDTOMapper.toDto(headquarterDomain))
                .thenReturn(headquarterDto);

        ArgumentCaptor<SwiftCodeDTO> dtoCaptor =
                ArgumentCaptor.forClass(SwiftCodeDTO.class);

        SwiftCode branchDomain = new SwiftCode(
                null,
                "AAAABBCC001",
                "BANQUE PARISIENNE DE CRÉDIT",
                "456 Avenue des Champs‑Élysées, 75008 Paris",
                "US",
                "United States",
                headquarterDomain //headquarter
        );
        when(swiftCodeDTOMapper.toDomain(dtoCaptor.capture()))
                .thenReturn(branchDomain);

        service.createSwiftCode(request);

        SwiftCodeDTO captured = dtoCaptor.getValue();
        assertThat(captured.getSwiftCode()).isEqualTo("AAAABBCC001");
        assertThat(captured.getHeadquarter()).isSameAs(headquarterDto);

        verify(swiftCodeManager).createSwiftCode(branchDomain);
    }

    @Test
    void createSwiftCode_headquarter_savesHQAndBranches() {
        var request = new CreateSwiftCodeRequest(
                "123 Main St",
                "Some Bank",
                "US",
                "United States",
                "AAAABBCCXXX",
                true
        );

        when(swiftCodeDTOMapper.toDomain(argThat(dto ->
                "AAAABBCCXXX".equals(dto.getSwiftCode())
        ))).thenReturn(headquarterDomain);

        when(swiftCodeManager.createSwiftCode(headquarterDomain))
                .thenReturn(headquarterDomain);

        when(swiftCodeDTOMapper.toDto(headquarterDomain))
                .thenReturn(headquarterDto);

        SwiftCode branchDom = new SwiftCode(
                null,
                "AAAABBCC001",
                "Bank2",
                "789 Other St",
                "FR",
                "France",
                null
        );
        SwiftCodeDTO BranchDto = new SwiftCodeDTO(
                null,
                "AAAABBCC002",
                "Bank2",
                "789 Other St",
                "FR",
                "France",
                null
        );
        when(swiftCodeManager.findBranchesForHeadquarter("AAAABBCCXXX"))
                .thenReturn(List.of(branchDom));
        when(swiftCodeDTOMapper.toDto(branchDom))
                .thenReturn(BranchDto);
        when(swiftCodeDTOMapper.toDomain(BranchDto))
                .thenReturn(branchDom);

        service.createSwiftCode(request);

        InOrder inOrder = inOrder(swiftCodeManager);
        inOrder.verify(swiftCodeManager).createSwiftCode(headquarterDomain);
        inOrder.verify(swiftCodeManager).findBranchesForHeadquarter("AAAABBCCXXX");
        inOrder.verify(swiftCodeManager).createSwiftCode(branchDom);

        verifyNoMoreInteractions(swiftCodeManager);
    }

    @Test
    void deleteSwiftCode_branchOnly() {
        String code = "AAAABBCC001";
        when(swiftCodeManager.findSwiftCode(code)).thenReturn(branchDomain);

        service.deleteSwiftCode(code);

        verify(swiftCodeManager, times(1)).deleteSwiftCode(branchDomain);
        verify(swiftCodeManager, never()).findBranchesForHeadquarter(anyString());
        verifyNoMoreInteractions(swiftCodeManager, swiftCodeDTOMapper);
    }

    @Test
    void deleteSwiftCode_headquarter() {
        String code = "AAAABBCCXXX";
        when(swiftCodeManager.findBranchesForHeadquarter(code))
                .thenReturn(List.of(branchDomain));
        when(swiftCodeDTOMapper.toDto(branchDomain)).thenReturn(branchDto);
        when(swiftCodeDTOMapper.toDomain(branchDto)).thenReturn(branchDomain);
        when(swiftCodeManager.findSwiftCode(code)).thenReturn(headquarterDomain);

        service.deleteSwiftCode(code);

        InOrder inOrder = inOrder(swiftCodeManager);
        inOrder.verify(swiftCodeManager).deleteSwiftCode(branchDomain);
        inOrder.verify(swiftCodeManager).deleteSwiftCode(headquarterDomain);

        verifyNoMoreInteractions(swiftCodeManager);
    }
}