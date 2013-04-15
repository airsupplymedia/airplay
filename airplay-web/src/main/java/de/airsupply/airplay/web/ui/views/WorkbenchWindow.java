package de.airsupply.airplay.web.ui.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Component
@SuppressWarnings("serial")
public class WorkbenchWindow extends Window {

	@Component
	static class MainPanel extends VerticalLayout {

		@Autowired
		private ChartView chartPanel;

		@Autowired
		private RecordImportView recordImportPanel;

		@Autowired
		private SongView songPanel;

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
		setContent(mainPanel);
	}

}