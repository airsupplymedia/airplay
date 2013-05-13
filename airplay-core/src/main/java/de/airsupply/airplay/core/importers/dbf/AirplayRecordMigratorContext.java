package de.airsupply.airplay.core.importers.dbf;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;

import de.airsupply.airplay.core.model.Artist;
import de.airsupply.airplay.core.model.Chart;
import de.airsupply.airplay.core.model.ChartPosition;
import de.airsupply.airplay.core.model.ChartState;
import de.airsupply.airplay.core.model.Publisher;
import de.airsupply.airplay.core.model.RecordCompany;
import de.airsupply.airplay.core.model.Song;
import de.airsupply.airplay.core.model.SongBroadcast;
import de.airsupply.airplay.core.model.Station;

class AirplayRecordMigratorContext {

	private Map<Long, Object> hashes = new HashMap<>(5200000);

	private List<Object> objects;

	public AirplayRecordMigratorContext(List<Object> objects) {
		this.objects = objects;
	}

	private int createHash(Artist object) {
		return new HashCodeBuilder().append(object.getName()).toHashCode();
	}

	private int createHash(Chart object) {
		return new HashCodeBuilder().append(object.getName()).toHashCode();
	}

	@SuppressWarnings("unused")
	private int createHash(ChartPosition object) {
		return new HashCodeBuilder().append(createHash(object.getChartState())).append(createHash(object.getSong()))
				.append(object.getPosition()).toHashCode();
	}

	private int createHash(ChartState object) {
		return new HashCodeBuilder().append(createHash(object.getChart())).append(object.getWeekDate()).toHashCode();
	}

	private Long createHash(Object object) {
		try {
			Method method = getClass().getDeclaredMethod("createHash", object.getClass());
			Integer hash = (Integer) method.invoke(this, object);
			return Long.valueOf((hash.longValue() << 32) | (object.getClass().hashCode() & 0xFFFFFFFL));
		} catch (Exception exception) {
			throw new IllegalArgumentException(exception);
		}
	}

	@SuppressWarnings("unused")
	private int createHash(Publisher object) {
		return new HashCodeBuilder().append(object.getName()).toHashCode();
	}

	@SuppressWarnings("unused")
	private int createHash(RecordCompany object) {
		return new HashCodeBuilder().append(object.getName()).toHashCode();
	}

	private int createHash(Song object) {
		return new HashCodeBuilder().append(createHash(object.getArtist())).append(object.getName()).toHashCode();
	}

	@SuppressWarnings("unused")
	private int createHash(SongBroadcast object) {
		return new HashCodeBuilder().append(createHash(object.getBroadcastedSong()))
				.append(createHash(object.getStation())).append(object.getFromDate()).append(object.getToDate())
				.append(object.getCount()).append(object.getBroadcastType().getType()).toHashCode();
	}

	private int createHash(Station object) {
		return new HashCodeBuilder().append(object.getName()).toHashCode();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(T object) {
		return (T) hashes.get(createHash(object));
	}

	public List<Object> getObjects() {
		return objects;
	}

	public <T> T getOrPersist(T template) {
		T existing = get(template);
		if (existing == null) {
			return persist(template);
		} else {
			return existing;
		}
	}

	public <T> T persist(T object) {
		if (object == null) {
			return null;
		}
		T existing = get(object);
		if (existing != null) {
			throw new IllegalArgumentException("Existing unit found! Old: '" + existing + "' - New: '" + object + "'");
		}
		hashes.put(createHash(object), object);
		objects.add(object);
		return object;
	}

}
