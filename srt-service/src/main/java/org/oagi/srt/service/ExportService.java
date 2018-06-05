package org.oagi.srt.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.*;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ExportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ContextCategoryRepository contextCategoryRepository;

    @Autowired
    private ContextSchemeRepository contextSchemeRepository;

    @Autowired
    private ContextSchemeValueRepository contextSchemeValueRepository;

    @Autowired
    private BusinessContextRepository businessContextRepository;

    @Autowired
    private BusinessContextValueRepository businessContextValueRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ProfileBODGenerateService profileBODGenerateService;

    private interface TraversalExportVisitor {

        public void visitStart() throws Exception;

        public void visitUsers(Collection<User> users) throws Exception;

        public void visitBusinessContextCollections(
                Collection<ContextCategory> contextCategories,
                Collection<ContextScheme> contextSchemes,
                Collection<ContextSchemeValue> contextSchemeValues,
                Collection<BusinessContext> businessContexts,
                Collection<BusinessContextValue> businessContextValues
        ) throws Exception;

        public void visitCodeListCollections(
                Collection<CodeList> codeLists,
                Collection<CodeListValue> codeListValues
        ) throws Exception;

        public void visitProfileBIEs(Collection<TopLevelAbie> topLevelAbies) throws Exception;

        public void visitEnd() throws Exception;

    }

    private class XMLTraversalExportVisitor implements TraversalExportVisitor {

        private final org.jdom2.Namespace XSD_NS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        private final Namespace OAGI_NS = Namespace.getNamespace("", SRTConstants.OAGI_NS);

        private Document document;
        private Element rootElement;
        private Map<Long, User> userMap;

        private Document codeListDocument;
        private Map<Long, File> profileBIEs;
        private File zipFile;

        @Override
        public void visitStart() {
            this.document = new Document();
            this.rootElement = new Element("srt");
            this.document.addContent(this.rootElement);
        }

        @Override
        public void visitUsers(Collection<User> users) {
            userMap = users.stream().collect(
                    Collectors.toMap(User::getAppUserId, Function.identity()));

            Element usersEle = new Element("users");
            for (User user : users) {
                Element userEle = new Element("user");
                usersEle.addContent(userEle);

                userEle.setAttribute("id", user.getLoginId());
                userEle.setAttribute("oagis_developer", (user.isOagisDeveloperIndicator()) ? "true" : "false");

                Element passwordEle = new Element("password");
                String scheme = passwordEncoder.getClass().getSimpleName().replaceAll("PasswordEncoder", "").toLowerCase();
                passwordEle.setAttribute("password_scheme", scheme);
                passwordEle.setText(user.getPassword());
                userEle.addContent(passwordEle);

                if (!StringUtils.isEmpty(user.getName())) {
                    Element nameEle = new Element("name");
                    nameEle.setText(user.getName());
                    userEle.addContent(nameEle);
                }

                if (!StringUtils.isEmpty(user.getOrganization())) {
                    Element organizationEle = new Element("organization");
                    organizationEle.setText(user.getOrganization());
                    userEle.addContent(organizationEle);
                }
            }

            this.rootElement.addContent(usersEle);
        }

        @Override
        public void visitBusinessContextCollections(
                Collection<ContextCategory> contextCategories,
                Collection<ContextScheme> contextSchemes,
                Collection<ContextSchemeValue> contextSchemeValues,
                Collection<BusinessContext> businessContexts,
                Collection<BusinessContextValue> businessContextValues
        ) {
            addContextCategories(contextCategories);
            addContextSchemes(contextSchemes, contextSchemeValues);
            addBusinessContexts(businessContexts, businessContextValues);
        }

        private void addContextCategories(Collection<ContextCategory> contextCategories) {
            if (contextCategories.isEmpty()) {
                return;
            }

            Element contextCategoriesEle = new Element("context_categories");
            for (ContextCategory contextCategory : contextCategories) {
                Element contextCategoryEle = new Element("context_category");
                contextCategoriesEle.addContent(contextCategoryEle);
                contextCategoryEle.setAttribute("id", contextCategory.getGuid());

                Element nameEle = new Element("name");
                nameEle.setText(contextCategory.getName());
                contextCategoryEle.addContent(nameEle);

                if (!StringUtils.isEmpty(contextCategory.getDescription())) {
                    Element descriptionEle = new Element("description");
                    descriptionEle.addContent(new CDATA(contextCategory.getDescription()));
                    contextCategoryEle.addContent(descriptionEle);
                }
            }
            this.rootElement.addContent(contextCategoriesEle);
        }

        private void addContextSchemes(
                Collection<ContextScheme> contextSchemes,
                Collection<ContextSchemeValue> contextSchemeValues
        ) {
            if (contextSchemes.isEmpty()) {
                return;
            }

            Map<Long, List<ContextSchemeValue>> contextSchemeValueMap = contextSchemeValues.stream()
                    .collect(groupingBy(e -> e.getContextScheme().getCtxSchemeId()));

            Element contextSchemesEle = new Element("context_schemes");
            for (ContextScheme contextScheme : contextSchemes) {
                Element contextSchemeEle = new Element("context_scheme");
                contextSchemesEle.addContent(contextSchemeEle);
                contextSchemeEle.setAttribute("id", contextScheme.getGuid());
                contextSchemeEle.setAttribute("context_category_ref", contextScheme.getContextCategory().getGuid());

                if (!StringUtils.isEmpty(contextScheme.getSchemeId())) {
                    Element schemeIdEle = new Element("scheme_id");
                    schemeIdEle.setText(contextScheme.getSchemeId());
                    contextSchemeEle.addContent(schemeIdEle);
                }
                if (!StringUtils.isEmpty(contextScheme.getSchemeName())) {
                    Element schemeNameEle = new Element("scheme_name");
                    schemeNameEle.setText(contextScheme.getSchemeName());
                    contextSchemeEle.addContent(schemeNameEle);
                }
                if (!StringUtils.isEmpty(contextScheme.getDescription())) {
                    Element descriptionEle = new Element("description");
                    descriptionEle.addContent(new CDATA(contextScheme.getDescription()));
                    contextSchemeEle.addContent(descriptionEle);
                }
                if (!StringUtils.isEmpty(contextScheme.getSchemeAgencyId())) {
                    Element schemeAgencyIdEle = new Element("scheme_agency_id");
                    schemeAgencyIdEle.setText(contextScheme.getSchemeAgencyId());
                    contextSchemeEle.addContent(schemeAgencyIdEle);
                }
                if (!StringUtils.isEmpty(contextScheme.getSchemeVersionId())) {
                    Element schemeVersionIdEle = new Element("scheme_version_id");
                    schemeVersionIdEle.setText(contextScheme.getSchemeVersionId());
                    contextSchemeEle.addContent(schemeVersionIdEle);
                }

                Element createdEle = new Element("created");
                createdEle.setAttribute("by", userMap.get(contextScheme.getCreatedBy()).getLoginId());
                createdEle.setAttribute("timestamp", toZuluTimeString(contextScheme.getCreationTimestamp()));
                contextSchemeEle.addContent(createdEle);

                Element updatedEle = new Element("updated");
                updatedEle.setAttribute("by", userMap.get(contextScheme.getCreatedBy()).getLoginId());
                updatedEle.setAttribute("timestamp", toZuluTimeString(contextScheme.getCreationTimestamp()));
                contextSchemeEle.addContent(updatedEle);

                //-- Context Scheme Values --//
                List<ContextSchemeValue> contextSchemeValueList = contextSchemeValueMap.get(contextScheme.getCtxSchemeId());
                if (contextSchemeValueList != null && !contextSchemeValueList.isEmpty()) {
                    Element schemeValuesEle = new Element("scheme_values");
                    contextSchemeEle.addContent(schemeValuesEle);

                    for (ContextSchemeValue contextSchemeValue : contextSchemeValueList) {
                        Element schemeValueEle = new Element("scheme_value");
                        schemeValueEle.setAttribute("id", contextSchemeValue.getGuid());
                        schemeValuesEle.addContent(schemeValueEle);

                        if (!StringUtils.isEmpty(contextSchemeValue.getValue())) {
                            Element valueEle = new Element("value");
                            valueEle.setText(contextSchemeValue.getValue());
                            schemeValueEle.addContent(valueEle);
                        }
                        if (!StringUtils.isEmpty(contextSchemeValue.getMeaning())) {
                            Element meaningEle = new Element("meaning");
                            meaningEle.addContent(new CDATA(contextSchemeValue.getMeaning()));
                            schemeValueEle.addContent(meaningEle);
                        }
                    }
                }
            }
            this.rootElement.addContent(contextSchemesEle);
        }

        private void addBusinessContexts(
                Collection<BusinessContext> businessContexts,
                Collection<BusinessContextValue> businessContextValues
        ) {
            if (businessContexts.isEmpty()) {
                return;
            }

            Map<Long, List<BusinessContextValue>> businessContextValueMap = businessContextValues.stream()
                    .collect(groupingBy(e -> e.getBusinessContext().getBizCtxId()));

            Element businessContextsEle = new Element("business_contexts");
            for (BusinessContext businessContext : businessContexts) {
                Element businessContextEle = new Element("business_context");
                businessContextEle.setAttribute("id", businessContext.getGuid());
                businessContextsEle.addContent(businessContextEle);

                if (!StringUtils.isEmpty(businessContext.getName())) {
                    Element nameEle = new Element("name");
                    nameEle.setText(businessContext.getName());
                    businessContextEle.addContent(nameEle);
                }

                Element createdEle = new Element("created");
                createdEle.setAttribute("by", userMap.get(businessContext.getCreatedBy()).getLoginId());
                createdEle.setAttribute("timestamp", toZuluTimeString(businessContext.getCreationTimestamp()));
                businessContextEle.addContent(createdEle);

                Element updatedEle = new Element("updated");
                updatedEle.setAttribute("by", userMap.get(businessContext.getCreatedBy()).getLoginId());
                updatedEle.setAttribute("timestamp", toZuluTimeString(businessContext.getCreationTimestamp()));
                businessContextEle.addContent(updatedEle);

                //-- Business Context Values --//
                List<BusinessContextValue> businessContextValueList = businessContextValueMap.get(businessContext.getBizCtxId());
                if (businessContextValueList != null && !businessContextValueList.isEmpty()) {
                    Element contextValuesEle = new Element("context_values");
                    businessContextEle.addContent(contextValuesEle);

                    for (BusinessContextValue businessContextValue : businessContextValueList) {
                        Element contextValueEle = new Element("context_value");
                        contextValueEle.setAttribute("context_scheme_value_ref", businessContextValue.getContextSchemeValue().getGuid());
                        contextValuesEle.addContent(contextValueEle);
                    }
                }
            }

            this.rootElement.addContent(businessContextsEle);
        }

        private String toZuluTimeString(Date date) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.format(date);
        }

        @Override
        public void visitCodeListCollections(
                Collection<CodeList> codeLists,
                Collection<CodeListValue> codeListValues
        ) {
            if (codeLists.isEmpty()) {
                return;
            }

            codeListDocument = new Document();
            Element codeListSchemaElement = new Element("schema", XSD_NS);
            codeListSchemaElement.addNamespaceDeclaration(OAGI_NS);
            codeListSchemaElement.setAttribute("targetNamespace", SRTConstants.OAGI_NS);
            codeListSchemaElement.setAttribute("elementFormDefault", "qualified");
            codeListSchemaElement.setAttribute("attributeFormDefault", "unqualified");
            codeListDocument.addContent(codeListSchemaElement);

            Map<Long, CodeList> codeListMap = codeLists.stream()
                    .collect(Collectors.toMap(CodeList::getCodeListId, Function.identity()));

            Map<Long, List<CodeListValue>> codeListValueMap = codeListValues.stream()
                    .collect(groupingBy(CodeListValue::getCodeListId));

            for (CodeList codeList : codeLists) {
                String name = codeList.getName();
                Element codeListElement = new Element("simpleType", XSD_NS);
                if (!StringUtils.isEmpty(codeList.getDefinition())) {
                    Element annotationElement = new Element("annotation", XSD_NS);
                    codeListElement.addContent(annotationElement);

                    Element documentElement = new Element("document", XSD_NS);
                    annotationElement.addContent(documentElement);

                    if (!StringUtils.isEmpty(codeList.getDefinitionSource())) {
                        annotationElement.setAttribute("source", codeList.getDefinitionSource());
                    }

                    annotationElement.setText(codeList.getDefinition());
                }

                if (codeList.getEnumTypeGuid() != null) {
                    codeListElement.setAttribute("name", name + "EnumerationType");
                    codeListElement.setAttribute("id", codeList.getEnumTypeGuid());

                    List<CodeListValue> codeListValueList = codeListValueMap.get(codeList.getCodeListId());
                    if (codeListValueList != null && !codeListValueList.isEmpty()) {
                        addRestriction(codeListElement, codeListValueList);
                    }
                    codeListSchemaElement.addContent(codeListElement);

                    codeListElement = new Element("simpleType", XSD_NS);
                    codeListElement.setAttribute("name", name + "ContentType");
                    codeListElement.setAttribute("id", codeList.getGuid());
                    Element unionElement = new Element("union", XSD_NS);

                    CodeList baseCodeList = codeListMap.get(codeList.getBasedCodeListId());
                    if (baseCodeList == null) {
                        unionElement.setAttribute("memberTypes", name + "EnumerationType" + " xsd:token");
                    } else {
                        unionElement.setAttribute("memberTypes", baseCodeList.getName() + "ContentType" + " xsd:token");
                    }

                    codeListElement.addContent(unionElement);
                    codeListSchemaElement.addContent(codeListElement);

                } else {
                    codeListElement.setAttribute("name", name + "ContentType");
                    codeListElement.setAttribute("id", codeList.getGuid());

                    CodeList baseCodeList = codeListMap.get(codeList.getBasedCodeListId());
                    if (baseCodeList != null) {
                        Element unionElement = new Element("union", XSD_NS);
                        unionElement.setAttribute("memberTypes", baseCodeList.getName() + "ContentType" + " xsd:token");
                        codeListElement.addContent(unionElement);
                    } else {
                        List<CodeListValue> codeListValueList = codeListValueMap.get(codeList.getCodeListId());
                        if (codeListValueList == null || codeListValueList.isEmpty()) {
                            Element restrictionElement = new Element("restriction", XSD_NS);
                            restrictionElement.setAttribute("base", "xsd:normalizedString");
                            codeListElement.addContent(restrictionElement);
                        } else {
                            addRestriction(codeListElement, codeListValueList);
                        }
                    }

                    codeListSchemaElement.addContent(codeListElement);
                }
            }
        }

        private void addRestriction(Element codeListElement, Collection<CodeListValue> values) {
            Element restrictionElement = new Element("restriction", XSD_NS);
            restrictionElement.setAttribute("base", "xsd:token");
            codeListElement.addContent(restrictionElement);

            for (CodeListValue value : values) {
                Element enumerationElement = new Element("enumeration", XSD_NS);
                enumerationElement.setAttribute("value", value.getValue());

                if (!StringUtils.isEmpty(value.getDefinition())) {
                    Element annotationElement = new Element("annotation", XSD_NS);
                    enumerationElement.addContent(annotationElement);

                    Element documentElement = new Element("document", XSD_NS);
                    annotationElement.addContent(documentElement);

                    if (!StringUtils.isEmpty(value.getDefinitionSource())) {
                        annotationElement.setAttribute("source", value.getDefinitionSource());
                    }

                    String definition = value.getDefinition();
                    try {
                        StringBuilder sb = new StringBuilder();
                        // To read the definition, it must has defined 'xsd:schema' as a parent.
                        sb.append("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                                "xmlns=\"http://www.openapplications.org/oagis/10\">");
                        sb.append(definition);
                        sb.append("</xsd:schema>");

                        Element content = new SAXBuilder().build(
                                new StringReader(sb.toString())).getRootElement();
                        for (Content child : content.removeContent()) {
                            documentElement.addContent(child);
                        }
                    } catch (JDOMException | IOException e) {
                        documentElement.setText(definition);
                    }
                }

                restrictionElement.addContent(enumerationElement);
            }
        }

        @Override
        public void visitProfileBIEs(Collection<TopLevelAbie> topLevelAbies) throws Exception {
            if (topLevelAbies.isEmpty()) {
                return;
            }

            ProfileBODGenerationOption option = new ProfileBODGenerationOption();
            option.setSchemaPackage(ProfileBODGenerationOption.SchemaPackage.Each);
            option.setSchemaExpression(ProfileBODGenerationOption.SchemaExpression.XML);

            profileBIEs = profileBODGenerateService.generateSchemaForEach(
                    topLevelAbies.stream()
                            .map(e -> e.getTopLevelAbieId()).collect(Collectors.toList()),
                    option);

            Element businessInformationEntitiesEle = new Element("business_information_entities");
            for (TopLevelAbie topLevelAbie : topLevelAbies) {
                Element bieEle = new Element("business_information_entity");

                bieEle.setAttribute("owner", userMap.get(topLevelAbie.getOwnerUserId()).getLoginId());
                Release release = releaseRepository.findOne(topLevelAbie.getReleaseId());
                bieEle.setAttribute("release", release.getReleaseNum());
                bieEle.setAttribute("state", topLevelAbie.getState().toString());

                BusinessContext businessContext = businessContextRepository.findOne(topLevelAbie.getAbie().getBizCtxId());
                bieEle.setAttribute("business_context_ref", businessContext.getGuid());
                File file = profileBIEs.get(topLevelAbie.getTopLevelAbieId());
                bieEle.setAttribute("href", "file:///" + file.getName());

                businessInformationEntitiesEle.addContent(bieEle);
            }

            this.rootElement.addContent(businessInformationEntitiesEle);
        }

        @Override
        public void visitEnd() throws IOException {
            if (this.document == null) {
                throw new IllegalStateException();
            }

            File tempFile = File.createTempFile(Utility.generateGUID(), null);
            File tempDir = tempFile.getParentFile();
            FileUtils.deleteQuietly(tempFile);

            List<File> targetFiles = new ArrayList();

            File exportFile = new File(tempDir, "export.xml");

            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(exportFile))) {
                outputter.output(this.document, outputStream);
                outputStream.flush();
            }
            targetFiles.add(exportFile);

            if (this.codeListDocument != null) {
                File codeListFile = new File(tempDir, "CodeList.xsd");
                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(codeListFile))) {
                    outputter.output(this.document, outputStream);
                    outputStream.flush();
                }
                targetFiles.add(codeListFile);
            }

            targetFiles.addAll(profileBIEs.values());

            String zipFilename = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "-export";
            zipFile = Zip.compression(targetFiles, zipFilename);
        }
    }

    @Transactional
    public File export() throws Exception {
        XMLTraversalExportVisitor visitor = new XMLTraversalExportVisitor();

        visitor.visitStart();
        visitor.visitUsers(userRepository.findAll());
        visitor.visitBusinessContextCollections(
                contextCategoryRepository.findAll(),
                contextSchemeRepository.findAll(),
                contextSchemeValueRepository.findAll(),
                businessContextRepository.findAll(),
                businessContextValueRepository.findAll()
        );
        visitor.visitCodeListCollections(
                codeListRepository.findAll(),
                codeListValueRepository.findAll()
        );
        visitor.visitProfileBIEs(topLevelAbieRepository.findAll());
        visitor.visitEnd();

        return visitor.zipFile;
    }
}
