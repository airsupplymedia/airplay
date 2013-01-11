package de.airsupply.airplay.web.ui.panel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window.Notification;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.services.ImportService;
import de.airsupply.airplay.web.application.model.Containers.RecordImportContainer;
import de.airsupply.airplay.web.ui.component.ChartSelectorComponent;
import de.airsupply.airplay.web.ui.component.UploadComponent;
import de.airsupply.airplay.web.ui.component.UploadComponent.UploadContext;
import de.airsupply.airplay.web.ui.panel.WorkbenchWindow.ContentPanel;
import de.airsupply.airplay.web.ui.util.WeekOfYearColumnGenerator;
import de.airsupply.commons.core.context.Loggable;

@Component
@SuppressWarnings("serial")
public class RecordImportPanel extends ContentPanel {

	@Configurable
	private final class RecordImportUploadContext extends UploadContext {

		private transient File file;

		@Autowired
		private transient ImportService importService;

		@Loggable
		private Logger logger;

		@Override
		protected void process(SucceededEvent event) {
			super.process(event);

			if (file != null) {
				try {
					Chart chart = chartSelectorComponent.getSelectedChart();
					Date date = chartSelectorComponent.getSelectedDate();
					importService.importRecords(chart, date, new FileInputStream(file));
				} catch (Exception exception) {
					logger.error("Error during migration", exception);

					getWindow().showNotification("Migration was not successful",
							"Please consult your system administrator for further details",
							Notification.TYPE_ERROR_MESSAGE);

					throw new RuntimeException(exception);
				}
			}
		}

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			try {
				file = File.createTempFile("ri_" + filename, "am");
				file.deleteOnExit();
				return new FileOutputStream(file) {

					public void close() throws IOException {
						super.close();
						// file.delete();
					};

				};
			} catch (IOException exception) {
				logger.error("Error during upload", exception);

				getProgressProvider().cancel();
				getWindow()
						.showNotification("Upload was not successful",
								"Please consult your system administrator for further details",
								Notification.TYPE_ERROR_MESSAGE);

				throw new RuntimeException(exception);
			}
		}
	}

	@Autowired
	private ChartSelectorComponent chartSelectorComponent;

	@Autowired
	private RecordImportContainer recordImportContainer;

	@Override
	@PostConstruct
	protected void init() {
		UploadComponent uploadComponent = new UploadComponent(new RecordImportUploadContext());

		Panel importPanel = new Panel();

		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setValue(0f);
		progressIndicator.setPollingInterval(500);

		final String[] propertyIds = new String[] { "weekDate" };
		final String[] columnHeaders = new String[] { "Week Of Year" };
		final boolean[] sortDirections = new boolean[propertyIds.length];
		Arrays.fill(sortDirections, true);

		final Action deleteAction = new Action("Delete");
		final Action reportAction = new Action("Open Report");

		final Table table = new Table("Record Imports");
		table.setSizeFull();
		table.setContainerDataSource(recordImportContainer);
		table.setVisibleColumns(propertyIds);
		table.setColumnHeaders(columnHeaders);
		table.setSelectable(true);
		table.setImmediate(true);
		table.addGeneratedColumn("weekDate", new WeekOfYearColumnGenerator());
		table.addActionHandler(new Handler() {

			@Override
			public Action[] getActions(Object target, Object sender) {
				return new Action[] { reportAction, deleteAction };
			}

			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (deleteAction == action) {
					// FIXME Implement Record Import deletion
					getWindow().showNotification("Not implemented!");
				}
			}

		});

		recordImportContainer.update();

		importPanel.addComponent(progressIndicator);
		addComponent(chartSelectorComponent);
		addComponent(uploadComponent);
		addComponent(importPanel);
		addComponent(table);
	}
}
