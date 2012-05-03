package de.airsupply.commons.web.application;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;

@SuppressWarnings("serial")
public class SpringAwareApplicationServlet extends ApplicationServlet {

	private ApplicationContext applicationContext;

	@Bean(destroyMethod = "close")
	@Scope(value = WebApplicationContext.SCOPE_SESSION)
	@Override
	protected Application getNewApplication(HttpServletRequest request) throws ServletException {
		try {
			if (applicationContext == null) {
				applicationContext = WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
			}
			return applicationContext.getBean(getApplicationClass());
		} catch (Exception exception) {
			log(exception.getMessage(), exception);
			return super.getNewApplication(request);
		}
	}

}
