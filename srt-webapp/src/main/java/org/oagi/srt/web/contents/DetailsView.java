package org.oagi.srt.web.contents;

import org.oagi.srt.web.jsf.beans.context.category.ContextCategoryBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

@Controller()
@Scope("view")
@ManagedBean(name = "detailsView")
@ViewScoped
public class DetailsView implements Serializable{
	private List<ContextCategoryBean> details;
	
	
	@PostConstruct
	public void init() {
		//details =  
	}
	
	public List<ContextCategoryBean> getDetails() {
		return details;
	}
	

}
