package de.airsupply.airplay.core.model.util;

import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.SongBroadcast;

public abstract class RecordImportProgressProvider {

	private int currentIndex;

	private int numberOfRecords;

	public int getCurrentIndex() {
		return currentIndex;
	}

	public int getNumberOfRecords() {
		return numberOfRecords;
	}

	public void imported(ChartPosition chartPosition) {
	}

	public void imported(RecordImport recordImport) {
	}

	public void imported(SongBroadcast songBroadcast) {
	}

	public final void incrementIndex() {
		currentIndex++;
		indexChanged(currentIndex);
	}

	protected abstract void indexChanged(int currentIndex);

	protected void numberOfRecordsChanged(int numberOfRecords) {
	}

	public void reset() {
		currentIndex = 0;
		numberOfRecords = 0;
	}

	public final void setNumberOfRecords(int numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
		numberOfRecordsChanged(numberOfRecords);
	}

}
