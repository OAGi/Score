package org.oagi.srt.web.handler;

import org.oagi.srt.repository.UserRepository;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UIHandler {

	@Autowired
	private UserRepository userRepository;
	protected int userId;

	@PostConstruct
	public void init() {
		userId = userRepository.findOneByLoginId("oagis").getAppUserId();
	}

	public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this);
    }
}
