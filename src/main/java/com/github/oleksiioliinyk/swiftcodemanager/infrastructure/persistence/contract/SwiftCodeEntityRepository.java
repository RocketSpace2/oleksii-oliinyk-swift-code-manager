package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;

import java.util.List;

public interface SwiftCodeEntityRepository {
    Long count();
    void saveAll(List<SwiftCode> swiftCodes);
    void deleteSwiftCode(SwiftCode swiftCode);
    SwiftCode findSwiftCodeBySwiftCode(String swiftCode);
    SwiftCodeEntity findSwiftCodeWithoutMappingBySwiftCode(String swiftCode);
    List<SwiftCode> findBranches(String prefix, String suffix);
    List<SwiftCode> findSwiftCodeByCountryISO2Code(String countryISO2Code);
    SwiftCode createSwiftCode(SwiftCode swiftCode);
}
