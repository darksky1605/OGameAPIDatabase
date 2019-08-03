package darksky.ogameapidatabasefx.database.entities;

import java.util.Optional;

/**
 * 
 * @author darksky
 *
 */
public class SystemPosition {

	private final int position;
	private final Optional<Player> player;
	private final Optional<Planet> planet;
	private final Optional<Moon> moon;
	private final Optional<Alliance> alliance;

	public SystemPosition(int position, Player player, Planet planet, Moon moon, Alliance alliance) {
		this.position = position;
		this.player = Optional.ofNullable(player);
		this.planet = Optional.ofNullable(planet);
		this.moon = Optional.ofNullable(moon);
		this.alliance = Optional.ofNullable(alliance);
	}

	public int getPosition() {
		return position;
	}

	public Optional<Player> getPlayer() {
		return player;
	}

	public Optional<Planet> getPlanet() {
		return planet;
	}

	public Optional<Moon> getMoon() {
		return moon;
	}

	public Optional<Alliance> getAlliance() {
		return alliance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alliance == null) ? 0 : alliance.hashCode());
		result = prime * result + ((moon == null) ? 0 : moon.hashCode());
		result = prime * result + ((planet == null) ? 0 : planet.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + position;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SystemPosition))
			return false;
		SystemPosition other = (SystemPosition) obj;
		if (alliance == null) {
			if (other.alliance != null)
				return false;
		} else if (!alliance.equals(other.alliance))
			return false;
		if (moon == null) {
			if (other.moon != null)
				return false;
		} else if (!moon.equals(other.moon))
			return false;
		if (planet == null) {
			if (other.planet != null)
				return false;
		} else if (!planet.equals(other.planet))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (position != other.position)
			return false;
		return true;
	}
}
