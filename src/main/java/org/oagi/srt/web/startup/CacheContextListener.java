package org.oagi.srt.web.startup;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.persistence.dto.ACCVO;
import org.oagi.srt.persistence.dto.ASCCVO;
import org.oagi.srt.persistence.dto.BCCVO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;

public class CacheContextListener implements ServletContextListener {

    private static CacheManager cacheManager;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Class<Integer> keyType = Integer.class;
        Class<ArrayList<BCCVO>> bccValueType = (Class) ArrayList.class;
        Class<ArrayList<ASCCVO>> asccValueType = (Class) ArrayList.class;

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withDefaultSizeOfMaxObjectSize(500, MemoryUnit.B)
                .withDefaultSizeOfMaxObjectGraph(2000)
                .withCache("bcc",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, bccValueType,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, MemoryUnit.KB)
                                        .offheap(10, MemoryUnit.MB))
                                .withSizeOfMaxObjectGraph(1000)
                                .withSizeOfMaxObjectSize(1000, MemoryUnit.B)
                                .build())
                .withCache("ascc",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, asccValueType,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, MemoryUnit.KB)
                                        .offheap(10, MemoryUnit.MB))
                                .withSizeOfMaxObjectGraph(1000)
                                .withSizeOfMaxObjectSize(1000, MemoryUnit.B)
                                .build())
                .withCache("acc",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, ACCVO.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, MemoryUnit.KB)
                                        .offheap(10, MemoryUnit.MB))
                                .withSizeOfMaxObjectGraph(1000)
                                .withSizeOfMaxObjectSize(1000, MemoryUnit.B)
                                .build())
                .build();

        cacheManager.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        cacheManager.close();
    }

    public static CacheManager getChcheManager() {
        return cacheManager;
    }
}
