package com.github.oleksiioliinyk.swiftcodemanager.application.mapper;

import com.github.oleksiioliinyk.swiftcodemanager.presentation.dto.SwiftCodeDTO;
import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SwiftCodeDTOMapper {
    SwiftCodeDTO toDto(SwiftCode domain);
    SwiftCode toDomain(SwiftCodeDTO dto);
}
