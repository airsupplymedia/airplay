package de.airsupply.airplay.web.ui.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.util.RecordImportProgressProvider;
import de.airsupply.airplay.core.services.ImportService;
import de.airsupply.airplay.web.ui.components.ChartSelectorComponent;
import de.airsupply.airplay.web.ui.components.ChartSelectorComponent.ChartSelectorListener;
import de.airsupply.airplay.web.ui.components.ContentPanel;
import de.airsupply.airplay.web.ui.components.UploadComponent;
import de.airsupply.airplay.web.ui.components.UploadComponent.UploadContext;
import de.airsupply.airplay.web.ui.model.Containers.RecordImportCategoryContainer;
import de.airsupply.airplay.web.ui.model.Containers.RecordImportContainer;
import de.airsupply.airplay.web.ui.util.WeekOfYearColumnGenerator;
import de.airsupply.commons.core.context.Loggable;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@VaadinView(RecordImportView.NAME)
@SuppressWarnings("serial")
public class RecordImportView extends ContentPanel implements View {

	private final class RecordImportActionHandler implements Handler {

		private final Action deleteAction;

		private final Action reportAction;

		private RecordImportActionHandler() {
			this.deleteAction = new Action("Delete");
			this.reportAction = new Action("Open Report");
		}

		@Override
		public Action[] getActions(Object target, Object sender) {
			return new Action[] { reportAction, deleteAction };
		}

		@Override
		public void handleAction(Action action, Object sender, Object target) {
			if (deleteAction == action) {
				Tree tree = new Tree();
				tree.setContainerDataSource(recordImportCategoryContainer);
				tree.setSizeFull();
				RecordImport recordImport = recordImportContainer.getItem(target).getBean();
				recordImportCategoryContainer.update(recordImport);

				VerticalLayout layout = new VerticalLayout();
				layout.addComponent(tree);

				Window window = new Window();
				window.setHeight("50%");
				window.setWidth("50%");
				window.setContent(layout);

				getUI().addWindow(window);
			}
		}
	}

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
			final UploadContext uploadContext = this;
			if (file != null) {
				new Thread() {

					public void run() {
						try {
							Chart chart = chartSelectorComponent.getSelectedChart();
							Date date = chartSelectorComponent.getSelectedDate();
							importService.importRecords(chart, date, new FileInputStream(file),
									new RecordImportProgressProvider() {

										@Override
										public void imported(ChartPosition chartPosition) {
											uploadContext.process("Imported Record: " + getCurrentIndex() + " of "
													+ getNumberOfRecords(), getCurrentIndex());
										}

										@Override
										protected void numberOfRecordsChanged(int numberOfRecords) {
											uploadContext.reset(numberOfRecords);
										}

										@Override
										protected void indexChanged(int currentIndex) {

										}

									});
						} catch (Exception exception) {
							logger.error("Error during migration", exception);
							Notification.show("Migration was not successful", exception.getMessage(),
									Type.ERROR_MESSAGE);
							throw new RuntimeException(exception);
						}
					};

				}.start();

			}
		}

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			try {
				file = File.createTempFile("ri_" + filename, "asm");
				file.deleteOnExit();
				return FileUtils.openOutputStream(file);
			} catch (IOException exception) {
				logger.error("Error during upload", exception);
				getProgressProvider().cancel();
				Notification.show("Upload was not successful",
						"Please consult your system administrator for further details", Type.ERROR_MESSAGE);
				throw new RuntimeException(exception);
			}
		}

	}

	public static final String NAME = "recordImportView";

	@Autowired
	private ChartSelectorComponent chartSelectorComponent;

	@Autowired
	private RecordImportCategoryContainer recordImportCategoryContainer;

	@Autowired
	private RecordImportContainer recordImportContainer;

	@Override
	public void enter(ViewChangeEvent event) {
	}

	@Override
	@PostConstruct
	protected void init() {
		final UploadComponent uploadComponent = new UploadComponent(new RecordImportUploadContext());
		uploadComponent.setEnabled(false);

		final Table table = new Table("Record Imports");
		table.setSizeFull();
		table.setContainerDataSource(recordImportContainer);
		table.setVisibleColumns(recordImportContainer.getPropertyIds());
		table.setColumnHeaders(recordImportContainer.getColumnHeaders());
		table.setSelectable(true);
		table.setImmediate(true);
		table.addGeneratedColumn("weekDate", new WeekOfYearColumnGenerator());
		table.addActionHandler(new RecordImportActionHandler());
		recordImportContainer.update();
		chartSelectorComponent.addListener(new ChartSelectorListener() {

			@Override
			public void valueChange(Chart selectedChart, Date selectedDate) {
				uploadComponent.setEnabled(true);
			}

		});

		VerticalLayout importLayout = new VerticalLayout();
		addComponent(chartSelectorComponent);
		addComponent(uploadComponent);
		addComponent(importLayout);
		addComponent(table);
	}
}
