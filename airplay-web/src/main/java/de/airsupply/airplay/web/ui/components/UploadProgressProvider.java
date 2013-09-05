package de.airsupply.airplay.web.ui.components;

import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededListener;

public interface UploadProgressProvider extends FailedListener, FinishedListener, ProgressListener, StartedListener,
		SucceededListener {

	void cancel();

	void process(FinishedEvent event);

	void reportProgress(String message, int ticks);

	void setCount(int count);

}