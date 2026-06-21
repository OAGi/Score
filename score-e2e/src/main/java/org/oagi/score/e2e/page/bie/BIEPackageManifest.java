package org.oagi.score.e2e.page.bie;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the BIE Package Manifest ({@code manifest.json}) from a generated BIE Package ZIP and exposes
 * the fields relevant to issue #1733: the package-level Revision Reason and the per-BIE backward
 * compatibility indicator.
 *
 * <p>The actual emitted JSON is produced by the backend {@code BiePackageManifest} /
 * {@code BiePackageManifestEntry} records (the frontend {@code BiePackageManifest} TS model is a
 * stale, separate shape and is NOT what is generated into the package), so this reader targets the
 * backend field names: {@code biePackage.revisionReason}, {@code biePackage.priorPackageVersionId},
 * and {@code biePackage.bieList[i].backwardCompatibility.{syntaxIndependent,xmlSchema,jsonSchema}}.</p>
 */
public class BIEPackageManifest {

    private final JSONObject root;

    private BIEPackageManifest(JSONObject root) {
        this.root = root;
    }

    /**
     * Locate and parse the {@code manifest.json} entry inside a generated BIE Package ZIP.
     */
    public static BIEPackageManifest fromGeneratedZip(File zip) {
        try (ZipFile zipFile = new ZipFile(zip)) {
            ZipEntry manifestEntry = zipFile.stream()
                    .filter(entry -> entry.getName().endsWith("manifest.json"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "The generated BIE Package does not contain a manifest.json entry: " + zip));
            try (InputStream is = zipFile.getInputStream(manifestEntry)) {
                String content = IOUtils.toString(is, StandardCharsets.UTF_8);
                return new BIEPackageManifest(new JSONObject(content));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read manifest.json from " + zip, e);
        }
    }

    public JSONObject root() {
        return root;
    }

    public String manifestVersion() {
        return root.optString("manifestVersion", null);
    }

    public JSONObject biePackage() {
        return root.getJSONObject("biePackage");
    }

    public boolean hasRevisionReason() {
        JSONObject biePackage = biePackage();
        return biePackage.has("revisionReason") && !biePackage.isNull("revisionReason");
    }

    public String revisionReason() {
        JSONObject biePackage = biePackage();
        return biePackage.isNull("revisionReason") ? null : biePackage.optString("revisionReason", null);
    }

    public boolean hasPriorPackageVersionId() {
        JSONObject biePackage = biePackage();
        return biePackage.has("priorPackageVersionId") && !biePackage.isNull("priorPackageVersionId");
    }

    public String priorPackageVersionId() {
        JSONObject biePackage = biePackage();
        return biePackage.isNull("priorPackageVersionId") ? null : biePackage.optString("priorPackageVersionId", null);
    }

    public JSONArray bieList() {
        return biePackage().optJSONArray("bieList");
    }

    /**
     * The DEN values of every BIE entry in the manifest (useful for diagnostics on lookup failure).
     */
    public List<String> dens() {
        List<String> dens = new ArrayList<>();
        JSONArray list = bieList();
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject bie = list.getJSONObject(i).optJSONObject("bie");
                dens.add(bie == null ? null : bie.optString("den", null));
            }
        }
        return dens;
    }

    /**
     * Find the manifest entry for the BIE with the given DEN.
     */
    public JSONObject entryByDen(String den) {
        JSONArray list = bieList();
        if (list == null || den == null) {
            return null;
        }
        String target = den.trim();
        for (int i = 0; i < list.length(); i++) {
            JSONObject entry = list.getJSONObject(i);
            JSONObject bie = entry.optJSONObject("bie");
            if (bie == null) {
                continue;
            }
            String entryDen = bie.optString("den", null);
            if (entryDen != null && entryDen.trim().equals(target)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Whether any BIE entry carries a {@code backwardCompatibility} object. The per-BIE backward
     * compatibility indicator is emitted only in the draft {@code 0.3} manifest; the stable {@code 0.2}
     * manifest omits it on every entry. Used to assert the 0.2 manifest does not leak the draft fields.
     */
    public boolean hasAnyBackwardCompatibility() {
        JSONArray list = bieList();
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject entry = list.getJSONObject(i);
                if (entry.has("backwardCompatibility") && !entry.isNull("backwardCompatibility")) {
                    return true;
                }
            }
        }
        return false;
    }

    private BackwardCompatibility backwardCompatibilityOf(JSONObject entry) {
        JSONObject backwardCompatibility = entry.getJSONObject("backwardCompatibility");
        return new BackwardCompatibility(
                // Issue #1733: syntaxIndependent is no longer emitted in the manifest;
                // tolerate its absence (defaults to false). It is intentionally not asserted.
                backwardCompatibility.optBoolean("syntaxIndependent", false),
                backwardCompatibility.getBoolean("xmlSchema"),
                backwardCompatibility.getBoolean("jsonSchema"));
    }

    /**
     * The {@code backwardCompatibility} triple for the BIE with the given DEN.
     */
    public BackwardCompatibility backwardCompatibilityByDen(String den) {
        JSONObject entry = entryByDen(den);
        if (entry == null) {
            throw new IllegalStateException(
                    "No BIE entry with DEN '" + den + "' in the manifest. Available DENs: " + dens());
        }
        return backwardCompatibilityOf(entry);
    }

    /**
     * The {@code backwardCompatibility} triple of the single BIE in the manifest. Used for scenarios
     * where the package contains exactly one BIE (e.g. a revision whose only BIE was replaced, or an
     * unchanged carried-forward BIE).
     */
    public BackwardCompatibility backwardCompatibilityOfOnlyBie() {
        JSONArray list = bieList();
        int size = (list == null) ? 0 : list.length();
        if (size != 1) {
            throw new IllegalStateException(
                    "Expected exactly one BIE in the manifest but found " + size + ": " + describe());
        }
        return backwardCompatibilityOf(list.getJSONObject(0));
    }

    /**
     * The {@code backwardCompatibility} triple of the first BIE that is not included in the prior
     * package version (a brand-new BIE).
     */
    public BackwardCompatibility backwardCompatibilityOfFirstNewBie() {
        JSONArray list = bieList();
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject entry = list.getJSONObject(i);
                if (!entry.optBoolean("includedInPriorPackageVersion", true)) {
                    return backwardCompatibilityOf(entry);
                }
            }
        }
        throw new IllegalStateException(
                "No brand-new (not-in-prior) BIE entry found in the manifest: " + describe());
    }

    /**
     * A compact diagnostic of every BIE entry: DEN, included-in-prior flag, and backward
     * compatibility triple.
     */
    public String describe() {
        StringBuilder sb = new StringBuilder("[");
        JSONArray list = bieList();
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                JSONObject entry = list.getJSONObject(i);
                JSONObject bie = entry.optJSONObject("bie");
                JSONObject bc = entry.optJSONObject("backwardCompatibility");
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("{den=").append(bie == null ? null : bie.optString("den", null))
                        .append(", inPrior=").append(entry.optBoolean("includedInPriorPackageVersion", false))
                        .append(", bc=").append(bc).append("}");
            }
        }
        return sb.append("]").toString();
    }

    /**
     * Whether the BIE with the given DEN is reported as included in the prior package version.
     */
    public boolean isIncludedInPriorPackageVersion(String den) {
        JSONObject entry = entryByDen(den);
        if (entry == null) {
            throw new IllegalStateException(
                    "No BIE entry with DEN '" + den + "' in the manifest. Available DENs: " + dens());
        }
        return entry.optBoolean("includedInPriorPackageVersion", false);
    }

    /**
     * The backward compatibility indicator. Per the manifest contract, each boolean is
     * {@code true} when the change is backward <em>compatible</em> for that target syntax and
     * {@code false} when the change breaks it.
     */
    public record BackwardCompatibility(boolean syntaxIndependent, boolean xmlSchema, boolean jsonSchema) {
    }
}
