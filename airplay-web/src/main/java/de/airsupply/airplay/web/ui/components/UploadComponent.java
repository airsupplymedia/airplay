package de.airsupply.airplay.web.ui.components;

import java.io.OutputStream;

import org.springframework.util.Assert;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class UploadComponent extends VerticalLayout implements Receiver, UploadProgressProvider {

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

		public void process(String message, int ticks) {
			if (progressProvider != null) {
				progressProvider.process(message, ticks);
			}
		}

		protected void process(SucceededEvent event) {
			if (progressProvider != null) {
				progressProvider.process(event, "Uploaded: " + event.getFilename());
			}
		}

		public void reset(int count) {
			if (progressProvider != null) {
				progressProvider.reset(count);
			}
		}

		private void setProgressProvider(UploadProgressProvider progressProvider) {
			this.progressProvider = progressProvider;
		}

	}

	private Button cancelProcessing;

	private final UploadContext context;

	private int count;

	private ProgressBar progressBar;

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

		upload = new Upload("Upload File", context);
		upload.setImmediate(true);
		upload.setEnabled(true);

		cancelProcessing = new Button("Cancel");
		cancelProcessing.setVisible(false);
		cancelProcessing.setStyleName("small");
		cancelProcessing.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				upload.interruptUpload();
			}

		});

		state = new Label("Idle");

		progressBar = new ProgressBar();
		progressBar.setVisible(false);

		textualProgress = new Label();
		textualProgress.setVisible(false);

		upload.addStartedListener(context.getStartedListener());
		upload.addProgressListener(context.getProgressListener());
		upload.addSucceededListener(context.getSucceededListener());
		upload.addFailedListener(context.getFailedListener());
		upload.addFinishedListener(context.getFinishedListener());

		stateLayout.addComponent(state);
		stateLayout.addComponent(cancelProcessing);
		formLayout.addComponent(stateLayout);
		formLayout.addComponent(progressBar);
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
		progressBar.setVisible(false);
		textualProgress.setVisible(false);
		cancelProcessing.setVisible(false);
	}

	@Override
	public void process(StartedEvent event, String message) {
		statusPanel.setVisible(true);
		state.setValue(message);
		progressBar.setValue(Float.valueOf(0f));
		progressBar.setVisible(true);
		textualProgress.setVisible(true);
		cancelProcessing.setVisible(true);
	}

	@Override
	public void process(String message, int ticks) {
		float newValue = (float) (progressBar.getValue().intValue() + ticks);
		progressBar.setValue(Float.valueOf(count / newValue));
		textualProgress.setValue(message);
	}

	@Override
	public void process(String message, long readBytes, long contentLength) {
		progressBar.setValue(Float.valueOf(readBytes / (float) contentLength));
		textualProgress.setValue(message);
	}

	@Override
	public void process(SucceededEvent event, String message) {
		state.setValue(message);
	}

	@Override
	public void reset(int count) {
		this.count = count;
		progressBar.setValue(Float.valueOf(0f));
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		upload.setEnabled(enabled);
	}

	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		return null;
	}

}