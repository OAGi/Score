package org.oagi.score.export.schematron;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.score.common.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class SchemaDiffGenerator {

    @Autowired
    private ResourceLoader resourceLoader;

    private File baseDir;
    private File source;

    public void setBaseDirectory(File baseDirectory) throws IOException {
        this.baseDir = baseDirectory.getCanonicalFile();
    }

    public void setSource(File source) throws IOException {
        this.source = source;
    }

    public void generate() {
        Transformer diffTransformer = toTransformer(getResourceFile("classpath:schematron/SchemaDiff8_1.xsl"));
        generateSchematronFile(diffTransformer, this.source);

        Transformer reportTransformer = toTransformer(getResourceFile("classpath:schematron/sch/schematron-report.xsl"));
        generateReportXSLFile(reportTransformer, this.baseDir);
    }

    private File getResourceFile(String expression) {
        Resource resource = resourceLoader.getResource(expression);
        if (resource == null) {
            throw new IllegalStateException("Could not find " + expression);
        }
        try {
            return resource.getFile();
        } catch (IOException e) {
            throw new IllegalStateException("I/O error", e);
        }
    }

    private Transformer toTransformer(File resourceFile) {
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        try {
            Source xsl = new StreamSource(resourceFile);
            Templates templates = factory.newTemplates(xsl);
            Transformer transformer = templates.newTransformer();
            return transformer;
        } catch (TransformerConfigurationException e) {
            throw new IllegalArgumentException("Invalid XSLT content", e);
        }
    }

    private void generateSchematronFile(Transformer transformer, File sourceFile) {
        if (sourceFile == null) {
            return;
        }

        if (sourceFile.isDirectory()) {
            for (File childSource : sourceFile.listFiles()) {
                generateSchematronFile(transformer, childSource);
            }
        } else {
            if (!sourceFile.getName().endsWith(".xsd")) {
                return;
            }

            File resultFile = getResultFile(sourceFile);
            try {
                FileUtils.forceMkdir(resultFile.getParentFile());
            } catch (IOException e) {
                throw new IllegalStateException("I/O error", e);
            }

            transform(transformer, sourceFile, resultFile);
        }
    }

    private void transform(Transformer transformer, File sourceFile, File resultFile) {
        Source xmlSource = new StreamSource(sourceFile);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream outputStream = new BufferedOutputStream(byteArrayOutputStream);
        Result result = new StreamResult(outputStream);
        try {
            transformer.transform(xmlSource, result);
            outputStream.flush();

            byte[] content = byteArrayOutputStream.toByteArray();

            String extension = FilenameUtils.getExtension(resultFile.getName());
            if (Arrays.asList("xsd", "xsl").contains(extension)) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));

                try (InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(content));
                     OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(resultFile))) {
                    outputter.output(new DOMBuilder().build(documentBuilder.parse(inputStream)), outputStream1);
                    outputStream.flush();
                }
            } else {
                try (InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(content));
                     OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(resultFile))) {
                    IOUtils.copyLarge(inputStream, outputStream1);
                    outputStream1.flush();
                }
            }
        } catch (TransformerException | ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalArgumentException("Could not transform by " + sourceFile, e);
        }
    }

    private void generateReportXSLFile(Transformer transformer, File sourceFile) {
        if (sourceFile == null) {
            return;
        }

        if (sourceFile.isDirectory()) {
            for (File childSource : sourceFile.listFiles()) {
                generateReportXSLFile(transformer, childSource);
            }
        } else {
            if (!sourceFile.getName().endsWith("Sch.xsd")) {
                return;
            }

            File resultFile;
            try {
                resultFile = new File(sourceFile.getCanonicalPath().replaceAll("Sch.xsd", ".xsl"));
            } catch (IOException e) {
                throw new IllegalStateException("I/O error", e);
            }

            transform(transformer, sourceFile, resultFile);
            FileUtils.deleteQuietly(sourceFile);
        }
    }

    private File getResultFile(File sourceFile) {
        try {
            Path pathAbsolute = Paths.get(sourceFile.getCanonicalPath());
            Path pathBase = Paths.get(source.getCanonicalPath());
            Path pathRelative = pathBase.relativize(pathAbsolute);

            File relativeSourceFile = new File(source.getName(),
                    FilenameUtils.separatorsToSystem(pathRelative.toString()));
            return new File(
                    FilenameUtils.removeExtension(
                            new File(baseDir, relativeSourceFile.getPath()).getCanonicalPath()) + "Sch.xsd"
            );
        } catch (IOException e) {
            throw new IllegalStateException("I/O error", e);
        }
    }

    public void diff() {
        diff(baseDir);
    }

    private void diff(File xslFile) {
        if (xslFile == null) {
            return;
        }

        if (xslFile.isDirectory()) {
            for (File childSource : xslFile.listFiles()) {
                diff(childSource);
            }
        } else {
            if (!xslFile.getName().endsWith(".xsl")) {
                return;
            }

            String xslFilePath;
            try {
                xslFilePath = xslFile.getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalStateException("I/O error", e);
            }
            File xsdFile = new File(FilenameUtils.removeExtension(xslFilePath) + ".xsd");
            if (!xsdFile.exists()) {
                FileUtils.deleteQuietly(xslFile);
                return;
            }

            File htmlFile = new File(FilenameUtils.removeExtension(xslFilePath) + ".html");
            Transformer transformer = toTransformer(xslFile);
            transform(transformer, xsdFile, htmlFile);
        }
    }

    public void consolidateIssues() {
        File outputFile = new File(baseDir, "issues.txt");
        try {
            try (PrintWriter outWriter = new PrintWriter(
                    new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outputFile))))) {
                consolidateIssues(baseDir, outWriter);
                outWriter.flush();
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Can't open " + outputFile, e);
        }
    }

    private void consolidateIssues(File htmlFile, PrintWriter outWriter) {
        if (htmlFile == null) {
            return;
        }

        if (htmlFile.isDirectory()) {
            for (File childSource : htmlFile.listFiles()) {
                consolidateIssues(childSource, outWriter);
            }
        } else {
            if (!htmlFile.getName().endsWith(".html")) {
                return;
            }

            List<String> issues = new ArrayList();
            try {
                try (BufferedReader bufferedReader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             new BufferedInputStream(new FileInputStream(htmlFile))));) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        int issueIdx = line.indexOf("#Issue:");
                        if (issueIdx != -1) {
                            int endIdx = line.indexOf('<', issueIdx);
                            if (endIdx == -1) {
                                line += " " + bufferedReader.readLine().trim();
                                endIdx = line.indexOf('<', issueIdx);
                            }
                            String text = line.substring(issueIdx, (endIdx != -1) ? endIdx : line.length()).trim();
                            issues.add(text);
                        }
                    }
                }

                HashSet<String> issueSet = new HashSet<>(issues);
                if (!issueSet.isEmpty()) {
                    String moduleName = Utility.extractModuleName(htmlFile.getCanonicalPath()).replace(".html", ".xsd");
                    outWriter.println("[" + moduleName + "]");

                    for (String issue : issueSet) {
                        outWriter.println(issue);
                    }

                    outWriter.println();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Can't open " + htmlFile, e);
            }
        }
    }
}
