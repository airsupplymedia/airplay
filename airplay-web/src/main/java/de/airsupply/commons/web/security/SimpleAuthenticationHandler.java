package de.airsupply.commons.web.security;

import java.util.HashMap;
import java.util.Map;

public class SimpleAuthenticationHandler implements IAuthenticationHandler {

	private Map<String, String> credentialMap = new HashMap<>();

	private AuthenticatedUser user;

	@Override
	public void authenticate(IAuthenticationDelegate delegate, String login, String password) throws Exception {
		if (delegate != null && isValid(login, password)) {
			user = new AuthenticatedUser(login, password);
			delegate.setUser(user);
		} else {
			throw new Exception("Login failed!");
		}
	}

	@Override
	public AuthenticatedUser getAuthenticatedUser() {
		return user;
	}

	private boolean isValid(String login, String password) {
		String result = credentialMap.get(login);
		return result != null && result.equals(password);
	}

	public void put(String login, String password) {
		credentialMap.put(login, password);
	}

}
