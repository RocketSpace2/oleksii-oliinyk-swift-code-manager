package com.github.oleksiioliinyk.swiftcodemanager.infrastructure.filereader.contract;

import com.github.oleksiioliinyk.swiftcodemanager.domain.model.SwiftCode;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FileReaderContract {
    List<SwiftCode> readFile(Resource file);
}
