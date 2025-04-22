package com.github.oleksiioliinyk.swiftcodemanager.presentation;

import com.github.oleksiioliinyk.swiftcodemanager.application.service.SwiftCodeManagerService;
import com.github.oleksiioliinyk.swiftcodemanager.presentation.controller.SwiftCodeController;
import com.github.oleksiioliinyk.swiftcodemanager.presentation.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SwiftCodeController.class)
class SwiftCodeControllerUnitTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SwiftCodeManagerService service;

    @Test
    void findSwiftCode_ok() throws Exception {
        SwiftCodeWithBranchesResponse stub =
                new SwiftCodeWithBranchesResponse(
                        "123 Rue de Rivoli, 75001 Paris",
                        "BANQUE PARISIENNE DE CRÉDIT",
                        "FR",
                        "France",
                        true,
                        "BPCRFRPPXXX",
                        List.of(
                                new SwiftCodeBranchResponse(
                                        "456 Avenue des Champs‑Élysées, 75008 Paris",
                                        "BANQUE PARISIENNE DE CRÉDIT",
                                        "FR",
                                        "France",
                                        false,
                                        "BPCRFRPP001"
                                )
                        )
                );
        when(service.findSwiftCode("BPCRFRPPXXX")).thenReturn(stub);

        mvc.perform(get("/v1/swift-codes/BPCRFRPPXXX"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.address").value("123 Rue de Rivoli, 75001 Paris"))
                .andExpect(jsonPath("$.bankName").value("BANQUE PARISIENNE DE CRÉDIT"))
                .andExpect(jsonPath("$.countryISO2").value("FR"))
                .andExpect(jsonPath("$.countryName").value("France"))
                .andExpect(jsonPath("$.isHeadquarter").value(true))
                .andExpect(jsonPath("$.swiftCode").value("BPCRFRPPXXX"))
                .andExpect(jsonPath("$.branches", hasSize(1)))
                .andExpect(jsonPath("$.branches[0].address")
                        .value("456 Avenue des Champs‑Élysées, 75008 Paris"))
                .andExpect(jsonPath("$.branches[0].bankName")
                        .value("BANQUE PARISIENNE DE CRÉDIT"))
                .andExpect(jsonPath("$.branches[0].countryISO2").value("FR"))
                .andExpect(jsonPath("$.branches[0].isHeadquarter").value(false))
                .andExpect(jsonPath("$.branches[0].swiftCode").value("BPCRFRPP001"));
    }

    @Test
    void findSwiftCode_validationError() throws Exception {
        mvc.perform(get("/v1/swift-codes/SHORT"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messages", hasSize(1)))
                .andExpect(jsonPath("$.messages[0]")
                        .value("SWIFT code is incorrect or is not 11 characters long."));
    }

    @Test
    void findSwiftCode_notFoundKey() throws Exception {
        doThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Provided SWIFT code was not found."
        )).when(service).findSwiftCode(eq("BPCRFRPPXXX"));

        mvc.perform(get("/v1/swift-codes/BPCRFRPPXXX"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(
                        "Provided SWIFT code was not found."
                ));
    }

    @Test
    void findByCountry_ok() throws Exception {
        final var stub = getSwiftCodesByCountryResponse();
        when(service.findSwiftCodeByCountryISO2Code("FR"))
                .thenReturn(stub);

        mvc.perform(get("/v1/swift-codes/country/FR"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.countryISO2").value("FR"))
                .andExpect(jsonPath("$.countryName").value("France"))
                .andExpect(jsonPath("$.swiftCodes", hasSize(1)))
                .andExpect(jsonPath("$.swiftCodes[0].address").value("1 Rue de Test, 75000 Paris"))
                .andExpect(jsonPath("$.swiftCodes[0].bankName").value("TEST BANK"))
                .andExpect(jsonPath("$.swiftCodes[0].countryISO2").value("FR"))
                .andExpect(jsonPath("$.swiftCodes[0].isHeadquarter").value(true))
                .andExpect(jsonPath("$.swiftCodes[0].swiftCode").value("TESTFRPPXXX"));
    }

    private static SwiftCodesByCountryResponse getSwiftCodesByCountryResponse() {
        SwiftCodeCountryResponse code1 = new SwiftCodeCountryResponse(
                "1 Rue de Test, 75000 Paris",
                "TEST BANK",
                "FR",
                true,
                "TESTFRPPXXX"
        );


        return new SwiftCodesByCountryResponse(
                "FR",
                "France",
                List.of(code1)
        );
    }

    @Test
    void findByCountry_validationError() throws Exception {
        mvc.perform(get("/v1/swift-codes/country/fr"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messages", hasSize(1)))
                .andExpect(jsonPath("$.messages[0]")
                        .value("Country ISO2 code is incorrect or is not 2 characters long."));
    }

    @Test
    void findByCountry_notFoundKey() throws Exception {
        doThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "SWIFT codes for provided country ISO2 code were not found."
        )).when(service).findSwiftCodeByCountryISO2Code(eq("LL"));

        mvc.perform(get("/v1/swift-codes/country/LL"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(
                        "SWIFT codes for provided country ISO2 code were not found."
                ));
    }

    @Test
    void createSwiftCode_ok() throws Exception {
        String body = """
                {
                "address": "123 Rue de Rivoli, 75001 Paris",
                "bankName": "BANQUE PARISIENNE DE CRÉDIT",
                "countryISO2": "FR",
                "countryName": "France",
                "isHeadquarter": true,
                "swiftCode": "BPCRFRPPXXX"
                }
        """;

        mvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("SWIFT code was successfully created."));
    }

    @ParameterizedTest
    @MethodSource("invalidCreateJsonProvider")
    void createSwiftCode_validationError(String jsonBody, String expectedMessage) throws Exception {
        mvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messages", hasItem(expectedMessage)));
    }

    static Stream<Arguments> invalidCreateJsonProvider() {
        return Stream.of(
                Arguments.of(
                        """
                        {
                        "address": "123 Rue de Rivoli, 75001 Paris",
                        "bankName": "BANQUE PARISIENNE DE CRÉDIT",
                        "countryISO2": "FR",
                        "countryName": "France",
                        "isHeadquarter": true,
                        "swiftCode": "SHORT"
                        }
                        """,
                        "SWIFT code is incorrect or is not 11 characters long."
                ),
                Arguments.of(
                        """
                        {
                        "address": "123 Rue de Rivoli, 75001 Paris",
                        "bankName": "BANQUE PARISIENNE DE CRÉDIT",
                        "countryISO2": "fr",
                        "countryName": "France",
                        "isHeadquarter": true,
                        "swiftCode": "BPCRFRPPXXX"
                        }
                        """,
                        "Country ISO2 code is incorrect or is not 2 characters long."
                )
        );
    }

    @Test
    void createSwiftCode_duplicateKey() throws Exception {
        String validBody = """
            {
              "address":"ADDR",
              "bankName":"BANK",
              "countryISO2":"US",
              "countryName":"United States",
              "isHeadquarter":true,
              "swiftCode":"BANKUS33XXX"
            }
            """;

        doThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "SWIFT code was not created. BANKUS33XXX already exists. Please provide correct data."
        )).when(service).createSwiftCode(any(CreateSwiftCodeRequest.class));

        mvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(
                        "SWIFT code was not created. BANKUS33XXX already exists. Please provide correct data."
                ));
    }

    @Test
    void deleteSwiftCode_ok() throws Exception {
        mvc.perform(delete("/v1/swift-codes/BPCRFRPPXXX"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("SWIFT code was successfully deleted."));
    }

    @Test
    void deleteSwiftCode_validationError() throws Exception {
        mvc.perform(delete("/v1/swift-codes/BPCRFRPPxxx"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messages")
                        .value("SWIFT code is incorrect or is not 11 characters long."));
    }

    @Test
    void deleteSwiftCode_notFoundKey() throws Exception {
        doThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "SWIFT code was not deleted. Provided SWIFT code does not exists."
        )).when(service).deleteSwiftCode(eq("BPCRFRPPXXX"));

        mvc.perform(delete("/v1/swift-codes/BPCRFRPPXXX"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(
                        "SWIFT code was not deleted. Provided SWIFT code does not exists."
                ));
    }
}
