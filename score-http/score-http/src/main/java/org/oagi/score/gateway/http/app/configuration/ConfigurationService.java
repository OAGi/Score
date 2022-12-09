package org.oagi.score.gateway.http.app.configuration;

import org.oagi.score.repo.component.app.configuration.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfigurationService {
	
	@Autowired
	private ConfigurationRepository configRepo;
    
    private static final String TENANT_CONFIG_PARAM_NAME = "isTenant";
	
	public String getConfigurationValueByName(String paramConfigName) {
		return configRepo.getConfigurationValueByName(paramConfigName);
	}
	
	public boolean isTenantInstance() {
	    	Boolean isTenant = Boolean.valueOf(configRepo.getConfigurationValueByName(TENANT_CONFIG_PARAM_NAME));
	    	return isTenant != null ? isTenant : false;
    }

}
