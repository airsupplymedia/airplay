package de.airsupply.airplay.web.ui.component;

import org.springframework.util.Assert;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class UploadComponent extends VerticalLayout implements UploadProgressProvider {

	public static abstract class UploadContext implements Receiver {

		private UploadProgressProvider progressProvider;

		public final Upload.FailedListener getFailedListener() {
			return new Upload.FailedListener() {

				@Override
				public void uploadFailed(FailedEvent event) {
					process(event);
				}

			};
		}

		public final Upload.FinishedListener getFinishedListener() {
			return new Upload.FinishedListener() {

				@Override
				public void uploadFinished(FinishedEvent event) {
					process(event);
				}

			};
		}

		public final Upload.ProgressListener getProgressListener() {
			return new Upload.ProgressListener() {

				@Override
				public void updateProgress(long readBytes, long contentLength) {
					process(readBytes, contentLength);
				}

			};
		}

		protected UploadProgressProvider getProgressProvider() {
			return progressProvider;
		}

		public final Upload.StartedListener getStartedListener() {
			return new Upload.StartedListener() {

				@Override
				public void uploadStarted(StartedEvent event) {
					process(event);
				}

			};
		}

		public final Upload.SucceededListener getSucceededListener() {
			return new Upload.SucceededListener() {

				@Override
				public void uploadSucceeded(SucceededEvent event) {
					process(event);
				}

			};
		}

		protected void process(FailedEvent event) {
			if (progressProvider != null) {
				progressProvider.process(event, "Canceled upload of: " + event.getFilename());
			}
		}

		protected void process(FinishedEvent event) {
			if (progressProvider != null) {
				progressProvider.process(event, "Uploaded: " + event.getFilename());
			}
		}

		protected void process(long readBytes, long contentLength) {
			if (progressProvider != null) {
				progressProvider.process("Uploaded " + readBytes + " bytes of " + contentLength, readBytes,
						contentLength);
			}
		}

		protected void process(StartedEvent event) {
			if (progressProvider != null) {
				progressProvider.process(event, "Uploading: " + event.getFilename());
			}
		}

		protected void process(SucceededEvent event) {
			if (progressProvider != null) {
				progressProvider.process(event, "Uploaded: " + event.getFilename());
			}
		}

		private void setProgressProvider(UploadProgressProvider progressProvider) {
			this.progressProvider = progressProvider;
		}

	}

	private Button cancelProcessing;

	private final UploadContext context;

	private ProgressIndicator progressIndicator;

	private Label state;

	private Panel statusPanel;

	private Label textualProgress;

	private Upload upload;

	public UploadComponent(UploadContext context) {
		super();
		Assert.notNull(context);
		this.context = context;
		context.setProgressProvider(this);
		setSpacing(true);
		init();
	}

	public void cancel() {
		Assert.notNull(upload);
		upload.interruptUpload();
	}

	protected void init() {
		statusPanel = new Panel();
		statusPanel.setVisible(false);

		FormLayout formLayout = new FormLayout();
		formLayout.setMargin(true);

		HorizontalLayout stateLayout = new HorizontalLayout();
		stateLayout.setSpacing(true);

		upload = new Upload(null, context);
		upload.setImmediate(true);
		upload.setButtonCaption("Upload File");

		cancelProcessing = new Button("Cancel");
		cancelProcessing.setVisible(false);
		cancelProcessing.setStyleName("small");
		cancelProcessing.addListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				upload.interruptUpload();
			}

		});

		state = new Label("Idle");

		progressIndicator = new ProgressIndicator();
		progressIndicator.setVisible(false);

		textualProgress = new Label();
		textualProgress.setVisible(false);

		if (context.getStartedListener() != null) {
			upload.addListener(context.getStartedListener());
		}
		if (context.getProgressListener() != null) {
			upload.addListener(context.getProgressListener());
		}
		if (context.getSucceededListener() != null) {
			upload.addListener(context.getSucceededListener());
		}
		if (context.getFailedListener() != null) {
			upload.addListener(context.getFailedListener());
		}
		if (context.getFinishedListener() != null) {
			upload.addListener(context.getFinishedListener());
		}

		stateLayout.addComponent(state);
		stateLayout.addComponent(cancelProcessing);
		formLayout.addComponent(stateLayout);
		formLayout.addComponent(progressIndicator);
		formLayout.addComponent(textualProgress);

		statusPanel.setContent(formLayout);

		addComponent(upload);
		addComponent(statusPanel);
	}

	@Override
	public void process(FailedEvent event, String message) {
		state.setValue(message);
	}

	@Override
	public void process(FinishedEvent event, String message) {
		progressIndicator.setVisible(false);
		textualProgress.setVisible(false);
		cancelProcessing.setVisible(false);
	}

	@Override
	public void process(StartedEvent event, String message) {
		statusPanel.setVisible(true);
		state.setValue(message);
		progressIndicator.setValue(0f);
		progressIndicator.setVisible(true);
		progressIndicator.setPollingInterval(500);
		textualProgress.setVisible(true);
		cancelProcessing.setVisible(true);
	}

	@Override
	public void process(String message, long readBytes, long contentLength) {
		progressIndicator.setValue(new Float(readBytes / (float) contentLength));
		textualProgress.setValue(message);
	}

	@Override
	public void process(SucceededEvent event, String message) {
		state.setValue(message);
	}

}