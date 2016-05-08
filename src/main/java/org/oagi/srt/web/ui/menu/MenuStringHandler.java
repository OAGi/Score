package org.oagi.srt.web.ui.menu;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

/**
 *
 * @author Yunsu Lee
 * @version 1.0
 *
 */

@ManagedBean(name="termType")
@SessionScoped
public class MenuStringHandler implements Serializable{

	private static final long serialVersionUID = -8301728159662247307L;

	private String localeCode;

	private static Map<String, Object> userTypes;
	
	static {
		userTypes = new LinkedHashMap<String,Object>();
		userTypes.put("User-EN", "user_en"); 
		userTypes.put("Developer-EN", "dev_en"); 
		userTypes.put("User-KR", "user_kr");
		userTypes.put("Developer-KR", "dev_kr");
	}

	public Map<String, Object> getUserTypesInMap() {
		return userTypes;
	}

	public String getLocaleCode() {
		return localeCode;
	}

	public void setLocaleCode(String localeCode) {
		this.localeCode = localeCode;
	}

	public void countryUserTypeCodeChanged(ValueChangeEvent e){

		String newLocaleValue = e.getNewValue().toString();

		for (Map.Entry<String, Object> entry : userTypes.entrySet()) {
			if(entry.getValue().toString().equals(newLocaleValue)){
				String value = (String)entry.getValue();
				Locale currentLocale = new Locale(value.substring(0, value.indexOf('_')), value.substring(value.indexOf('_') + 1));
				FacesContext.getCurrentInstance()
				.getViewRoot().setLocale(currentLocale);
			}
		}
	}
}