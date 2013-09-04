package de.airsupply.airplay.core.model.util;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.commons.core.context.Loggable;

@Component
public class LoggingRecordImportProgressProvider extends RecordImportProgressProvider {

	@Loggable
	private Logger logger;

	private boolean shouldLogChartPositionImport = true;

	private boolean shouldLogProgress = true;

	private boolean shouldLogRecordImportImport = true;

	private boolean shouldLogSongBroadcastImport = false;

	@Override
	public void imported(ChartPosition chartPosition) {
		if (shouldLogChartPositionImport) {
			logger.info("Imported: " + chartPosition);
		}
	}

	@Override
	public void imported(RecordImport recordImport) {
		if (shouldLogRecordImportImport) {
			logger.info("Imported: " + recordImport);
		}
	}

	@Override
	public void imported(SongBroadcast songBroadcast) {
		if (shouldLogSongBroadcastImport) {
			logger.info("Imported: " + songBroadcast);
		}
	}

	@Override
	protected void indexChanged(int currentIndex) {
		if (shouldLogProgress) {
			logger.info("Processed: " + currentIndex + "/" + getNumberOfRecords());
		}
	}

	public void setShouldLogChartPositionImport(boolean shouldLogChartPositionImport) {
		this.shouldLogChartPositionImport = shouldLogChartPositionImport;
	}

	public void setShouldLogProgress(boolean shouldLogProgress) {
		this.shouldLogProgress = shouldLogProgress;
	}

	public void setShouldLogRecordImportImport(boolean shouldLogRecordImportImport) {
		this.shouldLogRecordImportImport = shouldLogRecordImportImport;
	}

	public void setShouldLogSongBroadcastImport(boolean shouldLogSongBroadcastImport) {
		this.shouldLogSongBroadcastImport = shouldLogSongBroadcastImport;
	}

}
