package de.airsupply.commons.core.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.util.Assert;

public abstract class DateUtils {

	private final static DateFormat WEEK_OF_YEAR_FORMAT = new SimpleDateFormat("w / YYYY");

	private static Calendar create(Date date) {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public static Date getEndOfDay(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		maximizeTimeOfDay(calendar);
		return calendar.getTime();
	}

	public static Date getEndOfMonth(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		maximize(calendar, Calendar.DAY_OF_MONTH);
		maximizeTimeOfDay(calendar);
		return calendar.getTime();
	}

	public static Date getEndOfWeek(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() - 1);
		maximizeTimeOfDay(calendar);
		return calendar.getTime();
	}

	public static Date getEndOfYear(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		maximize(calendar, Calendar.DAY_OF_YEAR);
		maximizeTimeOfDay(calendar);
		return calendar.getTime();
	}

	public static Date getStartOfDay(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		minimizeTimeOfTheDay(calendar);
		return calendar.getTime();
	}

	public static Date getStartOfMonth(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		minimize(calendar, Calendar.DAY_OF_MONTH);
		minimizeTimeOfTheDay(calendar);
		return calendar.getTime();
	}

	public static Date getStartOfWeek(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
		minimizeTimeOfTheDay(calendar);
		return calendar.getTime();
	}

	public static Date getStartOfYear(Date date) {
		Assert.notNull(date);
		Calendar calendar = create(date);
		minimize(calendar, Calendar.DAY_OF_YEAR);
		minimizeTimeOfTheDay(calendar);
		return calendar.getTime();
	}

	public static String getWeekOfYearFormat(Date date) {
		return WEEK_OF_YEAR_FORMAT.format(date);
	}

	private static void maximize(Calendar calendar, int field) {
		calendar.set(field, calendar.getActualMaximum(field));
	}

	private static void maximizeTimeOfDay(Calendar calendar) {
		maximize(calendar, Calendar.HOUR);
		maximize(calendar, Calendar.HOUR_OF_DAY);
		maximize(calendar, Calendar.MINUTE);
		maximize(calendar, Calendar.SECOND);
		maximize(calendar, Calendar.MILLISECOND);
	}

	private static void minimize(Calendar calendar, int field) {
		calendar.set(field, calendar.getActualMinimum(field));
	}

	private static void minimizeTimeOfTheDay(Calendar calendar) {
		minimize(calendar, Calendar.HOUR_OF_DAY);
		minimize(calendar, Calendar.MINUTE);
		minimize(calendar, Calendar.SECOND);
		minimize(calendar, Calendar.MILLISECOND);
	}

}
