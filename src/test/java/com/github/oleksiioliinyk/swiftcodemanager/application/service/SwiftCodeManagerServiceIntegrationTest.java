package com.github.oleksiioliinyk.swiftcodemanager.application.service;

import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeManager;
import com.github.oleksiioliinyk.swiftcodemanager.presentation.dto.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;

@SpringBootTest
public class SwiftCodeManagerServiceIntegrationTest {
    @Autowired
    private SwiftCodeManagerService service;

    @MockitoBean
    private SwiftCodeManager swiftCodeManager;

    @MockitoBean
    private com.github.oleksiioliinyk.swiftcodemanager.application.mapper.SwiftCodeDTOMapper swiftCodeDTOMapper;

    // shared fixtures
    private SwiftCodeDTO headquarterDto;
    private com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode headquarterDomain;
    private com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode branchDomain;
    private SwiftCodeDTO branchDto;

    @BeforeEach
    void setUp() {
        clearInvocations(swiftCodeManager, swiftCodeDTOMapper);

        headquarterDomain = new com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode(
                null, "AAAABBCCXXX",
                "Some Bank", "123 Main St",
                "US", "United States", null
        );
        headquarterDto = new SwiftCodeDTO(
                null, "AAAABBCCXXX",
                "Some Bank", "123 Main St",
                "US", "United States", null
        );

        branchDomain = new com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode(
                null, "AAAABBCC001",
                "Branch Bank", "456 Side St",
                "US", "United States", null
        );
        branchDto = new SwiftCodeDTO(
                null, "AAAABBCC001",
                "Branch Bank", "456 Side St",
                "US", "United States", null
        );
    }

    @Test
    void findSwiftCode_headquarterWithBranches() {
        given(swiftCodeManager.findSwiftCode("AAAABBCCXXX"))
                .willReturn(headquarterDomain);
        given(swiftCodeDTOMapper.toDto(headquarterDomain))
                .willReturn(headquarterDto);

        given(swiftCodeManager.findBranchesForHeadquarter("AAAABBCCXXX"))
                .willReturn(List.of(branchDomain));
        given(swiftCodeDTOMapper.toDto(branchDomain))
                .willReturn(branchDto);

        SwiftCodeWithBranchesResponse resp = service.findSwiftCode("AAAABBCCXXX");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(resp.getSwiftCode()).isEqualTo("AAAABBCCXXX");
        softly.assertThat(resp.getIsHeadquarter()).isTrue();
        softly.assertThat(resp.getBankName()).isEqualTo("Some Bank");
        softly.assertThat(resp.getAddress()).isEqualTo("123 Main St");
        softly.assertThat(resp.getCountryISO2()).isEqualTo("US");
        softly.assertThat(resp.getCountryName()).isEqualTo("United States");

        softly.assertThat(resp.getBranches()).hasSize(1);
        var b = resp.getBranches().get(0);
        softly.assertThat(b.getSwiftCode()).isEqualTo("AAAABBCC001");
        softly.assertThat(b.getIsHeadquarter()).isFalse();
        softly.assertThat(b.getBankName()).isEqualTo("Branch Bank");
        softly.assertThat(b.getAddress()).isEqualTo("456 Side St");
        softly.assertThat(b.getCountryISO2()).isEqualTo("US");
        softly.assertThat(b.getCountryName()).isEqualTo("United States");
        softly.assertAll();

        then(swiftCodeManager).should().findSwiftCode("AAAABBCCXXX");
        then(swiftCodeDTOMapper).should().toDto(headquarterDomain);
        then(swiftCodeManager).should().findBranchesForHeadquarter("AAAABBCCXXX");
        then(swiftCodeDTOMapper).should().toDto(branchDomain);
    }

    @Test
    void findSwiftCode_nonexistent_throwsBadRequest() {

        willThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Provided SWIFT code was not found."
        )).given(swiftCodeManager).findSwiftCode("NONEXISTENT");

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        service.findSwiftCode("NONEXISTENT")
                )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    var rse = (ResponseStatusException) ex;
                    org.assertj.core.api.Assertions.assertThat(rse.getReason())
                            .isEqualTo("Provided SWIFT code was not found.");
                });
    }

    @Test
    void findByCountry_happyPath() {
        given(swiftCodeManager.findSwiftCodesForCountry("US"))
                .willReturn(List.of(headquarterDomain));
        given(swiftCodeDTOMapper.toDto(headquarterDomain))
                .willReturn(headquarterDto);

        SwiftCodesByCountryResponse resp = service.findSwiftCodeByCountryISO2Code("US");

        org.assertj.core.api.Assertions.assertThat(resp.getCountryISO2()).isEqualTo("US");
        org.assertj.core.api.Assertions.assertThat(resp.getCountryName())
                .isEqualTo("United States");

        org.assertj.core.api.Assertions.assertThat(resp.getSwiftCodes()).hasSize(1);
        var c = resp.getSwiftCodes().get(0);
        org.assertj.core.api.Assertions.assertThat(c.getSwiftCode())
                .isEqualTo("AAAABBCCXXX");
        org.assertj.core.api.Assertions.assertThat(c.getIsHeadquarter()).isTrue();
        then(swiftCodeManager).should().findSwiftCodesForCountry("US");
    }

    @Test
    void findByCountry_notFound_throwsBadRequest() {
        willThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "SWIFT codes for provided country ISO2 code were not found."
        )).given(swiftCodeManager).findSwiftCodesForCountry("LL");

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        service.findSwiftCodeByCountryISO2Code("LL")
                )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    var rse = (ResponseStatusException) ex;
                    org.assertj.core.api.Assertions.assertThat(rse.getReason())
                            .isEqualTo("SWIFT codes for provided country ISO2 code were not found.");
                });
    }

    @Test
    void createSwiftCode_branch_invokesManagerOnlyForBranch() {
        var req = new CreateSwiftCodeRequest(
                "Some Addr", "Branch Bank",
                "US", "United States",
                "AAAABBCC001", false
        );

        given(swiftCodeManager.findHeadquarterForBranch("AAAABBCC001"))
                .willReturn(headquarterDomain);
        given(swiftCodeDTOMapper.toDto(headquarterDomain))
                .willReturn(headquarterDto);

        ArgumentCaptor<SwiftCodeDTO> cap = ArgumentCaptor.forClass(SwiftCodeDTO.class);
        given(swiftCodeDTOMapper.toDomain(cap.capture()))
                .willReturn(branchDomain);

        service.createSwiftCode(req);

        var built = cap.getValue();
        org.assertj.core.api.Assertions.assertThat(built.getSwiftCode())
                .isEqualTo("AAAABBCC001");
        org.assertj.core.api.Assertions.assertThat(built.getHeadquarter())
                .isSameAs(headquarterDto);

        then(swiftCodeManager).should().createSwiftCode(branchDomain);
        then(swiftCodeManager).should(never())
                .createSwiftCode(argThat(d -> d.swiftCode().endsWith("XXX")));
    }

    @Test
    void createSwiftCode_headquarter_savesHQ_thenBranches() {
        var req = new CreateSwiftCodeRequest(
                "123 Main St", "Some Bank",
                "US", "United States",
                "AAAABBCCXXX", true
        );
        given(swiftCodeDTOMapper.toDomain(any())).willReturn(headquarterDomain);
        given(swiftCodeManager.createSwiftCode(headquarterDomain))
                .willReturn(headquarterDomain);
        given(swiftCodeDTOMapper.toDto(headquarterDomain))
                .willReturn(headquarterDto);

        var otherBranch = new com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode(
                null, "BBBBAA11YYY",
                "B2", "Addr2", "FR", "France", null
        );
        var otherDto = new SwiftCodeDTO(
                null, "BBBBAA11YYY",
                "B2", "Addr2", "FR", "France", null
        );
        given(swiftCodeManager.findBranchesForHeadquarter("AAAABBCCXXX"))
                .willReturn(List.of(otherBranch));
        given(swiftCodeDTOMapper.toDto(otherBranch)).willReturn(otherDto);
        given(swiftCodeDTOMapper.toDomain(otherDto)).willReturn(otherBranch);

        service.createSwiftCode(req);

        var order = inOrder(swiftCodeManager);
        order.verify(swiftCodeManager).createSwiftCode(headquarterDomain);
        order.verify(swiftCodeManager).findBranchesForHeadquarter("AAAABBCCXXX");
        order.verify(swiftCodeManager).createSwiftCode(otherBranch);
    }

    @Test
    void deleteSwiftCode_branchOnly() {
        given(swiftCodeManager.findSwiftCode("AAAABBCC001"))
                .willReturn(branchDomain);

        service.deleteSwiftCode("AAAABBCC001");

        then(swiftCodeManager).should().deleteSwiftCode(branchDomain);
        then(swiftCodeManager).should(never())
                .findBranchesForHeadquarter(anyString());
    }

    @Test
    void deleteSwiftCode_headquarter_deletesBranchesThenHQ() {
        // given
        given(swiftCodeManager.findBranchesForHeadquarter("AAAABBCCXXX"))
                .willReturn(List.of(branchDomain));
        given(swiftCodeDTOMapper.toDto(branchDomain)).willReturn(branchDto);
        given(swiftCodeDTOMapper.toDomain(branchDto)).willReturn(branchDomain);
        given(swiftCodeManager.findSwiftCode("AAAABBCCXXX"))
                .willReturn(headquarterDomain);

        // when
        service.deleteSwiftCode("AAAABBCCXXX");

        // then
        var ord = inOrder(swiftCodeManager);
        ord.verify(swiftCodeManager).deleteSwiftCode(branchDomain);
        ord.verify(swiftCodeManager).deleteSwiftCode(headquarterDomain);
    }
}
