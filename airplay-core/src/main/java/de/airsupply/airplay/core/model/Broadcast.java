package de.airsupply.airplay.core.model;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.util.Assert;

import de.airsupply.commons.core.neo4j.annotation.Persistent;
import de.airsupply.commons.core.util.DateUtils;

@NodeEntity
@SuppressWarnings("serial")
public abstract class Broadcast extends PersistentNode {

	protected static enum BroadcastType implements IBroadcastType {

		DAY(2), EXACT_BEGIN(0), EXACT_RANGE(1), MONTH(4), WEEK(3), YEAR(5);

		private static IBroadcastType valueOf(int type) {
			switch (type) {
			case 0:
				return EXACT_BEGIN;
			case 1:
				return EXACT_RANGE;
			case 2:
				return DAY;
			case 3:
				return WEEK;
			case 4:
				return MONTH;
			case 5:
				return YEAR;
			default:
				throw new IllegalArgumentException();
			}
		}

		private final int type;

		private BroadcastType(int type) {
			this.type = type;
		}

		@Override
		public int getType() {
			return type;
		}

		@Override
		public boolean isDaily() {
			return equals(DAY);
		}

		@Override
		public boolean isExactBegin() {
			return equals(EXACT_BEGIN);
		}

		@Override
		public boolean isExactRange() {
			return equals(EXACT_RANGE);
		}

		@Override
		public boolean isMonthly() {
			return equals(MONTH);
		}

		@Override
		public boolean isWeekly() {
			return equals(WEEK);
		}

		@Override
		public boolean isYearly() {
			return equals(YEAR);
		}

	}

	public static interface IBroadcastType {

		public int getType();

		public boolean isDaily();

		public boolean isExactBegin();

		public boolean isExactRange();

		public boolean isMonthly();

		public boolean isWeekly();

		public boolean isYearly();

	}

	@Min(value = 1)
	private int count;

	@Indexed
	private long from;

	@NotNull
	@Persistent
	@RelatedTo(type = "BROADCAST_ON")
	private Station station;

	@Indexed
	private long to;

	@Indexed
	private int type;

	Broadcast() {
		super();
	}

	protected Broadcast(Station station, BroadcastType broadcastType, Date date, int count) {
		super();
		Assert.notNull(station);
		Assert.notNull(broadcastType);
		Assert.notNull(date);
		Assert.isTrue(!broadcastType.isExactRange());
		if (broadcastType.isExactBegin()) {
			Assert.isTrue(count == 1);
		}
		this.station = station;
		this.type = broadcastType.getType();
		this.count = count;
		computeDate(broadcastType, date);
	}

	protected Broadcast(Station station, Date from, Date to) {
		super();
		Assert.notNull(station);
		Assert.notNull(from);
		Assert.notNull(to);
		Assert.isTrue(from.before(to));
		this.station = station;
		this.from = from.getTime();
		this.to = to.getTime();
		this.type = BroadcastType.EXACT_RANGE.getType();
		this.count = 1;
	}

	private void computeDate(BroadcastType broadcastType, Date date) {
		switch (broadcastType) {
		case DAY:
			this.from = DateUtils.getStartOfDay(date).getTime();
			this.to = DateUtils.getEndOfDay(date).getTime();
			break;
		case WEEK:
			this.from = DateUtils.getStartOfWeek(date).getTime();
			this.to = DateUtils.getEndOfWeek(date).getTime();
			break;
		case MONTH:
			this.from = DateUtils.getStartOfMonth(date).getTime();
			this.to = DateUtils.getEndOfMonth(date).getTime();
			break;
		case YEAR:
			this.from = DateUtils.getStartOfYear(date).getTime();
			this.to = DateUtils.getEndOfYear(date).getTime();
			break;
		case EXACT_BEGIN:
			this.from = date.getTime();
			this.to = 0;
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public IBroadcastType getBroadcastType() {
		return BroadcastType.valueOf(type);
	}

	public int getCount() {
		return count;
	}

	public Date getFromDate() {
		return new Date(from);
	}

	public Station getStation() {
		return station;
	}

	public Date getToDate() {
		return new Date(to);
	}

	@Override
	public String toString() {
		return "Broadcast [type=" + type + ", count=" + count + ", from=" + from + ", station=" + station + ", to="
				+ to + "]";
	}

}
