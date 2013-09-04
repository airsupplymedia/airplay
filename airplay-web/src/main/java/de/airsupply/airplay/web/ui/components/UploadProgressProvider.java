package de.airsupply.airplay.web.ui.components;

import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;

public interface UploadProgressProvider {

	public void cancel();

	public void process(FailedEvent event, String message);

	public void process(FinishedEvent event, String message);

	public void process(StartedEvent event, String message);

	public void process(String message, int ticks);

	public void process(String message, long readBytes, long contentLength);

	public void process(SucceededEvent event, String message);

	public void reset(int count);

}