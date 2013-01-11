package de.airsupply.airplay.web.ui.panel;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Component
@SuppressWarnings("serial")
public class WorkbenchWindow extends Window {

	static abstract class ContentPanel extends VerticalLayout {

		public ContentPanel() {
			super();
			setMargin(true);
			setSpacing(true);
			setSizeFull();
		}

		@PostConstruct
		protected abstract void init();

	}

	@Component
	static class MainPanel extends VerticalLayout {

		@Autowired
		private ChartPanel chartPanel;

		@Autowired
		private RecordImportPanel recordImportPanel;

		@Autowired
		private SongPanel songPanel;

		public MainPanel() {
			super();
			setSizeFull();
			setSpacing(true);
		}

		@PostConstruct
		public void init() {
			final TabSheet tabSheet = new TabSheet();
			tabSheet.setSizeFull();
			tabSheet.addTab(songPanel, "Song Database");
			tabSheet.addTab(chartPanel, "Chart Database");
			tabSheet.addTab(recordImportPanel, "Record Imports");

			addComponent(tabSheet);
		}

	}

	@Autowired
	private MainPanel mainPanel;

	public WorkbenchWindow() {
		super("Airplay Manager");
		setSizeFull();
	}

	@PostConstruct
	public void init() {
		addComponent(mainPanel);
	}

}