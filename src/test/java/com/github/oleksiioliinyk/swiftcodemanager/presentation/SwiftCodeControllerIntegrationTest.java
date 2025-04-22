package com.github.oleksiioliinyk.swiftcodemanager.presentation;

import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.repository.SwiftCodeEntityJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SwiftCodeControllerIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private SwiftCodeEntityJpaRepository jpa;

    private SwiftCodeEntity headquarter;
    private SwiftCodeEntity branch;

    @BeforeEach
    void setUp() {
        //Head‑office entity
        headquarter = new SwiftCodeEntity();
        headquarter.setSwiftCode("AAAABBCCXXX");
        headquarter.setBankName("HEAD BANK");
        headquarter.setAddress("HQ ADDRESS");
        headquarter.setCountryISO2Code("US");
        headquarter.setCountryName("United States");
        jpa.saveAndFlush(headquarter);

        //Branch entity, pointing to the HQ
        branch = new SwiftCodeEntity();
        branch.setSwiftCode("AAAABBCC001");
        branch.setBankName("BRANCH BANK");
        branch.setAddress("BRANCH ADDRESS");
        branch.setCountryISO2Code("US");
        branch.setCountryName("United States");
        branch.setHeadquarter(headquarter);
        jpa.saveAndFlush(branch);
    }
    @AfterEach
    void tearDown() {
        jpa.delete(branch);
        jpa.delete(headquarter);
    }

    @Test
    void getHeadquarterWithBranches_success() throws Exception {
        mvc.perform(get("/v1/swift-codes/AAAABBCCXXX"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // HQ fields
                .andExpect(jsonPath("$.swiftCode").value("AAAABBCCXXX"))
                .andExpect(jsonPath("$.isHeadquarter").value(true))
                .andExpect(jsonPath("$.bankName").value("HEAD BANK"))
                // branches array
                .andExpect(jsonPath("$.branches", hasSize(1)))
                .andExpect(jsonPath("$.branches[0].swiftCode")
                        .value("AAAABBCC001"));
    }

    @Test
    void getByCountry_success() throws Exception {
        mvc.perform(get("/v1/swift-codes/country/US"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.countryISO2").value("US"))
                .andExpect(jsonPath("$.swiftCodes", hasSize(2)))
                .andExpect(jsonPath("$.swiftCodes[*].swiftCode",
                        containsInAnyOrder("AAAABBCCXXX","AAAABBCC001")));
    }

    @Test
    void createAndDelete_cycle() throws Exception {
        String newJson = """
            {
              "swiftCode":"BBBBCCCCXXX",
              "bankName":"NEW BANK",
              "address":"NEW ADDR",
              "countryISO2":"GB",
              "countryName":"United Kingdom",
              "isHeadquarter":true
            }
            """;
        mvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("SWIFT code was successfully created."));

        mvc.perform(get("/v1/swift-codes/BBBBCCCCXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("BBBBCCCCXXX"))
                .andExpect(jsonPath("$.bankName").value("NEW BANK"));

        mvc.perform(delete("/v1/swift-codes/BBBBCCCCXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("SWIFT code was successfully deleted."));

        mvc.perform(get("/v1/swift-codes/BBBBCCCCXXX"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Provided SWIFT code was not found."));
    }

    @Test
    void validationErrors() throws Exception {
        mvc.perform(get("/v1/swift-codes/SHORT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("SWIFT code is incorrect or is not 11 characters long."));

        mvc.perform(get("/v1/swift-codes/country/us"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("Country ISO2 code is incorrect or is not 2 characters long."));

        mvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"swiftCode\":\"\", \"bankName\":\"\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages", hasItem(
                        "SWIFT code must not be empty."
                )));
    }

    @Test
    void deleteSwiftCode_thenBadRequest() throws Exception {
        mvc.perform(delete("/v1/swift-codes/QWERTYUIOPA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("SWIFT code was not deleted. Provided SWIFT code does not exists."));
    }

    @Test
    void whenSwiftCodeNotInDb_thenBadRequestWithNotFoundMessage() throws Exception {
        // 11‑char but nonexistent
        mvc.perform(get("/v1/swift-codes/QWERTYUIOPA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Provided SWIFT code was not found."));
    }

    @Test
    void whenCountryIsoExistsFormatButNoCodes_thenBadRequestWithNotFoundMessage() throws Exception {
        // valid‑format ISO but no entries in DB
        mvc.perform(get("/v1/swift-codes/country/LL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("SWIFT codes for provided country ISO2 code were not found."));
    }

    @Test
    void createDuplicateSwiftCode_thenBadRequest() throws Exception {
        String dupJson = """
                            {
                               "address": "HQ ADDRESS",
                               "bankName": "HEAD BANK",
                               "countryISO2": "US",
                               "countryName": "United States",
                               "isHeadquarter": true,
                               "swiftCode": "AAAABBCCXXX"
                            }
                            """;

        mvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dupJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message")
                        .value("SWIFT code was not created. AAAABBCCXXX already exists. Please provide correct data."));
    }
}
