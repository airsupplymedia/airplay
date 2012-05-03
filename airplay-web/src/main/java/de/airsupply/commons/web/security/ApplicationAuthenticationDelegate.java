package de.airsupply.commons.web.security;

import org.springframework.util.Assert;

import com.vaadin.Application;

public class ApplicationAuthenticationDelegate implements IAuthenticationDelegate {

	public static ApplicationAuthenticationDelegate create(Application application) {
		return new ApplicationAuthenticationDelegate(application);
	}

	private final Application application;

	public ApplicationAuthenticationDelegate(Application application) {
		Assert.notNull(application);
		this.application = application;
	}

	@Override
	public void setUser(AuthenticatedUser authenticatedUser) {
		application.setUser(authenticatedUser);
	}

}
