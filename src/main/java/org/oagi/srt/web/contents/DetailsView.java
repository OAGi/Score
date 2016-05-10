package org.oagi.srt.web.contents;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import org.oagi.srt.web.handler.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

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
