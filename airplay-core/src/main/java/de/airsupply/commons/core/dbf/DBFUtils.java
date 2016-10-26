package de.airsupply.commons.core.dbf;

import java.util.List;

import nl.knaw.dans.common.dbflib.DbfLibException;
import nl.knaw.dans.common.dbflib.Field;
import nl.knaw.dans.common.dbflib.Record;
import nl.knaw.dans.common.dbflib.Table;

public class DBFUtils {

	public static String toString(Record record, Table table) {
		StringBuilder builder = new StringBuilder();
		builder.append(record);
		if (record != null) {
			builder.append("\n");
			builder.append("*DEL:");
			builder.append("\t");
			builder.append(record.isMarkedDeleted());
			builder.append("\n");
			List<Field> fields = table.getFields();
			for (Field field : fields) {
				try {
					byte[] rawValue = record.getRawValue(field);
					builder.append(field.getName());
					builder.append(":");
					builder.append("\t");
					builder.append(rawValue == null ? "<NULL>" : new String(rawValue));
					builder.append("\n");
				} catch (DbfLibException exception) {
					builder.append("[ERROR]");
				}
			}
			builder.append("\n");
			builder.append("----------------------");
		}
		return builder.toString();
	}

}
