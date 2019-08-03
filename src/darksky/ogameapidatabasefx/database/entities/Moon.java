package darksky.ogameapidatabasefx.database.entities;

import java.time.Instant;

/**
 * 
 * @author darksky
 *
 */
public class Moon extends Entity {

	private int moonSize;
	private int playerId;
	private int planetId;

	public Moon(int id, String name, Instant insertedOn, Instant lastUpdate, boolean deleted, int moonSize,
			int playerId, int planetId) {
		super(id, name, insertedOn, lastUpdate, deleted);
		this.moonSize = moonSize;
		this.playerId = playerId;
		this.planetId = planetId;
	}

	public int getMoonSize() {
		return moonSize;
	}

	public Moon setMoonSize(int moonSize) {
		this.moonSize = moonSize;
		return this;
	}

	public int getPlayerId() {
		return playerId;
	}

	public Moon setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public int getPlanetId() {
		return planetId;
	}

	public Moon setPlanetId(int planetId) {
		this.planetId = planetId;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + moonSize;
		result = prime * result + planetId;
		result = prime * result + playerId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Moon))
			return false;
		Moon other = (Moon) obj;
		if (moonSize != other.moonSize)
			return false;
		if (planetId != other.planetId)
			return false;
		if (playerId != other.playerId)
			return false;
		return true;
	}

}
