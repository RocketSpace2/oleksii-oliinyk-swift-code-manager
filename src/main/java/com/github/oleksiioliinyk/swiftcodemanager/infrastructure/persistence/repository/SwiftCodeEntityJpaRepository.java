package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.repository;

import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwiftCodeEntityJpaRepository extends JpaRepository<SwiftCodeEntity, Long> {
    SwiftCodeEntity findSwiftCodeEntityBySwiftCodeIgnoreCase(String swiftCode);
    List<SwiftCodeEntity> findSwiftCodeByCountryISO2Code(String countryISO2Code);
    List<SwiftCodeEntity> findAllBySwiftCodeStartingWithAndSwiftCodeNotLikeAllIgnoreCase(String prefix, String suffix);
}
