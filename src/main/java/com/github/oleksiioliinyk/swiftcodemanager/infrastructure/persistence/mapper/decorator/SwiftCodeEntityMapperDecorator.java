package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.mapper.decorator;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.contract.SwiftCodeEntityRepository;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.entity.SwiftCodeEntity;
import com.github.oleksiioliinyk.swiftcodemanager.infrastructure.persistence.mapper.contract.SwiftCodeEntityMapper;
import org.mapstruct.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SwiftCodeEntityMapperDecorator implements SwiftCodeEntityMapper{
    @Autowired
    @Qualifier("delegate")
    private SwiftCodeEntityMapper delegate;

    @Override
    public SwiftCode toDomain(SwiftCodeEntity entity) {
        return delegate.toDomain(entity);
    }

    @Override
    public SwiftCodeEntity toEntityForFile(SwiftCode domain, @Context SwiftCodeEntityRepository repository) {
        SwiftCodeEntity swiftCodeEntity = delegate.toEntity(domain);

        if (domain.headquarter() != null) {
            swiftCodeEntity.setHeadquarter(
                    repository.findSwiftCodeWithoutMappingBySwiftCode(domain.headquarter().swiftCode())
            );
        }
        return swiftCodeEntity;
    }

    @Override
    public SwiftCodeEntity toEntity(SwiftCode domain) {
        return delegate.toEntity(domain);
    }
}
