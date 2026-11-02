package org.oagi.score.gateway.http.api.bie_management.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

/**
 * Request-local mappings keyed by the complete source occurrence path.
 * Stored BIE paths are never rewritten; local/legacy paths are not expanded here.
 */
public final class BieUpliftingCustomMappingTable {

    private final List<BieUpliftingMapping> mappingList;
    private final Map<String, BieUpliftingMapping> bySourcePath = new LinkedHashMap<>();
    private final Map<String, BieUpliftingMapping> byTargetPath = new LinkedHashMap<>();
    private final Map<String, BieUpliftingMapping> suppressedBySourcePath = new LinkedHashMap<>();

    public BieUpliftingCustomMappingTable(List<BieUpliftingMapping> mappings) {
        if (mappings == null) {
            throw new IllegalArgumentException("Custom mappings are required.");
        }
        if (mappings.stream().anyMatch(mapping -> mapping == null)) {
            throw new IllegalArgumentException("Custom mappings must not contain null entries.");
        }
        mappingList = List.copyOf(new ArrayList<>(mappings));
        Set<String> seenSourcePaths = new HashSet<>();
        for (BieUpliftingMapping mapping : mappingList) {
            if (!hasLength(mapping.getSourcePath())) {
                continue;
            }
            if (!seenSourcePaths.add(mapping.getSourcePath())) {
                throw new IllegalArgumentException("Duplicate source occurrence path: " + mapping.getSourcePath());
            }
            if (mapping.isSuppressAutoMapping() && !hasLength(mapping.getTargetPath())) {
                if (suppressedBySourcePath.putIfAbsent(mapping.getSourcePath(), mapping) != null) {
                    throw new IllegalArgumentException("Duplicate suppressed source occurrence path: " + mapping.getSourcePath());
                }
                continue;
            }
            if (!hasLength(mapping.getTargetPath())) {
                continue;
            }
            // Root ABIE entries do not override association mappings.
            String tag = getLastTag(mapping.getSourcePath());
            if (!tag.startsWith("ASCC-") && !tag.startsWith("BCC-") && !tag.startsWith("DT_SC-")) {
                continue;
            }
            if (bySourcePath.putIfAbsent(mapping.getSourcePath(), mapping) != null) {
                throw new IllegalArgumentException("Duplicate source occurrence path: " + mapping.getSourcePath());
            }
            if (byTargetPath.putIfAbsent(mapping.getTargetPath(), mapping) != null) {
                throw new IllegalArgumentException("Duplicate target occurrence path: " + mapping.getTargetPath());
            }
        }
    }

    public BieUpliftingMapping getMapping(String sourcePath) {
        return bySourcePath.get(sourcePath);
    }

    public boolean isAutoMappingSuppressed(String sourcePath) {
        return suppressedBySourcePath.containsKey(sourcePath);
    }

    public BieUpliftingMapping getMappingByTargetPath(String targetPath) {
        return byTargetPath.get(targetPath);
    }

    public List<BieUpliftingMapping> getMappingList() {
        return mappingList;
    }

    public static String getLastTag(String path) {
        return path.substring(path.lastIndexOf('>') + 1);
    }

    public static BigInteger extractManifestId(String tag) {
        return new BigInteger(tag.substring(tag.indexOf('-') + 1));
    }
}
