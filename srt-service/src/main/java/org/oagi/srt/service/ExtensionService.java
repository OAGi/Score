package org.oagi.srt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtensionService {

    @Transactional(rollbackFor = Throwable.class)
    public void createABIEExtensionLocally() {

    }

    @Transactional(rollbackFor = Throwable.class)
    public void createABIEExtensionGlobally() {

    }
}
