package darksky.ogameapidatabasefx.database.entities;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author darksky
 *
 */
public class Highscore {

	public enum HighscoreType {
		Total, Economy, Research, Military, MilitaryBuilt, MilitaryDestroyed, MilitaryLost, Honor, Ships
	};

	private Instant instant;

	private final Map<HighscoreType, Integer> ranksMap = new EnumMap<HighscoreType, Integer>(HighscoreType.class);
	private final Map<HighscoreType, Long> pointsMap = new EnumMap<HighscoreType, Long>(HighscoreType.class);
	
	public Highscore(){
		for (HighscoreType t : HighscoreType.values()) {
			setEntry(t, 0, 0);
		}
	}

	public Highscore(Instant instant) {
		this();
		this.instant = instant;
	}

	public Highscore setEntry(HighscoreType t, long points, int rank) {
		ranksMap.put(t, rank);
		pointsMap.put(t, points);
		return this;
	}

	public long getPoints(HighscoreType t) {
		return pointsMap.getOrDefault(t, 0L);
	}

	public int getRank(HighscoreType t) {
		return ranksMap.getOrDefault(t, 0);
	}
	
	public Instant getInstant() {
		return instant;
	}

	public Highscore setInstant(Instant instant) {
		this.instant = instant;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instant == null) ? 0 : instant.hashCode());
		result = prime * result + ((pointsMap == null) ? 0 : pointsMap.hashCode());
		result = prime * result + ((ranksMap == null) ? 0 : ranksMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Highscore))
			return false;
		Highscore other = (Highscore) obj;
		if (instant == null) {
			if (other.instant != null)
				return false;
		} else if (!instant.equals(other.instant))
			return false;
		if (pointsMap == null) {
			if (other.pointsMap != null)
				return false;
		} else if (!pointsMap.equals(other.pointsMap))
			return false;
		if (ranksMap == null) {
			if (other.ranksMap != null)
				return false;
		} else if (!ranksMap.equals(other.ranksMap))
			return false;
		return true;
	}

}
