package de.airsupply.airplay.web.ui;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.airsupply.airplay.web.application.model.AirplayDataProvider;

@SuppressWarnings("serial")
public class WorkbenchWindow extends Window {

	static abstract class ContentPanel extends VerticalLayout {

		private final transient AirplayDataProvider dataProvider;

		public ContentPanel(AirplayDataProvider dataProvider) {
			super();
			this.dataProvider = dataProvider;
			setMargin(true);
			setSpacing(true);
			setSizeFull();
			init();
		}

		public final AirplayDataProvider getDataProvider() {
			return dataProvider;
		}

		protected abstract void init();

	}

	private static class MainPanel extends VerticalLayout {

		public MainPanel(AirplayDataProvider dataProvider) {
			super();
			setSizeFull();
			setSpacing(true);

			final Label label = new Label("Airplay Manager");

			final Panel panel = new Panel();
			panel.addComponent(label);

			final TabSheet tabSheet = new TabSheet();
			tabSheet.setSizeFull();
			tabSheet.addTab(new SongPanel(dataProvider), "Song Database");
			tabSheet.addTab(new ChartPanel(dataProvider), "Chart Database");

			addComponent(panel);
			addComponent(tabSheet);
		}

	}

	public WorkbenchWindow(AirplayDataProvider dataProvider) {
		super("Airplay Manager");
		setSizeFull();
		addComponent(new MainPanel(dataProvider));
	}

}
