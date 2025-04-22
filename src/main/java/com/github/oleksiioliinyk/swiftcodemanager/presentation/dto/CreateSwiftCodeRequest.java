package com.github.oleksiioliinyk.swiftcodemanager.presentation.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateSwiftCodeRequest {
    @NotBlank(message = "SWIFT code must not be empty.")
    @Pattern(regexp = "^[A-Z0-9]{11}$", message = "SWIFT code is incorrect or is not 11 characters long.")
    private String swiftCode;

    @NotBlank(message = "Bank name must not be empty.")
    private String bankName;

    @NotBlank(message = "Address must not be empty.")
    private String address;

    @NotBlank(message = "Country ISO2 Code must not be empty.")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country ISO2 code is incorrect or is not 2 characters long.")
    private String countryISO2Code;

    @NotBlank(message = "Country name must not be empty.")
    private String countryName;

    @NotNull(message = "Bank name must not be empty.")
    private Boolean isHeadquarter;

    @JsonCreator
    public CreateSwiftCodeRequest(
            @JsonProperty("address") String address,
            @JsonProperty("bankName") String bankName,
            @JsonProperty("countryISO2") String countryISO2Code,
            @JsonProperty("countryName") String countryName,
            @JsonProperty("swiftCode") String swiftCode,
            @JsonProperty("isHeadquarter") Boolean isHeadquarter
    ) {
        this.address = address;
        this.bankName = bankName;
        this.countryISO2Code = countryISO2Code;
        this.countryName = countryName;
        this.swiftCode = swiftCode;
        this.isHeadquarter = isHeadquarter;
    }
}