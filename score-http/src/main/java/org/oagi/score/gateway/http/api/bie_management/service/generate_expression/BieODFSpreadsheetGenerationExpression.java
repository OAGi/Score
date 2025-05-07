package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import com.github.jferard.fastods.*;
import com.github.jferard.fastods.odselement.MetaElement;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.camelCase;
import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.toName;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class BieODFSpreadsheetGenerationExpression implements BieGenerateExpression, InitializingBean {

    private static final String INDEXER_STR = "[0]";

    private class RowRecord {
        String type;
        String columnName;
        String fullPath;
        String maxCardinality;
        String contextDefinition;
        String example;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GenerateExpressionOption option;

    private TopLevelAsbiepSummaryRecord topLevelAsbiep;
    private List<RowRecord> rowRecords;

    @Autowired
    private ApplicationContext applicationContext;

    private GenerationContext generationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        rowRecords = new ArrayList();
    }

    @Override
    public void reset() throws Exception {
        this.afterPropertiesSet();
    }

    @Override
    public GenerationContext generateContext(ScoreUser requester, List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option) {
        return applicationContext.getBean(GenerationContext.class, requester, topLevelAsbieps);
    }

    @Override
    public void generate(ScoreUser requester, TopLevelAsbiepSummaryRecord topLevelAsbiep, GenerationContext generationContext, GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    private void generateTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        this.topLevelAsbiep = topLevelAsbiep;

        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);

            AsccpSummaryRecord basedAsccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
            String name = camelCase(basedAsccp.propertyTerm());

            Stack<String> paths = new Stack();
            paths.push(name);

            RowRecord rowRecord = new RowRecord();
            rowRecord.type = "ASBIEP";
            rowRecord.fullPath = String.join(".", paths);
            rowRecord.maxCardinality = "1";
            rowRecord.contextDefinition = asbiep.definition();
            rowRecords.add(rowRecord);

            traverse(typeAbie, paths);
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private void traverse(AbieSummaryRecord abie, Stack<String> paths) {
        List<BIE> children = generationContext.queryChildBIEs(abie);
        AccSummaryRecord acc = generationContext.getAcc(abie.basedAccManifestId());
        for (BIE bie : children) {
            traverse(bie, paths);
        }
    }

    private void traverse(BIE bie, Stack<String> paths) {
        Stack<String> copiedPaths = new Stack();
        copiedPaths.addAll(paths);

        if (bie instanceof BbieSummaryRecord) {
            BbieSummaryRecord bbie = (BbieSummaryRecord) bie;

            BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
            BccpSummaryRecord bccp = generationContext.queryToBCCP(bcc);
            DtSummaryRecord bdt = generationContext.queryBDT(bccp);

            List<BbieScSummaryRecord> bbieScList = generationContext.queryBBIESCs(bbie)
                    .stream().filter(e -> e.cardinality().max() != 0).collect(Collectors.toList());

            String name = camelCase(bccp.propertyTerm());
            String prefix = name;
            if (!bbieScList.isEmpty()) {
                if (bbie.cardinality().max() == -1 || bbie.cardinality().max() > 1) {
                    prefix = prefix + INDEXER_STR;
                }
                name = prefix + ".content";
            }

            copiedPaths.push(name);

            RowRecord bbieRowRecord = new RowRecord();
            bbieRowRecord.type = "BBIE";
            bbieRowRecord.fullPath = String.join(".", copiedPaths);
            bbieRowRecord.maxCardinality = (bbie.cardinality().max() == -1) ? "unbounded" : Integer.toString(bbie.cardinality().max());
            if (StringUtils.hasLength(bbie.definition())) {
                bbieRowRecord.contextDefinition = bbie.definition();
            }
            if (StringUtils.hasLength(bbie.example())) {
                bbieRowRecord.example = bbie.example();
            }
            rowRecords.add(bbieRowRecord);

            for (BbieScSummaryRecord bbieSc : bbieScList) {
                DtScSummaryRecord dtSc = generationContext.getDtSc(bbieSc.basedDtScManifestId());
                String dtScName = toName(dtSc.propertyTerm(), dtSc.representationTerm(), rt -> {
                    if ("Text".equals(rt)) {
                        return "";
                    }
                    return rt;
                }, true);

                Stack<String> dtScPaths = new Stack();
                dtScPaths.addAll(copiedPaths);
                dtScPaths.pop();
                dtScPaths.push(prefix);
                dtScPaths.push(dtScName);

                RowRecord dtScRowRecord = new RowRecord();
                dtScRowRecord.type = "BBIE_SC";
                dtScRowRecord.fullPath = String.join(".", dtScPaths);
                dtScRowRecord.maxCardinality = (bbieSc.cardinality().max() == -1) ? "unbounded" : Integer.toString(bbieSc.cardinality().max());
                if (StringUtils.hasLength(bbieSc.definition())) {
                    dtScRowRecord.contextDefinition = bbieSc.definition();
                }
                if (StringUtils.hasLength(bbieSc.example())) {
                    dtScRowRecord.example = bbieSc.example();
                }
                rowRecords.add(dtScRowRecord);
            }
        } else {
            AsbieSummaryRecord asbie = (AsbieSummaryRecord) bie;

            AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);
            generationContext.referenceCounter().increase(asbiep)
                    .ifNotCircularReference(asbiep,
                            () -> {
                                AbieSummaryRecord typeAbie = generationContext.queryTargetABIE(asbiep);

                                AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
                                String name = camelCase(asccp.propertyTerm());

                                copiedPaths.push(name);

                                RowRecord asbieRowRecord = new RowRecord();
                                asbieRowRecord.type = "ASBIE";
                                asbieRowRecord.fullPath = String.join(".", copiedPaths);
                                asbieRowRecord.maxCardinality = (asbie.cardinality().max() == -1) ? "unbounded" : Integer.toString(asbie.cardinality().max());
                                if (StringUtils.hasLength(asbie.definition())) {
                                    asbieRowRecord.contextDefinition = asbie.definition();
                                }
                                rowRecords.add(asbieRowRecord);

                                if (asbie.cardinality().max() == -1 || asbie.cardinality().max() > 1) {
                                    copiedPaths.push(copiedPaths.pop() + INDEXER_STR);
                                }

                                traverse(typeAbie, copiedPaths);
                            })
                    .decrease(asbiep);
        }
    }

    @Override
    public File asFile(String filename) throws IOException {
        List<RowRecord> rowRecords = this.rowRecords;
        if (this.option.isOnlyBCCPsForOpenDocumentFormat()) {
            rowRecords = rowRecords.stream().filter(e -> !e.type.startsWith("ASBIE")).collect(Collectors.toList());
        }
        generateColumnNames(rowRecords, 1);

        File tempFile = File.createTempFile(ScoreGuidUtils.randomGuid(), null);

        switch (this.option.getOdfExpressionFormat()) {
            case "XLSX":
                tempFile = new File(tempFile.getParentFile(), filename + ".xlsx");
                writeAsXlsx(tempFile, rowRecords);
                break;
            case "FODS":
                tempFile = new File(tempFile.getParentFile(), filename + ".fods");
                writeAsFods(tempFile, rowRecords);
                break;
            case "ODS":
                tempFile = new File(tempFile.getParentFile(), filename + ".ods");
                writeAsOds(tempFile, rowRecords);
                break;
            default:
        }

        logger.info("ODF Spreadsheet is generated: " + tempFile);

        return tempFile;
    }

    private void writeAsFods(File file, List<RowRecord> rowRecords) throws IOException {
        FlatXMLODFSpreadsheetWriter writer = new FlatXMLODFSpreadsheetWriter();
        String creator = this.generationContext.findUserName(
                this.topLevelAsbiep.owner().userId());
        writer.addMeta(creator, this.topLevelAsbiep.lastUpdated().when());
        writer.startBody();
        writer.fillRowRecords(rowRecords);
        writer.endBody();
        writer.write(file);
    }

    private void writeAsOds(File file, List<RowRecord> rowRecords) throws IOException {
        String creator = this.generationContext.findUserName(
                this.topLevelAsbiep.owner().userId());
        MetaElement metaElement = MetaElement.builder()
                .creator(creator)
                .date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        .format(this.topLevelAsbiep.lastUpdated().when()))
                .build();
        OdsFactory odsFactory = new OdsFactoryBuilder(
                java.util.logging.Logger.getLogger(this.getClass().getName()), Locale.US)
                .metaElement(metaElement)
                .build();
        NamedOdsFileWriter odsFileWriter = odsFactory.createWriter(file);
        NamedOdsDocument odsDocument = odsFileWriter.document();

        Table templateTable = odsDocument.addTable("Template");
        int templateTableCellIndex = 0;
        for (RowRecord rowRecord : rowRecords) {
            TableRowImpl templateTableRow0 = templateTable.getRow(0);
            TableCell templateTableRow0Cell = templateTableRow0.getOrCreateCell(templateTableCellIndex);
            templateTableRow0Cell.setStringValue(rowRecord.columnName);

            TableRowImpl templateTableRow1 = templateTable.getRow(1);
            TableCell templateTableRow1Cell = templateTableRow1.getOrCreateCell(templateTableCellIndex++);
            templateTableRow1Cell.setStringValue(rowRecord.example);
        }

        Table specificationTable = odsDocument.addTable("Specification");
        int specificationTableRowIndex = 0;
        TableRowImpl specificationTableRow0 = specificationTable.getRow(specificationTableRowIndex++);
        List<String> headers = Arrays.asList("Cell", "ColumnName", "FullPath",
                "MaxCardinality", "ContextDefinition", "ExampleData");
        for (int i = 0, len = headers.size(); i < len; ++i) {
            TableCell specificationTableRow0Cell = specificationTableRow0.getOrCreateCell(i);
            specificationTableRow0Cell.setStringValue(headers.get(i));
        }

        templateTableCellIndex = 0;
        for (RowRecord rowRecord : rowRecords) {
            TableRowImpl specificationTableRow = specificationTable.getRow(specificationTableRowIndex++);
            int specificationTableRowCellIndex = 0;
            TableCell specificationTableRowCell =
                    specificationTableRow.getOrCreateCell(specificationTableRowCellIndex++);
            specificationTableRowCell.setStringValue(cellColumnNumberToString(++templateTableCellIndex) + "1");

            specificationTableRowCell =
                    specificationTableRow.getOrCreateCell(specificationTableRowCellIndex++);
            specificationTableRowCell.setStringValue(rowRecord.columnName);

            specificationTableRowCell =
                    specificationTableRow.getOrCreateCell(specificationTableRowCellIndex++);
            specificationTableRowCell.setStringValue(rowRecord.fullPath);

            specificationTableRowCell =
                    specificationTableRow.getOrCreateCell(specificationTableRowCellIndex++);
            specificationTableRowCell.setStringValue(rowRecord.maxCardinality);

            specificationTableRowCell =
                    specificationTableRow.getOrCreateCell(specificationTableRowCellIndex++);
            specificationTableRowCell.setStringValue(rowRecord.contextDefinition);

            specificationTableRowCell =
                    specificationTableRow.getOrCreateCell(specificationTableRowCellIndex++);
            specificationTableRowCell.setStringValue(rowRecord.example);
        }

        odsFileWriter.save();
    }

    private void writeAsXlsx(File file, List<RowRecord> rowRecords) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        POIXMLProperties poixmlProperties = workbook.getProperties();
        POIXMLProperties.CoreProperties coreProperties = poixmlProperties.getCoreProperties();
        String creator = this.generationContext.findUserName(
                this.topLevelAsbiep.owner().userId());
        coreProperties.setCreator(creator);
        try {
            coreProperties.setCreated(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .format(this.topLevelAsbiep.lastUpdated().when()));
        } catch (InvalidFormatException e) {
            throw new IllegalStateException(e);
        }

        Sheet templateSheet = workbook.createSheet("Template");
        int templateSheetCellIndex = 0;
        Row templateSheetRow0 = templateSheet.createRow(0);
        Row templateSheetRow1 = templateSheet.createRow(1);
        for (RowRecord rowRecord : rowRecords) {
            Cell templateSheetRow0Cell = templateSheetRow0.createCell(templateSheetCellIndex);
            templateSheetRow0Cell.setCellValue(rowRecord.columnName);

            Cell templateSheetRow1Cell = templateSheetRow1.createCell(templateSheetCellIndex++);
            templateSheetRow1Cell.setCellValue(rowRecord.example);
        }
        // autoSizeColumn
        templateSheet.getRow(0).cellIterator().forEachRemaining(cell -> {
            templateSheet.autoSizeColumn(cell.getColumnIndex());
        });

        Sheet specificationSheet = workbook.createSheet("Specification");
        int specificationSheetRowIndex = 0;
        Row specificationSheetRow0 = specificationSheet.createRow(specificationSheetRowIndex++);
        List<String> headers = Arrays.asList("Cell", "ColumnName", "FullPath",
                "MaxCardinality", "ContextDefinition", "ExampleData");
        for (int i = 0, len = headers.size(); i < len; ++i) {
            Cell specificationSheetRow0Cell = specificationSheetRow0.createCell(i, CellType.STRING);
            specificationSheetRow0Cell.setCellValue(headers.get(i));
        }

        templateSheetCellIndex = 0;
        for (RowRecord rowRecord : rowRecords) {
            Row specificationSheetRow = specificationSheet.createRow(specificationSheetRowIndex++);
            int specificationSheetRowCellIndex = 0;
            Cell specificationSheetRowCell =
                    specificationSheetRow.createCell(specificationSheetRowCellIndex++, CellType.STRING);
            specificationSheetRowCell.setCellValue(cellColumnNumberToString(++templateSheetCellIndex) + "1");

            specificationSheetRowCell =
                    specificationSheetRow.createCell(specificationSheetRowCellIndex++, CellType.STRING);
            specificationSheetRowCell.setCellValue(rowRecord.columnName);

            specificationSheetRowCell =
                    specificationSheetRow.createCell(specificationSheetRowCellIndex++, CellType.STRING);
            specificationSheetRowCell.setCellValue(rowRecord.fullPath);

            specificationSheetRowCell =
                    specificationSheetRow.createCell(specificationSheetRowCellIndex++, CellType.STRING);
            specificationSheetRowCell.setCellValue(rowRecord.maxCardinality);

            specificationSheetRowCell =
                    specificationSheetRow.createCell(specificationSheetRowCellIndex++, CellType.STRING);
            specificationSheetRowCell.setCellValue(rowRecord.contextDefinition);

            specificationSheetRowCell =
                    specificationSheetRow.createCell(specificationSheetRowCellIndex++, CellType.STRING);
            specificationSheetRowCell.setCellValue(rowRecord.example);
        }
        // autoSizeColumn
        specificationSheet.getRow(0).cellIterator().forEachRemaining(cell -> {
            specificationSheet.autoSizeColumn(cell.getColumnIndex());
        });

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            workbook.write(outputStream);
            outputStream.flush();
        }
    }

    private void generateColumnNames(List<RowRecord> rowRecords, int depth) {
        Map<String, List<RowRecord>> columnNameMap = new HashMap();
        for (RowRecord rowRecord : rowRecords) {
            String fullPath = rowRecord.fullPath.replaceAll("\\[0\\]", "");
            List<String> paths = Arrays.asList(fullPath.split("\\."));
            if (paths.size() > depth) {
                paths = paths.subList(paths.size() - depth, paths.size());
            }

            String columnName = String.join(".", paths);
            if (!columnNameMap.containsKey(columnName)) {
                columnNameMap.put(columnName, new ArrayList());
            }
            columnNameMap.get(columnName).add(rowRecord);
        }
        for (Map.Entry<String, List<RowRecord>> entry : columnNameMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                generateColumnNames(entry.getValue(), depth + 1);
            } else {
                entry.getValue().get(0).columnName = entry.getKey();
            }
        }
    }

    private static String cellColumnNumberToString(int cellColumnNumber) {
        String str = "";
        while (cellColumnNumber > 0) {
            int remainder = (cellColumnNumber - 1) % 26;
            str = Character.toString('A' + remainder) + str;
            cellColumnNumber = (cellColumnNumber - remainder) / 26;
        }
        return str;
    }

    private static class FlatXMLODFSpreadsheetWriter {

        Namespace officeNs = Namespace.getNamespace("office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0");
        Namespace metaNs = Namespace.getNamespace("meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
        Namespace foNs = Namespace.getNamespace("fo", "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0");
        Namespace oooNs = Namespace.getNamespace("ooo", "http://openoffice.org/2004/office");
        Namespace xlinkNs = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
        Namespace styleNs = Namespace.getNamespace("style", "urn:oasis:names:tc:opendocument:xmlns:style:1.0");
        Namespace configNs = Namespace.getNamespace("config", "urn:oasis:names:tc:opendocument:xmlns:config:1.0");
        Namespace dcNs = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
        Namespace textNs = Namespace.getNamespace("text", "urn:oasis:names:tc:opendocument:xmlns:text:1.0");
        Namespace drawNs = Namespace.getNamespace("draw", "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0");
        Namespace dr3dNs = Namespace.getNamespace("dr3d", "urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0");
        Namespace svgNs = Namespace.getNamespace("svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0");
        Namespace chartNs = Namespace.getNamespace("chart", "urn:oasis:names:tc:opendocument:xmlns:chart:1.0");
        Namespace rptNs = Namespace.getNamespace("rpt", "http://openoffice.org/2005/report");
        Namespace tableNs = Namespace.getNamespace("table", "urn:oasis:names:tc:opendocument:xmlns:table:1.0");
        Namespace numberNs = Namespace.getNamespace("number", "urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0");
        Namespace ooowNs = Namespace.getNamespace("ooow", "http://openoffice.org/2004/writer");
        Namespace ooocNs = Namespace.getNamespace("oooc", "http://openoffice.org/2004/calc");
        Namespace ofNs = Namespace.getNamespace("of", "urn:oasis:names:tc:opendocument:xmlns:of:1.2");
        Namespace tableoooNs = Namespace.getNamespace("tableooo", "http://openoffice.org/2009/table");
        Namespace calcextNs = Namespace.getNamespace("calcext", "urn:org:documentfoundation:names:experimental:calc:xmlns:calcext:1.0");
        Namespace drawoooNs = Namespace.getNamespace("drawooo", "http://openoffice.org/2010/draw");
        Namespace loextNs = Namespace.getNamespace("loext", "urn:org:documentfoundation:names:experimental:office:xmlns:loext:1.0");
        Namespace fieldNs = Namespace.getNamespace("field", "urn:openoffice:names:experimental:ooo-ms-interop:xmlns:field:1.0");
        Namespace mathNs = Namespace.getNamespace("math", "http://www.w3.org/1998/Math/MathML");
        Namespace formNs = Namespace.getNamespace("form", "urn:oasis:names:tc:opendocument:xmlns:form:1.0");
        Namespace scriptNs = Namespace.getNamespace("script", "urn:oasis:names:tc:opendocument:xmlns:script:1.0");
        Namespace domNs = Namespace.getNamespace("dom", "http://www.w3.org/2001/xml-events");
        Namespace xformsNs = Namespace.getNamespace("xforms", "http://www.w3.org/2002/xforms");
        Namespace xsdNs = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        Namespace xsiNs = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Namespace formxNs = Namespace.getNamespace("formx", "urn:openoffice:names:experimental:ooxml-odf-interop:xmlns:form:1.0");
        Namespace xhtmlNs = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml");
        Namespace grddlNs = Namespace.getNamespace("grddl", "http://www.w3.org/2003/g/data-view#");
        Namespace css3tNs = Namespace.getNamespace("css3t", "http://www.w3.org/TR/css3-text/");
        Namespace presentationNs = Namespace.getNamespace("presentation", "urn:oasis:names:tc:opendocument:xmlns:presentation:1.0");

        private Document document;
        private Element documentElement;
        private Element spreadsheet;
        private Element documentStatistic;
        private Element templateTable;
        private Element specificationTable;

        private int tableCount = 0;
        private int cellCount = 0;

        FlatXMLODFSpreadsheetWriter() {
            document = new Document();

            documentElement = new Element("document", officeNs);
            documentElement.addNamespaceDeclaration(metaNs);
            documentElement.addNamespaceDeclaration(foNs);
            documentElement.addNamespaceDeclaration(oooNs);
            documentElement.addNamespaceDeclaration(xlinkNs);
            documentElement.addNamespaceDeclaration(styleNs);
            documentElement.addNamespaceDeclaration(configNs);
            documentElement.addNamespaceDeclaration(dcNs);
            documentElement.addNamespaceDeclaration(textNs);
            documentElement.addNamespaceDeclaration(drawNs);
            documentElement.addNamespaceDeclaration(dr3dNs);
            documentElement.addNamespaceDeclaration(svgNs);
            documentElement.addNamespaceDeclaration(chartNs);
            documentElement.addNamespaceDeclaration(rptNs);
            documentElement.addNamespaceDeclaration(tableNs);
            documentElement.addNamespaceDeclaration(numberNs);
            documentElement.addNamespaceDeclaration(ooowNs);
            documentElement.addNamespaceDeclaration(ooocNs);
            documentElement.addNamespaceDeclaration(ofNs);
            documentElement.addNamespaceDeclaration(tableoooNs);
            documentElement.addNamespaceDeclaration(calcextNs);
            documentElement.addNamespaceDeclaration(drawoooNs);
            documentElement.addNamespaceDeclaration(loextNs);
            documentElement.addNamespaceDeclaration(fieldNs);
            documentElement.addNamespaceDeclaration(mathNs);
            documentElement.addNamespaceDeclaration(formNs);
            documentElement.addNamespaceDeclaration(scriptNs);
            documentElement.addNamespaceDeclaration(domNs);
            documentElement.addNamespaceDeclaration(xformsNs);
            documentElement.addNamespaceDeclaration(xsdNs);
            documentElement.addNamespaceDeclaration(xsiNs);
            documentElement.addNamespaceDeclaration(formxNs);
            documentElement.addNamespaceDeclaration(xhtmlNs);
            documentElement.addNamespaceDeclaration(grddlNs);
            documentElement.addNamespaceDeclaration(css3tNs);
            documentElement.addNamespaceDeclaration(presentationNs);

            documentElement.setAttribute("version", "1.3", officeNs);
            documentElement.setAttribute("mimetype", "application/vnd.oasis.opendocument.spreadsheet", officeNs);

            document.addContent(documentElement);
        }

        public void addMeta(String username, Date creationDateTime) {
            Element officeMeta = new Element("meta", officeNs);
            documentElement.addContent(officeMeta);

            Element initialCreator = new Element("initial-creator", metaNs);
            initialCreator.setText(username);
            officeMeta.addContent(initialCreator);

            Element creationDate = new Element("creation-date", metaNs);
            creationDate.setText(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS").format(creationDateTime));
            officeMeta.addContent(creationDate);

            Element date = new Element("date", dcNs);
            date.setText(creationDate.getText());
            officeMeta.addContent(date);

            Element creator = new Element("creator", dcNs);
            creator.setText(initialCreator.getText());
            officeMeta.addContent(creator);

//            Element editingDuration = new Element("editing-duration", metaNs);
//            editingDuration.setText("");
//            officeMeta.addContent(editingDuration);
//
//            Element editingCycles = new Element("editing-cycles", metaNs);
//            editingCycles.setText("");
//            officeMeta.addContent(editingCycles);

            Element generator = new Element("generator", metaNs);
            generator.setText("Score/2.4.0");
            officeMeta.addContent(generator);

            documentStatistic = new Element("document-statistic", metaNs);
            documentStatistic.setAttribute("object-count", "0", metaNs);
            officeMeta.addContent(documentStatistic);
        }

        public void startBody() {
            Element officeBody = new Element("body", officeNs);
            documentElement.addContent(officeBody);

            spreadsheet = new Element("spreadsheet", officeNs);
            officeBody.addContent(spreadsheet);

            Element tableCalculationSettings = new Element("calculation-settings", tableNs);
            tableCalculationSettings.setAttribute("automatic-find-labels", "false", tableNs);
            tableCalculationSettings.setAttribute("use-regular-expressions", "false", tableNs);
            tableCalculationSettings.setAttribute("use-wildcards", "true", tableNs);
            spreadsheet.addContent(tableCalculationSettings);

            templateTable = new Element("table", tableNs);
            tableCount++;
            templateTable.setAttribute("name", "Template", tableNs);
            templateTable.setAttribute("style-name", "ta1", tableNs);
            spreadsheet.addContent(templateTable);

            specificationTable = new Element("table", tableNs);
            tableCount++;
            specificationTable.setAttribute("name", "Specification", tableNs);
            specificationTable.setAttribute("style-name", "ta1", tableNs);
            spreadsheet.addContent(specificationTable);
        }

        public void fillRowRecords(List<RowRecord> rowRecords) {
            // Template Table
            Element templateTableNameRow = new Element("table-row", tableNs);
            templateTableNameRow.setAttribute("style-name", "ro1", tableNs);
            templateTable.addContent(templateTableNameRow);

            Element templateTableExampleRow = new Element("table-row", tableNs);
            templateTableExampleRow.setAttribute("style-name", "ro1", tableNs);
            templateTable.addContent(templateTableExampleRow);

            for (RowRecord rowRecord : rowRecords) {
                Element templateTableNameCell = tableCellAsString(rowRecord.columnName);
                templateTableNameRow.addContent(templateTableNameCell);

                Element templateTableExampleCell = tableCellAsString(rowRecord.example);
                templateTableExampleRow.addContent(templateTableExampleCell);
            }

            // Specification Table
            List<String> headers = Arrays.asList("Cell", "ColumnName", "FullPath",
                    "MaxCardinality", "ContextDefinition", "ExampleData");
            Element specificationHeaderTableRow = new Element("table-row", tableNs);
            specificationHeaderTableRow.setAttribute("style-name", "ro1", tableNs);
            specificationTable.addContent(specificationHeaderTableRow);

            for (String header : headers) {
                Element specificationTableCell = tableCellAsString(header);
                specificationHeaderTableRow.addContent(specificationTableCell);
            }

            int index = 1;
            for (RowRecord rowRecord : rowRecords) {
                Element specificationTableRow = new Element("table-row", tableNs);
                specificationTableRow.setAttribute("style-name", "ro1", tableNs);
                specificationTable.addContent(specificationTableRow);

                specificationTableRow.addContent(tableCellAsString(cellColumnNumberToString(index++) + "1"));
                specificationTableRow.addContent(tableCellAsString(rowRecord.columnName));
                specificationTableRow.addContent(tableCellAsString(rowRecord.fullPath));
                specificationTableRow.addContent(tableCellAsString(rowRecord.maxCardinality));
                specificationTableRow.addContent(tableCellAsString(rowRecord.contextDefinition));
                specificationTableRow.addContent(tableCellAsString(rowRecord.example));
            }
        }

        private Element tableCellAsString(String text) {
            Element tableCell = new Element("table-cell", tableNs);
            tableCell.setAttribute("value-type", "string", officeNs);
            tableCell.setAttribute("value-type", "string", calcextNs);

            Element textP = new Element("p", textNs);
            textP.setText(text);
            tableCell.addContent(textP);

            this.cellCount++;

            return tableCell;
        }

        public void endBody() {
            documentStatistic.setAttribute("table-count", Integer.toString(tableCount), metaNs);
            documentStatistic.setAttribute("cell-count", Integer.toString(cellCount), metaNs);
        }

        private void write(File file) throws IOException {
            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.setFormat(Format.getPrettyFormat());
            try (FileWriter writer = new FileWriter(file)) {
                xmlOutputter.output(document, writer);
                writer.flush();
            }
        }
    }
}
