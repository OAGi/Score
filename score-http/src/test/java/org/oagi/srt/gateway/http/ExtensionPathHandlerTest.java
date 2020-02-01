package org.oagi.srt.gateway.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.data.ACC;
import org.oagi.srt.data.OagisComponentType;
import org.oagi.srt.gateway.http.api.cc_management.helper.CcUtility;
import org.oagi.srt.gateway.http.api.cc_management.service.ExtensionPathHandler;
import org.oagi.srt.repository.ACCRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExtensionPathHandlerTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ACCRepository accRepository;

    @Test
    public void testContainsExtension() {
        long releaseId = 1L;

        ExtensionPathHandler extensionPathHandler =
                applicationContext.getBean(ExtensionPathHandler.class, releaseId);

        List<ACC> accList = accRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getGuid()))
                .entrySet().stream()
                .map(entries -> CcUtility.getLatestEntity(releaseId, entries.getValue()))
                .filter(e -> e != null)
                .collect(Collectors.toList());

        List<ACC> extensions = accList.stream()
                .filter(e -> e.getOagisComponentType() == OagisComponentType.Extension.getValue())
                .collect(Collectors.toList());

        accList = accList.stream()
                .filter(e -> e.getOagisComponentType() != OagisComponentType.Extension.getValue())
                .collect(Collectors.toList());

        int count = 0;
        for (ACC acc : accList) {
            for (ACC extension : extensions) {
                boolean result =
                        extensionPathHandler.containsExtension(acc.getCurrentAccId(), extension.getCurrentId());
                if (result) {
                    logger.debug("containsExtension(" + acc.getAccId() + ", " + extension.getAccId() + "): " + result);
                }
            }
        }

        assertThat(count).isGreaterThan(0);
    }
}
