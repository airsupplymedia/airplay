package de.airsupply.commons.web.security;

import java.io.Serializable;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SuppressWarnings("serial")
public class SpringSecurityAuthenticationHandler implements IAuthenticationHandler, Serializable {

	private transient AuthenticationManager authenticationManager;

	private transient CertificateVerifier certificateVerifier;

	private boolean trustCertificates = false;

	private AuthenticatedUser user;

	@Override
	public void authenticate(IAuthenticationDelegate delegate, String login, String password) throws Exception {
		Authentication token = new UsernamePasswordAuthenticationToken(login, password);

		if (trustCertificates && certificateVerifier != null && !certificateVerifier.isTrusted()) {
			certificateVerifier.trustCertificate();
		}

		Authentication authentication = authenticationManager.authenticate(token);

		if (authentication.isAuthenticated()) {
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

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setCertificateVerifier(CertificateVerifier certificateVerifier) {
		this.certificateVerifier = certificateVerifier;
	}

	public void setTrustCertificates(boolean trustCertificates) {
		this.trustCertificates = trustCertificates;
	}

}
