package darksky.ogameapidatabasefx.database.entities;

import java.time.Instant;
import java.util.Optional;

/**
 * 
 * @author darksky
 *
 */
public class Planet extends Entity {

	private int galaxy;
	private int system;
	private int position;
	private int playerId;
	private Optional<Moon> moon = Optional.ofNullable(null);

	public Planet() {

	}

	public Planet(int id, String name, Instant insertedOn, Instant lastUpdate, boolean deleted, int galaxy, int system,
			int position, int playerId) {
		super(id, name, insertedOn, lastUpdate, deleted);
		this.galaxy = galaxy;
		this.system = system;
		this.position = position;
		this.playerId = playerId;
	}

	public Planet(Planet other) {
		super(other.getId(), other.getName(), other.getInsertedOn(), other.getLastUpdate(), other.isDeleted());
		this.galaxy = other.getGalaxy();
		this.system = other.getSystem();
		this.position = other.getPosition();
	}

	public int getGalaxy() {
		return galaxy;
	}

	public Planet setGalaxy(int galaxy) {
		this.galaxy = galaxy;
		return this;
	}

	public int getSystem() {
		return system;
	}

	public Planet setSystem(int system) {
		this.system = system;
		return this;
	}

	public int getPosition() {
		return position;
	}

	public Planet setPosition(int position) {
		this.position = position;
		return this;
	}

	public int getPlayerId() {
		return playerId;
	}

	public Planet setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public Optional<Moon> getMoon() {
		return moon;
	}

	public Planet setMoon(Optional<Moon> moon) {
		this.moon = moon;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + galaxy;
		result = prime * result + ((moon == null) ? 0 : moon.hashCode());
		result = prime * result + playerId;
		result = prime * result + position;
		result = prime * result + system;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Planet other = (Planet) obj;
		if (galaxy != other.galaxy)
			return false;
		if (moon == null) {
			if (other.moon != null)
				return false;
		} else if (!moon.equals(other.moon))
			return false;
		if (playerId != other.playerId)
			return false;
		if (position != other.position)
			return false;
		if (system != other.system)
			return false;
		return true;
	}

}
