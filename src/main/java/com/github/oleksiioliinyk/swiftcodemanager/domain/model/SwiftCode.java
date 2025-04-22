package com.github.oleksiioliinyk.swiftcodemanager.domain.model;

import lombok.Builder;

@Builder
public record SwiftCode(
        Long id,
        String swiftCode,
        String bankName,
        String address,
        String countryISO2Code,
        String countryName,
        SwiftCode headquarter
) {

}
