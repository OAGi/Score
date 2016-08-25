package org.oagi.srt.web.contents;

import org.oagi.srt.web.handler.ContextCategoryHandler;
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
	private List<ContextCategoryHandler> details;
	
	
	@PostConstruct
	public void init() {
		//details =  
	}
	
	public List<ContextCategoryHandler> getDetails() {
		return details;
	}
	

}
