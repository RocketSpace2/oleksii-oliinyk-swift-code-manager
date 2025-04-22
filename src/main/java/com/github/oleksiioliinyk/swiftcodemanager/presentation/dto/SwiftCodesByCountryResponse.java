package com.github.oleksiioliinyk.swiftcodemanager.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwiftCodesByCountryResponse {
    private String countryISO2;
    private String countryName;
    private List<SwiftCodeCountryResponse> swiftCodes;
}
