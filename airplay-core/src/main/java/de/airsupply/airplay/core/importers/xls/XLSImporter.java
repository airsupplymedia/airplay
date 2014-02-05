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

	private static final String IDENTIFIER_CHARTS_AIPLAY = "Airplay Charts";

	private static final String IDENTIFIER_CHARTS_SALES = "Sales-Charts-Single";

	@Autowired
	private ChartService chartService;

	@Autowired
	private ContentService contentService;

	@Loggable
	private Logger logger;

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
					processRecordsOfAirplayCharts(recordImport, chartState, sheet);
				} else if (IDENTIFIER_CHARTS_SALES.equals(cellWithSalesIdentifier.getContents())) {
					// processSalesRecords(sheet);
				} else {
					throw new RuntimeException("Unsupported format: Could not identify Chart type");
				}
			}
		} catch (BiffException | IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void processRecordsOfAirplayCharts(final RecordImport recordImport, ChartState chartState, Sheet sheet) {
		int from = 0;
		int to = 300;
		int offset = 2;

		for (int i = from + offset; i < to + offset; i++) {
			Cell[] row = sheet.getRow(i);
			Cell cellWithPosition = row[from];
			Cell cellWithArtistName = row[3];
			Cell cellWithSongName = row[4];

			String artistName = cellWithArtistName.getContents().trim();
			String songName = cellWithSongName.getContents().trim();
			int position = Integer.valueOf(cellWithPosition.getContents().trim()).intValue();

			Artist artist = importRecord(contentService, recordImport, new Artist(artistName));
			Song song = importRecord(contentService, recordImport, new Song(artist, songName));
			importRecord(chartService, recordImport, new ChartPosition(chartState, song, position));
		}
	}

}
