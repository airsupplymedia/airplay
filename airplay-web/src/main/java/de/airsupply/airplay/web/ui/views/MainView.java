package de.airsupply.airplay.web.ui.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

import de.airsupply.airplay.web.ui.components.ContentPanel;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@VaadinView(MainView.NAME)
@SuppressWarnings("serial")
public class MainView extends ContentPanel implements View {

	public static final String NAME = "";

	@Override
	public void enter(ViewChangeEvent event) {
	}

	@PostConstruct
	public void init() {
	}

}