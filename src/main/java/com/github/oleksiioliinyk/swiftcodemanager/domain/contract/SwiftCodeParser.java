package com.github.oleksiioliinyk.swiftcodemanager.domain.contract;

import org.springframework.core.io.Resource;

public interface SwiftCodeParser {
    boolean isInitialized();
    void parseAndSave(Resource file);
}
