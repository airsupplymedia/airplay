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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

public class DBFReader {

	public static interface RecordHandler {

		void handle(Record record, Table table);

	}

	private static final Log LOG = LogFactory.getLog(DBFReader.class);

	public static void processRecords(File file, RecordHandler recordHandler) {
		Assert.notNull(file);
		Assert.notNull(recordHandler);

		final Table table = new Table(file);
		try {
			table.open(IfNonExistent.ERROR);
			LOG.info("Opened " + file.getAbsolutePath());

			final int recordCount = table.getRecordCount();
			LOG.info("Found " + recordCount + " records.");

			for (int i = 0; i < recordCount; i++) {
				Record record = null;
				try {
					record = table.getRecordAt(i);
					recordHandler.handle(record, table);
				} catch (Exception exception) {
					LOG.error("Error importing record: " + DBFUtils.toString(record, table), exception);
				}
			}
		} catch (IOException exception) {
			LOG.error("Trouble reading table or table not found", exception);
		} catch (CorruptedTableException exception) {
			LOG.error("Table is corrupted", exception);
		} finally {
			try {
				table.close();
			} catch (IOException exception) {
				LOG.error("Unable to close the table", exception);
			}
		}
	}

	public static Table readRecords(File file) {
		Assert.notNull(file);

		final Table table = new Table(file);
		try {
			table.open(IfNonExistent.ERROR);
			LOG.info("Opened " + file.getAbsolutePath());

			final int recordCount = table.getRecordCount();
			LOG.info("Found " + recordCount + " records.");
		} catch (IOException exception) {
			LOG.error("Trouble reading table or table not found", exception);
		} catch (DbfLibException exception) {
			LOG.error("Problem getting raw value", exception);
		}
		return table;
	}

	public static List<Record> readRecords(File file, boolean includeDeleted, NumberRange recordsToSkip) {
		Assert.notNull(file);

		List<Record> results = null;
		final Table table = new Table(file);
		try {
			table.open(IfNonExistent.ERROR);
			LOG.info("Opened " + file.getAbsolutePath());

			final int recordCount = table.getRecordCount();
			LOG.info("Found " + recordCount + " records.");

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
			LOG.error("Trouble reading table or table not found", exception);
		} catch (DbfLibException exception) {
			LOG.error("Problem getting raw value", exception);
		} finally {
			try {
				table.close();
			} catch (IOException exception) {
				LOG.error("Unable to close the table", exception);
			}
		}
		if (results == null) {
			results = Collections.emptyList();
		}
		return Collections.unmodifiableList(results);
	}

}
