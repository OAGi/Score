package org.oagi.srt.model.bod;

import static org.oagi.srt.model.bod.ProfileBODGenerationOption.SchemaExpression.XML;
import static org.oagi.srt.model.bod.ProfileBODGenerationOption.SchemaPackage.All;

public class ProfileBODGenerationOption {
    private boolean bieDefinition = true;
    private boolean bieGuid;
    private boolean bieCctsMetaData;
    private boolean businessContext;
    private boolean includeCctsDefinitionTag;
    private boolean bieOagiSrtMetaData;
    private boolean basedCcMetaData;

    public boolean isBieDefinition() {
        return bieDefinition;
    }

    public void setBieDefinition(boolean bieDefinition) {
        this.bieDefinition = bieDefinition;
    }

    public boolean isBieGuid() {
        return bieGuid;
    }

    public void setBieGuid(boolean bieGuid) {
        this.bieGuid = bieGuid;
    }

    public boolean isBieCctsMetaData() {
        return bieCctsMetaData;
    }

    public void setBieCctsMetaData(boolean bieCctsMetaData) {
        this.bieCctsMetaData = bieCctsMetaData;
    }

    public boolean isBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(boolean businessContext) {
        this.businessContext = businessContext;
    }

    public boolean isIncludeCctsDefinitionTag() {
        return includeCctsDefinitionTag;
    }

    public void setIncludeCctsDefinitionTag(boolean includeCctsDefinitionTag) {
        this.includeCctsDefinitionTag = includeCctsDefinitionTag;
    }

    public boolean isBieOagiSrtMetaData() {
        return bieOagiSrtMetaData;
    }

    public void setBieOagiSrtMetaData(boolean bieOagiSrtMetaData) {
        this.bieOagiSrtMetaData = bieOagiSrtMetaData;
    }

    public boolean isBasedCcMetaData() {
        return basedCcMetaData;
    }

    public void setBasedCcMetaData(boolean basedCcMetaData) {
        this.basedCcMetaData = basedCcMetaData;
    }

    public enum SchemaExpression {
        XML,
        JSON
    }

    public enum SchemaPackage {
        All,
        Each
    }

    private SchemaExpression schemaExpression = XML;
    private SchemaPackage schemaPackage = All;

    public SchemaExpression getSchemaExpression() {
        return schemaExpression;
    }

    public void setSchemaExpression(SchemaExpression schemaExpression) {
        this.schemaExpression = schemaExpression;
    }

    public SchemaPackage getSchemaPackage() {
        return schemaPackage;
    }

    public void setSchemaPackage(SchemaPackage schemaPackage) {
        this.schemaPackage = schemaPackage;
    }

    public ProfileBODGenerationOption clone() {
        ProfileBODGenerationOption clone = new ProfileBODGenerationOption();
        clone.bieDefinition = this.bieDefinition;
        clone.bieGuid = this.bieGuid;
        clone.bieCctsMetaData = this.bieCctsMetaData;
        clone.businessContext = this.businessContext;
        clone.includeCctsDefinitionTag = this.includeCctsDefinitionTag;
        clone.bieOagiSrtMetaData = this.bieOagiSrtMetaData;
        clone.basedCcMetaData = this.basedCcMetaData;
        clone.schemaExpression = this.schemaExpression;
        clone.schemaPackage = this.schemaPackage;
        return clone;
    }
}
