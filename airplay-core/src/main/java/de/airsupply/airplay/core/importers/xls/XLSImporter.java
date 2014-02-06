package de.airsupply.airplay.core.importers.xls;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.airsupply.airplay.core.importers.Importer;
import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.RecordImport;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.services.ChartService;
import de.airsupply.airplay.core.services.ContentService;
import de.airsupply.commons.core.context.Loggable;

@Service
public class XLSImporter extends Importer {

	private static enum CellDescriptor {

		AIRPLAY(300, 2, 0, 3, 4), SALES(100, 2, 0, 2, 3);

		private final int artistColumn;

		private final int count;

		private final int offset;

		private final int positionColumn;

		private final int songColumn;

		private CellDescriptor(int count, int offset, int positionColumn, int artistColumn, int songColumn) {
			this.count = count;
			this.offset = offset;
			this.positionColumn = positionColumn;
			this.artistColumn = artistColumn;
			this.songColumn = songColumn;
		}

		private int getArtistColumn() {
			return artistColumn;
		}

		private int getCount() {
			return count;
		}

		private int getOffset() {
			return offset;
		}

		private int getPositionColumn() {
			return positionColumn;
		}

		private int getSongColumn() {
			return songColumn;
		}

	}

	private static final String IDENTIFIER_CHARTS_AIPLAY = "Airplay Charts";

	private static final String IDENTIFIER_CHARTS_SALES = "Sales-Charts-Single";

	@Autowired
	private ChartService chartService;

	@Autowired
	private ContentService contentService;

	@Loggable
	private Logger logger;

	private void processRecords(RecordImport recordImport, CellDescriptor descriptor, ChartState chartState, Sheet sheet) {
		for (int i = 0 + descriptor.getOffset(); i < descriptor.getCount() + descriptor.getOffset(); i++) {
			Cell[] row = sheet.getRow(i);
			Cell cellWithPosition = row[descriptor.getPositionColumn()];
			Cell cellWithArtistName = row[descriptor.getArtistColumn()];
			Cell cellWithSongName = row[descriptor.getSongColumn()];

			String artistName = cellWithArtistName.getContents().trim();
			String songName = cellWithSongName.getContents().trim();
			int position = Integer.valueOf(cellWithPosition.getContents().trim()).intValue();

			Artist artist = importRecord(contentService, recordImport, new Artist(artistName));
			Song song = importRecord(contentService, recordImport, new Song(artist, songName));
			importRecord(chartService, recordImport, new ChartPosition(chartState, song, position));
		}
	}

	@Override
	public void processRecords(RecordImport recordImport, Chart chart, Date week, InputStream inputStream) {
		Assert.notNull(recordImport);
		Assert.notNull(inputStream);
		try {
			logger.info("Running import for week: " + recordImport.getWeekDate());
			ChartState chartState = importRecord(contentService, recordImport, new ChartState(chart, week));

			Workbook workbook = Workbook.getWorkbook(inputStream);
			if (workbook.getNumberOfSheets() > 0) {
				Sheet sheet = workbook.getSheet(0);
				Cell cellWithAirplayIdentifier = sheet.getCell(3, 0);
				Cell cellWithSalesIdentifier = sheet.getCell(0, 0);
				if (IDENTIFIER_CHARTS_AIPLAY.equals(cellWithAirplayIdentifier.getContents())) {
					processRecords(recordImport, CellDescriptor.AIRPLAY, chartState, sheet);
				} else if (IDENTIFIER_CHARTS_SALES.equals(cellWithSalesIdentifier.getContents())) {
					processRecords(recordImport, CellDescriptor.SALES, chartState, sheet);
				} else {
					throw new RuntimeException("Unsupported format: Could not identify Chart type");
				}
			}
		} catch (BiffException | IOException exception) {
			throw new RuntimeException(exception);
		}
	}

}
