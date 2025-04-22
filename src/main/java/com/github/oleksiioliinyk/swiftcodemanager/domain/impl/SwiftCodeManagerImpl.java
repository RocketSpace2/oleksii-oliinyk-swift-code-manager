package com.github.oleksiioliinyk.swiftcodemanager.domain.impl;

import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeManager;
import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SwiftCodeManagerImpl implements SwiftCodeManager {
    @Autowired
    private SwiftCodeEntityRepository swiftCodeEntityRepository;

    @Override
    public List<SwiftCode> findSwiftCodesForCountry(String countryISO2Code) {
        return swiftCodeEntityRepository.findSwiftCodeByCountryISO2Code(countryISO2Code);
    }

    @Override
    public SwiftCode findHeadquarterForBranch(String branchSwiftCode) {
        String headquarterSwiftCode = branchSwiftCode.substring(0,8) + "XXX";
        return swiftCodeEntityRepository.findSwiftCodeBySwiftCode(headquarterSwiftCode);
    }

    @Override
    public SwiftCode findSwiftCode(String swiftCode) {
        return swiftCodeEntityRepository.findSwiftCodeBySwiftCode(swiftCode);
    }

    @Override
    public SwiftCode createSwiftCode(SwiftCode swiftCode) {
        return swiftCodeEntityRepository.createSwiftCode(swiftCode);
    }

    @Override
    public List<SwiftCode> findBranchesForHeadquarter(String headquarterSwiftCode) {
        String prefix = headquarterSwiftCode.substring(0,8);
        return swiftCodeEntityRepository.findBranches(prefix,"%XXX");
    }

    @Override
    public void deleteSwiftCode(SwiftCode swiftCode) {
        swiftCodeEntityRepository.deleteSwiftCode(swiftCode);
    }
}
