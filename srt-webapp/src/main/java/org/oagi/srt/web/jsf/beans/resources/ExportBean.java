package org.oagi.srt.web.jsf.beans.resources;

import org.oagi.srt.service.ExportService;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
@Transactional(readOnly = true)
public class ExportBean {

    @Autowired
    private ExportService exportService;
    private File exportedFile;

    public void export() throws Exception {
        exportedFile = exportService.export();
    }

    public StreamedContent getExportedFile() throws Exception {
        InputStream stream = new FileInputStream(exportedFile);
        String fileName = exportedFile.getName();
        return new DefaultStreamedContent(stream, "application/zip", fileName);
    }
}
