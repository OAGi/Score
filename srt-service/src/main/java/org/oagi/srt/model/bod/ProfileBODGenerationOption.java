package org.oagi.srt.model.bod;

import static org.oagi.srt.model.bod.ProfileBODGenerationOption.SchemaExpression.XML;
import static org.oagi.srt.model.bod.ProfileBODGenerationOption.SchemaPackage.All;

public class ProfileBODGenerationOption {

    public static class BIEDocumentationOption {
        private boolean bieDefinition;
        private boolean inheritIfEmpty;
        private boolean bieGuid;
        private boolean businessContext;
        private boolean remarkAndStatus;
        private boolean bieVersion;
        private boolean businessTerm;
        private boolean others;

        private BIEDocumentationOption() {
        }

        public boolean isBieDefinition() {
            return bieDefinition;
        }

        public void setBieDefinition(boolean bieDefinition) {
            this.bieDefinition = bieDefinition;
        }

        public boolean isInheritIfEmpty() {
            return inheritIfEmpty;
        }

        public void setInheritIfEmpty(boolean inheritIfEmpty) {
            this.inheritIfEmpty = inheritIfEmpty;
        }

        public boolean isBieGuid() {
            return bieGuid;
        }

        public void setBieGuid(boolean bieGuid) {
            this.bieGuid = bieGuid;
        }

        public boolean isBusinessContext() {
            return businessContext;
        }

        public void setBusinessContext(boolean businessContext) {
            this.businessContext = businessContext;
        }

        public boolean isRemarkAndStatus() {
            return remarkAndStatus;
        }

        public void setRemarkAndStatus(boolean remarkAndStatus) {
            this.remarkAndStatus = remarkAndStatus;
        }

        public boolean isBieVersion() {
            return bieVersion;
        }

        public void setBieVersion(boolean bieVersion) {
            this.bieVersion = bieVersion;
        }

        public boolean isBusinessTerm() {
            return businessTerm;
        }

        public void setBusinessTerm(boolean businessTerm) {
            this.businessTerm = businessTerm;
        }

        public boolean isOthers() {
            return others;
        }

        public void setOthers(boolean others) {
            this.others = others;
        }
    }

    public static class CCDocumentationOption {
        private boolean ccDefinition;
        private boolean ccGuid;
        private boolean ccVersion;
        private boolean others;

        private CCDocumentationOption() {
        }

        public boolean isCcDefinition() {
            return ccDefinition;
        }

        public void setCcDefinition(boolean ccDefinition) {
            this.ccDefinition = ccDefinition;
        }

        public boolean isCcGuid() {
            return ccGuid;
        }

        public void setCcGuid(boolean ccGuid) {
            this.ccGuid = ccGuid;
        }

        public boolean isCcVersion() {
            return ccVersion;
        }

        public void setCcVersion(boolean ccVersion) {
            this.ccVersion = ccVersion;
        }

        public boolean isOthers() {
            return others;
        }

        public void setOthers(boolean others) {
            this.others = others;
        }
    }

    private BIEDocumentationOption bieDocumentationOption = new BIEDocumentationOption();
    private CCDocumentationOption ccDocumentationOption = new CCDocumentationOption();

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

    public BIEDocumentationOption getBieDocumentationOption() {
        return bieDocumentationOption;
    }

    public void setBieDocumentationOption(BIEDocumentationOption bieDocumentationOption) {
        this.bieDocumentationOption = bieDocumentationOption;
    }

    public CCDocumentationOption getCcDocumentationOption() {
        return ccDocumentationOption;
    }

    public void setCcDocumentationOption(CCDocumentationOption ccDocumentationOption) {
        this.ccDocumentationOption = ccDocumentationOption;
    }

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
}
