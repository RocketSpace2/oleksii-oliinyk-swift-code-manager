package com.github.oleksiioliinyk.swiftcodemanager.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwiftCodeDTO {
    private Long id;
    private String swiftCode;
    private String bankName;
    private String address;
    private String countryISO2Code;
    private String countryName;
    private SwiftCodeDTO headquarter;
}
