package de.airsupply.commons.web.security;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

@SuppressWarnings("serial")
public abstract class AuthenticationAwareApplication extends Application implements
		ApplicationContext.TransactionListener {

	private final static class AuthenticationWindowCloseListener implements CloseListener {

		private final AuthenticationWindow authenticationWindow;

		private final Window mainWindow;

		public AuthenticationWindowCloseListener(Window mainWindow, AuthenticationWindow authenticationWindow) {
			this.mainWindow = mainWindow;
			this.authenticationWindow = authenticationWindow;
		}

		@Override
		public void windowClose(CloseEvent event) {
			mainWindow.removeWindow(authenticationWindow);
		}

	}

	private IAuthenticationHandler authenticationHandler;

	private AuthenticationWindow authenticationWindow;

	private boolean clearSessionAfterLogout = false;

	private void authenticate() {
		AuthenticationWindow authenticationWindow = getAuthenticationWindow();
		if (this.getUser() == null && !getMainWindow().getChildWindows().contains(authenticationWindow)) {
			authenticationWindow.addListener(new AuthenticationWindowCloseListener(getMainWindow(),
					authenticationWindow));
			getMainWindow().addWindow(authenticationWindow);
		}
	}

	@Override
	public final void close() {
		logout();
		super.close();
		onClose();
	}

	public final AuthenticatedUser getAuthenticatedUser() {
		return (AuthenticatedUser) getUser();
	}

	public AuthenticationWindow getAuthenticationWindow() {
		if (authenticationWindow == null) {
			authenticationWindow = new AuthenticationWindow(authenticationHandler);
		}
		return authenticationWindow;
	}

	@Override
	public final void init() {
		onInit();
		getContext().addTransactionListener(this);
	}

	public final void logout() {
		setUser(null);
		if (clearSessionAfterLogout) {
			((WebApplicationContext) getContext()).getHttpSession().invalidate();
		}
	}

	protected void onClose() {
	}

	protected abstract void onInit();

	public void setAuthenticationHandler(IAuthenticationHandler authenticationHandler) {
		this.authenticationHandler = authenticationHandler;
	}

	public void setClearSessionAfterLogout(boolean clearSessionAfterLogout) {
		this.clearSessionAfterLogout = clearSessionAfterLogout;
	}

	@Override
	public void transactionEnd(Application application, Object transactionData) {
	}

	@Override
	public final void transactionStart(Application application, Object object) {
		authenticate();
	}

}
