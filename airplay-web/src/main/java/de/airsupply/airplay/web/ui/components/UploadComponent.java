package de.airsupply.airplay.web.ui.components;

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

import de.airsupply.airplay.web.ui.components.UploadComponent.UploadContext;

@SuppressWarnings("serial")
public class UploadComponent<C extends UploadContext> extends VerticalLayout implements UploadProgressProvider {

	public abstract static class UploadContext implements Receiver {

		private UploadProgressProvider progressProvider;

		public UploadProgressProvider getProgressProvider() {
			return progressProvider;
		}

		public void setProgressProvider(UploadProgressProvider progressProvider) {
			this.progressProvider = progressProvider;
		}

	}

	private Button cancelProcessing;

	private int count;

	private ProgressBar progressBar;

	private Label state;

	private Panel statusPanel;

	private Label textualProgress;

	private Upload upload;

	private C uploadContext;

	public UploadComponent(C uploadContext) {
		super();
		Assert.notNull(uploadContext);
		uploadContext.setProgressProvider(this);
		this.uploadContext = uploadContext;
		setSpacing(true);
		init();
	}

	public void cancel() {
		Assert.notNull(upload);
		upload.interruptUpload();
	}

	private void finishUpload(FinishedEvent event) {
		progressBar.setVisible(false);
		textualProgress.setVisible(false);
		cancelProcessing.setVisible(false);
	}

	public C getUploadContext() {
		return uploadContext;
	}

	protected void init() {
		statusPanel = new Panel();
		statusPanel.setVisible(false);

		FormLayout formLayout = new FormLayout();
		formLayout.setMargin(true);

		HorizontalLayout stateLayout = new HorizontalLayout();
		stateLayout.setSpacing(true);

		upload = new Upload("Upload File", uploadContext);
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

		upload.addStartedListener(this);
		upload.addProgressListener(this);
		upload.addSucceededListener(this);
		upload.addFailedListener(this);
		upload.addFinishedListener(this);

		stateLayout.addComponent(state);
		stateLayout.addComponent(cancelProcessing);
		formLayout.addComponent(stateLayout);
		formLayout.addComponent(progressBar);
		formLayout.addComponent(textualProgress);

		statusPanel.setContent(formLayout);

		addComponent(upload);
		addComponent(statusPanel);
	}

	protected boolean isPostProcessing() {
		return false;
	}

	@Override
	public void process(FinishedEvent event) {
	}

	@Override
	public void reportProgress(String message, int ticks) {
		float newValue = (float) (progressBar.getValue().intValue() + ticks);
		progressBar.setValue(Float.valueOf(count / newValue));
		textualProgress.setValue(message);
	}

	@Override
	public void setCount(int count) {
		this.count = count;
		progressBar.setValue(Float.valueOf(0f));
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		upload.setEnabled(enabled);
	}

	private void startUpload(StartedEvent event) {
		statusPanel.setVisible(true);
		state.setValue("Uploading: " + event.getFilename());
		progressBar.setValue(Float.valueOf(0f));
		progressBar.setVisible(true);
		textualProgress.setVisible(true);
		cancelProcessing.setVisible(true);
	}

	@Override
	public void updateProgress(long readBytes, long contentLength) {
		progressBar.setValue(Float.valueOf(readBytes / (float) contentLength));
		textualProgress.setValue("Uploaded " + readBytes + " bytes of " + contentLength);
	}

	@Override
	public void uploadFailed(FailedEvent event) {
		state.setValue("Canceled upload of: " + event.getFilename());
	}

	@Override
	public void uploadFinished(FinishedEvent event) {
		if (isPostProcessing()) {
			process(event);
		}
		finishUpload(event);
	}

	@Override
	public void uploadStarted(StartedEvent event) {
		startUpload(event);
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		state.setValue("Uploaded: " + event.getFilename());
	}

}