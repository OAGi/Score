package org.oagi.srt.web.handler;

import org.oagi.srt.repository.entity.ContextCategory;
import org.oagi.srt.repository.entity.ContextScheme;
import org.oagi.srt.service.ContextCategoryService;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("view")
@ManagedBean
@ViewScoped
public class ContextCategoryHandler extends UIHandler {

    @Autowired
    private ContextCategoryService contextCategoryService;

    private String name;
    private String description;
    private String GUID;
    private long id;

    private List<ContextCategory> contextCategories;
    private ContextCategory selectedCategory;

    private ContextCategory contextCategory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ContextCategory> getContextCategories() {
        contextCategories = contextCategoryService.findAll(Sort.Direction.DESC, "ctxCategoryId");
        return contextCategories;
    }

    public void refreshContextCategories() {
        getContextCategories();
    }

    public void setContextCategories(List<ContextCategory> contextCategories) {
        this.contextCategories = contextCategories;
    }

    public void edit(ActionEvent actionEvent) {
        addMessage("Coming soon!!!");
    }

    public void search() {
        addMessage("Coming soon!!!");
    }

    public void addMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void createContextCategory() {
        contextCategoryService.newContextCategoryBuilder()
                .name(this.name)
                .description(this.description)
                .build();
    }

    public List<String> completeInput(String query) {
        return getContextCategories().stream()
                .filter(contextCategory -> contextCategory.getName().contains(query))
                .map(contextCategory -> contextCategory.getName()).collect(Collectors.toList());
    }

    public List<String> completeDescription(String query) {
        return getContextCategories().stream()
                .filter(contextCategory -> contextCategory.getDescription().contains(query))
                .map(contextCategory -> contextCategory.getDescription()).collect(Collectors.toList());
    }

    public ContextCategory getContextCategory() {
        return contextCategory;
    }

    public void setContextCategory(ContextCategory contextCategory) {
        this.contextCategory = contextCategory;
    }

    public ContextCategory getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(ContextCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public void onEdit(ContextCategory contextCategory) {
        this.id = contextCategory.getCtxCategoryId();
        this.GUID = contextCategory.getGuid();
        this.name = contextCategory.getName();
        this.description = contextCategory.getDescription();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void save() {
        ContextCategory contextCategory = new ContextCategory();
        contextCategory.setName(this.name);
        contextCategory.setDescription(this.description);
        contextCategory.setGuid(this.GUID);
        contextCategory.setCtxCategoryId(this.id);

        contextCategoryService.update(contextCategory);
        refreshContextCategories();
        setSelectedCategory(contextCategory);
    }

    public void cancel() {
        this.selectedCategory = null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void delete(int id) {
        contextCategoryService.deleteById(id);
        refreshContextCategories();
    }

    private String partResult(List<ContextScheme> list) {
        StringBuffer sb = new StringBuffer();
        for (ContextScheme contextScheme : list) {
            sb.append(contextScheme.getSchemeName() + ", ");
        }
        String res = sb.toString();
        return res.substring(0, res.lastIndexOf(","));
    }

    @Transactional(rollbackFor = Throwable.class)
    public void onRowEdit(RowEditEvent event) {
        ContextCategory eventObject = (ContextCategory) event.getObject();

        FacesMessage msg = new FacesMessage("Context Category Edited  " + eventObject.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);

        ContextCategory contextCategory = new ContextCategory();
        contextCategory.setName(eventObject.getName());
        contextCategory.setDescription(eventObject.getDescription());
        contextCategory.setGuid(eventObject.getGuid());
        contextCategory.setCtxCategoryId(eventObject.getCtxCategoryId());
        contextCategoryService.update(contextCategory);
    }

    public void onRowCancel(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("Edit Cancelled", ((ContextCategory) event.getObject()).getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}