package com.github.oleksiioliinyk.swiftcodemanager.domain.contract;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;

import java.util.List;

public interface SwiftCodeManager {
    List<SwiftCode> findSwiftCodesForCountry(String countryISO2Code);

    SwiftCode findHeadquarterForBranch(String branchSwiftCode);
    SwiftCode findSwiftCode(String swiftCode);
    SwiftCode createSwiftCode(SwiftCode swiftCode);
    List<SwiftCode> findBranchesForHeadquarter(String headquarterSwiftCode);
    void deleteSwiftCode(SwiftCode swiftCode);

}
