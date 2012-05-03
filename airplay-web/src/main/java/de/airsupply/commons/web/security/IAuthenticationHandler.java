package de.airsupply.commons.web.security;

public interface IAuthenticationHandler {

	public void authenticate(IAuthenticationDelegate delegate, String login, String password) throws Exception;

	public AuthenticatedUser getAuthenticatedUser();

}
