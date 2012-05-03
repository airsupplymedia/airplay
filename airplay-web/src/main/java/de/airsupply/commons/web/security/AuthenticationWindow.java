package de.airsupply.commons.web.security;

import java.io.Serializable;

import org.springframework.util.Assert;

import com.vaadin.Application;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AuthenticationWindow extends Window {

	private final class AuthenticationListener implements LoginForm.LoginListener, Serializable {

		@Override
		public void onLogin(LoginEvent event) {
			try {
				Application application = getApplication();
				authenticationHandler.authenticate(ApplicationAuthenticationDelegate.create(application),
						event.getLoginParameter("username"), event.getLoginParameter("password"));

				close();
			} catch (Exception exception) {
				exception.printStackTrace();
				showNotification(exception.toString());
			}
		}
	}

	private final transient IAuthenticationHandler authenticationHandler;

	public AuthenticationWindow(IAuthenticationHandler authenticationHandler) {
		super("Authentication");
		Assert.notNull(authenticationHandler);
		this.authenticationHandler = authenticationHandler;
		init();
	}

	private void init() {
		setClosable(false);
		setDraggable(false);
		setModal(true);
		setWidth(40, UNITS_PERCENTAGE);
		setHeight(300, UNITS_PIXELS);

		LoginForm login = new LoginForm();
		login.setCaption("Please identify yourself with your GENO- / RACF credentials...");
		login.setUsernameCaption("User Id");
		login.setPasswordCaption("Password");
		login.setWidth(100, UNITS_PERCENTAGE);
		login.addListener(new AuthenticationListener());

		addComponent(login);
	}

}
