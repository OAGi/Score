package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public class BieUpliftingCustomMappingTable {

    private List<BieUpliftingMapping> mappingList;

    private Map<String, BieUpliftingMapping> targetAsccMappingMap;
    private Map<String, BieUpliftingMapping> targetAsccMappingByTargetPathMap;
    private Map<String, AsccpManifestId> targetAsccpManifestIdBySourcePathMap;
    private Map<String, AccManifestId> targetAccManifestIdBySourcePathMap;
    private Map<String, BieUpliftingMapping> targetBccMappingMap;
    private Map<String, BccpManifestId> targetBccpManifestIdBySourcePathMap;
    private Map<String, BieUpliftingMapping> targetDtScMappingMap;

    public BieUpliftingCustomMappingTable(CcDocument sourceCcDocument,
                                          CcDocument targetCcDocument,
                                          List<BieUpliftingMapping> mappingList) {
        if (mappingList == null) {
            throw new IllegalArgumentException();
        }

        this.mappingList = mappingList;

        targetAsccMappingMap = mappingList.stream()
                .filter(e -> hasLength(e.getSourcePath()))
                .filter(e -> getLastTag(e.getSourcePath()).contains("ASCC"))
                .collect(Collectors.toMap(BieUpliftingMapping::getSourcePath, Function.identity(), (a1, a2) -> a2));

        targetAsccMappingByTargetPathMap = mappingList.stream()
                .filter(e -> hasLength(e.getSourcePath()) && hasLength(e.getTargetPath()))
                .filter(e -> getLastTag(e.getTargetPath()).contains("ASCC"))
                .collect(Collectors.toMap(BieUpliftingMapping::getTargetPath, Function.identity(), (a1, a2) -> a2));

        targetAsccpManifestIdBySourcePathMap = targetAsccMappingMap.values().stream()
                .filter(e -> hasLength(e.getSourcePath()) && hasLength(e.getTargetPath()))
                .collect(Collectors.toMap(e -> {
                    AsccManifestId sourceAsccManifestId = new AsccManifestId(extractManifestId(getLastTag(e.getSourcePath())));
                    AsccSummaryRecord sourceAscc = sourceCcDocument.getAscc(sourceAsccManifestId);
                    return e.getSourcePath() + ">" + "ASCCP-" + sourceAscc.toAsccpManifestId();
                }, e -> {
                    AsccManifestId targetAsccManifestId = new AsccManifestId(extractManifestId(getLastTag(e.getTargetPath())));
                    AsccSummaryRecord targetAsccManifest = targetCcDocument.getAscc(targetAsccManifestId);
                    return targetAsccManifest.toAsccpManifestId();
                }, (a1, a2) -> a2));

        targetAccManifestIdBySourcePathMap = targetAsccMappingMap.values().stream()
                .filter(e -> hasLength(e.getSourcePath()) && hasLength(e.getTargetPath()))
                .collect(Collectors.toMap(e -> {
                    AsccManifestId sourceAsccManifestId = new AsccManifestId(extractManifestId(getLastTag(e.getSourcePath())));
                    AsccSummaryRecord sourceAscc = sourceCcDocument.getAscc(sourceAsccManifestId);
                    AsccpSummaryRecord sourceAsccp = sourceCcDocument.getAsccp(sourceAscc.toAsccpManifestId());
                    return e.getSourcePath() + ">" + "ASCCP-" + sourceAscc.toAsccpManifestId() +
                            ">" + "ACC-" + sourceAsccp.roleOfAccManifestId();
                }, e -> {
                    AsccManifestId targetAsccManifestId = new AsccManifestId(extractManifestId(getLastTag(e.getTargetPath())));
                    AsccSummaryRecord targetAscc = targetCcDocument.getAscc(targetAsccManifestId);
                    AsccpSummaryRecord targetAsccp = targetCcDocument.getAsccp(targetAscc.toAsccpManifestId());
                    return targetAsccp.roleOfAccManifestId();
                }, (a1, a2) -> a2));

        targetBccMappingMap = mappingList.stream()
                .filter(e -> hasLength(e.getSourcePath()))
                .filter(e -> getLastTag(e.getSourcePath()).contains("BCC"))
                .collect(Collectors.toMap(BieUpliftingMapping::getSourcePath, Function.identity(), (a1, a2) -> a2));

        targetBccpManifestIdBySourcePathMap = targetBccMappingMap.values().stream()
                .filter(e -> hasLength(e.getSourcePath()) && hasLength(e.getTargetPath()))
                .collect(Collectors.toMap(e -> {
                    BccManifestId sourceBccManifestId = new BccManifestId(extractManifestId(getLastTag(e.getSourcePath())));
                    BccSummaryRecord sourceBcc = sourceCcDocument.getBcc(sourceBccManifestId);
                    return e.getSourcePath() + ">" + "BCCP-" + sourceBcc.toBccpManifestId();
                }, e -> {
                    BccManifestId targetBccManifestId = new BccManifestId(extractManifestId(getLastTag(e.getTargetPath())));
                    BccSummaryRecord targetBcc = targetCcDocument.getBcc(targetBccManifestId);
                    return targetBcc.toBccpManifestId();
                }, (a1, a2) -> a2));

        targetDtScMappingMap = mappingList.stream()
                .filter(e -> hasLength(e.getSourcePath()))
                .filter(e -> getLastTag(e.getSourcePath()).contains("DT_SC"))
                .collect(Collectors.toMap(BieUpliftingMapping::getSourcePath, Function.identity(), (a1, a2) -> a2));
    }

    public static String getLastTag(String path) {
        if (path == null) {
            return null;
        }
        String[] tags = path.split(">");
        return tags[tags.length - 1];
    }

    public static BigInteger extractManifestId(String tag) {
        return new BigInteger(tag.substring(tag.indexOf('-') + 1));
    }

    public AccManifestId getTargetAccManifestIdBySourcePath(String sourcePath) {
        return targetAccManifestIdBySourcePathMap.get(sourcePath);
    }

    public AsccpManifestId getTargetAsccpManifestIdBySourcePath(String sourcePath) {
        return targetAsccpManifestIdBySourcePathMap.get(sourcePath);
    }

    public BccpManifestId getTargetBccpManifestIdBySourcePath(String sourcePath) {
        return targetBccpManifestIdBySourcePathMap.get(sourcePath);
    }

    public BieUpliftingMapping getTargetAsccMappingBySourcePath(String sourcePath) {
        return targetAsccMappingMap.get(sourcePath);
    }

    public BieUpliftingMapping getTargetAsccMappingByTargetPath(String targetPath) {
        return targetAsccMappingByTargetPathMap.get(targetPath);
    }

    public BieUpliftingMapping getTargetBccMappingBySourcePath(String sourcePath) {
        return targetBccMappingMap.get(sourcePath);
    }

    public BieUpliftingMapping getTargetDtScMappingBySourcePath(String sourcePath) {
        return targetDtScMappingMap.get(sourcePath);
    }

    public List<BieUpliftingMapping> getMappingList() {
        return this.mappingList;
    }

}
