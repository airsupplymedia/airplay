package de.airsupply.airplay.web.ui.components;

import javax.annotation.PostConstruct;

import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public abstract class ContentPanel extends VerticalLayout {

	public ContentPanel() {
		super();
		setMargin(true);
		setSpacing(true);
		setSizeFull();
	}

	@PostConstruct
	protected abstract void init();

}