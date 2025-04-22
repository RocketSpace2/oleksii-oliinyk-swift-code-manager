package com.github.oleksiioliinyk.swiftcodemanager.application.service;

import com.github.oleksiioliinyk.swiftcodemanager.application.mapper.SwiftCodeDTOMapper;
import com.github.oleksiioliinyk.swiftcodemanager.domain.contract.SwiftCodeManager;
import com.github.oleksiioliinyk.swiftcodemanager.presentation.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SwiftCodeManagerService {
    @Autowired
    private SwiftCodeManager swiftCodeManager;
    @Autowired
    private SwiftCodeDTOMapper swiftCodeDTOMapper;

    public SwiftCodeWithBranchesResponse findSwiftCode(String swiftCode){
        try {
            SwiftCodeDTO swiftCodeDTO = swiftCodeDTOMapper.toDto(swiftCodeManager.findSwiftCode(swiftCode));

            SwiftCodeWithBranchesResponse response = new SwiftCodeWithBranchesResponse(
                    swiftCodeDTO.getAddress(),
                    swiftCodeDTO.getBankName(),
                    swiftCodeDTO.getCountryISO2Code(),
                    swiftCodeDTO.getCountryName(),
                    true,
                    swiftCodeDTO.getSwiftCode(),
                    null
            );

            if (isHeadquarter(swiftCodeDTO.getSwiftCode())){
                List<SwiftCodeBranchResponse> branches = findBranchesForHeadquarter(swiftCodeDTO.getSwiftCode())
                        .stream()
                        .map(branch -> new SwiftCodeBranchResponse(
                                branch.getAddress(),
                                branch.getBankName(),
                                branch.getCountryISO2Code(),
                                branch.getCountryName(),
                                false,
                                branch.getSwiftCode()
                        ))
                        .toList();

                response.setBranches(branches);
            }

            return response;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provided SWIFT code was not found."
            );
        }
    }

    public SwiftCodesByCountryResponse findSwiftCodeByCountryISO2Code(String countryISO2code){
            List<SwiftCodeDTO> swiftCodes = swiftCodeManager
                    .findSwiftCodesForCountry(countryISO2code)
                    .stream()
                    .map(swiftCodeDTOMapper::toDto)
                    .toList();

            return new SwiftCodesByCountryResponse(
                    countryISO2code,
                    swiftCodes.get(0).getCountryName(),
                    swiftCodes
                            .stream()
                            .map(swiftCodeDTO -> new SwiftCodeCountryResponse(
                                    swiftCodeDTO.getAddress(),
                                    swiftCodeDTO.getBankName(),
                                    swiftCodeDTO.getCountryISO2Code(),
                                    isHeadquarter(swiftCodeDTO.getSwiftCode()),
                                    swiftCodeDTO.getSwiftCode()
                            ))
                            .toList()
            );
    }

    public void createSwiftCode(CreateSwiftCodeRequest createSwiftCodeRequest){
        SwiftCodeDTO swiftCodeDTO = createSwiftCodeDTO(createSwiftCodeRequest);
        if(isHeadquarter(createSwiftCodeRequest.getSwiftCode())){
            saveHeadquarter(swiftCodeDTO);
        }else {
            saveBranch(swiftCodeDTO);
        }
    }

    public void deleteSwiftCode(String swiftCode){
        if (isHeadquarter(swiftCode)){
            List<SwiftCodeDTO> branches = findBranchesForHeadquarter(swiftCode);
            for (SwiftCodeDTO branch : branches){
                swiftCodeManager.deleteSwiftCode(swiftCodeDTOMapper.toDomain(branch));
            }
        }

        swiftCodeManager.deleteSwiftCode(swiftCodeManager.findSwiftCode(swiftCode));
    }

    private boolean isHeadquarter(String swiftCode) {
        return swiftCode.endsWith("XXX");
    }

    private SwiftCodeDTO createSwiftCodeDTO(CreateSwiftCodeRequest request) {
        return new SwiftCodeDTO(
                null,
                request.getSwiftCode().toUpperCase(),
                request.getBankName().toUpperCase(),
                request.getAddress().toUpperCase(),
                request.getCountryISO2Code().toUpperCase(),
                request.getCountryName().toUpperCase(),
                null
        );
    }

    private void saveBranch(SwiftCodeDTO branch) {
        SwiftCodeDTO swiftCodeHeadquarter = swiftCodeDTOMapper.toDto(
                swiftCodeManager.findHeadquarterForBranch(branch.getSwiftCode())
        );

        branch.setHeadquarter(swiftCodeHeadquarter);

        swiftCodeManager.createSwiftCode(
                swiftCodeDTOMapper.toDomain(branch)
        );
    }
    private void saveHeadquarter(SwiftCodeDTO headquarter) {
        headquarter = swiftCodeDTOMapper.toDto(swiftCodeManager.createSwiftCode(
                swiftCodeDTOMapper.toDomain(headquarter)
        ));

        List<SwiftCodeDTO> branches = findBranchesForHeadquarter(headquarter.getSwiftCode());

        for (SwiftCodeDTO branch : branches){
            branch.setHeadquarter(headquarter);
            swiftCodeManager.createSwiftCode(
                    swiftCodeDTOMapper.toDomain(branch)
            );
        }
    }

    private List<SwiftCodeDTO> findBranchesForHeadquarter(String headquarterSwiftCode){
        return swiftCodeManager.findBranchesForHeadquarter(headquarterSwiftCode)
                .stream()
                .map(swiftCodeDTOMapper::toDto)
                .toList();
    }

}
