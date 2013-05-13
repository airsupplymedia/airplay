package de.airsupply.commons.core.dbf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nl.knaw.dans.common.dbflib.CorruptedTableException;
import nl.knaw.dans.common.dbflib.DbfLibException;
import nl.knaw.dans.common.dbflib.IfNonExistent;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;

import org.apache.commons.lang.math.NumberRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class DBFReader {

	private static final String ENCODING = "ISO-8859-1";

	public static interface RecordHandler {

		void handle(Record record, Table table, int index, boolean isLast);

	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DBFReader.class);

	public static void processRecords(File file, RecordHandler recordHandler) {
		Assert.notNull(file);
		Assert.notNull(recordHandler);

		final Table table = new Table(file, ENCODING);
		try {
			table.open(IfNonExistent.ERROR);
			LOGGER.info("Opened " + file.getAbsolutePath());

			final int recordCount = table.getRecordCount();
			LOGGER.info("Found " + recordCount + " records.");

			for (int i = 0; i < recordCount; i++) {
				Record record = null;
				try {
					if (i % 10000 == 0) {
						float percent = ((float) i * 100.0f) / (float) recordCount;
						LOGGER.info(String.valueOf(Math.round(percent)) + "%");
					}
					record = table.getRecordAt(i);
					recordHandler.handle(record, table, i, i == recordCount - 1);
				} catch (Exception exception) {
					LOGGER.error("Error importing record: " + DBFUtils.toString(record, table), exception);
				}
			}
		} catch (IOException exception) {
			LOGGER.error("Trouble reading table or table not found", exception);
		} catch (CorruptedTableException exception) {
			LOGGER.error("Table is corrupted", exception);
		} finally {
			try {
				table.close();
			} catch (IOException exception) {
				LOGGER.error("Unable to close the table", exception);
			}
		}
	}

	public static Table readRecords(File file) {
		Assert.notNull(file);

		final Table table = new Table(file, ENCODING);
		try {
			table.open(IfNonExistent.ERROR);
			LOGGER.info("Opened " + file.getAbsolutePath());

			final int recordCount = table.getRecordCount();
			LOGGER.info("Found " + recordCount + " records.");
		} catch (IOException exception) {
			LOGGER.error("Trouble reading table or table not found", exception);
		} catch (DbfLibException exception) {
			LOGGER.error("Problem getting raw value", exception);
		}
		return table;
	}

	public static List<Record> readRecords(File file, boolean includeDeleted, NumberRange recordsToSkip) {
		Assert.notNull(file);

		List<Record> results = null;
		final Table table = new Table(file, ENCODING);
		try {
			table.open(IfNonExistent.ERROR);
			LOGGER.info("Opened " + file.getAbsolutePath());

			final int recordCount = table.getRecordCount();
			LOGGER.info("Found " + recordCount + " records.");

			final Iterator<Record> recordIterator = table.recordIterator(includeDeleted);
			results = new ArrayList<>(recordCount);
			int counter = 0;
			while (recordIterator.hasNext()) {
				final Record record = recordIterator.next();
				counter++;
				if (recordsToSkip == null || !recordsToSkip.containsInteger(counter)) {
					results.add(record);
				}
			}
		} catch (IOException exception) {
			LOGGER.error("Trouble reading table or table not found", exception);
		} catch (DbfLibException exception) {
			LOGGER.error("Problem getting raw value", exception);
		} finally {
			try {
				table.close();
			} catch (IOException exception) {
				LOGGER.error("Unable to close the table", exception);
			}
		}
		if (results == null) {
			results = Collections.emptyList();
		}
		return Collections.unmodifiableList(results);
	}

}
