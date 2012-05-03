package de.airsupply.commons.web.security;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AuthenticatedUser implements Serializable {

	private final String login;

	private final String password;

	public AuthenticatedUser(final String login, final String password) {
		this.login = login;
		this.password = password;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

}
