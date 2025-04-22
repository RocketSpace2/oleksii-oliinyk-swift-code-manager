package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.mapper.contract;

import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;
import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.mapper.decorator.SwiftCodeEntityMapperDecorator;
import org.mapstruct.Context;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
@DecoratedWith(SwiftCodeEntityMapperDecorator.class)
public interface SwiftCodeEntityMapper {
    SwiftCode toDomain(SwiftCodeEntity entity);
    @Named("fileMapping")
    SwiftCodeEntity toEntityForFile(SwiftCode domain, @Context SwiftCodeEntityRepository repository);
    SwiftCodeEntity toEntity(SwiftCode domain);


}
