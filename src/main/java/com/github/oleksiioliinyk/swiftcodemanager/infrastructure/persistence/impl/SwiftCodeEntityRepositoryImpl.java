package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.impl;

import com.github.oleksiioliinyk.swiftcodemanager.application.mapper.SwiftCodeDTOMapper;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.mapper.contract.SwiftCodeEntityMapper;
import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.repository.SwiftCodeEntityJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class SwiftCodeEntityRepositoryImpl implements SwiftCodeEntityRepository {
    @Autowired
    private SwiftCodeEntityJpaRepository swiftCodeEntityJpaRepository;
    @Autowired
    private SwiftCodeEntityMapper swiftCodeEntityMapper;
    @Autowired
    private SwiftCodeDTOMapper swiftCodeDTOMapper;

    @Override
    public Long count() {
        return swiftCodeEntityJpaRepository.count();
    }

    @Override
    public void saveAll(List<SwiftCode> swiftCodes) {
        List<SwiftCodeEntity> swiftCodeEntities = swiftCodes.stream()
                .map(
            swiftCode -> swiftCodeEntityMapper.toEntityForFile(swiftCode, this)
                ).toList();
        swiftCodeEntityJpaRepository.saveAll(swiftCodeEntities);
    }

    @Override
    public void deleteSwiftCode(SwiftCode swiftCode) {
        try {
            swiftCodeEntityJpaRepository.delete(swiftCodeEntityMapper.toEntity(swiftCode));
        }catch (Exception exception){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SWIFT code was not deleted. Provided SWIFT code does not exists."
            );
        }

    }

    @Override
    public SwiftCode findSwiftCodeBySwiftCode(String swiftCode) {
        SwiftCodeEntity swiftCodeEntity = findSwiftCodeWithoutMappingBySwiftCode(swiftCode);
        return swiftCodeEntityMapper.toDomain(swiftCodeEntity);
    }

    //This method is used in mapper class to correctly map each SwiftCodeEntity to SwiftCode and vice versa.
    @Override
    public SwiftCodeEntity findSwiftCodeWithoutMappingBySwiftCode(String swiftCode) {
        return swiftCodeEntityJpaRepository.findSwiftCodeEntityBySwiftCodeIgnoreCase(swiftCode);
    }

    @Override
    public List<SwiftCode> findBranches(String prefix, String suffix) {
        return swiftCodeEntityJpaRepository
                .findAllBySwiftCodeStartingWithAndSwiftCodeNotLikeAllIgnoreCase(prefix, suffix)
                .stream()
                .map(swiftCodeEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<SwiftCode> findSwiftCodeByCountryISO2Code(String countryISO2Code) {
        List<SwiftCode> swiftCodes = swiftCodeEntityJpaRepository
                .findSwiftCodeByCountryISO2Code(countryISO2Code)
                .stream()
                .map(swiftCodeEntityMapper::toDomain)
                .toList();

        if (swiftCodes.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SWIFT codes for provided country ISO2 code were not found."
            );
        }
        return swiftCodes;
    }

    @Override
    public SwiftCode createSwiftCode(SwiftCode swiftCode) {
        try {
            return swiftCodeEntityMapper.toDomain(
                    swiftCodeEntityJpaRepository.save(swiftCodeEntityMapper.toEntity(swiftCode))
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SWIFT code was not created. " + swiftCode.swiftCode() + " already exists. " +
                    "Please provide correct data."
            );
        }
    }
}
